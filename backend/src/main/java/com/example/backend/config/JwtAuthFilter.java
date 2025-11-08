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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.text.ParseException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private final JWKSource<SecurityContext> remoteJwkSet;

    public JwtAuthFilter(JwtProperties jwtProperties) {
        try {
            // JWKセット(JWT署名検証用の公開鍵)を取得
            // RemoteJWKSetは内部でキャッシュを管理
            // タイムアウト設定付きのリソースリトリーバーを作成
            DefaultResourceRetriever resourceRetriever = new DefaultResourceRetriever(
                    5000, // 接続タイムアウト（ミリ秒）
                    5000, // 読み取りタイムアウト（ミリ秒）
                    1024 * 1024 // 最大エンティティサイズ（1MB）
            );
            
            // RemoteJWKSetは非推奨だが、まだ機能しており、適切な代替手段がないため使用
            @SuppressWarnings("deprecation")
            RemoteJWKSet<SecurityContext> remoteJWKSet = new RemoteJWKSet<>(
                    URI.create(jwtProperties.getJwkSetUrl()).toURL(),
                    resourceRetriever);
            this.remoteJwkSet = remoteJWKSet;

            // JWTの検証を行うプロセッサーを作成
            this.jwtProcessor = new DefaultJWTProcessor<>();
            // RS256アルゴリズム用の鍵セレクターを設定
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
                    JWSAlgorithm.RS256,
                    this.remoteJwkSet);
            this.jwtProcessor.setJWSKeySelector(keySelector);
            logger.info("JWT認証フィルターを初期化しました。JWK URL: {}", jwtProperties.getJwkSetUrl());
        
        } catch (Exception e) {
            logger.error("JWT認証フィルターの初期化に失敗しました", e);
            throw new RuntimeException("JWT認証フィルターの初期化に失敗しました", e);
        }
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwtToken = authorizationHeader.substring(7);
            try {
                // JWTの署名を検証しクレーム(ユーザID、有効期限、発行者等の情報)を取得
                JWTClaimsSet claimsSet = jwtProcessor.process(jwtToken, null);

                // JWTのヘッダー情報を取得
                SignedJWT signedJWT = SignedJWT.parse(jwtToken);
                Map<String, Object> headerMap = signedJWT.getHeader().toJSONObject();

                Map<String, Object> claims = claimsSet.getClaims();

                // Spring Security用の認証情報オブジェクト作成
                Jwt jwt = Jwt.withTokenValue(jwtToken)
                        .headers(h -> h.putAll(headerMap))
                        .claims(c -> c.putAll(claims))
                        .build();

                JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, Collections.emptyList());
                // セキュリティコンテキストに認証情報を設定
                // これによりアプリケーション全体でこのリクエストは認証済みとして扱われる
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ParseException e) {
                logger.warn("JWTパースエラー: {}", e.getMessage());
                sendUnauthorizedResponse(response, "JWTトークンの形式が不正です");
                return;
            } catch (BadJWTException e) {
                logger.warn("JWT検証エラー: {}", e.getMessage());
                sendUnauthorizedResponse(response, "JWTトークンの検証に失敗しました");
                return;
            } catch (Exception e) {
                logger.error("JWT認証中に予期しないエラーが発生しました", e);
                sendUnauthorizedResponse(response, "認証処理中にエラーが発生しました");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(
                String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message));
    }

}