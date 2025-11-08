package com.example.backend.auth.provider;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * 現在の認証情報を提供するクラス
 * 
 * このクラスは、Spring Securityのセキュリティコンテキストから
 * 現在の認証情報（JWT）を取得し、ユーザーIDやメールアドレスなどの
 * クレーム情報を提供します。
 */
@Component
public class CurrentAuthProvider {

    /**
     * 現在のJWTのsub（CognitoユーザーID）を取得
     * 
     * @return CognitoユーザーID（subクレームの値）
     */
    public String getCurrentSub() {
        return getClaimAsString("sub");
    }

    /**
     * 現在のJWTのemailを取得
     * 
     * @return メールアドレス（emailクレームの値）
     */
    public String getCurrentEmail() {
        return getClaimAsString("email");
    }

    /**
     * 任意のクレーム名でJWTから値を取得
     * 
     * @param claimName 取得したいクレーム名（例: "sub", "email"）
     * @return クレームの値（文字列）、存在しない場合はnull
     */
    public String getClaimAsString(String claimName) {
        return getJwt()
                .map(jwt -> jwt.getClaimAsString(claimName))
                .orElse(null);
    }
    
    /**
     * セキュリティコンテキストからJWTを取得
     * 
     * @return JWTオブジェクトのOptional、認証されていない場合は空のOptional
     */
    private Optional<Jwt> getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt) {
            return Optional.of((Jwt) authentication.getPrincipal());
        }
        return Optional.empty();
    }

}

