package com.example.backend.application.service;

import com.example.backend.entity.Expense;
import com.example.backend.entity.MonthlyReport;
import com.example.backend.entity.User;
import com.example.backend.exception.AiServiceException;
import com.example.backend.exception.QuotaExceededException;
import com.example.backend.repository.ExpenseRepository;
import com.example.backend.repository.MonthlyReportRepository;

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
     * regenerate=false のときはキャッシュがあればそれを返し、なければ空。OpenAI は呼ばない。
     * regenerate=true のときはキャッシュを無視して再生成し、永続化して返す。
     * 支出が0件で再生成する場合はレポート生成を想定していないため、IllegalArgumentException をスローする。
     *
     * @param month      対象月（YYYY-MM形式）
     * @param regenerate trueの場合、キャッシュを無視して再生成する。falseの場合はキャッシュのみ返す（なければ空）
     * @return 月次レポート（キャッシュなし・再生成しない場合は空）
     * @throws IllegalArgumentException 対象月の支出が0件で再生成する場合
     */
    @Transactional
    public Optional<MonthlyReport> generateReport(String month, boolean regenerate) {
        User user = userApplicationService.getUser();
        Optional<MonthlyReport> cached = monthlyReportRepository.findByUserAndMonth(user, month);

        if (!regenerate) {
            return cached;
        }

        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern(MONTH_FORMAT));
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, startDate, endDate);

        if (expenses.isEmpty()) {
            throw new IllegalArgumentException(
                    "この月の支出データがありません。レポートを生成するには支出を登録してください。");
        }
        //TODO MonthlySummaryを使用するように修正する。
        String prompt = buildPrompt(month, expenses);
        String aiResponse = callOpenAI(prompt);
        ParsedAiResponse parsed = parseAiResponse(aiResponse);

        String suggestionsJson = serializeSuggestions(parsed.suggestions());

        if (cached.isPresent()) {
            MonthlyReport entity = cached.get();
            entity.update(parsed.summary(), suggestionsJson);
            return Optional.of(entity);
        }
        return Optional.of(monthlyReportRepository.save(
                new MonthlyReport(user, month, parsed.summary(), suggestionsJson)));
    }

    private String buildPrompt(String month, List<Expense> expenses) {
        int total = expenses.stream().mapToInt(e -> e.getAmount().getAmount()).sum();
        int count = expenses.size();
        String categoryBreakdown = buildCategoryBreakdown(month, expenses, total);
        String topItemsByCategory = buildTopItemsByCategory(expenses);
        String topOverallItems = buildTopOverallItems(expenses);
        int daysInMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern(MONTH_FORMAT))
                .lengthOfMonth();
        int dailyAverage = total / daysInMonth;

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
                month, total, count, dailyAverage,
                categoryBreakdown,
                TOP_ITEMS_PER_CATEGORY, topItemsByCategory,
                TOP_OVERALL_ITEMS, topOverallItems);
    }

    private String buildCategoryBreakdown(String month, List<Expense> expenses, int total) {
        Map<String, List<Expense>> byCategory = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getCategory().getDisplayName()));
        return byCategory.entrySet().stream()
                .map(entry -> {
                    int catTotal = entry.getValue().stream()
                            .mapToInt(e -> e.getAmount().getAmount()).sum();
                    int catCount = entry.getValue().size();
                    return Map.entry(entry.getKey(), new int[]{catTotal, catCount});
                })
                .sorted((a, b) -> b.getValue()[0] - a.getValue()[0])
                .map(entry -> {
                    int catTotal = entry.getValue()[0];
                    int catCount = entry.getValue()[1];
                    double percentage = total > 0 ? (catTotal * 100.0 / total) : 0;
                    return String.format("  - %s: %,d円（%d件、%.1f%%）",
                            entry.getKey(), catTotal, catCount, percentage);
                })
                .collect(Collectors.joining("\n"));
    }

    private String buildTopItemsByCategory(List<Expense> expenses) {
        Map<String, List<Expense>> byCategory = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getCategory().getDisplayName()));
        return byCategory.entrySet().stream()
                .map(entry -> {
                    List<Expense> sorted = entry.getValue().stream()
                            .sorted(Comparator.comparingInt((Expense e) -> e.getAmount().getAmount()).reversed())
                            .limit(TOP_ITEMS_PER_CATEGORY)
                            .toList();
                    String items = sorted.stream()
                            .map(e -> String.format("    * %s: %,d円（%s）",
                                    e.getDescription(), e.getAmount().getAmount(),
                                    e.getDate().getDate()))
                            .collect(Collectors.joining("\n"));
                    return String.format("  [%s]\n%s", entry.getKey(), items);
                })
                .collect(Collectors.joining("\n"));
    }

    private String buildTopOverallItems(List<Expense> expenses) {
        return expenses.stream()
                .sorted(Comparator.comparingInt((Expense e) -> e.getAmount().getAmount()).reversed())
                .limit(TOP_OVERALL_ITEMS)
                .map(e -> String.format("  - %s（%s）: %,d円",
                        e.getDescription(), e.getCategory().getDisplayName(),
                        e.getAmount().getAmount()))
                .collect(Collectors.joining("\n"));
    }
    //TODO APIの呼び出し部分は他のAIサービスと共通化する。型の違いはジェネリクスで対応する。
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
                    aiResponse, new TypeReference<Map<String, Object>>() {});

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

    private record ParsedAiResponse(String summary, List<String> suggestions) {}

    // OpenAI APIレスポンス用の型
    private record OpenAiChatResponse(List<OpenAiChatChoice> choices) {}
    private record OpenAiChatChoice(OpenAiChatMessage message) {}
    private record OpenAiChatMessage(String content) {}
}
