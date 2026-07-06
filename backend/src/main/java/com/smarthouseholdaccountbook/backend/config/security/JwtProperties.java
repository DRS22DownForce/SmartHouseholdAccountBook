package com.smarthouseholdaccountbook.backend.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT設定プロパティクラス
 *
 * application.properties から Cognito の Issuer URL と Client ID を読み込みます。
 * JWKS URL は Issuer の OIDC メタデータから自動解決されます。
 */
@Component
public class JwtProperties {

    @Value("${cognito.issuer-url}")
    private String issuerUrl;

    @Value("${cognito.client-id}")
    private String clientId;

    /**
     * Cognito の Issuer URL（JWT の iss クレームと一致する値）を取得する。
     */
    public String getIssuerUrl() {
        return issuerUrl;
    }

    /**
     * Cognito App Client ID（Access Token の client_id クレームと一致する値）を取得する。
     */
    public String getClientId() {
        return clientId;
    }
}
