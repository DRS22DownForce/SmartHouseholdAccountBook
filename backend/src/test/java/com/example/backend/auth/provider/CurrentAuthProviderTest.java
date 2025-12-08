package com.example.backend.auth.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
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
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        // テスト対象のオブジェクトを作成
        currentAuthProvider = new CurrentAuthProvider();

        // セキュリティコンテキストをモック化
        securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("正常にJWTのsubを取得できる")
    void getCurrentSub_正常に取得() {
        // テストデータの準備: JWTトークンを作成
        String expectedSub = "cognitoSub123";
        Jwt jwt = createJwt(Map.of("sub", expectedSub, "email", "test@example.com"));
        // プロダクションコードと同じ方法でJwtAuthenticationTokenを作成
        // Collections.emptyList()を第2引数として渡すことで、認証済みとして扱われます
        JwtAuthenticationToken jwtAuthentication = new JwtAuthenticationToken(jwt, Collections.emptyList());

        // モックの設定: セキュリテキストから認証情報を取得できるように設定
        when(securityContext.getAuthentication()).thenReturn(jwtAuthentication);

        // テスト実行: 現在のsubを取得
        String result = currentAuthProvider.getCurrentSub();

        // 検証: 期待されるsubが取得できることを確認
        assertEquals(expectedSub, result);
    }

    @Test
    @DisplayName("正常にJWTのemailを取得できる")
    void getCurrentEmail_正常に取得() {
        // テストデータの準備: JWTトークンを作成
        String expectedEmail = "test@example.com";
        Jwt jwt = createJwt(Map.of("sub", "cognitoSub123", "email", expectedEmail));
        JwtAuthenticationToken jwtAuthentication = new JwtAuthenticationToken(jwt, Collections.emptyList());

        // モックの設定
        when(securityContext.getAuthentication()).thenReturn(jwtAuthentication);

        // テスト実行: 現在のemailを取得
        String result = currentAuthProvider.getCurrentEmail();

        // 検証: 期待されるemailが取得できることを確認
        assertEquals(expectedEmail, result);
    }

    @Test
    @DisplayName("任意のクレームを取得できる")
    void getClaimAsString_正常に取得() {
        // テストデータの準備: カスタムクレームを含むJWTトークンを作成
        String customClaimValue = "customValue";
        Jwt jwt = createJwt(Map.of("sub", "cognitoSub123", "customClaim", customClaimValue));
        JwtAuthenticationToken jwtAuthentication = new JwtAuthenticationToken(jwt, Collections.emptyList());

        // モックの設定
        when(securityContext.getAuthentication()).thenReturn(jwtAuthentication);

        // テスト実行: カスタムクレームを取得
        String result = currentAuthProvider.getClaimAsString("customClaim");

        // 検証: 期待されるクレーム値が取得できることを確認
        assertEquals(customClaimValue, result);
    }

    @Test
    @DisplayName("認証されていない場合はnullが返される")
    void getCurrentSub_認証されていない場合() {
        // モックの設定: 認証情報がnullの場合
        when(securityContext.getAuthentication()).thenReturn(null);

        // テスト実行: 現在のsubを取得
        String result = currentAuthProvider.getCurrentSub();

        // 検証: nullが返されることを確認
        assertNull(result);
    }

    @Test
    @DisplayName("JWTでない認証情報の場合はnullが返される")
    void getCurrentSub_JWTでない認証情報の場合() {
        // モックの設定: JWTでない認証情報を設定
        Authentication nonJwtAuthentication = mock(Authentication.class);
        when(nonJwtAuthentication.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(nonJwtAuthentication);

        // テスト実行: 現在のsubを取得
        String result = currentAuthProvider.getCurrentSub();

        // 検証: nullが返されることを確認
        assertNull(result);
    }

    @Test
    @DisplayName("存在しないクレームの場合はnullが返される")
    void getClaimAsString_存在しないクレームの場合() {
        // テストデータの準備: クレームが少ないJWTトークンを作成
        Jwt jwt = createJwt(Map.of("sub", "cognitoSub123"));
        JwtAuthenticationToken jwtAuthentication = new JwtAuthenticationToken(jwt, Collections.emptyList());

        // モックの設定
        when(securityContext.getAuthentication()).thenReturn(jwtAuthentication);

        // テスト実行: 存在しないクレームを取得
        String result = currentAuthProvider.getClaimAsString("nonExistentClaim");

        // 検証: nullが返されることを確認
        assertNull(result);
    }

    @Test
    @DisplayName("認証されていない場合でも例外が発生しない")
    void getCurrentEmail_認証されていない場合でも例外が発生しない() {
        // モックの設定: 認証情報がnullの場合
        when(securityContext.getAuthentication()).thenReturn(null);

        // テスト実行と検証: 例外が発生しないことを確認
        assertDoesNotThrow(() -> {
            String result = currentAuthProvider.getCurrentEmail();
            assertNull(result);
        });
    }

    /**
     * テスト用のJWTトークンを作成するヘルパーメソッド
     * 
     * @param claims JWTに含めるクレーム
     * @return JWTオブジェクト
     */
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

