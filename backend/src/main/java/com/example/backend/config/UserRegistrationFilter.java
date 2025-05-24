package com.example.backend.config;

import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.backend.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class UserRegistrationFilter extends OncePerRequestFilter {
    private final UserService userService;

    public UserRegistrationFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // /apiで始まるリクエストだけこのフィルターを有効化
        return !path.startsWith("/api");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // ユーザーが存在しない場合はデータベースに保存
        userService.createUserIfNotExists();
        filterChain.doFilter(request, response);
    }
}
