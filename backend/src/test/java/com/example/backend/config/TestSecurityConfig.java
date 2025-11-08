package com.example.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import com.example.backend.auth.filter.UserRegistrationFilter;

@TestConfiguration
public class TestSecurityConfig {
    @Autowired
    private UserRegistrationFilter userRegistrationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterAfter(userRegistrationFilter,
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
