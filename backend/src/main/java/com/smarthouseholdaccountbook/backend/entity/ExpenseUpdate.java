package com.example.backend.entity;

import java.util.Objects;

import com.example.backend.valueobject.CategoryType;
import com.example.backend.valueobject.ExpenseAmount;
import com.example.backend.valueobject.ExpenseDate;

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
        Objects.requireNonNull(description, "説明はnullであってはなりません。");
        if (description.trim().isEmpty()) {
            throw new IllegalArgumentException("説明は空文字列であってはなりません。");
        }
        Objects.requireNonNull(amount, "金額はnullであってはなりません。");
        Objects.requireNonNull(date, "日付はnullであってはなりません。");
        Objects.requireNonNull(category, "カテゴリーはnullであってはなりません。");
    }
}
