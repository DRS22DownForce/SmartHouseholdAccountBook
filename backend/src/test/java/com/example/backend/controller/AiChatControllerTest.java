package com.example.backend.controller;

import com.example.backend.application.service.AiChatService;
import com.example.backend.entity.ChatMessage;
import com.example.backend.entity.User;
import com.example.backend.generated.model.ChatHistoryResponse;
import com.example.backend.generated.model.ChatRequest;
import com.example.backend.generated.model.ChatResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AiChatControllerのユニットテストクラス
 * Controllerの責務（サービス呼び出しとレスポンス組み立て）に絞って検証する。
 */
@ExtendWith(MockitoExtension.class)
class AiChatControllerTest {

    @Mock
    private AiChatService aiChatService;

    @InjectMocks
    private AiChatController aiChatController;

    @Nested
    @DisplayName("apiAiChatPost")
    class ApiAiChatPost {

        @Test
        @DisplayName("メッセージ送信時、サービス結果を200で返す")
        void returnsOkWithChatResponse() {
            ChatRequest request = new ChatRequest();
            request.setMessage("今月の支出は?");
            when(aiChatService.chat("今月の支出は?")).thenReturn("合計10000円です");

            ResponseEntity<ChatResponse> response = aiChatController.apiAiChatPost(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("合計10000円です");
            verify(aiChatService).chat("今月の支出は?");
        }
    }

    @Nested
    @DisplayName("apiAiChatGet")
    class ApiAiChatGet {

        @Test
        @DisplayName("履歴取得時、エンティティをDTOへ変換して200で返す")
        void returnsOkWithChatHistoryResponse() {
            User user = new User("chat-user", "chat-user@example.com");
            ChatMessage message = new ChatMessage(ChatMessage.Role.USER, "食費を分析して", user);
            when(aiChatService.getChatHistory()).thenReturn(List.of(message));

            ResponseEntity<ChatHistoryResponse> response = aiChatController.apiAiChatGet();

            OffsetDateTime expectedOffset = message.getCreatedAt().atOffset(
                    ZoneOffset.systemDefault().getRules().getOffset(message.getCreatedAt()));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessages()).hasSize(1);
            assertThat(response.getBody().getMessages().get(0).getRole()).isEqualTo("USER");
            assertThat(response.getBody().getMessages().get(0).getContent()).isEqualTo("食費を分析して");
            assertThat(response.getBody().getMessages().get(0).getCreatedAt()).isEqualTo(expectedOffset);
            verify(aiChatService).getChatHistory();
        }
    }
}

