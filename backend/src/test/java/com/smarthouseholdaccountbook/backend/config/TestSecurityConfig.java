package com.example.backend.config;

import com.example.backend.auth.filter.UserRegistrationFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Import;

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
                .addFilterBefore(userRegistrationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(testJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
