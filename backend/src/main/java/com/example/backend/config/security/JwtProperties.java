package com.example.backend.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT設定プロパティクラス
 * 
 * application.propertiesからJWT関連の設定値を読み込みます。
 * CognitoのJWKセットURLを管理します。
 */
@Component
public class JwtProperties {
    @Value("${cognito.jwk-set-url}")
    private String jwkSetUrl;

    /**
     * CognitoのJWKセットURLを取得
     * 
     * @return JWKセットURL
     */
    public String getJwkSetUrl() {
        return jwkSetUrl;
    }
}

