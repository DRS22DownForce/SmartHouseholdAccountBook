package com.example.backend.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ExpenseAmount値オブジェクトのテストクラス
 *
 * 支出金額を表現する値オブジェクトのバリデーションとビジネスロジックをテストします。
 */
class ExpenseAmountTest {

    @Nested
    @DisplayName("コンストラクタ（正常系）")
    class ConstructorSuccess {
        @Test
        @DisplayName("正常な金額を作成できる")
        void createWithValidValue() {
            // given, when
            ExpenseAmount amount = new ExpenseAmount(1000);

            // then
            assertThat(amount).isNotNull();
            assertThat(amount.getAmount()).isEqualTo(1000);
        }

        @Test
        @DisplayName("金額が1でも作成できる（最小値）")
        void createWithMinimumValue() {
            // given, when
            ExpenseAmount amount = new ExpenseAmount(1);

            // then
            assertThat(amount).isNotNull();
            assertThat(amount.getAmount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("コンストラクタ（異常系）")
    class ConstructorFailure {
        @Test
        @DisplayName("金額がnullの場合は例外が発生する")
        void createWithNull() {
            // given, when, then
            assertThatThrownBy(() -> new ExpenseAmount(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("金額はnullであってはなりません。");
        }

        @Test
        @DisplayName("金額が0の場合は例外が発生する")
        void createWithZero() {
            // given, when, then
            assertThatThrownBy(() -> new ExpenseAmount(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("金額は1以上でなければなりません。");
        }

        @Test
        @DisplayName("金額が負の値の場合は例外が発生する")
        void createWithNegative() {
            // given, when, then
            assertThatThrownBy(() -> new ExpenseAmount(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("金額は1以上でなければなりません。");
        }
    }

    @Nested
    @DisplayName("加算（正常系）")
    class AddSuccess {
        @Test
        @DisplayName("金額を加算できる")
        void addSuccess() {
            // given
            ExpenseAmount amount1 = new ExpenseAmount(1000);
            ExpenseAmount amount2 = new ExpenseAmount(500);

            // when
            ExpenseAmount result = amount1.add(amount2);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualTo(1500);
            assertThat(amount1.getAmount()).isEqualTo(1000);
            assertThat(amount2.getAmount()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("加算（異常系）")
    class AddFailure {
        @Test
        @DisplayName("加算する金額がnullの場合は例外が発生する")
        void addWithNull() {
            // given
            ExpenseAmount amount = new ExpenseAmount(1000);

            // when, then
            assertThatThrownBy(() -> amount.add(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("加算する金額はnullであってはなりません。");
        }
    }

    @Nested
    @DisplayName("比較（より大きいか）")
    class IsGreaterThan {
        @Test
        @DisplayName("金額を比較できる")
        void isGreaterThanSuccess() {
            // given
            ExpenseAmount amount1 = new ExpenseAmount(1000);
            ExpenseAmount amount2 = new ExpenseAmount(500);

            // when, then
            assertThat(amount1.isGreaterThan(amount2)).isTrue();
            assertThat(amount2.isGreaterThan(amount1)).isFalse();
        }

        @Test
        @DisplayName("比較する金額がnullの場合は例外が発生する")
        void isGreaterThanWithNull() {
            // given
            ExpenseAmount amount = new ExpenseAmount(1000);

            // when, then
            assertThatThrownBy(() -> amount.isGreaterThan(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("比較する金額はnullであってはなりません。");
        }
    }

    @Nested
    @DisplayName("比較（より小さいか）")
    class IsLessThan {
        @Test
        @DisplayName("金額を比較できる")
        void isLessThanSuccess() {
            // given
            ExpenseAmount amount1 = new ExpenseAmount(500);
            ExpenseAmount amount2 = new ExpenseAmount(1000);

            // when, then
            assertThat(amount1.isLessThan(amount2)).isTrue();
            assertThat(amount2.isLessThan(amount1)).isFalse();
        }

        @Test
        @DisplayName("比較する金額がnullの場合は例外が発生する")
        void isLessThanWithNull() {
            // given
            ExpenseAmount amount = new ExpenseAmount(1000);

            // when, then
            assertThatThrownBy(() -> amount.isLessThan(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("比較する金額はnullであってはなりません。");
        }
    }

    @Nested
    @DisplayName("同じ値での比較")
    class CompareEqual {
        @Test
        @DisplayName("同じ値の場合はより大きい/小さいの比較はfalseになる。equalsはtrueになる。")
        void compareEqualValues() {
            // given
            ExpenseAmount amount1 = new ExpenseAmount(1000);
            ExpenseAmount amount2 = new ExpenseAmount(1000);

            // when, then
            assertThat(amount1.isGreaterThan(amount2)).isFalse();
            assertThat(amount1.isLessThan(amount2)).isFalse();
            assertThat(amount1.equals(amount2)).isTrue();
        }
    }
}
