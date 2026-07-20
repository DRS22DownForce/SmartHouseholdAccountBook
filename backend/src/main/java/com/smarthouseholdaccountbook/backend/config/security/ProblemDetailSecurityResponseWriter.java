package com.smarthouseholdaccountbook.backend.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;

/**
 * Spring Security のフィルター層で発生したエラーを Problem Details として書き出す共通部品。
 *
 * Security の例外は Controller へ到達しないため、GlobalExceptionHandler ではなく
 * AuthenticationEntryPoint / AccessDeniedHandler からこのクラスを使います。
 */
@Component
public class ProblemDetailSecurityResponseWriter {
    private final JsonMapper jsonMapper;

    public ProblemDetailSecurityResponseWriter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String detail) throws IOException {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(jsonMapper.writeValueAsString(problemDetail));
    }
}
