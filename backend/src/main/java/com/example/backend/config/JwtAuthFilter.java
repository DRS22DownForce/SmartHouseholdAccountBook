package com.example.backend.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.proc.*;
import com.nimbusds.jwt.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import org.springframework.stereotype.Component;
import java.util.HashMap;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProperties jwtProperties;
    // CognitoのJWKセット(JWT署名検証用の公開鍵)URL

    //コンストラクタが一つだけの場合は@Autowiredは省略可能
    @Autowired
    public JwtAuthFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwtToken = authorizationHeader.substring(7);
            try {
                // JWKセットを取得してJWTを検証
                // JWKとはJSON Web Keyの略で、公開鍵や秘密鍵をJSON形式で表したもの
                // 公開鍵をcognitoから取得してJWTの署名を検証する
                JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(
                        URI.create(jwtProperties.getJwkSetUrl()).toURL(),
                        new DefaultResourceRetriever());
                //JWTの検証を行うオブジェクトを作成
                ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
                JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256,
                        jwkSource);
                jwtProcessor.setJWSKeySelector(keySelector);

                // JWTの署名を検証しクレームを取得
                // クレームとはJWTの中にあるユーザID、有効期限、発行者等の情報
                JWTClaimsSet claimsSet = jwtProcessor.process(jwtToken, null);

                // JWTのヘッダー情報を取得
                SignedJWT signedJWT = SignedJWT.parse(jwtToken);
                Map<String, Object> headerMap = signedJWT.getHeader().toJSONObject();

                // クレームをInstant型に変換
                Map<String, Object> claims = convertDateClaimsToInstant(claimsSet.getClaims());

                // Spring SecurityのJwtオブジェクトを作成
                Jwt jwt = Jwt.withTokenValue(jwtToken)
                        .headers(h -> h.putAll(headerMap))
                        .claims(c -> c.putAll(claims))
                        .build();

                // JwtAuthenticationTokenを作成
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, Collections.emptyList());
                // セキュリティコンテキストに認証情報を設定
                // これによりアプリケーション全体でこのリクエストは認証済みとして扱われる
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * JWTクレームのうち、iat, exp, nbfがDate型の場合はInstant型に変換するユーティリティ
     * それ以外はそのまま返す
     */
    private Map<String, Object> convertDateClaimsToInstant(Map<String, Object> originalClaims) {
        Map<String, Object> claims = new HashMap<>();
        for (Map.Entry<String, Object> entry : originalClaims.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // iat, exp, nbfはInstant型に変換
            if ((key.equals("iat") || key.equals("exp") || key.equals("nbf")) && value instanceof java.util.Date) {
                claims.put(key, ((java.util.Date) value).toInstant());
            } else {
                claims.put(key, value);
            }
        }
        return claims;
    }
}