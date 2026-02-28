package com.example.backend.application.service;

import com.example.backend.entity.Expense;
import com.example.backend.entity.MonthlyReport;
import com.example.backend.entity.User;
import com.example.backend.exception.AiServiceException;
import com.example.backend.exception.QuotaExceededException;
import com.example.backend.repository.ExpenseRepository;
import com.example.backend.repository.MonthlyReportRepository;
import com.example.backend.valueobject.MonthlySummary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 月次AIレポート生成サービス
 *
 * 指定された月の支出データを詳細に分析し、AIが生成した総評と改善提案を含むレポートを返します。
 * 生成済みレポートはDBに永続化し、再リクエスト時はキャッシュから返します。
 */
@Service
public class MonthlyReportService {
    private static final Logger logger = LoggerFactory.getLogger(MonthlyReportService.class);
    private static final String MONTH_FORMAT = "yyyy-MM";
    private static final int TOP_ITEMS_PER_CATEGORY = 3;
    private static final int TOP_OVERALL_ITEMS = 5;

    private final ExpenseRepository expenseRepository;
    private final MonthlyReportRepository monthlyReportRepository;
    private final UserApplicationService userApplicationService;
    private final RestClient restClient;
    private final String openAiApiKey;
    private final String openAiApiUrl;
    private final ObjectMapper objectMapper;

    public MonthlyReportService(
            ExpenseRepository expenseRepository,
            MonthlyReportRepository monthlyReportRepository,
            UserApplicationService userApplicationService,
            @Value("${openai.api.key}") String openAiApiKey,
            @Value("${openai.api.url}") String openAiApiUrl) {
        this.expenseRepository = expenseRepository;
        this.monthlyReportRepository = monthlyReportRepository;
        this.userApplicationService = userApplicationService;
        this.restClient = RestClient.builder().build();
        this.openAiApiKey = openAiApiKey;
        this.openAiApiUrl = openAiApiUrl;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 指定された月のレポートを返す。
     *
     * @param month    対象月（YYYY-MM形式）
     * @param generate trueの場合、すでに生成済みのレポートを無視して再生成する。falseの場合はすでに生成済みのレポートを返す（なければ空）
     * @return 月次レポート（キャッシュなし・再生成しない場合は空）
     * @throws IllegalArgumentException 対象月の支出が0件で再生成する場合
     */
    @Transactional
    public Optional<MonthlyReport> generateReport(String month, boolean generate) {
        User user = userApplicationService.getUser();
        Optional<MonthlyReport> existing = monthlyReportRepository.findByUserAndMonth(user, month);

        if (!generate) {
            return existing;
        }

        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern(MONTH_FORMAT));
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, startDate, endDate);

        if (expenses.isEmpty()) {
            throw new IllegalArgumentException(
                    "この月の支出データがありません。レポートを生成するには支出を登録してください。");
        }
        MonthlySummary summary = MonthlySummary.createMonthlySummaryFromExpenses(expenses, month);
        String prompt = buildPrompt(summary);
        String aiResponse = callOpenAI(prompt);
        ParsedAiResponse parsed = parseAiResponse(aiResponse);

        String suggestionsJson = serializeSuggestions(parsed.suggestions());

