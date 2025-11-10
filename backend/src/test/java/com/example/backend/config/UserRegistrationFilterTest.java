// UserRegistrationIntegrationTest.java
package com.example.backend.config;

import com.example.backend.domain.repository.UserRepository;
import com.example.backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest //spring bootアプリケーション全体を起動してtestを実施
@AutoConfigureMockMvc //APIリクエストをテストするためのMockMvcを自動設定
@Transactional // テストごとにDBをロールバック
@ActiveProfiles("test") //application-test.propertiesを読み込む
@Import(TestSecurityConfig.class) //テスト用のセキュリティ設定を読み込む
class UserRegistrationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testUserIsRegisteredInDatabase() throws Exception {
        // テスト用JWTを作成
        Jwt jwt = new Jwt("token", null, null,
                Map.of("sub", "none"), Map.of("sub", "integrationSub", "email", "integration@example.com"));
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt, null, jwt.getClaimAsString("sub"));

        // 適当なAPIエンドポイントにリクエスト（例: /api/expenses など）
        mockMvc.perform(get("/api/expenses")
                .with(authentication(authentication)))
                .andExpect(status().isOk());

        // DBにユーザーが登録されたか確認
        User user = userRepository.findByCognitoSub("integrationSub").orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("integration@example.com");
    }
}