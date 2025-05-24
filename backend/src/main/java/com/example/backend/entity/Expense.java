package com.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity // このアノテーションは、このクラスがJPAのエンティティであることを示します。
@Getter // getterのみ自動生成
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA用にデフォルトコンストラクタを生成
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
    // Java上ではUser型で参照するが、データベース上ではuser_idというカラム名でUserテーブルのid(主キー)を参照される
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Expense(String description, Integer amount, LocalDate date, String category) {
        validate(description, amount, date, category);
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
    }

    // バリデーションロジック
    private static void validate(String description, Integer amount, LocalDate date, String category) {
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
    }

    // 変更メソッド（必要なものだけ公開）
    public void changeDescription(String description) {
        validate(description, this.amount, this.date, this.category);
        this.description = description;
    }

    public void changeAmount(Integer amount) {
        validate(this.description, amount, this.date, this.category);
        this.amount = amount;
    }

    public void changeDate(LocalDate date) {
        validate(this.description, this.amount, date, this.category);
        this.date = date;
    }

    public void changeCategory(String category) {
        validate(this.description, this.amount, this.date, category);
        this.category = category;
    }

    public void setUser(User user) {
        this.user = user;
    }
}