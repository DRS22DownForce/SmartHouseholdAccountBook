package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test") //test環境では無効化する
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final UserRegistrationFilter userRegistrationFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserRegistrationFilter userRegistrationFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userRegistrationFilter = userRegistrationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF保護を無効化
            // REST APIでJWTを使用する場合、CSRF保護は不要です
            // CSRFは主にセッションクッキーを使用する場合に有効です
            // Authorizationヘッダーはブラウザから自動送信されないため、CSRF保護は不要です
            .csrf(csrf -> csrf.disable())
            
            // セッション管理の設定
            // STATELESS: セッションを作成しない（JWT認証ではステートレスが推奨）
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 認可ルールの設定
            .authorizeHttpRequests(authz -> authz
                // /api/** で始まるパスは認証が必要
                // authenticated()は認証済みユーザーのみアクセス可能にする
                .requestMatchers("/api/**").authenticated()
                
                // それ以外のパスは誰でもアクセス可能
                // 例: /health, /actuator, /swagger-ui など
                .anyRequest().permitAll()
            )
            
            // フィルターの追加
            // JwtAuthFilterをUsernamePasswordAuthenticationFilterの前に配置
            // これにより、JWT認証が先に実行されます
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            
            // UserRegistrationFilterをJwtAuthFilterの後に配置
            // JWT認証が成功した後、ユーザーが存在しない場合は自動登録します
            .addFilterAfter(userRegistrationFilter, JwtAuthFilter.class);
        
        return http.build();
    }
}