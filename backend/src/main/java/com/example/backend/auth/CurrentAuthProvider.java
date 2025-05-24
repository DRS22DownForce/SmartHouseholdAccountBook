package com.example.backend.auth;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentAuthProvider {

    /**
     * 現在のJWTのsub（CognitoユーザーID）を取得
     */
    public String getCurrentSub() {
        return getClaimAsString("sub");
    }

    /**
     * 現在のJWTのemailを取得
     */
    public String getCurrentEmail() {
        return getClaimAsString("email");
    }

    /**
     * 任意のクレーム名でJWTから値を取得
     */
    public String getClaimAsString(String claimName) {
        return getJwt()
                .map(jwt -> jwt.getClaimAsString(claimName))
                .orElse(null);
    }
    
    private Optional<Jwt> getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt) {
            return Optional.of((Jwt) authentication.getPrincipal());
        }
        return Optional.empty();
    }

}