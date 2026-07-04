package com.smarthouseholdaccountbook.backend.entity;

import com.smarthouseholdaccountbook.backend.valueobject.CategoryType;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseAmount;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseDate;

/**
 * 支出の更新内容を表すレコード。
 * 作成時に説明のバリデーションを行うため、存在するインスタンスは常に有効。
 *
 * @param description 説明
 * @param amount      金額
 * @param date        日付
 * @param category    カテゴリ
 */
public record ExpenseUpdate(
        String description,
        ExpenseAmount amount,
        ExpenseDate date,
        CategoryType category) {

    public ExpenseUpdate {
        requireNonNullArgument(description, "説明はnullであってはなりません。");
        if (description.trim().isEmpty()) {
            throw new IllegalArgumentException("説明は空文字列であってはなりません。");
        }
        requireNonNullArgument(amount, "金額はnullであってはなりません。");
        requireNonNullArgument(date, "日付はnullであってはなりません。");
        requireNonNullArgument(category, "カテゴリーはnullであってはなりません。");
    }

    private static void requireNonNullArgument(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
