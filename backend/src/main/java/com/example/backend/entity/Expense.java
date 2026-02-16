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

    //支出金額
    @Embedded
    private ExpenseAmount amount;

    //支出日付
    @Embedded
    private ExpenseDate date;

    //支出カテゴリ
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private CategoryType category;

    //ユーザー
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 支出を作成する
     * @param description 説明
     * @param amount 金額
     * @param date 日付
     * @param category カテゴリ
     * @param user ユーザ
     * @throws NullPointerException 説明、金額、日付、カテゴリ、ユーザがnullの場合
     * @throws IllegalArgumentException 説明が空文字列の場合
     */
    public Expense(String description, ExpenseAmount amount, ExpenseDate date, CategoryType category, User user) {
        Objects.requireNonNull(description, "説明はnullであってはなりません。");
        if (description.trim().isEmpty()) {
            throw new IllegalArgumentException("説明は空文字列であってはなりません。");
        }
        this.description = description;
        this.amount = Objects.requireNonNull(amount, "金額はnullであってはなりません。");
        this.date = Objects.requireNonNull(date, "日付はnullであってはなりません。");
        this.category = Objects.requireNonNull(category, "カテゴリーはnullであってはなりません。");
        this.user = Objects.requireNonNull(user, "ユーザーはnullであってはなりません。");
    }
    /**
     * 支出を更新する
     * @param update 更新用の値オブジェクト（ExpenseUpdateのコンストラクタで検証済みであること）
     */
    public void update(ExpenseUpdate update) {
        this.description = update.description();
        this.amount = update.amount();
        this.date = update.date();
        this.category = update.category();
    }

    /**
     * 支出の更新内容を表すレコード。
     * 作成時に説明のバリデーションを行うため、存在するインスタンスは常に有効。
     *
     * @param description 説明
     * @param amount 金額
     * @param date 日付
     * @param category カテゴリ
     * @throws IllegalArgumentException 説明が空文字列の場合
     * @throws NullPointerException 説明、金額、日付、カテゴリがnullの場合
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
}
