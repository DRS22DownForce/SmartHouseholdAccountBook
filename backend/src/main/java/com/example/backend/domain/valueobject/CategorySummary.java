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
     * カテゴリー（値オブジェクト）
     */
    private final Category category;
    
    /**
     * 金額（カテゴリー別の合計金額、0以上でなければならない）
     */
    private final Integer amount;

    /**
     * コンストラクタ
     * 
     * @param category カテゴリー（値オブジェクト、nullであってはならない）
     * @param amount 金額（0以上でなければならない）
     * @throws IllegalArgumentException カテゴリーがnull、または金額が0未満の場合
     */
    public CategorySummary(Category category, Integer amount) {
        validate(category, amount);
        this.category = category;
        this.amount = amount;
    }

    /**
     * バリデーション: カテゴリー別集計が有効かチェック
     * 
     * @param category 検証するカテゴリー
     * @param amount 検証する金額
     * @throws IllegalArgumentException カテゴリーがnull、または金額が0未満の場合
     */
    private static void validate(Category category, Integer amount) {
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
     * 
     * @return カテゴリー名
     */
    public String getCategoryValue() {
        return this.category != null ? this.category.getValue() : null;
    }
}

