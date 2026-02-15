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

/**
 * 月別サマリーを表現する値オブジェクト
 * 
 * このクラスは月別の支出サマリーを表現し、以下の責務を持ちます:
 * - 月別サマリーの不変性の保証
 * - 月別サマリーのバリデーション（合計金額と件数が0以上であること）
 * - 支出リストから月別サマリーを計算するドメインロジック
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
    private final List<CategorySummary> byCategory;

    /**
     * コンストラクタ
     * 
     * @param total 合計金額（0以上でなければならない）
     * @param count 件数（0以上でなければならない）
     * @param byCategory カテゴリー別集計のリスト（nullの場合は空リストとして扱う）
     * @throws IllegalArgumentException 合計金額または件数が0未満の場合
     */
    public MonthlySummary(Integer total, Integer count, List<CategorySummary> byCategory) {
        validate(total, count);
        this.total = total;
        this.count = count;
        // 不変リストとして保存（外部からの変更を防ぐ）
        this.byCategory = byCategory != null 
            ? Collections.unmodifiableList(byCategory) 
            : Collections.emptyList();
    }

    /**
     * 支出リストから月別サマリーを作成するファクトリーメソッド
     * 
     * このメソッドは、支出リストから月別サマリーを計算してMonthlySummary値オブジェクトを作成します。
     * DDDの原則に従い、ドメインロジックを値オブジェクト内に集約します。
     * 
     * @param expenses 支出リスト（nullの場合は空リストとして扱う）
     * @return 月別サマリー値オブジェクト
     */
    public static MonthlySummary from(List<Expense> expenses) {
        // nullチェック: nullの場合は空リストとして扱う
        if (expenses == null || expenses.isEmpty()) {
            return new MonthlySummary(0, 0, Collections.emptyList());
        }

        // 1. 合計金額と件数を計算
        int total = expenses.stream()
            .mapToInt(expense -> expense.getAmount() != null ? expense.getAmount().getAmount() : 0)
            .sum();
        int count = expenses.size();
        
        // 2. カテゴリー別集計
        Map<String, Integer> categoryAmountMap = expenses.stream()
            .collect(Collectors.groupingBy(
                expense -> expense.getCategory() != null ? expense.getCategory().getDisplayName() : CategoryType.OTHER.getDisplayName(),
                Collectors.summingInt(expense -> expense.getAmount() != null ? expense.getAmount().getAmount() : 0)
            ));

        // 3. CategorySummary値オブジェクトのリストを作成（金額の降順でソート）
        List<CategorySummary> categorySummaries = categoryAmountMap.entrySet().stream()
            .map(entry -> {
                try {
                    CategoryType categoryType = CategoryType.fromDisplayName(entry.getKey());
                    return new CategorySummary(categoryType, entry.getValue());
                } catch (IllegalArgumentException e) {
                    return new CategorySummary(CategoryType.OTHER, entry.getValue());
                }
            })
            .sorted(Comparator.comparing(CategorySummary::getAmount).reversed())
            .collect(Collectors.toList());
        
        // 4. MonthlySummary値オブジェクトを作成
        return new MonthlySummary(total, count, categorySummaries);
    }

    /**
     * バリデーション: 月別サマリーが有効かチェック
     * 
     * @param total 検証する合計金額
     * @param count 検証する件数
     * @throws IllegalArgumentException 合計金額または件数がnull、または0未満の場合
     */
    private static void validate(Integer total, Integer count) {
        if (total == null) {
            throw new IllegalArgumentException("合計金額はnullであってはなりません。");
        }
        if (count == null) {
            throw new IllegalArgumentException("件数はnullであってはなりません。");
        }
        if (total < 0) {
            throw new IllegalArgumentException("合計金額は0以上でなければなりません。");
        }
        if (count < 0) {
            throw new IllegalArgumentException("件数は0以上でなければなりません。");
        }
    }
}

