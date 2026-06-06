package com.smarthouseholdaccountbook.backend.config;

import com.smarthouseholdaccountbook.backend.auth.filter.UserRegistrationFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@TestConfiguration
@Import(TestJwtAuthenticationFilter.class)
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UserRegistrationFilter userRegistrationFilter,
            TestJwtAuthenticationFilter testJwtAuthenticationFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                // 後から登録したフィルターほど内側になるため、JWT → ユーザー登録の順で実行される。
                .addFilterBefore(testJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(userRegistrationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
