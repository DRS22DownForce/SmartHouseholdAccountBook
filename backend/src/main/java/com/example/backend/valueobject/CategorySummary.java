package com.example.backend.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * カテゴリー別集計を表現する値オブジェクト
 */
@Getter
@ToString
@EqualsAndHashCode
public class CategorySummary {

    /**
     * カテゴリー（CategoryType Enum）
     */
    private final CategoryType category;

    /**
     * 金額（カテゴリー別の合計金額、0以上でなければならない）
     */
    private final Integer amount;

    /**
     * コンストラクタ
     *
     * @param category カテゴリー（CategoryType Enum、nullであってはならない）
     * @param amount  金額（0以上でなければならない）
     * @throws NullPointerException カテゴリーまたは金額がnullの場合
     * @throws IllegalArgumentException 金額が1未満の場合
     */
    public CategorySummary(CategoryType category, Integer amount) {
        validate(category, amount);
        this.category = category;
        this.amount = amount;
    }

    private static void validate(CategoryType category, Integer amount) {
        Objects.requireNonNull(category, "カテゴリーはnullであってはなりません。");
        Objects.requireNonNull(amount, "金額はnullであってはなりません。");
        if (amount < 1) {
            throw new IllegalArgumentException("金額は1以上でなければなりません。");
        }
    }

    /**
     * カテゴリー名を取得（DTO変換などで使用）
     */
    public String getDisplayName() {
        return category.getDisplayName();
    }
}
