package com.smarthouseholdaccountbook.backend.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 未認証または認証失敗時の 401 レスポンスを Problem Details 形式に統一します。
 */
@Component
public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ProblemDetailSecurityResponseWriter responseWriter;

    public ProblemDetailAuthenticationEntryPoint(ProblemDetailSecurityResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        responseWriter.write(
                request,
                response,
                HttpStatus.UNAUTHORIZED,
                "認証が必要です。再ログインしてください。");
    }
}
