package com.example.backend.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Category値オブジェクトのテストクラス
 * 
 * 支出カテゴリを表現する値オブジェクトのバリデーションをテストします。
 * カテゴリは定義された有効な値のみを受け付けます。
 */
class CategoryTest {

    @Test
    @DisplayName("有効なカテゴリを作成できる（食費）")
    void createCategory_食費() {
        // テスト実行: 有効なカテゴリ「食費」で作成
        Category category = new Category("食費");

        // 検証: 正常に作成され、値が正しく設定されていることを確認
        assertNotNull(category);
        assertEquals("食費", category.getValue());
    }

    @Test
    @DisplayName("有効なカテゴリを作成できる（光熱費）")
    void createCategory_光熱費() {
        // テスト実行
        Category category = new Category("光熱費");

        // 検証
        assertNotNull(category);
        assertEquals("光熱費", category.getValue());
    }

    @Test
    @DisplayName("有効なカテゴリを作成できる（住居費）")
    void createCategory_住居費() {
        // テスト実行
        Category category = new Category("住居費");

        // 検証
        assertNotNull(category);
        assertEquals("住居費", category.getValue());
    }

    @Test
    @DisplayName("有効なカテゴリを作成できる（交通費）")
    void createCategory_交通費() {
        // テスト実行
        Category category = new Category("交通費");

        // 検証
        assertNotNull(category);
        assertEquals("交通費", category.getValue());
    }

    @Test
    @DisplayName("有効なカテゴリを作成できる（娯楽費）")
    void createCategory_娯楽費() {
        // テスト実行
        Category category = new Category("娯楽費");

        // 検証
        assertNotNull(category);
        assertEquals("娯楽費", category.getValue());
    }

    @Test
    @DisplayName("有効なカテゴリを作成できる（日用品）")
    void createCategory_日用品() {
        // テスト実行
        Category category = new Category("日用品");

        // 検証
        assertNotNull(category);
        assertEquals("日用品", category.getValue());
    }

    @Test
    @DisplayName("有効なカテゴリを作成できる（その他）")
    void createCategory_その他() {
        // テスト実行
        Category category = new Category("その他");

        // 検証
        assertNotNull(category);
        assertEquals("その他", category.getValue());
    }

    @Test
    @DisplayName("カテゴリがnullなら例外")
    void createCategory_nullなら例外() {
        // テスト実行と検証: nullを渡すとIllegalArgumentExceptionが発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Category(null));

        // 例外メッセージが正しいことを確認
        assertEquals("カテゴリは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("カテゴリが空文字列なら例外")
    void createCategory_空文字列なら例外() {
        // テスト実行と検証: 空文字列を渡すと例外が発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Category(""));

        assertEquals("カテゴリは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("カテゴリが空白のみなら例外")
    void createCategory_空白のみなら例外() {
        // テスト実行と検証: 空白のみの文字列を渡すと例外が発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Category("   "));

        assertEquals("カテゴリは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("無効なカテゴリなら例外")
    void createCategory_無効なカテゴリなら例外() {
        // テスト実行と検証: 有効なカテゴリリストにない値を渡すと例外が発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Category("無効なカテゴリ"));

        // 例外メッセージに有効なカテゴリのリストが含まれていることを確認
        assertTrue(exception.getMessage().contains("無効なカテゴリです"));
        assertTrue(exception.getMessage().contains("食費"));
    }

    @Test
    @DisplayName("有効なカテゴリのリストを取得できる")
    void getValidCategories_正常に取得() {
        // テスト実行: 有効なカテゴリのリストを取得
        List<String> validCategories = Category.getValidCategories();

        // 検証: リストがnullでなく、期待されるカテゴリが含まれていることを確認
        assertNotNull(validCategories);
        assertFalse(validCategories.isEmpty());
        assertTrue(validCategories.contains("食費"));
        assertTrue(validCategories.contains("光熱費"));
        assertTrue(validCategories.contains("住居費"));
        assertTrue(validCategories.contains("交通費"));
        assertTrue(validCategories.contains("娯楽費"));
        assertTrue(validCategories.contains("日用品"));
        assertTrue(validCategories.contains("その他"));
        // リストのサイズが7であることを確認
        assertEquals(7, validCategories.size());
    }

    @Test
    @DisplayName("取得した有効なカテゴリのリストは不変")
    void getValidCategories_不変リスト() {
        // テスト実行: 有効なカテゴリのリストを取得
        List<String> validCategories = Category.getValidCategories();

        // 検証: 不変リストなので、変更しようとすると例外が発生することを確認
        assertThrows(UnsupportedOperationException.class,
                () -> validCategories.add("新しいカテゴリ"));
    }
}

