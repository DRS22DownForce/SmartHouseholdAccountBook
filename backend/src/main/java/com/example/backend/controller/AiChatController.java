package com.example.backend.controller;

import com.example.backend.application.service.AiChatService;
import com.example.backend.generated.api.ChatApi;
import com.example.backend.generated.model.ChatRequest;
import com.example.backend.generated.model.ChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiChatController implements ChatApi {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @Override
    public ResponseEntity<ChatResponse> apiChatPost(ChatRequest chatRequest) {
        String responseMessage = aiChatService.chat(chatRequest.getMessage());
        ChatResponse response = new ChatResponse();
        response.setMessage(responseMessage);
        return ResponseEntity.ok(response);
    }
}
