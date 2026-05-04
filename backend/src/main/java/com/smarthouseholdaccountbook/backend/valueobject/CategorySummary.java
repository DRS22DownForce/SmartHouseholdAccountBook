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
     * 件数（カテゴリー別の支出件数、0以上でなければならない）
     */
    private final Integer count;

    /**
     * コンストラクタ（件数は1として扱う。後方互換用）
     *
     * @param category カテゴリー（CategoryType Enum、nullであってはならない）
     * @param amount  金額（1以上でなければならない）
     */
    public CategorySummary(CategoryType category, Integer amount) {
        validateTwoArg(category, amount);
        this.category = category;
        this.amount = amount;
        this.count = 1;
    }

    /**
     * コンストラクタ
     *
     * @param category カテゴリー（null不可）
     * @param amount  金額（0以上）
     * @param count   件数（0以上）
     */
    public CategorySummary(CategoryType category, Integer amount, Integer count) {
        validate(category, amount, count);
        this.category = category;
        this.amount = amount;
        this.count = count;
    }

    private static void validateTwoArg(CategoryType category, Integer amount) {
        Objects.requireNonNull(category, "カテゴリーはnullであってはなりません。");
        Objects.requireNonNull(amount, "金額はnullであってはなりません。");
        if (amount < 1) {
            throw new IllegalArgumentException("金額は1以上でなければなりません。");
        }
    }

    private static void validate(CategoryType category, Integer amount, Integer count) {
        Objects.requireNonNull(category, "カテゴリーはnullであってはなりません。");
        Objects.requireNonNull(amount, "金額はnullであってはなりません。");
        Objects.requireNonNull(count, "件数はnullであってはなりません。");
        if (amount < 0) {
            throw new IllegalArgumentException("金額は0以上でなければなりません。");
        }
        if (count < 0) {
            throw new IllegalArgumentException("件数は0以上でなければなりません。");
        }
        if (amount > 0 && count < 1) {
            throw new IllegalArgumentException("金額が1以上のとき件数は1以上でなければなりません。");
        }
    }

    /**
     * カテゴリー名を取得（DTO変換などで使用）
     */
    public String getDisplayName() {
        return category.getDisplayName();
    }
}
