package com.smarthouseholdaccountbook.backend.auth.provider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Map;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CurrentAuthProviderのテストクラス
 * 
 * 現在の認証情報を提供するプロバイダーのテストを行います。
 * Spring SecurityのセキュリティコンテキストからJWTを取得する機能をテストします。
 */
class CurrentAuthProviderTest {

    private CurrentAuthProvider currentAuthProvider;

    @BeforeEach
    void setUp() {
        currentAuthProvider = new CurrentAuthProvider();
        SecurityContextHolder.createEmptyContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("正常にJWTのsubを取得できる")
    void getCurrentSub_正常に取得() {
        String expectedSub = "cognitoSub123";
        Jwt jwt = createJwt(Map.of("sub", expectedSub, "email", "test@example.com"));
        JwtAuthenticationToken jwtAuthentication = new JwtAuthenticationToken(jwt, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        String result = currentAuthProvider.getCurrentSub();

        assertEquals(expectedSub, result);
    }

    @Test
    @DisplayName("正常にJWTのemailを取得できる")
    void getCurrentEmail_正常に取得() {
        String expectedEmail = "test@example.com";
        Jwt jwt = createJwt(Map.of("sub", "cognitoSub123", "email", expectedEmail));
        JwtAuthenticationToken jwtAuthentication = new JwtAuthenticationToken(jwt, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        String result = currentAuthProvider.getCurrentEmail();

        assertEquals(expectedEmail, result);
    }

    @Test
    @DisplayName("任意のクレームを取得できる")
    void getClaimAsString_正常に取得() {
        String customClaimValue = "customValue";
        Jwt jwt = createJwt(Map.of("sub", "cognitoSub123", "customClaim", customClaimValue));
        JwtAuthenticationToken jwtAuthentication = new JwtAuthenticationToken(jwt, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        String result = currentAuthProvider.getClaimAsString("customClaim");

        assertEquals(customClaimValue, result);
    }

    @Test
    @DisplayName("認証されていない場合はnullが返される")
    void getCurrentSub_認証されていない場合() {
        SecurityContextHolder.getContext().setAuthentication(null);

        String result = currentAuthProvider.getCurrentSub();

        assertNull(result);
    }

    @Test
    @DisplayName("JWTでない認証情報の場合はnullが返される")
    void getCurrentSub_JWTでない認証情報の場合() {
        Authentication nonJwtAuthentication = mock(Authentication.class);
        when(nonJwtAuthentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(nonJwtAuthentication);

        String result = currentAuthProvider.getCurrentSub();

        assertNull(result);
    }

    @Test
    @DisplayName("存在しないクレームの場合はnullが返される")
    void getClaimAsString_存在しないクレームの場合() {
        Jwt jwt = createJwt(Map.of("sub", "cognitoSub123"));
        JwtAuthenticationToken jwtAuthentication = new JwtAuthenticationToken(jwt, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        String result = currentAuthProvider.getClaimAsString("nonExistentClaim");

        assertNull(result);
    }

    @Test
    @DisplayName("認証されていない場合でも例外が発生しない")
    void getCurrentEmail_認証されていない場合でも例外が発生しない() {
        SecurityContextHolder.getContext().setAuthentication(null);

        assertDoesNotThrow(() -> {
            String result = currentAuthProvider.getCurrentEmail();
            assertNull(result);
        });
    }

    private Jwt createJwt(Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .claim("sub", claims.get("sub"))
                .claims(c -> c.putAll(claims))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();
    }
}
