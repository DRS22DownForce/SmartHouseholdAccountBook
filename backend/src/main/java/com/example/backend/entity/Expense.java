package com.example.backend.entity;

import com.example.backend.domain.valueobject.Category;
import com.example.backend.domain.valueobject.ExpenseAmount;
import com.example.backend.domain.valueobject.ExpenseDate;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    private Long id; // 主キー（識別子）

    @Column(nullable = false)
    private String description; // 支出の説明

    /**
     * 支出金額（値オブジェクト）
     * 
     * @Embeddedアノテーションにより、値オブジェクトがエンティティに埋め込まれます。
     * データベースには値オブジェクトのフィールドが直接マッピングされます。
     */
    @Embedded
    private ExpenseAmount amount; // 金額（値オブジェクト）

    /**
     * 支出日付（値オブジェクト）
     */
    @Embedded
    private ExpenseDate date; // 日付（値オブジェクト）

    /**
     * 支出カテゴリ（値オブジェクト）
     */
    @Embedded
    private Category category; // カテゴリー（値オブジェクト）

    // UserEntityのidを外部キーとして参照
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * コンストラクタ
     * 
     * @param description 支出の説明
     * @param amount 金額（値オブジェクト）
     * @param date 日付（値オブジェクト）
     * @param category カテゴリ（値オブジェクト）
     * @param user ユーザーエンティティ
     */
    public Expense(String description, ExpenseAmount amount, ExpenseDate date, Category category, User user) {
        validate(description, amount, date, category, user);
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.user = user;
    }

    /**
     * バリデーションロジック
     * 
     * 値オブジェクトのバリデーションは値オブジェクト自体で行われますが、
     * エンティティレベルでの整合性チェックを行います。
     */
    private static void validate(String description, ExpenseAmount amount, ExpenseDate date, Category category, User user) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("説明は必須です。");
        }
        if (amount == null) {
            throw new IllegalArgumentException("金額は必須です。");
        }
        if (date == null) {
            throw new IllegalArgumentException("日付は必須です。");
        }
        if (category == null) {
            throw new IllegalArgumentException("カテゴリーは必須です。");
        }
        if (user == null) {
            throw new IllegalArgumentException("ユーザーは必須です。");
        }
    }

    /**
     * 説明を変更する
     * 
     * @param description 新しい説明
     */
    public void changeDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("説明は必須です。");
        }
        this.description = description;
    }

    /**
     * 金額を変更する
     * 
     * @param amount 新しい金額（値オブジェクト）
     */
    public void changeAmount(ExpenseAmount amount) {
        if (amount == null) {
            throw new IllegalArgumentException("金額は必須です。");
        }
        this.amount = amount;
    }

    /**
     * 日付を変更する
     * 
     * @param date 新しい日付（値オブジェクト）
     */
    public void changeDate(ExpenseDate date) {
        if (date == null) {
            throw new IllegalArgumentException("日付は必須です。");
        }
        this.date = date;
    }

    /**
     * カテゴリを変更する
     * 
     * @param category 新しいカテゴリ（値オブジェクト）
     */
    public void changeCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("カテゴリーは必須です。");
        }
        this.category = category;
    }

    /**
     * 支出の情報を更新する
     * 
     * @param description 新しい説明（nullの場合は更新しない）
     * @param amount 新しい金額（nullの場合は更新しない）
     * @param date 新しい日付（nullの場合は更新しない）
     * @param category 新しいカテゴリ（nullの場合は更新しない）
     */
    public void update(String description, ExpenseAmount amount, ExpenseDate date, Category category) {
        if (description != null && !description.trim().isEmpty()) {
            this.description = description;
        }
        if (amount != null) {
            this.amount = amount;
        }
        if (date != null) {
            this.date = date;
        }
        if (category != null) {
            this.category = category;
        }
    }

    /**
     * 金額をInteger値として取得（後方互換性のため）
     * 
     * @return 金額のInteger値
     */
    public Integer getAmountValue() {
        return this.amount != null ? this.amount.toInteger() : null;
    }

    /**
     * 日付をLocalDate値として取得（後方互換性のため）
     * 
     * @return 日付のLocalDate値
     */
    public LocalDate getDateValue() {
        return this.date != null ? this.date.toLocalDate() : null;
    }

    /**
     * カテゴリをString値として取得（後方互換性のため）
     * 
     * @return カテゴリのString値
     */
    public String getCategoryValue() {
        return this.category != null ? this.category.getValue() : null;
    }
}