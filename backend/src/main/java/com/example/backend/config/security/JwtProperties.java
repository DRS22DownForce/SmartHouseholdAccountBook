package com.example.backend.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT設定プロパティクラス
 * 
 * application.propertiesからJWT関連の設定値を読み込みます。
 * CognitoのJWKセットURL、Issuer URL、Client IDを管理します。
 */
@Component
public class JwtProperties {
    @Value("${cognito.jwk-set-url}")
    private String jwkSetUrl;

    @Value("${cognito.issuer-url}")
    private String issuerUrl;

    @Value("${cognito.client-id}")
    private String clientId;

    /**
     * CognitoのJWKセットURLを取得
     * 
     * @return JWKセットURL
     */
    public String getJwkSetUrl() {
        return jwkSetUrl;
    }
    /**
     * CognitoのIssuer URL(iss)を取得
     * @return
     */
    public String getIssuerUrl() {
        return issuerUrl;
    }

    /**
     * CognitoのClient ID(aud)を取得
     * @return
     */
    public String getClientId() {
        return clientId;
    }
}

