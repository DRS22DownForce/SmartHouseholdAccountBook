package com.smarthouseholdaccountbook.backend.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 認証済みだが権限が足りない場合の 403 レスポンスを Problem Details 形式に統一します。
 */
@Component
public class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {
    private final ProblemDetailSecurityResponseWriter responseWriter;

    public ProblemDetailAccessDeniedHandler(ProblemDetailSecurityResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        responseWriter.write(
                request,
                response,
                HttpStatus.FORBIDDEN,
                "この操作を行う権限がありません。");
    }
}
