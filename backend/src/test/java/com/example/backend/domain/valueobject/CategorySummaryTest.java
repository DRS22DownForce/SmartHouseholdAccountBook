package com.example.backend.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CategorySummary値オブジェクトのテストクラス
 * 
 * 値オブジェクトのバリデーションと不変性をテストします。
 */
class CategorySummaryTest {

    @Test
    @DisplayName("正常なカテゴリー別集計を作成できる")
    void createCategorySummary_正常な値() {
        // テストデータの準備
        Category category = new Category("食費");
        Integer amount = 1000;

        // テスト実行
        CategorySummary categorySummary = new CategorySummary(category, amount);

        // 検証
        assertNotNull(categorySummary);
        assertEquals("食費", categorySummary.getCategoryValue());
        assertEquals(1000, categorySummary.getAmount());
    }

    @Test
    @DisplayName("金額が0でも作成できる")
    void createCategorySummary_金額が0() {
        // テストデータの準備
        Category category = new Category("食費");
        Integer amount = 0;

        // テスト実行
        CategorySummary categorySummary = new CategorySummary(category, amount);

        // 検証
        assertNotNull(categorySummary);
        assertEquals(0, categorySummary.getAmount());
    }

    @Test
    @DisplayName("カテゴリーがnullなら例外")
    void createCategorySummary_カテゴリーがnull() {
        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new CategorySummary(null, 1000));

        assertEquals("カテゴリーはnullであってはなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("金額がnullなら例外")
    void createCategorySummary_金額がnull() {
        // テストデータの準備
        Category category = new Category("食費");

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new CategorySummary(category, null));

        assertEquals("金額はnullであってはなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("金額が負の値なら例外")
    void createCategorySummary_金額が負の値() {
        // テストデータの準備
        Category category = new Category("食費");

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new CategorySummary(category, -1));

        assertEquals("金額は0以上でなければなりません。", exception.getMessage());
    }
}

