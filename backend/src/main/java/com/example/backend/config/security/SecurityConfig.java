package com.example.backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.backend.auth.filter.JwtAuthFilter;
import com.example.backend.auth.filter.UserRegistrationFilter;

/**
 * Spring Securityの設定クラス
 * 
 * このクラスは、アプリケーション全体のセキュリティ設定を行います。
 * - CORS設定（異なるオリジンからのリクエストを許可）
 * - CSRF保護の無効化（JWT認証を使用するため不要）
 * - セッション管理の設定（ステートレス）
 * - 認可ルールの設定（/api/** は認証必須）
 * - 認証フィルターの登録
 */
@Configuration
@Profile("!test") // test環境では無効化する
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final UserRegistrationFilter userRegistrationFilter;
    private final CorsProperties corsProperties;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            UserRegistrationFilter userRegistrationFilter,
            CorsProperties corsProperties) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userRegistrationFilter = userRegistrationFilter;
        this.corsProperties = corsProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS設定を有効化
                .cors(cors -> {
                })

                // CSRF保護を無効化
                // セッションクッキーではなくJWT認証を利用するためCSRF保護を無効化
                .csrf(csrf -> csrf.disable())

                // セッション管理の設定
                // STATELESS: JWT認証利用のためステートレスに設定
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 認可ルールの設定
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(userRegistrationFilter, JwtAuthFilter.class);

        return http.build();
    }

    /**
     * CORS設定を定義するBean
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge());
        // /api/** パスに対してCORS設定を適用
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
