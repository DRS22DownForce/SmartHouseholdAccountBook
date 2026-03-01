package com.example.backend.application.service.openai;

import com.example.backend.exception.AiServiceException;
import com.example.backend.exception.QuotaExceededException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiClientTest {
    private static final String OPEN_AI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPEN_AI_API_KEY = "test-api-key";

    private OpenAiClient openAiClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        openAiClient = new OpenAiClient(builder, new ObjectMapper(), OPEN_AI_API_KEY, OPEN_AI_URL);
    }

    @Test
    @DisplayName("テキスト応答を取得してtrimして返す")
    void callTextReturnsTrimmedContent() {
        mockServer.expect(requestTo(OPEN_AI_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer " + OPEN_AI_API_KEY))
                .andRespond(withSuccess(
                        "{\"choices\":[{\"message\":{\"content\":\"  食費  \"}}]}",
                        MediaType.APPLICATION_JSON));

        String result = openAiClient.callText("system", "user");

        assertThat(result).isEqualTo("食費");
        mockServer.verify();
    }

    @Test
    @DisplayName("JSON応答をジェネリクスでMapへパースできる")
    void callJsonParsesWithTypeReference() {
        mockServer.expect(requestTo(OPEN_AI_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"choices\":[{\"message\":{\"content\":\"{\\\"1\\\":\\\"食費\\\",\\\"2\\\":\\\"交通費\\\"}\"}}]}",
                        MediaType.APPLICATION_JSON));

        Map<String, String> actual = openAiClient.callJson(
                "system",
                "user",
                new TypeReference<Map<String, String>>() {
                });

        assertThat(actual)
                .containsEntry("1", "食費")
                .containsEntry("2", "交通費");
        mockServer.verify();
    }

    @Test
    @DisplayName("OpenAIが429を返したときQuotaExceededExceptionへ変換する")
    void callTextThrowsQuotaExceededExceptionOnTooManyRequests() {
        mockServer.expect(requestTo(OPEN_AI_URL))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> openAiClient.callText("system", "user"))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    @DisplayName("JSON文字列が不正なときAiServiceExceptionを投げる")
    void callJsonThrowsAiServiceExceptionWhenJsonIsInvalid() {
        mockServer.expect(requestTo(OPEN_AI_URL))
                .andRespond(withSuccess(
                        "{\"choices\":[{\"message\":{\"content\":\"not-json\"}}]}",
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> openAiClient.callJson(
                "system",
                "user",
                new TypeReference<Map<String, String>>() {
                }))
                .isInstanceOf(AiServiceException.class)
                .hasMessage("AIレスポンスのパースに失敗しました。");
    }
}
