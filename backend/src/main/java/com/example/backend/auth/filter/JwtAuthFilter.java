package com.example.backend.auth.filter;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.proc.*;
import com.nimbusds.jwt.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private final JWKSource<SecurityContext> remoteJwkSet;

    public JwtAuthFilter(JwtProperties jwtProperties) {
        try {
            DefaultResourceRetriever resourceRetriever = new DefaultResourceRetriever(5000, 5000, 1024 * 1024);
            @SuppressWarnings("deprecation")
            RemoteJWKSet<SecurityContext> remoteJWKSet = new RemoteJWKSet<>(
                    URI.create(jwtProperties.getJwkSetUrl()).toURL(), resourceRetriever);
            this.remoteJwkSet = remoteJWKSet;

            this.jwtProcessor = new DefaultJWTProcessor<>();
            this.jwtProcessor.setJWSKeySelector(
                    new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, this.remoteJwkSet));
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
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                authenticateJwtToken(authHeader.substring(7));
            } catch (ParseException e) {
                logger.warn("JWTパースエラー: {}", e.getMessage());
                sendUnauthorizedResponse(response, "JWTトークンの形式が不正です");
                return;
            } catch (JOSEException e) {
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

    private void authenticateJwtToken(String jwtToken) throws ParseException, JOSEException, BadJOSEException {
        JWTClaimsSet claimsSet = jwtProcessor.process(jwtToken, null);
        Map<String, Object> headerMap = SignedJWT.parse(jwtToken).getHeader().toJSONObject();
        Jwt jwt = buildSpringSecurityJwt(jwtToken, headerMap, claimsSet);
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(jwt, Collections.emptyList()));
    }

    private Jwt buildSpringSecurityJwt(String jwtToken, Map<String, Object> headerMap, JWTClaimsSet claimsSet) {
        Jwt.Builder builder = Jwt.withTokenValue(jwtToken).headers(h -> h.putAll(headerMap));

        Date issuedAt = claimsSet.getIssueTime();
        if (issuedAt != null) builder.issuedAt(issuedAt.toInstant());
        Date expiresAt = claimsSet.getExpirationTime();
        if (expiresAt != null) builder.expiresAt(expiresAt.toInstant());
        Date notBefore = claimsSet.getNotBeforeTime();
        if (notBefore != null) builder.notBefore(notBefore.toInstant());

        Map<String, Object> claims = new HashMap<>(claimsSet.getClaims());
        claims.remove("iat");
        claims.remove("exp");
        claims.remove("nbf");
        builder.claims(c -> c.putAll(claims));

        return builder.build();
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message));
    }

}

