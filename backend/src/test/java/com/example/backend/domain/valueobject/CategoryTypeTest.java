package com.example.backend.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CategoryType Enumのテストクラス
 *
 * 支出カテゴリのEnumのバリデーションをテストします。
 */
class CategoryTypeTest {

    @Test
    @DisplayName("表示名からEnumを取得できる（食費）")
    void fromDisplayName_食費() {
        CategoryType type = CategoryType.fromDisplayName("食費");
        assertNotNull(type);
        assertEquals("食費", type.getDisplayName());
        assertEquals(CategoryType.FOOD, type);
    }

    @Test
    @DisplayName("表示名からEnumを取得できる（光熱費）")
    void fromDisplayName_光熱費() {
        CategoryType type = CategoryType.fromDisplayName("光熱費");
        assertNotNull(type);
        assertEquals("光熱費", type.getDisplayName());
    }

    @Test
    @DisplayName("表示名からEnumを取得できる（住居費）")
    void fromDisplayName_住居費() {
        CategoryType type = CategoryType.fromDisplayName("住居費");
        assertNotNull(type);
        assertEquals("住居費", type.getDisplayName());
    }

    @Test
    @DisplayName("表示名からEnumを取得できる（交通費）")
    void fromDisplayName_交通費() {
        CategoryType type = CategoryType.fromDisplayName("交通費");
        assertNotNull(type);
        assertEquals("交通費", type.getDisplayName());
    }

    @Test
    @DisplayName("表示名からEnumを取得できる（娯楽費）")
    void fromDisplayName_娯楽費() {
        CategoryType type = CategoryType.fromDisplayName("娯楽費");
        assertNotNull(type);
        assertEquals("娯楽費", type.getDisplayName());
    }

    @Test
    @DisplayName("表示名からEnumを取得できる（日用品）")
    void fromDisplayName_日用品() {
        CategoryType type = CategoryType.fromDisplayName("日用品");
        assertNotNull(type);
        assertEquals("日用品", type.getDisplayName());
    }

    @Test
    @DisplayName("表示名からEnumを取得できる（その他）")
    void fromDisplayName_その他() {
        CategoryType type = CategoryType.fromDisplayName("その他");
        assertNotNull(type);
        assertEquals("その他", type.getDisplayName());
    }

    @Test
    @DisplayName("表示名がnullなら例外")
    void fromDisplayName_nullなら例外() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CategoryType.fromDisplayName(null));
        assertEquals("カテゴリは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("表示名が空文字列なら例外")
    void fromDisplayName_空文字列なら例外() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CategoryType.fromDisplayName(""));
        assertEquals("カテゴリは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("表示名が空白のみなら例外")
    void fromDisplayName_空白のみなら例外() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CategoryType.fromDisplayName("   "));
        assertEquals("カテゴリは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("無効な表示名なら例外")
    void fromDisplayName_無効な表示名なら例外() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CategoryType.fromDisplayName("無効なカテゴリ"));
        assertTrue(exception.getMessage().contains("無効なカテゴリです"));
        assertTrue(exception.getMessage().contains("食費"));
    }

    @Test
    @DisplayName("有効なカテゴリのリストを取得できる")
    void getValidDisplayNames_正常に取得() {
        List<String> validCategories = CategoryType.getValidDisplayNames();
        assertNotNull(validCategories);
        assertFalse(validCategories.isEmpty());
        assertTrue(validCategories.contains("食費"));
        assertTrue(validCategories.contains("光熱費"));
        assertTrue(validCategories.contains("住居費"));
        assertTrue(validCategories.contains("交通費"));
        assertTrue(validCategories.contains("娯楽費"));
        assertTrue(validCategories.contains("日用品"));
        assertTrue(validCategories.contains("その他"));
        assertTrue(validCategories.contains("通信費"));
        assertTrue(validCategories.contains("医療費"));
        assertTrue(validCategories.contains("衣服費"));
        assertTrue(validCategories.contains("投資"));
        assertTrue(validCategories.contains("教育費"));
        assertEquals(12, validCategories.size());
    }

    @Test
    @DisplayName("取得した有効なカテゴリのリストは不変")
    void getValidDisplayNames_不変リスト() {
        List<String> validCategories = CategoryType.getValidDisplayNames();
        assertThrows(UnsupportedOperationException.class,
                () -> validCategories.add("新しいカテゴリ"));
    }
}