        if (existing.isPresent()) {
            MonthlyReport entity = existing.get();
            entity.update(parsed.summary(), suggestionsJson);
            return Optional.of(entity);
        }
        return Optional.of(monthlyReportRepository.save(
                new MonthlyReport(user, month, parsed.summary(), suggestionsJson)));
    }

    private String buildPrompt(MonthlySummary summary) {
        /**
         * カテゴリ別集計を文字列化
         * 
         * 出力例
         * - 食費: 150,000円（10件）
         * - 交通費: 100,000円（5件）
         * - 住居費: 80,000円（3件）
         */

        String categoryBreakdown = summary.categorySummaries().stream()
                .map(cs -> String.format("- %s: %,d円（%d件）",
                        cs.getCategory().getDisplayName(), cs.getAmount(), cs.getCount()))
                .collect(Collectors.joining("\n"));

        /**
         * カテゴリ別Top支出品目リストを文字列化
         * 
         * 出力例
         * [食費]
         *   - スーパーで買い物: 15,000円（2025-02-15）
         *   - コンビニ: 800円（2025-02-20）
         *   - 昼食: 600円（2025-02-10）
         * [交通費]
         *   - 電車定期: 10,000円（2025-02-01）
         *   - タクシー: 2,500円（2025-02-18）
         * [住居費]
         *   - 家賃: 80,000円（2025-02-01）
         * 
         */
        String topItemsByCategory = summary.getTopExpensesByCategory(TOP_ITEMS_PER_CATEGORY).entrySet().stream()
                .map(entry -> {
                    return String.format("[%s]\n%s", entry.getKey().getDisplayName(), entry.getValue().stream()
                            .map(e -> String.format("  - %s: %,d円（%s）",
                                    e.getDescription(), e.getAmount().getAmount(),
                                    e.getDate().getDate()))
                            .collect(Collectors.joining("\n")));
                })
                .collect(Collectors.joining("\n"));

        /**
         * 全体の高額支出トップN件を作成する。降順でソートして上位N件を返す。
         * 
         * 出力例
         * - スーパーで買い物: 15,000円（2025-02-15）
         * - コンビニ: 800円（2025-02-20）
         * - 昼食: 600円（2025-02-10）
         */
        String topOverallItems = summary.getTopExpenses(TOP_OVERALL_ITEMS).stream()
                .map(e -> String.format("- %s: %,d円（%s）",
                        e.getDescription(), e.getAmount().getAmount(),
                        e.getDate().getDate()))
                .collect(Collectors.joining("\n"));

        return String.format(
                """
                        以下は%sの家計支出データです。このデータをもとに、具体的で実践的な分析と改善提案を行ってください。

                        【基本情報】
                        - 合計支出: %,d円（%d件）
                        - 1日あたり平均: %,d円

                        【カテゴリ別内訳（金額降順）】
                        %s

                        【カテゴリ別トップ支出品目（カテゴリごと上位%d件）】
                        %s

                        【全体の高額支出トップ%d件】
                        %s

                        上記データを分析し、以下のJSON形式で返してください。
                        {
                          "summary": "月全体の支出傾向の総評。特に支出が多いカテゴリや注目すべき支出パターンに言及し、具体的な金額を交えながら3〜4文で記述してください。",
                          "suggestions": [
                            "改善提案1（具体的な品目名・金額・代替案を含む実践的な提案）",
                            "改善提案2",
                            "改善提案3",
                            "改善提案4",
                            "改善提案5"
                          ]
                        }

                        suggestionsは以下の基準で5件作成してください：
                        - 具体的な支出品目や金額に言及する
                        - 実践可能な代替案や節約方法を提示する
                        - 優先度の高い改善から順に並べる
                        """,
                summary.month(), summary.total(), summary.count(), summary.getDailyAverage(),
                categoryBreakdown,
                TOP_ITEMS_PER_CATEGORY, topItemsByCategory,
                TOP_OVERALL_ITEMS, topOverallItems);
    }

    // TODO APIの呼び出し部分は他のAIサービスと共通化する。型の違いはジェネリクスで対応する。
    private String callOpenAI(String prompt) {
        Map<String, Object> requestBody = buildOpenAiRequest(prompt);
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
                    return message.content().trim();
                }
            }
        } catch (HttpClientErrorException.TooManyRequests e) {
            logger.warn("OpenAI APIの利用枠を超過しました", e);
            throw new QuotaExceededException(e);
        } catch (Exception e) {
            logger.error("AIサービスとの通信でエラーが発生しました", e);
            throw new AiServiceException("AIサービスとの通信でエラーが発生しました。", e);
        }

        throw new AiServiceException("AIからの応答を取得できませんでした。");
    }

    private Map<String, Object> buildOpenAiRequest(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content",
                        "あなたは家計改善アドバイザーです。提供された支出データを詳細に分析し、具体的な品目・金額に基づいた実践的な改善提案を日本語で行ってください。"),
                Map.of("role", "user", "content", prompt)));
        requestBody.put("response_format", Map.of("type", "json_object"));
        return requestBody;
    }

    private ParsedAiResponse parseAiResponse(String aiResponse) {
        try {
            Map<String, Object> parsed = objectMapper.readValue(
                    aiResponse, new TypeReference<Map<String, Object>>() {
                    });

            String summary = (String) parsed.get("summary");
            @SuppressWarnings("unchecked")
            List<String> suggestions = (List<String>) parsed.get("suggestions");

            if (summary == null || suggestions == null) {
                throw new AiServiceException("AIのレスポンス形式が不正です。");
            }

            return new ParsedAiResponse(summary, suggestions);
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("AIレスポンスのパースに失敗しました: {}", aiResponse, e);
            throw new AiServiceException("AIレスポンスのパースに失敗しました。", e);
        }
    }

    private String serializeSuggestions(List<String> suggestions) {
        try {
            return objectMapper.writeValueAsString(suggestions);
        } catch (Exception e) {
            throw new AiServiceException("改善提案のシリアライズに失敗しました。", e);
        }
    }

    private record ParsedAiResponse(String summary, List<String> suggestions) {
    }

    // OpenAI APIレスポンス用の型
    private record OpenAiChatResponse(List<OpenAiChatChoice> choices) {
    }

    private record OpenAiChatChoice(OpenAiChatMessage message) {
    }

    private record OpenAiChatMessage(String content) {
    }
}
