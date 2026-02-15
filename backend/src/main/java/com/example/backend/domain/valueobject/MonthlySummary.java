package com.example.backend.domain.valueobject;

import com.example.backend.entity.Expense;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 月別サマリーを表現する値オブジェクト
 * 
 * このクラスは月別の支出サマリーを表現し、以下の責務を持ちます:
 * - 月別サマリーの不変性の保証
 * - 月別サマリーのバリデーション（合計金額と件数が0以上であること）
 * 
 */
@Getter
@ToString
@EqualsAndHashCode
public class MonthlySummary {
    
    /**
     * 合計金額（0以上でなければならない）
     */
    private final Integer total;
    
    /**
     * 件数（0以上でなければならない）
     */
    private final Integer count;
    
    /**
     * カテゴリー別集計のリスト（金額の降順でソート済み、不変リスト）
     */
    private final List<CategorySummary> categorySummaries;

    /**
     * コンストラクタ
     * 
     * @param total 合計金額（0以上でなければならない）
     * @param count 件数（0以上でなければならない）
     * @param categorySummaries カテゴリー別集計のリスト（金額の降順でソート済み、不変リスト）
     * @throws IllegalArgumentException 合計金額または件数が0未満の場合
     */
    private MonthlySummary(Integer total, Integer count, List<CategorySummary> byCategory) {
        validate(total, count, byCategory);
        this.total = total;
        this.count = count;
        this.categorySummaries = byCategory;
    }

    /**
     * 支出リストから月別サマリーを作成するファクトリーメソッド
     * 
     * 
     * @param montlyExpenses 特定月の支出リスト
     * @return 月別サマリー値オブジェクト
     * @throws NullPointerException 特定月の支出リストがnullの場合
     */
    public static MonthlySummary createMonthlySummaryFromExpenses(List<Expense> montlyExpenses) {
        Objects.requireNonNull(montlyExpenses);
        if (montlyExpenses.isEmpty()) {
            return new MonthlySummary(0, 0, Collections.emptyList());
        }

        // 1. 合計金額と件数を計算
        int total = montlyExpenses.stream()
            .mapToInt(expense -> expense.getAmount().getAmount())
            .sum();
        int count = montlyExpenses.size();
        
        // 2. カテゴリー別集計
        Map<CategoryType, Integer> categoryAmountMap = montlyExpenses.stream()
            .collect(Collectors.groupingBy(
                expense -> expense.getCategory(),
                Collectors.summingInt(expense -> expense.getAmount().getAmount())
            ));

        // 3. CategorySummary値オブジェクトのリストを作成（金額の降順でソート済み、不変リスト）
        List<CategorySummary> categorySummaries = categoryAmountMap.entrySet().stream()
            .map(entry -> new CategorySummary(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(CategorySummary::getAmount).reversed())
            .collect(Collectors.toUnmodifiableList());
        
        // 4. MonthlySummary値オブジェクトを作成
        return new MonthlySummary(total, count, categorySummaries);
    }

    private static void validate(Integer total, Integer count, List<CategorySummary> byCategory) {
        if (total < 0) {
            throw new IllegalArgumentException("合計金額は0以上でなければなりません。");
        }
        if (count < 0) {
            throw new IllegalArgumentException("件数は0以上でなければなりません。");
        }
    }
}

