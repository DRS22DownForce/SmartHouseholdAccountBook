package com.example.backend.auth.filter;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jwt.proc.*;
import com.nimbusds.jwt.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.lang.IllegalStateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.backend.config.security.JwtProperties;

/**
 * JWT認証フィルター
 *
 * このフィルターは、リクエストヘッダーからJWTトークンを取得し、
 * CognitoのJWKセットを使用して署名を検証します。
 * 検証が成功した場合、Spring Securityのセキュリティコンテキストに認証情報を設定します。
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    @Autowired
    public JwtAuthFilter(JwtProperties jwtProperties) {
        try {
            // JWKソース（公開鍵）を作成
            JWKSource<SecurityContext> jwkSource = JWKSourceBuilder
                    .create(URI.create(jwtProperties.getJwkSetUrl()).toURL())
                    .retrying(true) // エラーが発生した場合、最大3回リトライする
                    .build();

            this.jwtProcessor = new DefaultJWTProcessor<>();
            this.jwtProcessor.setJWSKeySelector(
                    new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource));
            
            // issとaudを検証し、subとexpが存在することを確認するバリデーターを設定
            DefaultJWTClaimsVerifier<SecurityContext> claimsVerifier = new DefaultJWTClaimsVerifier<>(
                jwtProperties.getClientId(),                                                // 期待する aud
                new JWTClaimsSet.Builder().issuer(jwtProperties.getIssuerUrl()).build(),     // 必須の一致クレーム（iss）
                Set.of("sub", "exp")                                                        // 必須クレーム名
            );       
            this.jwtProcessor.setJWTClaimsSetVerifier(claimsVerifier);

            logger.info("JWT認証フィルターを初期化しました。JWK URL: {}", jwtProperties.getJwkSetUrl());
        } catch (MalformedURLException e) {
            logger.error("JWT認証フィルターの初期化に失敗しました。JWK URLの形式を確認してください: {}", jwtProperties.getJwkSetUrl(), e);
            throw new IllegalStateException("JWT認証フィルターの初期化に失敗しました", e);
        }
    }

    /**
     * テスト用コンストラクタ
     */
    JwtAuthFilter(ConfigurableJWTProcessor<SecurityContext> jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            try {
                authenticateJwtToken(authHeader.substring(BEARER_PREFIX.length()));//Bearer 以降のトークンを認証
            } catch (ParseException e) {
                logger.warn("JWTパースエラー: {}", e.getMessage());
                throw new BadCredentialsException("JWTトークンの形式が不正です");
            } catch (BadJOSEException e) {
                logger.warn("JWTクレーム検証エラー: {}", e.getMessage());
                throw new BadCredentialsException("JWTトークンのクレーム検証に失敗しました");
            } catch (JOSEException e) {
                logger.warn("JWT検証エラー: {}", e.getMessage());
                throw new BadCredentialsException("JWTトークンの検証に失敗しました");
            }
        }
        filterChain.doFilter(request, response);
    }

    private void authenticateJwtToken(String jwtToken) throws ParseException, JOSEException, BadJOSEException {
        SignedJWT signedJWT = SignedJWT.parse(jwtToken);//JWTトークンをパースして、ヘッダー、署名、クレームに分解
        JWTClaimsSet claimsSet = jwtProcessor.process(signedJWT, null);//署名の検証を行う
        Map<String, Object> headerMap = signedJWT.getHeader().toJSONObject();
        Jwt jwt = buildJwtForSpringSecurity(jwtToken, headerMap, claimsSet);//Spring SecurityのJwtオブジェクトに変換
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(jwt, Collections.emptyList()));
    }

    private Jwt buildJwtForSpringSecurity(String jwtToken, Map<String, Object> headerMap, JWTClaimsSet claimsSet) {
        //Spring SecurityのJwtビルダーに元のJWTトークンとヘッダーを設定
        Jwt.Builder builder = Jwt.withTokenValue(jwtToken).headers(h -> h.putAll(headerMap));

        //NimbusのJWTClaimSetはDate型でiat/exp/nbfを持っているのが、
        //Spring SecurityのJwtビルダーはInstant型でissuenAt/expiresAt/notBeforeを持っているので、変換する。
        Date issuedAt = claimsSet.getIssueTime();
        if (issuedAt != null)
            builder.issuedAt(issuedAt.toInstant());
        Date expiresAt = claimsSet.getExpirationTime();
        if (expiresAt != null)
            builder.expiresAt(expiresAt.toInstant());
        Date notBefore = claimsSet.getNotBeforeTime();
        if (notBefore != null)
            builder.notBefore(notBefore.toInstant());

        Map<String, Object> claims = new HashMap<>(claimsSet.getClaims());
        //変換して設定済みのクレームを削除
        claims.remove("iat");
        claims.remove("exp");
        claims.remove("nbf");

        //Spring SecurityのJwtビルダーにクレームを設定
        builder.claims(c -> c.putAll(claims));

        return builder.build();
    }

}
