package com.example.backend.application.service;

import com.example.backend.domain.repository.ExpenseRepository;
import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class AiChatService {

    private final ExpenseRepository expenseRepository;
    private final UserApplicationService userApplicationService;
    private final RestClient restClient;
    private final String openAiApiKey;
    private final String openAiApiUrl;

    public AiChatService(
            ExpenseRepository expenseRepository,
            UserApplicationService userApplicationService,
            @Value("${openai.api.key}") String openAiApiKey,
            @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}") String openAiApiUrl) {
        this.expenseRepository = expenseRepository;
        this.userApplicationService = userApplicationService;
        this.restClient = RestClient.builder().build();
        this.openAiApiKey = openAiApiKey;
        this.openAiApiUrl = openAiApiUrl;
    }

    public String chat(String userMessage) {
        User user = userApplicationService.getUser();

        // Fetch expenses for the last 30 days
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, start, end);

        // Construct system prompt with expenses
        String systemPrompt = "あなたは親切な家計簿アドバイザーです。以下の過去30日間の支出データを分析し、ユーザーの質問に答えてください。\n\n" +
                "支出データ:\n" +
                expenses.stream()
                        .map(e -> String.format("- %s: %s (%d円) %s", e.getDate().getValue(), e.getCategory().getValue(),
                                e.getAmount().getValue(), e.getDescription()))
                        .collect(Collectors.joining("\n"));

        // Call OpenAI API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); // Use a cost-effective model
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)));

        try {
            OpenAiChatResponse response = restClient.post()
                    .uri(Objects.requireNonNull(openAiApiUrl))
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAiChatResponse.class);

            if (response != null && response.choices() != null && !response.choices().isEmpty()) {
                return response.choices().get(0).message().content();
            }
        } catch (HttpClientErrorException.TooManyRequests e) {
            return "申し訳ありません。OpenAI APIの利用枠（クォータ）を超過しました。課金設定を確認してください。";
        } catch (Exception e) {
            e.printStackTrace();
            return "申し訳ありません。AIサービスとの通信でエラーが発生しました。";
        }

        return "申し訳ありません。AIからの応答を取得できませんでした。";
    }

    // DTO Records for OpenAI API Response
    private record OpenAiChatResponse(List<OpenAiChatChoice> choices) {
    }

    private record OpenAiChatChoice(OpenAiChatMessage message) {
    }

    private record OpenAiChatMessage(String content) {
    }
}
