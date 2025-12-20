package com.example.backend.controller;

import com.example.backend.application.service.AiChatService;
import com.example.backend.entity.ChatMessage;
import com.example.backend.generated.api.ChatApi;
import com.example.backend.generated.model.ChatHistoryResponse;
import com.example.backend.generated.model.ChatMessageDto;
import com.example.backend.generated.model.ChatRequest;
import com.example.backend.generated.model.ChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AIチャットに関するREST APIコントローラー
 * 
 * このコントローラーはAIチャットの送信と会話履歴の取得を提供します。
 */
@RestController
public class AiChatController implements ChatApi {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }


    @Override
    public ResponseEntity<ChatHistoryResponse> apiChatGet() {
        // 1. サービスから会話履歴を取得
        List<ChatMessage> messages = aiChatService.getChatHistory();

        // 2. エンティティをDTOに変換
        List<ChatMessageDto> messageDtos = messages.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        // 3. レスポンスオブジェクトを作成
        ChatHistoryResponse response = new ChatHistoryResponse();
        response.setMessages(messageDtos);

        return ResponseEntity.ok(response);
    }

    /**
     * AIチャットにメッセージを送信する
     * 
     * @param chatRequest チャットリクエスト
     * @return チャットレスポンス
     */
    @Override
    public ResponseEntity<ChatResponse> apiChatPost(ChatRequest chatRequest) {
        String responseMessage = aiChatService.chat(chatRequest.getMessage());
        ChatResponse response = new ChatResponse();
        response.setMessage(responseMessage);
        return ResponseEntity.ok(response);
    }

    /**
     * ChatMessageエンティティをChatMessageDtoに変換する
     * 
     * @param message チャットメッセージエンティティ
     * @return チャットメッセージDTO
     */
    private ChatMessageDto toDto(ChatMessage message) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        // LocalDateTimeをOffsetDateTimeに変換（システムのタイムゾーンを使用）
        OffsetDateTime offsetDateTime = message.getCreatedAt().atOffset(ZoneOffset.systemDefault().getRules().getOffset(message.getCreatedAt()));
        dto.setCreatedAt(offsetDateTime);
        return dto;
    }
}
