package com.example.backend.controller;

import com.example.backend.application.service.AiChatService;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.generated.model.ChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AiChatControllerの統合テストクラス
 * 
 * AIチャットコントローラーのエンドポイントをテストします。
 * このコントローラーはAIチャットサービスを呼び出して、ユーザーの質問に答えます。
 * 
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@SuppressWarnings("null")
class AiChatControllerTest {

    // MockMvc: Spring MVCのコントローラーをテストするためのモックオブジェクト
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiChatService aiChatService;

    // ObjectMapper: JSONとJavaオブジェクトの変換に使用
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("AIチャットAPIに正常にリクエストできる")
    void apiChatPost_正常にリクエストできる() throws Exception {
        // テストデータの準備: チャットリクエストを作成
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessage("今月の支出はいくらですか？");

        // モックの設定: AiChatServiceのchatメソッドが呼ばれたときに、期待される応答を返すように設定
        String expectedResponse = "今月の支出は合計で10,000円です。";
        String userMessage = chatRequest.getMessage();
        when(aiChatService.chat(userMessage)).thenReturn(expectedResponse);

        // テスト実行: POSTリクエストを送信
        // 検証: ステータスコード200（OK）が返され、期待される応答が含まれていることを確認
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(expectedResponse));
    }

    @Test
    @DisplayName("AIチャットAPIでJSON形式が不正な場合は400エラー")
    void apiChatPost_不正なJSON形式() throws Exception {
        // テスト実行と検証: 不正なJSONを送信すると400エラーが返されることを確認
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }
}

