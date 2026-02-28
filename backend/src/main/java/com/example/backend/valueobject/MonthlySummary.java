package com.example.backend.valueobject;

import com.example.backend.entity.Expense;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 月別サマリーを表現する値オブジェクト
 */
public record MonthlySummary(
        // 対象月（YYYY-MM形式）
        String month,
        // 支出リスト
        List<Expense> expenses,
        // 合計金額（0以上）
        int total,
        // 件数（0以上）
        int count,
        // カテゴリー別集計のリスト（金額の降順でソート済み、不変リスト）
        List<CategorySummary> categorySummaries) {
    private static final String MONTH_FORMAT = "yyyy-MM";

    /**
     * recordのコンストラクタ。バリデーションと防御的コピーを行う。
     */
    public MonthlySummary {
        if (total < 0) {
            throw new IllegalArgumentException("合計金額は0以上でなければなりません。");
        }
        if (count < 0) {
            throw new IllegalArgumentException("件数は0以上でなければなりません。");
        }
        Objects.requireNonNull(expenses, "expenses はnullであってはなりません。");
        Objects.requireNonNull(month, "month はnullであってはなりません。");
        Objects.requireNonNull(categorySummaries, "categorySummaries はnullであってはなりません。");
        categorySummaries = List.copyOf(categorySummaries);
    }

    /**
     * 1日あたりの平均支出（total / 日数）。
     */
    public int getDailyAverage() {
        int days = YearMonth.parse(month, DateTimeFormatter.ofPattern(MONTH_FORMAT)).lengthOfMonth();
        return total / days;
    }

    /**
     * 支出リストと対象月から月別サマリーを作成する。
     * 月を指定すると getDaysInMonth / getDailyAverage が利用可能になる。
     *
     * @param monthlyExpenses 特定月の支出リスト
     * @param month           対象月（YYYY-MM形式）
     * @return 月別サマリー値オブジェクト
     */
    public static MonthlySummary createMonthlySummaryFromExpenses(List<Expense> monthlyExpenses, String month) {
        Objects.requireNonNull(monthlyExpenses);
        if (monthlyExpenses.isEmpty()) {
            return new MonthlySummary(month, Collections.emptyList(), 0, 0, Collections.emptyList());
        }
        // 合計金額
        int total = monthlyExpenses.stream()
                .mapToInt(expense -> expense.getAmount().getAmount())
                .sum();

        // 件数
        int count = monthlyExpenses.size();

        // カテゴリごとに「合計金額」と「件数」を一度に集計
        Map<CategoryType, int[]> byCategory = monthlyExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.teeing(
                                Collectors.summingInt(expense -> expense.getAmount().getAmount()),
                                Collectors.counting(),
                                (sum, cnt) -> new int[] { sum, cnt.intValue() })));

        List<CategorySummary> categorySummaries = byCategory.entrySet().stream()
                .map(e -> new CategorySummary(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .sorted(Comparator.comparing(CategorySummary::getAmount).reversed())
                .collect(Collectors.toUnmodifiableList());

        return new MonthlySummary(month, monthlyExpenses, total, count, categorySummaries);
    }
    
    /**
     * カテゴリ別Top支出品目リストを作成する。降順でソートして上位N件を返す。
     * @param topN 上位N件
     * @return カテゴリ別Top支出品目リスト
     */
    public Map<CategoryType, List<Expense>> getTopExpensesByCategory(int topN) {
        return expenses.stream().collect(Collectors.groupingBy(Expense::getCategory, Collectors.toList()))
                .entrySet().stream().map(entry -> {
                    List<Expense> sorted = entry.getValue().stream()
                            .sorted(Comparator.comparingInt((Expense e) -> e.getAmount().getAmount()).reversed())
                            .limit(topN)
                            .toList();
                    return Map.entry(entry.getKey(), sorted);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 全体の高額支出トップN件を作成する。降順でソートして上位N件を返す。
     * @param topN 上位N件
     * @return 全体の高額支出トップN件
     */
    public List<Expense> getTopExpenses(int topN) {
        return expenses.stream()
                .sorted(Comparator.comparingInt((Expense e) -> e.getAmount().getAmount()).reversed())
                .limit(topN)
                .toList();
    }
}
