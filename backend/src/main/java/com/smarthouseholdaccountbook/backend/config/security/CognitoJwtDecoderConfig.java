package com.smarthouseholdaccountbook.backend.config.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * Cognito 発行 JWT を検証する {@link JwtDecoder} の設定。
 *
 * <p>Spring OAuth2 Resource Server 標準の {@link NimbusJwtDecoder} に、
 * Cognito Access Token 固有のクレーム検証（client_id / token_use）を追加して利用します。
 */
@Configuration
@Profile("!test")
public class CognitoJwtDecoderConfig {

    /** Cognito Access Token の token_use クレーム値 */
    private static final String COGNITO_ACCESS_TOKEN_USE = "access";

    private final JwtProperties jwtProperties;

    public CognitoJwtDecoderConfig(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * JWKS から公開鍵を取得し、署名・有効期限・Cognito 固有クレームを検証する Decoder。
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(jwtProperties.getJwkSetUrl())
                .build();
        decoder.setJwtValidator(createCognitoTokenValidator(jwtProperties));
        return decoder;
    }

    /**
     * 権限は付与せず、認証（誰であるか）のみを SecurityContext に載せる。
     * 現状のアプリはロールベース認可を行っていないため、空のまま維持する。
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> List.of());
        return converter;
    }

    /**
     * Cognito Access Token 向けの検証チェーンを組み立てる。
     * テストからも同じロジックを検証できるよう package-private にしている。
     */
    static OAuth2TokenValidator<Jwt> createCognitoTokenValidator(JwtProperties props) {
        // iss + exp/nbf の標準検証
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(props.getIssuerUrl());

        // Cognito Access Token は aud ではなく client_id で App Client ID を表す
        OAuth2TokenValidator<Jwt> withClientId = jwt -> {
            String clientId = jwt.getClaimAsString("client_id");
            if (props.getClientId().equals(clientId)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "client_id does not match", null));
        };

        // Access Token のみ許可（ID Token 等を弾く）
        OAuth2TokenValidator<Jwt> withTokenUse = jwt -> {
            if (COGNITO_ACCESS_TOKEN_USE.equals(jwt.getClaimAsString("token_use"))) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "token_use must be access", null));
        };

        return new DelegatingOAuth2TokenValidator<>(withIssuer, withClientId, withTokenUse);
    }

    // TODO: issuer-uri 一本化（COGNITO_JWK_SET_URL 廃止）を行う場合は
    //       NimbusJwtDecoder.withIssuerLocation(props.getIssuerUrl()) への切替を検討する。
    // TODO: スコープベース認可を導入する場合は JwtAuthenticationConverter で
    //       scope クレームを GrantedAuthority に変換する。
    // TODO: Cognito App Client の Allowed OAuth Scopes（openid 等）が有効かデプロイ時に確認する。
}
