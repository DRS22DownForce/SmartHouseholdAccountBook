package com.example.backend.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

/**
 * 支出カテゴリを表現する値オブジェクト
 * 
 * このクラスは支出のカテゴリを表現し、以下の責務を持ちます:
 * - カテゴリのバリデーション（有効なカテゴリかチェック）
 * - カテゴリの不変性の保証
 */
@Embeddable
@Getter
@ToString
@EqualsAndHashCode
public class Category {
    //TODO カテゴリーはEnumで管理する
    private static final List<String> VALID_CATEGORIES = Arrays.asList(
        "食費", "光熱費", "住居費", "交通費", "娯楽費", "日用品", "その他"
    );

    @Column(name = "category", nullable = false, length = 50)
    private final String value;

    /**
     * JPA用のデフォルトコンストラクタ
     */
    protected Category() {
        this.value = null;
    }

    /**
     * コンストラクタ
     * 
     * @param value カテゴリ名（nullまたは空文字列であってはならない）
     * @throws IllegalArgumentException カテゴリがnull、空文字列、または無効な値の場合
     */
    public Category(String value) {
        validate(value);
        this.value = value;
    }

    /**
     * バリデーション: カテゴリが有効かチェック
     * 
     * @param value 検証するカテゴリ
     * @throws IllegalArgumentException カテゴリがnull、空文字列、または無効な値の場合
     */
    private static void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("カテゴリは必須です。");
        }
        if (!VALID_CATEGORIES.contains(value)) {
            throw new IllegalArgumentException(
                "無効なカテゴリです。有効なカテゴリ: " + String.join(", ", VALID_CATEGORIES)
            );
        }
    }

    /**
     * 有効なカテゴリのリストを取得
     * 
     * @return 有効なカテゴリのリスト
     */
    public static List<String> getValidCategories() {
        return List.copyOf(VALID_CATEGORIES);
    }
}

