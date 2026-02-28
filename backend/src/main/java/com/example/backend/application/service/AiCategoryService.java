package com.example.backend.application.service;

import com.example.backend.exception.QuotaExceededException;
import com.example.backend.valueobject.CategoryType;
import com.example.backend.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.concurrent.CompletionException;

/**
 * AIカテゴリー自動分類サービス
 * 
 * このサービスは支出の説明文から、AIを使用して適切なカテゴリーを自動分類します。
 * OpenAI APIを呼び出して、説明文の内容を分析し、最も適切なカテゴリーを推論します。
 */
@Service
public class AiCategoryService {
    private static final Logger logger = LoggerFactory.getLogger(AiCategoryService.class);

    private final RestClient restClient;
    private final String openAiApiKey;
    private final String openAiApiUrl;
    private final ObjectMapper objectMapper;
    private final Executor executor;
    private static final int BATCH_SIZE = 10; // 1リクエストあたりの最大件数（トークン制限を考慮）

    /**
     * コンストラクタ
     * 
     * @param openAiApiKey OpenAI APIキー（application.propertiesから注入）
     * @param openAiApiUrl OpenAI API URL（application.propertiesから注入、デフォルト値あり）
     */
    public AiCategoryService(
            @Value("${openai.api.key}") String openAiApiKey,
            @Value("${openai.api.url}") String openAiApiUrl,
            @Qualifier("aiCategoryTaskExecutor") Executor executor) {
        this.restClient = RestClient.builder().build();
        this.openAiApiKey = openAiApiKey;
        this.openAiApiUrl = openAiApiUrl;
        this.objectMapper = new ObjectMapper();
        this.executor = executor;
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
     * @throws QuotaExceededException   OpenAI APIの利用枠（クォータ）を超過した場合
     * @throws AiServiceException       AIサービスとの通信でエラーが発生した場合
     */
    @Cacheable(value = "aiCategory", key = "#description")
    public String predictCategory(String description) {
        // 有効なカテゴリーリストを取得
        List<String> validCategories = CategoryType.getValidDisplayNames();
        String categoriesList = String.join("、", validCategories);

        // システムプロンプトを構築
        // AIに対して、説明文から適切なカテゴリーを推論するよう指示します
        String systemPrompt = String.format(
                "あなたは家計簿アプリのカテゴリー分類AIです。\n" +
                        "ユーザーが入力した支出の説明文から、最も適切なカテゴリーを1つだけ選んでください。\n\n" +
                        "有効なカテゴリーは以下の通りです:\n%s\n\n" +
                        "カテゴリー名のみを返してください。説明やその他のテキストは含めないでください。\n" +
                        "例: 「コンビニでお弁当を購入」→「食費」",
                categoriesList);

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
            logger.warn("OpenAI APIの利用枠（クォータ）を超過しました: 説明文={}", description, e);
            throw new QuotaExceededException(e);
        } catch (Exception e) {
            logger.error("AIサービスとの通信でエラーが発生しました: 説明文={}", description, e);
            throw new AiServiceException("AIサービスとの通信でエラーが発生しました。", e);
        }

        // AI応答が取得できなかった場合
        if (predictedCategory == null || predictedCategory.isEmpty()) {
            logger.error("AIからの応答を取得できませんでした: 説明文={}", description);
            throw new AiServiceException("AIからの応答を取得できませんでした。");
        }

        // レスポンス検証: AIが返したカテゴリーが有効なカテゴリーリストに含まれることを確認
        if (!validCategories.contains(predictedCategory)) {
            logger.warn("AIが無効なカテゴリーを返しました: 説明文={}, 返されたカテゴリー={}, デフォルト「その他」を使用",
                    description, predictedCategory);
            return "その他";
        }

        return predictedCategory;
    }

