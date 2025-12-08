package com.example.backend.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExpenseDate値オブジェクトのテストクラス
 * 
 * 支出日付を表現する値オブジェクトのバリデーションとビジネスロジックをテストします。
 * 日付は未来日付であってはならず、今日以前の日付のみが有効です。
 */
class ExpenseDateTest {

    @Test
    @DisplayName("正常な日付を作成できる（今日）")
    void createExpenseDate_今日() {
        // テストデータの準備: 今日の日付
        LocalDate today = LocalDate.now();

        // テスト実行: ExpenseDateオブジェクトを作成
        ExpenseDate date = new ExpenseDate(today);

        // 検証: 正常に作成され、値が正しく設定されていることを確認
        assertNotNull(date);
        assertEquals(today, date.getValue());
        assertEquals(today, date.toLocalDate());
    }

    @Test
    @DisplayName("正常な日付を作成できる（過去の日付）")
    void createExpenseDate_過去の日付() {
        // テストデータの準備: 過去の日付
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // テスト実行
        ExpenseDate date = new ExpenseDate(pastDate);

        // 検証
        assertNotNull(date);
        assertEquals(pastDate, date.getValue());
    }

    @Test
    @DisplayName("日付がnullなら例外")
    void createExpenseDate_nullなら例外() {
        // テスト実行と検証: nullを渡すとIllegalArgumentExceptionが発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ExpenseDate(null));

        // 例外メッセージが正しいことを確認
        assertEquals("日付はnullであってはなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("未来の日付なら例外")
    void createExpenseDate_未来の日付なら例外() {
        // テストデータの準備: 明日の日付
        LocalDate futureDate = LocalDate.now().plusDays(1);

        // テスト実行と検証: 未来の日付を渡すと例外が発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ExpenseDate(futureDate));

        assertEquals("日付は今日以前でなければなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("年を取得できる")
    void getYear_正常に取得() {
        // テストデータの準備: 特定の日付を作成
        LocalDate testDate = LocalDate.of(2024, 6, 15);
        ExpenseDate date = new ExpenseDate(testDate);

        // テスト実行: 年を取得
        int year = date.getYear();

        // 検証: 正しい年が返されることを確認
        assertEquals(2024, year);
    }

    @Test
    @DisplayName("月を取得できる")
    void getMonthValue_正常に取得() {
        // テストデータの準備
        LocalDate testDate = LocalDate.of(2024, 6, 15);
        ExpenseDate date = new ExpenseDate(testDate);

        // テスト実行: 月を取得
        int month = date.getMonthValue();

        // 検証: 正しい月が返されることを確認（1-12の範囲）
        assertEquals(6, month);
    }

    @Test
    @DisplayName("日を取得できる")
    void getDayOfMonth_正常に取得() {
        // テストデータの準備
        LocalDate testDate = LocalDate.of(2024, 6, 15);
        ExpenseDate date = new ExpenseDate(testDate);

        // テスト実行: 日を取得
        int day = date.getDayOfMonth();

        // 検証: 正しい日が返されることを確認
        assertEquals(15, day);
    }

    @Test
    @DisplayName("日付を比較できる（より後か）")
    void isAfter_正常に比較() {
        // テストデータの準備: 2つの日付オブジェクトを作成
        LocalDate date1 = LocalDate.now().minusDays(5);
        LocalDate date2 = LocalDate.now().minusDays(10);
        ExpenseDate expenseDate1 = new ExpenseDate(date1);
        ExpenseDate expenseDate2 = new ExpenseDate(date2);

        // テスト実行: より後かどうかを判定
        boolean result = expenseDate1.isAfter(expenseDate2);

        // 検証: date1はdate2より後なのでtrueが返されることを確認
        assertTrue(result);
        // 逆の場合はfalseが返されることを確認
        assertFalse(expenseDate2.isAfter(expenseDate1));
    }

    @Test
    @DisplayName("比較する日付がnullなら例外")
    void isAfter_nullなら例外() {
        // テストデータの準備
        LocalDate today = LocalDate.now();
        ExpenseDate date = new ExpenseDate(today);

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> date.isAfter(null));

        assertEquals("比較する日付はnullであってはなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("日付を比較できる（より前か）")
    void isBefore_正常に比較() {
        // テストデータの準備
        LocalDate date1 = LocalDate.now().minusDays(10);
        LocalDate date2 = LocalDate.now().minusDays(5);
        ExpenseDate expenseDate1 = new ExpenseDate(date1);
        ExpenseDate expenseDate2 = new ExpenseDate(date2);

        // テスト実行: より前かどうかを判定
        boolean result = expenseDate1.isBefore(expenseDate2);

        // 検証: date1はdate2より前なのでtrueが返されることを確認
        assertTrue(result);
        // 逆の場合はfalseが返されることを確認
        assertFalse(expenseDate2.isBefore(expenseDate1));
    }

    @Test
    @DisplayName("比較する日付がnullなら例外")
    void isBefore_nullなら例外() {
        // テストデータの準備
        LocalDate today = LocalDate.now();
        ExpenseDate date = new ExpenseDate(today);

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> date.isBefore(null));

        assertEquals("比較する日付はnullであってはなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("同じ日付の場合は比較結果がfalseになる")
    void compareExpenseDate_同じ日付() {
        // テストデータの準備: 同じ日付のオブジェクト
        LocalDate sameDate = LocalDate.now().minusDays(5);
        ExpenseDate date1 = new ExpenseDate(sameDate);
        ExpenseDate date2 = new ExpenseDate(sameDate);

        // テスト実行と検証: 同じ日付なので、より後/前の比較はfalseになる
        assertFalse(date1.isAfter(date2));
        assertFalse(date1.isBefore(date2));
    }

    @Test
    @DisplayName("同じ月かどうかを判定できる")
    void isSameMonth_同じ月() {
        // テストデータの準備: 同じ月の異なる日付
        LocalDate date1 = LocalDate.of(2024, 6, 15);
        LocalDate date2 = LocalDate.of(2024, 6, 20);
        ExpenseDate expenseDate1 = new ExpenseDate(date1);
        ExpenseDate expenseDate2 = new ExpenseDate(date2);

        // テスト実行: 同じ月かどうかを判定
        boolean result = expenseDate1.isSameMonth(expenseDate2);

        // 検証: 同じ月なのでtrueが返されることを確認
        assertTrue(result);
    }

    @Test
    @DisplayName("異なる月の場合はfalseが返される")
    void isSameMonth_異なる月() {
        // テストデータの準備: 異なる月の日付
        LocalDate date1 = LocalDate.of(2024, 6, 15);
        LocalDate date2 = LocalDate.of(2024, 7, 15);
        ExpenseDate expenseDate1 = new ExpenseDate(date1);
        ExpenseDate expenseDate2 = new ExpenseDate(date2);

        // テスト実行: 同じ月かどうかを判定
        boolean result = expenseDate1.isSameMonth(expenseDate2);

        // 検証: 異なる月なのでfalseが返されることを確認
        assertFalse(result);
    }

    @Test
    @DisplayName("異なる年の場合はfalseが返される")
    void isSameMonth_異なる年() {
        // テストデータの準備: 異なる年の同じ月の日付
        LocalDate date1 = LocalDate.of(2024, 6, 15);
        LocalDate date2 = LocalDate.of(2025, 6, 15);
        ExpenseDate expenseDate1 = new ExpenseDate(date1);
        ExpenseDate expenseDate2 = new ExpenseDate(date2);

        // テスト実行: 同じ月かどうかを判定
        boolean result = expenseDate1.isSameMonth(expenseDate2);

        // 検証: 異なる年なのでfalseが返されることを確認
        assertFalse(result);
    }

    @Test
    @DisplayName("比較する日付がnullの場合はfalseが返される")
    void isSameMonth_nullならfalse() {
        // テストデータの準備
        LocalDate today = LocalDate.now();
        ExpenseDate date = new ExpenseDate(today);

        // テスト実行: nullと比較
        boolean result = date.isSameMonth(null);

        // 検証: nullの場合はfalseが返されることを確認（例外は発生しない）
        assertFalse(result);
    }
}

