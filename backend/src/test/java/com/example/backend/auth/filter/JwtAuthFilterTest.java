package com.example.backend.auth.filter;

import com.example.backend.config.security.JwtProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * JwtAuthFilterのテストクラス
 * 
 * JWT認証フィルターのテストを行います。
 * フィルターはリクエストヘッダーからJWTトークンを取得し、検証してセキュリティコンテキストに設定します。
 * 
 * このテストでは、JWTプロセッサーをモック化してフィルターのロジックをテストします。
 * 実際のJWT検証（JWKセットへのアクセスなど）は統合テストで行うことを推奨します。
 */
class JwtAuthFilterTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;
    private JwtProperties jwtProperties;
    private JwtAuthFilter jwtAuthFilter;
    private ConfigurableJWTProcessor<SecurityContext> mockJwtProcessor;

    @BeforeEach
    void setUp() throws Exception {
        // セキュリティコンテキストをクリア（各テストの前にクリーンな状態にする）
        SecurityContextHolder.clearContext();

        // リクエスト、レスポンス、フィルターチェーンをモック化
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        // JwtPropertiesをモック化
        jwtProperties = mock(JwtProperties.class);
        when(jwtProperties.getJwkSetUrl()).thenReturn("https://dummy-jwk-url.com/.well-known/jwks.json");

        // JWTプロセッサーをモック化
        @SuppressWarnings("unchecked")
        ConfigurableJWTProcessor<SecurityContext> processor =
            mock(ConfigurableJWTProcessor.class);
        mockJwtProcessor = processor;

        // フィルターを初期化
        jwtAuthFilter = new JwtAuthFilter(jwtProperties);
        jwtAuthFilter.jwtProcessor = mockJwtProcessor;
    }

    @Test
    @DisplayName("Authorizationヘッダーがない場合はフィルターチェーンを続行する")
    void doFilterInternal_Authorizationヘッダーがない場合() throws Exception {
        // テストデータの準備: Authorizationヘッダーがないリクエスト
        when(request.getHeader("Authorization")).thenReturn(null);

        // テスト実行: フィルターを実行
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // 検証: フィルターチェーンが呼び出されることを確認
        verify(filterChain, times(1)).doFilter(request, response);
        
        // 検証: JWTプロセッサーが呼び出されないことを確認
        verify(mockJwtProcessor, never()).process(anyString(), any());
        
        // 検証: セキュリティコンテキストに認証情報が設定されていないことを確認
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("AuthorizationヘッダーがBearerで始まらない場合はフィルターチェーンを続行する")
    void doFilterInternal_Bearerで始まらない場合() throws Exception {
        // テストデータの準備: Bearerで始まらないAuthorizationヘッダー（例: Basic認証）
        when(request.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0");

        // テスト実行: フィルターを実行
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // 検証: フィルターチェーンが呼び出されることを確認
        verify(filterChain, times(1)).doFilter(request, response);
        
        // 検証: JWTプロセッサーが呼び出されないことを確認
        verify(mockJwtProcessor, never()).process(anyString(), any());
        
        // 検証: セキュリティコンテキストに認証情報が設定されていないことを確認
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("不正なJWTトークンの場合は401エラーを返す")
    void doFilterInternal_不正なJWTトークンの場合() throws Exception {
        // テストデータの準備: 不正なJWTトークンを含むAuthorizationヘッダー
        String invalidToken = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        
        // StringWriterを使用してレスポンスの内容を取得
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // JWTプロセッサーがParseExceptionをスローするように設定
        when(mockJwtProcessor.process(anyString(), any())).thenThrow(new ParseException("Invalid JWT", 0));

        // テスト実行: フィルターを実行
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // 検証: 401エラーが返されることを確認
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, times(1)).setContentType("application/json; charset=UTF-8");
        
        // 検証: エラーメッセージが含まれていることを確認
        printWriter.flush();
        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("JWTトークンの形式が不正です"));
        
        // 検証: フィルターチェーンが呼び出されないことを確認
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("JWT検証エラーの場合は401エラーを返す")
    void doFilterInternal_JWT検証エラーの場合() throws Exception {
        // テストデータの準備: JWTトークンを含むAuthorizationヘッダー
        String token = "valid.format.but.invalid.signature";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        // StringWriterを使用してレスポンスの内容を取得
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // JWTプロセッサーがJOSEExceptionをスローするように設定
        // これは、JWTの署名検証に失敗した場合をシミュレートします
        when(mockJwtProcessor.process(anyString(), any())).thenThrow(new JOSEException("Invalid signature"));

        // テスト実行: フィルターを実行
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // 検証: 401エラーが返されることを確認
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // 検証: エラーメッセージが含まれていることを確認
        printWriter.flush();
        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("JWTトークンの検証に失敗しました"));
        
        // 検証: フィルターチェーンが呼び出されないことを確認
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("正常なJWTトークンの場合はセキュリティコンテキストに認証情報が設定される")
    void doFilterInternal_正常なJWTトークンの場合() throws Exception {
        // テストデータの準備: 正常なJWTトークンを含むAuthorizationヘッダー
        String validToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXN1YiIsImVtYWlsIjoidGVzdEBleGFtcGxlLmNvbSJ9.signature";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // JWTプロセッサーが正常にJWTクレームセットを返すように設定
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject("test-sub")
            .claim("email", "test@example.com")
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 3600000))
            .build();
        when(mockJwtProcessor.process(anyString(), any())).thenReturn(claimsSet);

        // テスト実行: フィルターを実行
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // 検証: フィルターチェーンが呼び出されることを確認
        verify(filterChain, times(1)).doFilter(request, response);
        
        // 検証: JWTプロセッサーが呼び出されることを確認
        verify(mockJwtProcessor, times(1)).process(eq(validToken), any());

        // 検証: セキュリティコンテキストに認証情報が設定されていることを確認
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        JwtAuthenticationToken jwtAuthentication = (JwtAuthenticationToken) authentication;
        assertEquals("test-sub", jwtAuthentication.getToken().getSubject());
        assertEquals("test@example.com", jwtAuthentication.getToken().getClaim("email"));
    }

    @Test
    @DisplayName("予期しないエラーが発生した場合は401エラーを返す")
    void doFilterInternal_予期しないエラーの場合() throws Exception {
        // テストデータの準備: JWTトークンを含むAuthorizationヘッダー
        String token = "some.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        // StringWriterを使用してレスポンスの内容を取得
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // JWTプロセッサーが予期しない例外をスローするように設定
        when(mockJwtProcessor.process(anyString(), any())).thenThrow(new RuntimeException("Unexpected error"));

        // テスト実行: フィルターを実行
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // 検証: 401エラーが返されることを確認
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // 検証: エラーメッセージが含まれていることを確認
        printWriter.flush();
        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("認証処理中にエラーが発生しました"));
        
        // 検証: フィルターチェーンが呼び出されないことを確認
        verify(filterChain, never()).doFilter(request, response);
    }
}

