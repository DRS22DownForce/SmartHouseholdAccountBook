package com.example.backend.application.service;

import com.example.backend.domain.repository.ChatMessageRepository;
import com.example.backend.domain.repository.ExpenseRepository;
import com.example.backend.entity.ChatMessage;
import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
@Transactional
public class AiChatService {

    private final ExpenseRepository expenseRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserApplicationService userApplicationService;
    private final RestClient restClient;
    private final String openAiApiKey;
    private final String openAiApiUrl;

    public AiChatService(
            ExpenseRepository expenseRepository,
            ChatMessageRepository chatMessageRepository,
            UserApplicationService userApplicationService,
            @Value("${openai.api.key}") String openAiApiKey,
            @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}") String openAiApiUrl) {
        this.expenseRepository = expenseRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userApplicationService = userApplicationService;
        this.restClient = RestClient.builder().build();
        this.openAiApiKey = openAiApiKey;
        this.openAiApiUrl = openAiApiUrl;
    }

    /**
     * AIチャットにメッセージを送信し、応答を取得する
     * 
     * ユーザーメッセージとAI応答の両方をデータベースに保存します。
     * 
     * @param userMessage ユーザーが送信したメッセージ
     * @return AIからの応答メッセージ
     */
    public String chat(String userMessage) {
        User user = userApplicationService.getUser();

        // 1. ユーザーメッセージをデータベースに保存
        ChatMessage userChatMessage = new ChatMessage("user", userMessage, user);
        chatMessageRepository.save(userChatMessage);

        // 2. 過去30日間の支出データを取得
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, start, end);

        // 3. システムプロンプトを構築（支出データを含む）
        String systemPrompt = "あなたは親切な家計簿アドバイザーです。以下の過去30日間の支出データを分析し、ユーザーの質問に答えてください。\n\n" +
                "支出データ:\n" +
                expenses.stream()
                        .map(e -> String.format("- %s: %s (%d円) %s", e.getDate().getValue(), e.getCategory().getValue(),
                                e.getAmount().getValue(), e.getDescription()))
                        .collect(Collectors.joining("\n"));

        // 4. OpenAI APIを呼び出し
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); // コスト効率の良いモデルを使用
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)));

        String assistantResponse = null;
        try {
            OpenAiChatResponse response = restClient.post()
                    .uri(Objects.requireNonNull(openAiApiUrl))
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAiChatResponse.class);

            if (response != null && response.choices() != null && !response.choices().isEmpty()) {
                OpenAiChatMessage message = response.choices().get(0).message();
                if (message != null && message.content() != null) {
                    assistantResponse = message.content();
                }
            }
        } catch (HttpClientErrorException.TooManyRequests e) {
            assistantResponse = "申し訳ありません。OpenAI APIの利用枠（クォータ）を超過しました。";
        } catch (Exception e) {
            e.printStackTrace();
            assistantResponse = "申し訳ありません。AIサービスとの通信でエラーが発生しました。";
        }

        // 5. AI応答が取得できなかった場合のデフォルトメッセージ
        if (assistantResponse == null) {
            assistantResponse = "申し訳ありません。AIからの応答を取得できませんでした。";
        }

        // 6. AI応答をデータベースに保存
        ChatMessage assistantChatMessage = new ChatMessage("assistant", assistantResponse, user);
        chatMessageRepository.save(assistantChatMessage);

        return assistantResponse;
    }

    /**
     * 現在のユーザーの会話履歴を取得する
     * 
     * 作成日時の降順（新しい順）で返します。
     * 
     * @return 会話履歴のリスト（新しい順）
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getChatHistory() {
        User user = userApplicationService.getUser();
        List<ChatMessage> messages = chatMessageRepository.findByUserOrderByCreatedAtDesc(user);
        // 降順で取得したので、時系列順（古い順）に並び替える
        Collections.reverse(messages);
        
        return messages;
    }

    // DTO Records for OpenAI API Response
    private record OpenAiChatResponse(List<OpenAiChatChoice> choices) {}
    private record OpenAiChatChoice(OpenAiChatMessage message) {}
    private record OpenAiChatMessage(String content) {}
}
