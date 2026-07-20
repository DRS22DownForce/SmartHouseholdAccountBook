package com.smarthouseholdaccountbook.backend.config.security;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CognitoJwtDecoderConfig} が組み立てる Cognito 向け JWT 検証チェーンのテスト。
 *
 * <p>JWKS へのネットワークアクセスは行わず、クレーム検証ロジックのみを検証する。
 */
class CognitoJwtDecoderConfigTest {

    private static final String ISSUER = "https://cognito-idp.ap-northeast-1.amazonaws.com/ap-northeast-1_test";
    private static final String CLIENT_ID = "test-client-id";

    private OAuth2TokenValidator<Jwt> validator;

    @BeforeEach
    void setUp() {
        validator = CognitoJwtDecoderConfig.createCognitoTokenValidator(new TestJwtProperties());
    }

    @Test
    @DisplayName("iss / client_id / token_use が正しい Access Token は検証を通過する")
    void validAccessToken_passesValidation() {
        Jwt jwt = buildJwt(ISSUER, CLIENT_ID, "access", Instant.now().plusSeconds(3600));

        assertFalse(validator.validate(jwt).hasErrors());
    }

    @Test
    @DisplayName("token_use が id の場合は拒否される")
    void idToken_isRejected() {
        Jwt jwt = buildJwt(ISSUER, CLIENT_ID, "id", Instant.now().plusSeconds(3600));

        assertTrue(validator.validate(jwt).hasErrors());
    }

    @Test
    @DisplayName("client_id が一致しない場合は拒否される")
    void wrongClientId_isRejected() {
        Jwt jwt = buildJwt(ISSUER, "wrong-client-id", "access", Instant.now().plusSeconds(3600));

        assertTrue(validator.validate(jwt).hasErrors());
    }

    @Test
    @DisplayName("iss が一致しない場合は拒否される")
    void wrongIssuer_isRejected() {
        Jwt jwt = buildJwt("https://evil.example.com", CLIENT_ID, "access", Instant.now().plusSeconds(3600));

        assertTrue(validator.validate(jwt).hasErrors());
    }

    @Test
    @DisplayName("有効期限切れの場合は拒否される")
    void expiredToken_isRejected() {
        Instant expiredAt = Instant.now().minusSeconds(60);
        Instant issuedAt = expiredAt.minusSeconds(3600);
        Jwt jwt = buildJwt(ISSUER, CLIENT_ID, "access", issuedAt, expiredAt);

        assertTrue(validator.validate(jwt).hasErrors());
    }

    private static Jwt buildJwt(String issuer, String clientId, String tokenUse, Instant expiresAt) {
        return buildJwt(issuer, clientId, tokenUse, Instant.now(), expiresAt);
    }

    private static Jwt buildJwt(String issuer, String clientId, String tokenUse, Instant issuedAt, Instant expiresAt) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .issuer(issuer)
                .claim("client_id", clientId)
                .claim("token_use", tokenUse)
                .subject("test-sub")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * {@link JwtProperties} のテスト用スタブ（@Value 注入なし）。
     */
    private static final class TestJwtProperties extends JwtProperties {
        @Override
        public String getIssuerUrl() {
            return ISSUER;
        }

        @Override
        public String getClientId() {
            return CLIENT_ID;
        }
    }
}
