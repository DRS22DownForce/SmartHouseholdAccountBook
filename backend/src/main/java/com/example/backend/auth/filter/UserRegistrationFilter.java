package com.example.backend.auth.filter;

import com.example.backend.application.service.UserApplicationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ユーザー登録フィルター
 * 
 * このフィルターは、JWT認証が成功した後、
 * データベースにユーザーが存在しない場合に自動的に登録します。
 * /apiで始まるパスに対してのみ動作します。
 */
@Component
public class UserRegistrationFilter extends OncePerRequestFilter {
    private final UserApplicationService userApplicationService;

    /**
     * コンストラクタ
     * 
     * @param userApplicationService ユーザーアプリケーションサービス
     */
    public UserRegistrationFilter(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @Override
    protected boolean shouldNotFilter( @NonNull HttpServletRequest request) throws ServletException {
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
        userApplicationService.createUserIfNotExists();
        filterChain.doFilter(request, response);
    }
}

