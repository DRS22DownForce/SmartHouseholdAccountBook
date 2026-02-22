package com.example.backend.integrationTest;

import com.example.backend.application.service.AiChatService;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.entity.ChatMessage;
import com.example.backend.entity.User;
import com.example.backend.exception.AiServiceException;
import com.example.backend.generated.model.ChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AiChatControllerの統合テスト。
 * HTTP境界（JSON入出力・ステータス）とControllerの契約を検証する。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AiChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    //外部APIを呼び出さないようにモック化する。
    @MockitoBean
    private AiChatService aiChatService;

    @Nested
    @DisplayName("POST /api/ai/chat")
    class ApiAiChatPost {

        @Test
        @DisplayName("正常なリクエストで200と応答メッセージを返す")
        void returnsOkWithMessageWhenRequestIsValid() throws Exception {
            // given
            ChatRequest request = new ChatRequest();
            request.setMessage("今月の支出を教えて");
            when(aiChatService.chat("今月の支出を教えて")).thenReturn("合計10000円です");

            // when
            String responseBody = mockMvc.perform(post("/api/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // then
            JsonNode node = objectMapper.readTree(responseBody);
            assertThat(node.get("message").asText()).isEqualTo("合計10000円です");
        }

        @Test
        @DisplayName("不正JSONのとき400を返す")
        void returnsBadRequestWhenJsonInvalid() throws Exception {
            // when
            mockMvc.perform(post("/api/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ invalid json }"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("AIサービス例外のとき500とエラーメッセージを返す")
        void returnsInternalServerErrorWhenAiServiceFails() throws Exception {
            // given
            ChatRequest request = new ChatRequest();
            request.setMessage("テスト");
            doThrow(new AiServiceException("AI接続に失敗しました"))
                    .when(aiChatService).chat("テスト");

            // when
            String responseBody = mockMvc.perform(post("/api/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andReturn().getResponse().getContentAsString();

            // then
            JsonNode node = objectMapper.readTree(responseBody);
            assertThat(node.get("message").asText()).isEqualTo("AI接続に失敗しました");
            assertThat(node.has("timestamp")).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /api/ai/chat")
    class ApiAiChatGet {

        @Test
        @DisplayName("履歴取得で200とメッセージ配列を返す")
        void returnsOkWithMessages() throws Exception {
            // given
            ChatMessage message = new ChatMessage(
                    ChatMessage.Role.USER,
                    "履歴メッセージ",
                    new User("integration-chat-user", "integration-chat-user@example.com"));
            when(aiChatService.getChatHistory()).thenReturn(List.of(message));

            // when
            String responseBody = mockMvc.perform(get("/api/ai/chat"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // then
            JsonNode node = objectMapper.readTree(responseBody);
            assertThat(node.get("messages")).isNotNull();
            assertThat(node.get("messages").isArray()).isTrue();
            assertThat(node.get("messages").get(0).get("role").asText()).isEqualTo("USER");
            assertThat(node.get("messages").get(0).get("content").asText()).isEqualTo("履歴メッセージ");
            assertThat(node.get("messages").get(0).has("createdAt")).isTrue();
        }
    }
}
