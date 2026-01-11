package com.example.backend.application.service;

import com.example.backend.domain.valueobject.Category;
import com.example.backend.exception.QuotaExceededException;
import com.example.backend.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AIカテゴリー自動分類サービス
 * 
 * このサービスは支出の説明文から、AIを使用して適切なカテゴリーを自動分類します。
 * OpenAI APIを呼び出して、説明文の内容を分析し、最も適切なカテゴリーを推論します。
 */
@Service
public class AiCategoryService {

    private final RestClient restClient;
    private final String openAiApiKey;
    private final String openAiApiUrl;

    /**
     * コンストラクタ
     * 
     * @param openAiApiKey OpenAI APIキー（application.propertiesから注入）
     * @param openAiApiUrl OpenAI API URL（application.propertiesから注入、デフォルト値あり）
     */
    public AiCategoryService(
            @Value("${openai.api.key}") String openAiApiKey,
            @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}") String openAiApiUrl) {
        this.restClient = RestClient.builder().build();
        this.openAiApiKey = openAiApiKey;
        this.openAiApiUrl = openAiApiUrl;
    }

    /**
     * 支出の説明文からカテゴリーを推論する
     * 
     * OpenAI APIを呼び出して、説明文の内容を分析し、最も適切なカテゴリーを返します。
     * 有効なカテゴリーリストをプロンプトに含めることで、AIが正しいカテゴリーを返すようにします。
     * 
     * @param description 支出の説明文
     * @return 推論されたカテゴリー名（有効なカテゴリーリストに含まれる値）
     * @throws IllegalArgumentException 説明文が空の場合
     * @throws QuotaExceededException OpenAI APIの利用枠（クォータ）を超過した場合
     * @throws AiServiceException AIサービスとの通信でエラーが発生した場合
     */
    public String predictCategory(String description) {
        // 入力バリデーション: 説明文が空でないことを確認
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("説明文は必須です。");
        }

        // 有効なカテゴリーリストを取得
        List<String> validCategories = Category.getValidCategories();
        String categoriesList = String.join("、", validCategories);

        // システムプロンプトを構築
        // AIに対して、説明文から適切なカテゴリーを推論するよう指示します
        String systemPrompt = String.format(
            "あなたは家計簿アプリのカテゴリー分類AIです。\n" +
            "ユーザーが入力した支出の説明文から、最も適切なカテゴリーを1つだけ選んでください。\n\n" +
            "有効なカテゴリーは以下の通りです:\n%s\n\n" +
            "カテゴリー名のみを返してください。説明やその他のテキストは含めないでください。\n" +
            "例: 「コンビニでお弁当を購入」→「食費」",
            categoriesList
        );

        // OpenAI APIリクエストボディを作成
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); // コスト効率の良いモデルを使用
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", description)));

        String predictedCategory = null;
        try {
            // OpenAI APIを呼び出し
            OpenAiChatResponse response = restClient.post()
                    .uri(Objects.requireNonNull(openAiApiUrl))
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAiChatResponse.class);

            // レスポンスからカテゴリーを抽出
            if (response != null && response.choices() != null && !response.choices().isEmpty()) {
                OpenAiChatMessage message = response.choices().get(0).message();
                if (message != null && message.content() != null) {
                    // AIの応答から前後の空白を削除
                    predictedCategory = message.content().trim();
                }
            }
        } catch (HttpClientErrorException.TooManyRequests e) {
            // OpenAI APIの利用枠（クォータ）を超過した場合
            throw new QuotaExceededException(e);
        } catch (Exception e) {
            // AIサービスとの通信でエラーが発生した場合
            throw new AiServiceException("AIサービスとの通信でエラーが発生しました。", e);
        }

        // AI応答が取得できなかった場合
        if (predictedCategory == null || predictedCategory.isEmpty()) {
            throw new AiServiceException("AIからの応答を取得できませんでした。");
        }

        // レスポンス検証: AIが返したカテゴリーが有効なカテゴリーリストに含まれることを確認
        if (!validCategories.contains(predictedCategory)) {
            // 無効なカテゴリーが返された場合、デフォルトとして「その他」を返す
            return "その他";
        }

        return predictedCategory;
    }

    // OpenAI APIのレスポンスをパースするためのレコードクラス
    private record OpenAiChatResponse(List<OpenAiChatChoice> choices) {}
    private record OpenAiChatChoice(OpenAiChatMessage message) {}
    private record OpenAiChatMessage(String content) {}
}