    /**
     * 複数の支出説明文からカテゴリーを一括で推論する（バッチ処理）
     * 
     * 複数の説明文を1つのOpenAI APIリクエストにまとめて送信し、
     * JSON形式でレスポンスを受け取ります。これにより、APIリクエスト数を大幅に削減できます。
     * 
     * 大量のデータ（BATCH_SIZEを超える場合）は自動的にチャンクに分割して処理します。
     * 
     * @param descriptions 支出の説明文のリスト
     * @return 説明文とカテゴリーのマッピング（説明文 → カテゴリー）
     *         分類に失敗した説明文は「その他」が設定されます
     */
    public Map<String, CategoryType> predictCategoriesBatch(List<String> descriptions) {
        if (descriptions.isEmpty()) {
            logger.info("説明文のリストが空のため、空のマッピングを返します。");
            return new HashMap<>();
        }

        // 空の説明文をフィルタリング
        List<String> validDescriptions = descriptions.stream()
                .filter(desc -> desc != null && !desc.trim().isEmpty())
                .distinct() // 重複を除去
                .toList();

        if (validDescriptions.isEmpty()) {
            logger.info("有効な説明文がないため、空のマッピングを返します。");
            return new HashMap<>();
        }

        // チャンクに分割
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < validDescriptions.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, validDescriptions.size());
            List<String> chunk = validDescriptions.subList(i, end);
            chunks.add(chunk);
        }
        // チャンクが1つの場合は並列処理のオーバヘッドを避ける
        if (chunks.size() == 1) {
            return predictCategoriesBatchChunk(chunks.get(0));
        }

        // 各チャンクを並列処理（SpringのTaskExecutorを使用）
        List<CompletableFuture<Map<String, CategoryType>>> futures = chunks.stream()
                .map(chunk -> CompletableFuture.supplyAsync(
                        () -> {
                            return predictCategoriesBatchChunk(chunk);
                        },
                        executor))
                .collect(Collectors.toList());

        // すべてのチャンク処理の完了を待つ
        Map<String, CategoryType> resultMap = new HashMap<>();
        for (CompletableFuture<Map<String, CategoryType>> future : futures) {
            try {
                Map<String, CategoryType> chunkResult = future.join();
                resultMap.putAll(chunkResult);
            } catch (CompletionException e) {
                logger.error("チャンク処理中にエラーが発生しました", e);
                throw (RuntimeException) e.getCause();
            }
        }
        return resultMap;
    }

    /**
     * 1チャンク分の説明文からカテゴリーを推論する
     * 
     * @param descriptions 説明文のリスト（BATCH_SIZE以下）
     * @return 説明文とカテゴリーのマッピング
     * @throws QuotaExceededException OpenAI APIの利用枠（クォータ）を超過した場合
     * @throws AiServiceException     AIサービスとの通信でエラーが発生した場合
     */
    private Map<String, CategoryType> predictCategoriesBatchChunk(List<String> descriptions) {
        // 有効なカテゴリーリストを取得
        List<String> validCategories = CategoryType.getValidDisplayNames();
        String categoriesList = String.join("、", validCategories);

        // 説明文を番号付きリストとして構築
        StringBuilder descriptionsList = new StringBuilder();
        for (int i = 0; i < descriptions.size(); i++) {
            descriptionsList.append(String.format("%d. %s\n", i + 1, descriptions.get(i)));
        }

        // システムプロンプトを構築
        String systemPrompt = String.format(
                "あなたは家計簿アプリのカテゴリー分類AIです。\n" +
                        "以下の支出の説明文のリストから、それぞれの説明文に最も適切なカテゴリーを1つずつ選んでください。\n\n" +
                        "有効なカテゴリーは以下の通りです:\n%s\n\n" +
                        "結果をJSON形式で返してください。形式は以下の通りです:\n" +
                        "{\n" +
                        "  \"1\": \"カテゴリー名\",\n" +
                        "  \"2\": \"カテゴリー名\",\n" +
                        "  ...\n" +
                        "}\n\n" +
                        "各説明文の番号をキーとして、対応するカテゴリー名を値として返してください。\n" +
                        "カテゴリー名のみを返してください。説明やその他のテキストは含めないでください。\n" +
                        "例: {\"1\": \"食費\", \"2\": \"交通費\"}",
                categoriesList);

        // ユーザープロンプトを構築
        String userPrompt = "以下の支出の説明文を分類してください:\n\n" + descriptionsList.toString();

        // OpenAI APIリクエストボディを作成
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)));
        requestBody.put("response_format", Map.of("type", "json_object")); // JSON形式で返すよう指示

        try {
            // OpenAI APIを呼び出し
            OpenAiChatResponse response = restClient.post()
                    .uri(openAiApiUrl)
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAiChatResponse.class);

            // レスポンスからJSONを抽出
            OpenAiChatMessage message = response.choices().get(0).message();
            String jsonContent = message.content().trim();

            // JSONをパース
            Map<String, String> categoryMap = objectMapper.readValue(
                    jsonContent,
                    new TypeReference<Map<String, String>>() {
                    });

            // JSONの型チェック
            validateCategoryMapFormat(categoryMap, descriptions.size());

            // 説明文とカテゴリーのマッピングを作成
            Map<String, CategoryType> resultMap = new HashMap<>();
            for (int i = 0; i < descriptions.size(); i++) {
                String description = descriptions.get(i);
                String key = String.valueOf(i + 1);
                CategoryType category = CategoryType.fromDisplayNameOrDefault(categoryMap.get(key), CategoryType.OTHER);
                resultMap.put(description, category);
            }
            return resultMap;

        } catch (HttpClientErrorException.TooManyRequests e) {
            // レート制限エラーの場合、警告ログを出力
            // チャンクサイズを記録して、どのくらいのデータ量で問題が発生したかを把握できます
            logger.warn("OpenAI APIの利用枠（クォータ）を超過しました: チャンクサイズ={}", descriptions.size(), e);
            throw new QuotaExceededException(e);
        } catch (Exception e) {
            // JSONパースエラーなど、その他のエラーの場合、エラーログを出力
            // チャンクサイズとエラーメッセージを記録します
            logger.error("AIサービスとの通信でエラーが発生しました: チャンクサイズ={}, エラー={}",
                    descriptions.size(), e.getMessage(), e);
            throw new AiServiceException("AIサービスとの通信でエラーが発生しました: " + e.getMessage(), e);
        }
    }

    /**
     * AIの応答JSONが期待する形式か検証する
     * 期待形式: {"1": "カテゴリー名", "2": "カテゴリー名", ...}
     *
     * @param categoryMap   パース済みのマップ
     * @param expectedCount 期待するエントリ数（説明文の件数）
     * @throws AiServiceException 形式が不正な場合
     */
    private void validateCategoryMapFormat(Map<String, String> categoryMap, int expectedCount) {
        if (categoryMap == null) {
            throw new AiServiceException("AIの応答が空です。JSON形式が不正です。");
        }
        for (int i = 0; i < expectedCount; i++) {
            String key = String.valueOf(i + 1);
            if (!categoryMap.containsKey(key)) {
                throw new AiServiceException(
                        String.format("AIの応答にキー「%s」が含まれていません。期待される形式: {\"1\": \"カテゴリー名\", \"2\": \"カテゴリー名\", ...}",
                                key));
            }
            Object value = categoryMap.get(key);
            if (value == null) {
                throw new AiServiceException(
                        String.format("AIの応答のキー「%s」の値がnullです。", key));
            }
            if (!(value instanceof String)) {
                throw new AiServiceException(
                        String.format("AIの応答のキー「%s」の値が文字列ではありません。型: %s",
                                key, value.getClass().getSimpleName()));
            }
        }
    }

    // OpenAI APIのレスポンスをパースするためのレコードクラス
    private record OpenAiChatResponse(List<OpenAiChatChoice> choices) {
    }

    private record OpenAiChatChoice(OpenAiChatMessage message) {
    }

    private record OpenAiChatMessage(String content) {
    }
}
