package com.example.backend.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MonthlySummary値オブジェクトのテストクラス
 * 
 * 値オブジェクトのバリデーションと不変性をテストします。
 */
class MonthlySummaryTest {

    @Test
    @DisplayName("正常な月別サマリーを作成できる")
    void createMonthlySummary_正常な値() {
        // テストデータの準備
        Integer total = 5000;
        Integer count = 3;
        CategoryType category1 = CategoryType.FOOD;
        CategoryType category2 = CategoryType.TRANSPORT;
        CategorySummary categorySummary1 = new CategorySummary(category1, 3000);
        CategorySummary categorySummary2 = new CategorySummary(category2, 2000);
        List<CategorySummary> byCategory = Arrays.asList(categorySummary1, categorySummary2);

        // テスト実行
        MonthlySummary monthlySummary = new MonthlySummary(total, count, byCategory);

        // 検証
        assertNotNull(monthlySummary);
        assertEquals(5000, monthlySummary.getTotal());
        assertEquals(3, monthlySummary.getCount());
        assertEquals(2, monthlySummary.getByCategory().size());
    }

    @Test
    @DisplayName("合計金額と件数が0でも作成できる")
    void createMonthlySummary_合計金額と件数が0() {
        // テストデータの準備
        Integer total = 0;
        Integer count = 0;
        List<CategorySummary> byCategory = new ArrayList<>();

        // テスト実行
        MonthlySummary monthlySummary = new MonthlySummary(total, count, byCategory);

        // 検証
        assertNotNull(monthlySummary);
        assertEquals(0, monthlySummary.getTotal());
        assertEquals(0, monthlySummary.getCount());
        assertTrue(monthlySummary.getByCategory().isEmpty());
    }

    @Test
    @DisplayName("byCategoryがnullの場合は空リストとして扱われる")
    void createMonthlySummary_byCategoryがnull() {
        // テストデータの準備
        Integer total = 1000;
        Integer count = 1;

        // テスト実行
        MonthlySummary monthlySummary = new MonthlySummary(total, count, null);

        // 検証
        assertNotNull(monthlySummary);
        assertTrue(monthlySummary.getByCategory().isEmpty());
    }

    @Test
    @DisplayName("合計金額がnullなら例外")
    void createMonthlySummary_合計金額がnull() {
        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MonthlySummary(null, 1, new ArrayList<>()));

        assertEquals("合計金額はnullであってはなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("件数がnullなら例外")
    void createMonthlySummary_件数がnull() {
        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MonthlySummary(1000, null, new ArrayList<>()));

        assertEquals("件数はnullであってはなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("合計金額が負の値なら例外")
    void createMonthlySummary_合計金額が負の値() {
        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MonthlySummary(-1, 1, new ArrayList<>()));

        assertEquals("合計金額は0以上でなければなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("件数が負の値なら例外")
    void createMonthlySummary_件数が負の値() {
        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MonthlySummary(1000, -1, new ArrayList<>()));

        assertEquals("件数は0以上でなければなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("byCategoryは不変リストとして返される")
    void createMonthlySummary_byCategoryは不変リスト() {
        // テストデータの準備
        Integer total = 1000;
        Integer count = 1;
        CategoryType category = CategoryType.FOOD;
        CategorySummary categorySummary = new CategorySummary(category, 1000);
        List<CategorySummary> byCategory = new ArrayList<>();
        byCategory.add(categorySummary);

        // テスト実行
        MonthlySummary monthlySummary = new MonthlySummary(total, count, byCategory);

        // 検証: 不変リストなので、変更しようとすると例外が発生する
        assertThrows(UnsupportedOperationException.class,
                () -> monthlySummary.getByCategory().add(new CategorySummary(CategoryType.FOOD, 2000)));
    }
}

