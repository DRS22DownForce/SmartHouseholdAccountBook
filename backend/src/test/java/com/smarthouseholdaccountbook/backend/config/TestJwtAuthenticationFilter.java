package com.example.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

/**
 * 統合テスト用フィルター。
 * リクエストごとにセキュリティコンテキストへテスト用JWTを設定し、セキュリティコンテキストにユーザー情報をセットする。
 */
@Component
public class TestJwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String TEST_SUB = "cognitoSub";
    public static final String TEST_EMAIL = "test@example.com";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Jwt jwt = createTestJwt();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext(); // テスト後にセキュリティコンテキストをクリアする。
        }
    }

    private static Jwt createTestJwt() {
        Instant now = Instant.now();
        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .claim("sub", TEST_SUB)
                .claim("email", TEST_EMAIL)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();
    }
}
