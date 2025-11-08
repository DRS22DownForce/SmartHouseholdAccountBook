package com.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主キー

    @Column(nullable = false)
    private String description; // 支出の説明

    @Column(nullable = false)
    private Integer amount; // 金額

    @Column(nullable = false)
    private LocalDate date; // 日付

    @Column(nullable = false)
    private String category; // カテゴリー

    // UserEntityのidを外部キーとして参照
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Expense(String description, Integer amount, LocalDate date, String category, User user) {
        validate(description, amount, date, category, user);
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.user = user;
    }

    // バリデーションロジック
    private static void validate(String description, Integer amount, LocalDate date, String category, User user) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("説明は必須です。");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("金額は1以上でなければなりません。");
        }
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("日付は今日以前でなければなりません。");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("カテゴリーは必須です。");
        }
        if (user == null) {
            throw new IllegalArgumentException("ユーザーは必須です。");
        }
    }

    // 変更メソッド（必要なものだけ公開）
    public void changeDescription(String description) {
        validate(description, this.amount, this.date, this.category, this.user);
        this.description = description;
    }

    public void changeAmount(Integer amount) {
        validate(this.description, amount, this.date, this.category, this.user);
        this.amount = amount;
    }

    public void changeDate(LocalDate date) {
        validate(this.description, this.amount, date, this.category, this.user);
        this.date = date;
    }

    public void changeCategory(String category) {
        validate(this.description, this.amount, this.date, category, this.user);
        this.category = category;
    }
}