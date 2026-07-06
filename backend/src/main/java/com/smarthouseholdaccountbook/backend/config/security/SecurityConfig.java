package com.smarthouseholdaccountbook.backend.config.security;

import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.smarthouseholdaccountbook.backend.auth.filter.UserRegistrationFilter;

/**
 * Spring Securityの設定クラス
 * 
 * このクラスは、アプリケーション全体のセキュリティ設定を行います。
 * - CORS設定（異なるオリジンからのリクエストを許可）
 * - CSRF保護の無効化（JWT認証を使用するため不要）
 * - セッション管理の設定（ステートレス）
 * - 認可ルールの設定（/api/** は認証必須）
 * - OAuth2 Resource Server による JWT 認証
 */
@Configuration
@Profile("!test") // test環境では無効化する
public class SecurityConfig {
    private final UserRegistrationFilter userRegistrationFilter;
    private final CorsProperties corsProperties;
    private final ProblemDetailAuthenticationEntryPoint authenticationEntryPoint;
    private final ProblemDetailAccessDeniedHandler accessDeniedHandler;
    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    public SecurityConfig(
            UserRegistrationFilter userRegistrationFilter,
            CorsProperties corsProperties,
            ProblemDetailAuthenticationEntryPoint authenticationEntryPoint,
            ProblemDetailAccessDeniedHandler accessDeniedHandler,
            JwtDecoder jwtDecoder,
            JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.userRegistrationFilter = userRegistrationFilter;
        this.corsProperties = corsProperties;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
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

                // Security フィルター層の 401/403 も Problem Details 形式に統一
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                // 認可ルールの設定
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())

                // OAuth2 Resource Server: Bearer トークンを JwtDecoder で検証
                // SecurityContextHolderへのセットは自動登録されるBearerTokenAuthenticationFilterで行われる。
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)))

                // JWT認証 → ユーザー登録（未登録ならDBに作成）→ 通常処理
                .addFilterAfter(userRegistrationFilter, BearerTokenAuthenticationFilter.class);

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
