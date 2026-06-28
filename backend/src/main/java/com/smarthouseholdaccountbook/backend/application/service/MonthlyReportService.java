package com.smarthouseholdaccountbook.backend.application.service;

import com.smarthouseholdaccountbook.backend.entity.MonthlyReport;
import com.smarthouseholdaccountbook.backend.entity.User;
import com.smarthouseholdaccountbook.backend.application.service.openai.OpenAiClient;
import com.smarthouseholdaccountbook.backend.exception.AiServiceException;
import com.smarthouseholdaccountbook.backend.valueobject.MonthlySummary;

import tools.jackson.core.type.TypeReference;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 月次AIレポート生成サービス
 *
 * 支出データの読み取り・OpenAI 呼び出し・保存を段階的に実行する。
 * DB アクセスは {@link MonthlyReportPersistenceService} に委譲し、OpenAI 呼び出し中は TX を張らない。
 */
@Service
public class MonthlyReportService {
    private static final int TOP_ITEMS_PER_CATEGORY = 3;
    private static final int TOP_OVERALL_ITEMS = 5;
    private static final String MONTHLY_REPORT_SYSTEM_PROMPT = "あなたは家計改善アドバイザーです。提供された支出データを詳細に分析し、具体的な品目・金額に基づいた実践的な改善提案を日本語で行ってください。";

    private final UserApplicationService userApplicationService;
    private final OpenAiClient openAiClient;
    private final MonthlyReportPersistenceService persistenceService;

    public MonthlyReportService(
            UserApplicationService userApplicationService,
            OpenAiClient openAiClient,
            MonthlyReportPersistenceService persistenceService) {
        this.userApplicationService = userApplicationService;
        this.openAiClient = openAiClient;
        this.persistenceService = persistenceService;
    }

    /**
     * 指定された月のレポートを返す。
     *
     * @param month    対象月（YYYY-MM形式）
     * @param generate trueの場合、すでに生成済みのレポートを無視して再生成する。falseの場合はすでに生成済みのレポートを返す（なければ空）
     * @return 月次レポート（再生成しないかつキャッシュがない場合は空）
     * @throws IllegalArgumentException 対象月の支出が0件で再生成する場合
     */
    public Optional<MonthlyReport> generateReport(String month, boolean generate) {
        User user = userApplicationService.getUser();

        if (!generate) {
            return persistenceService.findExisting(user, month);
        }

        MonthlyReportPersistenceService.ReportGenerationContext context =
                persistenceService.loadForGeneration(user, month);

        if (context.expenses().isEmpty()) {
            throw new IllegalArgumentException(
                    "この月の支出データがありません。レポートを生成するには支出を登録してください。");
        }

        MonthlySummary summary = MonthlySummary.createMonthlySummaryFromExpenses(context.expenses(), month);
        ParsedAiResponse parsed = callOpenAI(buildPrompt(summary));

        MonthlyReport saved = persistenceService.saveOrUpdate(
                user,
                month,
                context.existingReport(),
                parsed.summary(),
                parsed.suggestions());
        return Optional.of(saved);
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
                .map(entry -> String.format("[%s]\n%s", entry.getKey().getDisplayName(), entry.getValue().stream()
                        .map(e -> String.format("  - %s: %,d円（%s）",
                                e.getDescription(), e.getAmount().getAmount(),
                                e.getDate().getDate()))
                        .collect(Collectors.joining("\n"))))
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

    private ParsedAiResponse callOpenAI(String prompt) {
        ParsedAiResponse parsed = openAiClient.callJson(
                MONTHLY_REPORT_SYSTEM_PROMPT,
                prompt,
                new TypeReference<ParsedAiResponse>() {
                });
        validateParsedAiResponse(parsed);
        return parsed;
    }

    private void validateParsedAiResponse(ParsedAiResponse parsed) {
        if (parsed == null || parsed.summary() == null || parsed.suggestions() == null) {
            throw new AiServiceException("AIのレスポンス形式が不正です。");
        }
    }

    private record ParsedAiResponse(String summary, List<String> suggestions) {
    }
}
