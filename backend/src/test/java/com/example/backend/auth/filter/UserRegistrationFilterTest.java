// UserRegistrationIntegrationTest.java
package com.example.backend.auth.filter;

import com.example.backend.config.TestJwtAuthenticationFilter;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;

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
@SuppressWarnings("null")
class UserRegistrationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testUserIsRegisteredInDatabase() throws Exception {
        // TestJwtAuthenticationFilter と同じ sub/email のJWTでリクエストし、フィルターがユーザー登録することを検証する
        Jwt jwt = new Jwt("token", null, null,
                Map.of("sub", "none"), Map.of("sub", TestJwtAuthenticationFilter.TEST_SUB, "email", TestJwtAuthenticationFilter.TEST_EMAIL));
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt, null, jwt.getClaimAsString("sub"));

        mockMvc.perform(get("/api/expenses/months")
                .with(authentication(authentication)))
                .andExpect(status().isOk());

        User user = userRepository.findByCognitoSub(TestJwtAuthenticationFilter.TEST_SUB).orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(TestJwtAuthenticationFilter.TEST_EMAIL);
    }
}