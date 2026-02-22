package com.example.backend.valueobject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支出カテゴリの種類を表すEnum
 *
 * カテゴリの一覧を一元管理し、型安全性を確保します。
 * 各定数はDB・APIで使用する日本語の表示名を持ちます。
 */
public enum CategoryType {
    FOOD("食費"),
    TRANSPORT("交通費"),
    HOUSING("住居費"),
    UTILITIES("光熱費"),
    COMMUNICATION("通信費"),
    ENTERTAINMENT("娯楽費"),
    MEDICAL("医療費"),
    CLOTHING("衣服費"),
    DAILY_GOODS("日用品"),
    INVESTMENT("投資"),
    EDUCATION("教育費"),
    OTHER("その他");

    private final String displayName;

    CategoryType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * APIで利用する名前を取得
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 表示名からEnumを取得
     *
     * @param displayName カテゴリの表示名（例: "食費"）
     * @return 対応するCategoryType
     * @throws IllegalArgumentException 無効な表示名の場合
     */
    public static CategoryType fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("カテゴリは必須です。");
        }
        String trimmed = displayName.trim();
        return Arrays.stream(values())
                .filter(e -> e.displayName.equals(trimmed))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "無効なカテゴリです。有効なカテゴリ: " + getValidDisplayNames()));
    }

    /**
     * 表示名からEnumを取得
     * 無効な表示名の場合、デフォルト値を返す
     * @param displayName
     * @return
     */
    public static CategoryType fromDisplayNameOrDefault(String displayName, CategoryType defaultValue) {
        try {
            return fromDisplayName(displayName);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * 有効なカテゴリの表示名リストを取得
     */
    public static List<String> getValidDisplayNames() {
        return Arrays.stream(values())
                .map(CategoryType::getDisplayName)
                .collect(Collectors.toUnmodifiableList());
    }


}
