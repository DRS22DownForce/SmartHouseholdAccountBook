package com.example.backend.auth.filter;

import com.example.backend.application.service.UserApplicationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 認証済みリクエストに対して、該当ユーザーがDBに存在しない場合は登録するフィルター。
 */
@Component
public class UserRegistrationFilter extends OncePerRequestFilter {

    private static final String API_PATH_PREFIX = "/api/";

    private final UserApplicationService userApplicationService;

    public UserRegistrationFilter(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (isAuthenticatedApiRequest(request)) {
            userApplicationService.ensureUserExists();
        }
        filterChain.doFilter(request, response);
    }
    // /api/** へのリクエストかつ認証されているかをチェックする
    private boolean isAuthenticatedApiRequest(HttpServletRequest request) {
        if (!request.getRequestURI().startsWith(API_PATH_PREFIX)) {
            return false;
        }
        // /api/** へのリクエストだが認証されていないものもこのフィルターを通過する可能性があるため、認証されているかをチェックする
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt;
    }
}
