package com.example.backend.application.service.openai;

import com.example.backend.exception.AiServiceException;
import com.example.backend.exception.QuotaExceededException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OpenAI APIの呼び出しを共通化するクライアント。
 *
 * 通信処理、共通エラーハンドリング、JSONパースの共通ロジックを提供します。
 */
@Component
public class OpenAiClient {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiClient.class);
    private static final String MODEL_NAME = "gpt-4o-mini";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String openAiApiKey;
    private final String openAiApiUrl;

    public OpenAiClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${openai.api.key}") String openAiApiKey,
            @Value("${openai.api.url}") String openAiApiUrl) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.openAiApiKey = openAiApiKey;
        this.openAiApiUrl = openAiApiUrl;
    }

    /**
     * OpenAI APIへテキスト応答を要求する。
     *
     * @param systemPrompt システムプロンプト
     * @param userPrompt ユーザープロンプト
     * @return 応答コンテンツ（前後の空白を除去済み）
     */
    public String callText(String systemPrompt, String userPrompt) {
        return callForContent(systemPrompt, userPrompt, false);
    }


    /**
     * OpenAI APIへJSON応答を要求し、指定型にパースする。
     *
     * @param systemPrompt システムプロンプト
     * @param userPrompt ユーザープロンプト
     * @param responseType パース先の型情報
     * @param <T> 戻り値型
     * @return パース済みオブジェクト
     */
    public <T> T callJson(String systemPrompt, String userPrompt, TypeReference<T> responseType) {
        String content = callForContent(systemPrompt, userPrompt, true);
        try {
            return objectMapper.readValue(content, responseType);
        } catch (Exception e) {
            logger.error("OpenAIレスポンスのJSONパースに失敗しました: content={}", content, e);
            throw new AiServiceException("AIレスポンスのパースに失敗しました。", e);
        }
    }

    private String callForContent(String systemPrompt, String userPrompt, boolean jsonResponse) {
        Map<String, Object> requestBody = buildRequestBody(systemPrompt, userPrompt, jsonResponse);
        try {
            OpenAiChatResponse response = restClient.post()
                    .uri(Objects.requireNonNull(openAiApiUrl))
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAiChatResponse.class);

            return extractContent(response);
        } catch (HttpClientErrorException.TooManyRequests e) {
            logger.warn("OpenAI APIの利用枠を超過しました");
            throw new QuotaExceededException(e);
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("AIサービスとの通信でエラーが発生しました", e);
            throw new AiServiceException("AIサービスとの通信でエラーが発生しました。", e);
        }
    }

    private Map<String, Object> buildRequestBody(String systemPrompt, String userPrompt, boolean jsonResponse) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL_NAME);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)));
        if (jsonResponse) {
            requestBody.put("response_format", Map.of("type", "json_object")); // JSONオブジェクトを返すように指定
        }
        return requestBody;
    }

    private String extractContent(OpenAiChatResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new AiServiceException("AIからの応答を取得できませんでした。");
        }
        OpenAiChatMessage message = response.choices().get(0).message();
        if (message == null || message.content() == null || message.content().trim().isEmpty()) {
            throw new AiServiceException("AIからの応答を取得できませんでした。");
        }
        return message.content().trim();
    }

    private record OpenAiChatResponse(List<OpenAiChatChoice> choices) {
    }

    private record OpenAiChatChoice(OpenAiChatMessage message) {
    }

    private record OpenAiChatMessage(String content) {
    }
}
