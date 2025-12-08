package com.example.backend.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExpenseAmount値オブジェクトのテストクラス
 * 
 * 支出金額を表現する値オブジェクトのバリデーションとビジネスロジックをテストします。
 * 値オブジェクトは不変（immutable）であり、バリデーションによって不正な値の作成を防ぎます。
 */
class ExpenseAmountTest {

    @Test
    @DisplayName("正常な金額を作成できる")
    void createExpenseAmount_正常な値() {
        // テストデータの準備: 1以上の正の整数
        Integer value = 1000;

        // テスト実行: ExpenseAmountオブジェクトを作成
        ExpenseAmount amount = new ExpenseAmount(value);

        // 検証: 正常に作成され、値が正しく設定されていることを確認
        assertNotNull(amount);
        assertEquals(1000, amount.getValue());
        assertEquals(1000, amount.toInteger());
    }

    @Test
    @DisplayName("金額が1でも作成できる（最小値）")
    void createExpenseAmount_最小値1() {
        // テストデータの準備: 最小値である1
        Integer value = 1;

        // テスト実行
        ExpenseAmount amount = new ExpenseAmount(value);

        // 検証: 最小値でも正常に作成できることを確認
        assertNotNull(amount);
        assertEquals(1, amount.getValue());
    }

    @Test
    @DisplayName("金額がnullなら例外")
    void createExpenseAmount_nullなら例外() {
        // テスト実行と検証: nullを渡すとIllegalArgumentExceptionが発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ExpenseAmount(null));

        // 例外メッセージが正しいことを確認
        assertEquals("金額はnullであってはなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("金額が0なら例外")
    void createExpenseAmount_0なら例外() {
        // テスト実行と検証: 0を渡すとIllegalArgumentExceptionが発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ExpenseAmount(0));

        assertEquals("金額は1以上でなければなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("金額が負の値なら例外")
    void createExpenseAmount_負の値なら例外() {
        // テスト実行と検証: 負の値を渡すとIllegalArgumentExceptionが発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ExpenseAmount(-1));

        assertEquals("金額は1以上でなければなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("金額を加算できる")
    void addExpenseAmount_正常に加算() {
        // テストデータの準備: 2つの金額オブジェクトを作成
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseAmount amount2 = new ExpenseAmount(500);

        // テスト実行: 加算メソッドを呼び出す
        ExpenseAmount result = amount1.add(amount2);

        // 検証: 加算結果が正しいことを確認（値オブジェクトは不変なので、新しいインスタンスが返される）
        assertNotNull(result);
        assertEquals(1500, result.getValue());
        // 元のオブジェクトは変更されていないことを確認
        assertEquals(1000, amount1.getValue());
        assertEquals(500, amount2.getValue());
    }

    @Test
    @DisplayName("加算する金額がnullなら例外")
    void addExpenseAmount_nullなら例外() {
        // テストデータの準備
        ExpenseAmount amount = new ExpenseAmount(1000);

        // テスト実行と検証: nullを加算しようとすると例外が発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> amount.add(null));

        assertEquals("加算する金額はnullであってはなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("金額を減算できる")
    void subtractExpenseAmount_正常に減算() {
        // テストデータの準備
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseAmount amount2 = new ExpenseAmount(300);

        // テスト実行: 減算メソッドを呼び出す
        ExpenseAmount result = amount1.subtract(amount2);

        // 検証: 減算結果が正しいことを確認
        assertNotNull(result);
        assertEquals(700, result.getValue());
    }

    @Test
    @DisplayName("減算結果が0以下になる場合は例外")
    void subtractExpenseAmount_結果が0以下なら例外() {
        // テストデータの準備: 減算結果が0以下になるケース
        ExpenseAmount amount1 = new ExpenseAmount(500);
        ExpenseAmount amount2 = new ExpenseAmount(500);

        // テスト実行と検証: 減算結果が0になるため例外が発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> amount1.subtract(amount2));

        assertEquals("減算結果は1以上でなければなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("減算する金額がnullなら例外")
    void subtractExpenseAmount_nullなら例外() {
        // テストデータの準備
        ExpenseAmount amount = new ExpenseAmount(1000);

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> amount.subtract(null));

        assertEquals("減算する金額はnullであってはなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("金額を比較できる（より大きいか）")
    void isGreaterThan_正常に比較() {
        // テストデータの準備
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseAmount amount2 = new ExpenseAmount(500);

        // テスト実行: より大きいかどうかを判定
        boolean result = amount1.isGreaterThan(amount2);

        // 検証: 1000は500より大きいのでtrueが返されることを確認
        assertTrue(result);
        // 逆の場合はfalseが返されることを確認
        assertFalse(amount2.isGreaterThan(amount1));
    }

    @Test
    @DisplayName("比較する金額がnullなら例外")
    void isGreaterThan_nullなら例外() {
        // テストデータの準備
        ExpenseAmount amount = new ExpenseAmount(1000);

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> amount.isGreaterThan(null));

        assertEquals("比較する金額はnullであってはなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("金額を比較できる（より小さいか）")
    void isLessThan_正常に比較() {
        // テストデータの準備
        ExpenseAmount amount1 = new ExpenseAmount(500);
        ExpenseAmount amount2 = new ExpenseAmount(1000);

        // テスト実行: より小さいかどうかを判定
        boolean result = amount1.isLessThan(amount2);

        // 検証: 500は1000より小さいのでtrueが返されることを確認
        assertTrue(result);
        // 逆の場合はfalseが返されることを確認
        assertFalse(amount2.isLessThan(amount1));
    }

    @Test
    @DisplayName("比較する金額がnullなら例外")
    void isLessThan_nullなら例外() {
        // テストデータの準備
        ExpenseAmount amount = new ExpenseAmount(1000);

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> amount.isLessThan(null));

        assertEquals("比較する金額はnullであってはなりません。", exception.getMessage());
    }

    @Test
    @DisplayName("同じ値の場合は比較結果がfalseになる")
    void compareExpenseAmount_同じ値() {
        // テストデータの準備: 同じ値の金額オブジェクト
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseAmount amount2 = new ExpenseAmount(1000);

        // テスト実行と検証: 同じ値なので、より大きい/小さいの比較はfalseになる
        assertFalse(amount1.isGreaterThan(amount2));
        assertFalse(amount1.isLessThan(amount2));
    }
}

