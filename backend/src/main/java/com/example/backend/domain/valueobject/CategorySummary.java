package com.example.backend.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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
     * @throws IllegalArgumentException カテゴリーがnull、または金額が0未満の場合
     */
    public CategorySummary(CategoryType category, Integer amount) {
        validate(category, amount);
        this.category = category;
        this.amount = amount;
    }

    /**
     * バリデーション: カテゴリー別集計が有効かチェック
     */
    private static void validate(CategoryType category, Integer amount) {
        if (category == null) {
            throw new IllegalArgumentException("カテゴリーはnullであってはなりません。");
        }
        if (amount == null) {
            throw new IllegalArgumentException("金額はnullであってはなりません。");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("金額は0以上でなければなりません。");
        }
    }

    /**
     * カテゴリー名を取得（DTO変換などで使用）
     */
    public String getDisplayName() {
        return category.getDisplayName();
    }
}
