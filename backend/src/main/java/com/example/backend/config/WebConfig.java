package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

/**
 * CORS（Cross-Origin Resource Sharing）の設定
 */
@Configuration
public class WebConfig {

    private final CorsProperties corsProperties;

    public WebConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            @SuppressWarnings("null")
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new))
                        .allowedMethods(corsProperties.getAllowedMethods().toArray(String[]::new))
                        .allowedHeaders(corsProperties.getAllowedHeaders().toArray(String[]::new))
                        .exposedHeaders(corsProperties.getExposedHeaders().toArray(String[]::new))
                        .allowCredentials(corsProperties.isAllowCredentials())
                        .maxAge(corsProperties.getMaxAge());
            }
        };
    }
}