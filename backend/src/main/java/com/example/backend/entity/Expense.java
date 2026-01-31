package com.example.backend.entity;

import com.example.backend.domain.valueobject.CategoryType;
import com.example.backend.domain.valueobject.ExpenseAmount;
import com.example.backend.domain.valueobject.ExpenseDate;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 支出エンティティ
 *
 * ドメインモデル（エンティティ）の特徴:
 * - 識別子（ID）を持つ
 * - 状態が変わる（可変）
 * - 値オブジェクトを使用してドメインロジックを表現
 *
 * このクラスは支出を表現し、以下の責務を持ちます:
 * - 支出の識別（ID）
 * - 支出の状態管理（説明、金額、日付、カテゴリ、ユーザー）
 * - 支出の変更操作
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    /**
     * 支出金額（値オブジェクト）
     *
     * @Embeddedにより、値オブジェクトのフィールドがDBに直接マッピングされます。
     */
    @Embedded
    private ExpenseAmount amount;

    /**
     * 支出日付（値オブジェクト）
     */
    @Embedded
    private ExpenseDate date;

    /**
     * 支出カテゴリ（Enumで型安全に管理、DBにはEnum名を保存）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private CategoryType category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Expense(String description, ExpenseAmount amount, ExpenseDate date, CategoryType category, User user) {
        requireNonEmptyDescription(description);
        this.description = description.trim();
        this.amount = Objects.requireNonNull(amount, "金額は必須です。");
        this.date = Objects.requireNonNull(date, "日付は必須です。");
        this.category = Objects.requireNonNull(category, "カテゴリーは必須です。");
        this.user = Objects.requireNonNull(user, "ユーザーは必須です。");
    }

    private static void requireNonEmptyDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("説明は必須です。");
        }
    }

    public void changeDescription(String description) {
        requireNonEmptyDescription(description);
        this.description = description.trim();
    }

    public void changeAmount(ExpenseAmount amount) {
        this.amount = Objects.requireNonNull(amount, "金額は必須です。");
    }

    public void changeDate(ExpenseDate date) {
        this.date = Objects.requireNonNull(date, "日付は必須です。");
    }

    public void changeCategory(CategoryType category) {
        this.category = Objects.requireNonNull(category, "カテゴリーは必須です。");
    }

    /**
     * 部分更新: nullのパラメータは更新対象外。
     * 各フィールドのバリデーションはchange*メソッドに委譲。
     *
     * @param description 新しい説明（nullの場合は更新しない）
     * @param amount 新しい金額（nullの場合は更新しない）
     * @param date 新しい日付（nullの場合は更新しない）
     * @param category 新しいカテゴリ（nullの場合は更新しない）
     */
    public void update(String description, ExpenseAmount amount, ExpenseDate date, CategoryType category) {
        if (description != null && !description.trim().isEmpty()) {
            changeDescription(description);
        }
        if (amount != null) {
            changeAmount(amount);
        }
        if (date != null) {
            changeDate(date);
        }
        if (category != null) {
            changeCategory(category);
        }
    }
}
