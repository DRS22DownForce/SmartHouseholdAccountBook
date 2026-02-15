package com.example.backend.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ExpenseDate値オブジェクトのテストクラス
 *
 * 支出日付を表現する値オブジェクトのバリデーションとビジネスロジックをテストします。
 */
class ExpenseDateTest {

    @Nested
    @DisplayName("作成")
    class Create {

        @Test
        @DisplayName("今日の日付で作成できる")
        void createWithToday() {
            // given
            LocalDate today = LocalDate.now();

            // when
            ExpenseDate date = new ExpenseDate(today);

            // then
            assertThat(date).isNotNull();
            assertThat(date.getDate()).isEqualTo(today);
        }

        @Test
        @DisplayName("過去の日付で作成できる")
        void createWithPastDate() {
            // given
            LocalDate pastDate = LocalDate.now().minusDays(1);

            // when
            ExpenseDate date = new ExpenseDate(pastDate);

            // then
            assertThat(date).isNotNull();
            assertThat(date.getDate()).isEqualTo(pastDate);
        }

        @Test
        @DisplayName("特定の日付で作成するとgetDateでその値が取得できる")
        void createWithSpecificDate() {
            // given
            LocalDate testDate = LocalDate.of(2024, 6, 15);

            // when
            ExpenseDate date = new ExpenseDate(testDate);

            // then
            assertThat(date.getDate()).isEqualTo(testDate);
            assertThat(date.getDate().getYear()).isEqualTo(2024);
            assertThat(date.getDate().getMonthValue()).isEqualTo(6);
            assertThat(date.getDate().getDayOfMonth()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("バリデーション")
    class Validation {

        @Test
        @DisplayName("日付がnullの場合は例外が発生する")
        void validateDateNotNull() {
            // given, when, then
            assertThatThrownBy(() -> new ExpenseDate(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("日付はnullであってはなりません。");
        }

        @Test
        @DisplayName("未来の日付の場合は例外が発生する")
        void validateDateNotFuture() {
            // given
            LocalDate futureDate = LocalDate.now().plusDays(1);

            // when, then
            assertThatThrownBy(() -> new ExpenseDate(futureDate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("日付は今日以前でなければなりません。");
        }
    }

    @Nested
    @DisplayName("比較（より後か）")
    class IsAfter {

        @Test
        @DisplayName("日付を比較できる")
        void isAfterSuccess() {
            // given
            LocalDate date1 = LocalDate.now().minusDays(5);
            LocalDate date2 = LocalDate.now().minusDays(10);
            ExpenseDate expenseDate1 = new ExpenseDate(date1);
            ExpenseDate expenseDate2 = new ExpenseDate(date2);

            // when, then
            assertThat(expenseDate1.isAfter(expenseDate2)).isTrue();
            assertThat(expenseDate2.isAfter(expenseDate1)).isFalse();
        }

        @Test
        @DisplayName("比較する日付がnullの場合は例外が発生する")
        void isAfterWithNull() {
            // given
            ExpenseDate date = new ExpenseDate(LocalDate.now());

            // when, then
            assertThatThrownBy(() -> date.isAfter(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("比較する日付はnullであってはなりません。");
        }
    }

    @Nested
    @DisplayName("比較（より前か）")
    class IsBefore {

        @Test
        @DisplayName("日付を比較できる")
        void isBeforeSuccess() {
            // given
            LocalDate date1 = LocalDate.now().minusDays(10);
            LocalDate date2 = LocalDate.now().minusDays(5);
            ExpenseDate expenseDate1 = new ExpenseDate(date1);
            ExpenseDate expenseDate2 = new ExpenseDate(date2);

            // when, then
            assertThat(expenseDate1.isBefore(expenseDate2)).isTrue();
            assertThat(expenseDate2.isBefore(expenseDate1)).isFalse();
        }

        @Test
        @DisplayName("比較する日付がnullの場合は例外が発生する")
        void isBeforeWithNull() {
            // given
            ExpenseDate date = new ExpenseDate(LocalDate.now());

            // when, then
            assertThatThrownBy(() -> date.isBefore(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("比較する日付はnullであってはなりません。");
        }
    }

    @Nested
    @DisplayName("同じ日付の比較")
    class CompareEqual {

        @Test
        @DisplayName("同じ日付の場合はより後/前の比較はfalseになる")
        void compareEqualDates() {
            // given
            LocalDate sameDate = LocalDate.now().minusDays(5);
            ExpenseDate date1 = new ExpenseDate(sameDate);
            ExpenseDate date2 = new ExpenseDate(sameDate);

            // when, then
            assertThat(date1.isAfter(date2)).isFalse();
            assertThat(date1.isBefore(date2)).isFalse();
        }
    }

    @Nested
    @DisplayName("同じ月かどうか")
    class IsSameMonth {

        @Test
        @DisplayName("同じ月の異なる日付の場合はtrueが返される")
        void isSameMonthSuccess() {
            // given
            LocalDate date1 = LocalDate.of(2024, 6, 15);
            LocalDate date2 = LocalDate.of(2024, 6, 20);
            ExpenseDate expenseDate1 = new ExpenseDate(date1);
            ExpenseDate expenseDate2 = new ExpenseDate(date2);

            // when, then
            assertThat(expenseDate1.isSameMonth(expenseDate2)).isTrue();
        }

        @Test
        @DisplayName("異なる月の場合はfalseが返される")
        void isSameMonthDifferentMonth() {
            // given
            LocalDate date1 = LocalDate.of(2024, 6, 15);
            LocalDate date2 = LocalDate.of(2024, 7, 15);
            ExpenseDate expenseDate1 = new ExpenseDate(date1);
            ExpenseDate expenseDate2 = new ExpenseDate(date2);

            // when, then
            assertThat(expenseDate1.isSameMonth(expenseDate2)).isFalse();
        }

        @Test
        @DisplayName("異なる年の場合はfalseが返される")
        void isSameMonthDifferentYear() {
            // given
            LocalDate date1 = LocalDate.of(2024, 6, 15);
            LocalDate date2 = LocalDate.of(2025, 6, 15);
            ExpenseDate expenseDate1 = new ExpenseDate(date1);
            ExpenseDate expenseDate2 = new ExpenseDate(date2);

            // when, then
            assertThat(expenseDate1.isSameMonth(expenseDate2)).isFalse();
        }

        @Test
        @DisplayName("比較する日付がnullの場合は例外が発生する")
        void isSameMonthWithNull() {
            // given
            ExpenseDate date = new ExpenseDate(LocalDate.now());

            // when, then
            assertThatThrownBy(() -> date.isSameMonth(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("比較する日付はnullであってはなりません。");
        }
    }
}
