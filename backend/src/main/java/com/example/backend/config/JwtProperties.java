package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProperties {
    @Value("${cognito.jwk-set-url}")
    private String jwkSetUrl;

    public String getJwkSetUrl() {
        return jwkSetUrl;
    }
}
