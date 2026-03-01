package com.example.backend.valueobject;

import com.example.backend.entity.Expense;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.EnumMap;
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
        Objects.requireNonNull(month);
    
        if (monthlyExpenses.isEmpty()) {
            return new MonthlySummary(month, List.of(), 0, 0, List.of());
        }
    
        int total = 0;
        Map<CategoryType, int[]> byCategory = new EnumMap<>(CategoryType.class);
    
        // 1回のループで合計とカテゴリ集計を同時に作る
        for (Expense expense : monthlyExpenses) {
            int amount = expense.getAmount().getAmount();
            total += amount;
    
            int[] totalAndCount = byCategory.computeIfAbsent(expense.getCategory(), k -> new int[2]);
            totalAndCount[0] += amount; // 合計金額
            totalAndCount[1] += 1;      // 件数
        }
    
        List<CategorySummary> categorySummaries = byCategory.entrySet().stream()
                .map(e -> new CategorySummary(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .sorted(Comparator.comparing(CategorySummary::getAmount).reversed())
                .toList();
        
        //呼び出し元がmonthlyExpenseを変更しても影響を受けないように、copyOfを使用して新しいリストを作成する。(防御的コピー)
        return new MonthlySummary(month, List.copyOf(monthlyExpenses), total, monthlyExpenses.size(), categorySummaries);
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
