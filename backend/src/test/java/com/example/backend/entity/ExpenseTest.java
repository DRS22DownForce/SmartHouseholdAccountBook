package com.example.backend.entity;

import com.example.backend.domain.valueobject.CategoryType;
import com.example.backend.domain.valueobject.ExpenseAmount;
import com.example.backend.domain.valueobject.ExpenseDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Expenseエンティティのテストクラス
 *
 * 支出エンティティのバリデーションと変更メソッドをテストします。
 */
class ExpenseTest {

    /** テスト用の固定日付（実行日に依存しない再現可能なテストのため） */
    private static final LocalDate TEST_DATE = LocalDate.of(2025, 1, 15);

    private static User defaultUser() {
        return new User("cognitoSub", "test@example.com");
    }

    private static ExpenseAmount defaultAmount() {
        return new ExpenseAmount(1000);
    }

    private static ExpenseDate defaultDate() {
        return new ExpenseDate(TEST_DATE);
    }

    private static CategoryType defaultCategory() {
        return CategoryType.FOOD;
    }

    private static Expense defaultExpense() {
        return new Expense("テスト支出", defaultAmount(), defaultDate(), defaultCategory(), defaultUser());
    }

    private static void assertExpenseConstructorThrows(
            String description, ExpenseAmount amount, ExpenseDate date,
            CategoryType category, User user,
            Class<? extends Throwable> exceptionClass, String message) {
        assertThatThrownBy(() -> new Expense(description, amount, date, category, user))
                .isInstanceOf(exceptionClass)
                .hasMessage(message);
    }

    private static void assertExpenseUpdateThrows(
            String description, ExpenseAmount amount, ExpenseDate date, CategoryType category,
            Class<? extends Throwable> exceptionClass, String message) {
        assertThatThrownBy(() -> new Expense.ExpenseUpdate(description, amount, date, category))
                .isInstanceOf(exceptionClass)
                .hasMessage(message);
    }

    @Nested
    @DisplayName("コンストラクタ（正常系）")
    class ConstructorSuccess {
        @Test
        @DisplayName("正常な支出エンティティを作成できる")
        void createWithValidValues() {
            User user = defaultUser();
            Expense expense = new Expense(
                    "テスト支出", defaultAmount(), defaultDate(), defaultCategory(), user);

            assertThat(expense).isNotNull();
            assertThat(expense.getDescription()).isEqualTo("テスト支出");
            assertThat(expense.getAmount().getAmount()).isEqualTo(1000);
            assertThat(expense.getDate().getDate()).isEqualTo(TEST_DATE);
            assertThat(expense.getCategory().getDisplayName()).isEqualTo("食費");
            assertThat(expense.getUser()).isEqualTo(user);
        }
    }

    @Nested
    @DisplayName("コンストラクタ（異常系・説明）")
    class ConstructorFailureDescription {
        @Test
        @DisplayName("説明がnullの場合は例外が発生する")
        void createWithNullDescription() {
            assertExpenseConstructorThrows(
                    null, defaultAmount(), defaultDate(), defaultCategory(), defaultUser(),
                    NullPointerException.class, "説明はnullであってはなりません。");
        }

        @Test
        @DisplayName("説明が空文字列の場合は例外が発生する")
        void createWithEmptyDescription() {
            assertExpenseConstructorThrows(
                    "", defaultAmount(), defaultDate(), defaultCategory(), defaultUser(),
                    IllegalArgumentException.class, "説明は空文字列であってはなりません。");
        }

        @Test
        @DisplayName("説明が空白のみの場合は例外が発生する")
        void createWithBlankDescription() {
            assertExpenseConstructorThrows(
                    "   ", defaultAmount(), defaultDate(), defaultCategory(), defaultUser(),
                    IllegalArgumentException.class, "説明は空文字列であってはなりません。");
        }
    }

    @Nested
    @DisplayName("コンストラクタ（異常系・必須項目がnull）")
    class ConstructorFailureNullRequired {
        @Test
        @DisplayName("金額がnullの場合は例外が発生する")
        void createWithNullAmount() {
            assertExpenseConstructorThrows(
                    "テスト支出", null, defaultDate(), defaultCategory(), defaultUser(),
                    NullPointerException.class, "金額はnullであってはなりません。");
        }

        @Test
        @DisplayName("日付がnullの場合は例外が発生する")
        void createWithNullDate() {
            assertExpenseConstructorThrows(
                    "テスト支出", defaultAmount(), null, defaultCategory(), defaultUser(),
                    NullPointerException.class, "日付はnullであってはなりません。");
        }

        @Test
        @DisplayName("カテゴリーがnullの場合は例外が発生する")
        void createWithNullCategory() {
            assertExpenseConstructorThrows(
                    "テスト支出", defaultAmount(), defaultDate(), null, defaultUser(),
                    NullPointerException.class, "カテゴリーはnullであってはなりません。");
        }

        @Test
        @DisplayName("ユーザーがnullの場合は例外が発生する")
        void createWithNullUser() {
            assertExpenseConstructorThrows(
                    "テスト支出", defaultAmount(), defaultDate(), defaultCategory(), null,
                    NullPointerException.class, "ユーザーはnullであってはなりません。");
        }
    }

    @Nested
    @DisplayName("update（正常系）")
    class UpdateSuccess {
        @Test
        @DisplayName("説明を変更できる")
        void updateDescription() {
            Expense expense = new Expense(
                    "元の説明", defaultAmount(), defaultDate(), defaultCategory(), defaultUser());
            Expense.ExpenseUpdate update = new Expense.ExpenseUpdate(
                    "新しい説明", defaultAmount(), defaultDate(), defaultCategory());

            expense.update(update);

            assertThat(expense.getDescription()).isEqualTo("新しい説明");
            assertThat(expense.getAmount().getAmount()).isEqualTo(1000);
            assertThat(expense.getDate().getDate()).isEqualTo(TEST_DATE);
            assertThat(expense.getCategory().getDisplayName()).isEqualTo("食費");
        }

        @Test
        @DisplayName("金額を変更できる")
        void updateAmount() {
            Expense expense = defaultExpense();
            Expense.ExpenseUpdate update = new Expense.ExpenseUpdate(
                    "テスト支出", new ExpenseAmount(2000), defaultDate(), defaultCategory());

            expense.update(update);

            assertThat(expense.getAmount().getAmount()).isEqualTo(2000);
        }

        @Test
        @DisplayName("日付を変更できる")
        void updateDate() {
            LocalDate originalDate = TEST_DATE.minusDays(5);
            LocalDate newDate = TEST_DATE.minusDays(1);
            Expense expense = new Expense(
                    "テスト支出", defaultAmount(),
                    new ExpenseDate(originalDate), defaultCategory(), defaultUser());
            Expense.ExpenseUpdate update = new Expense.ExpenseUpdate(
                    "テスト支出", defaultAmount(), new ExpenseDate(newDate), defaultCategory());

            expense.update(update);

            assertThat(expense.getDate().getDate()).isEqualTo(newDate);
        }

        @Test
        @DisplayName("カテゴリーを変更できる")
        void updateCategory() {
            Expense expense = defaultExpense();
            Expense.ExpenseUpdate update = new Expense.ExpenseUpdate(
                    "テスト支出", defaultAmount(), defaultDate(), CategoryType.TRANSPORT);

            expense.update(update);

            assertThat(expense.getCategory().getDisplayName()).isEqualTo("交通費");
        }

        @Test
        @DisplayName("複数のフィールドを一度に更新できる")
        void updateMultipleFields() {
            LocalDate originalDate = TEST_DATE.minusDays(5);
            LocalDate newDate = TEST_DATE.minusDays(1);
            Expense expense = new Expense(
                    "元の説明",
                    defaultAmount(),
                    new ExpenseDate(originalDate),
                    defaultCategory(),
                    defaultUser());
            Expense.ExpenseUpdate update = new Expense.ExpenseUpdate(
                    "新しい説明",
                    new ExpenseAmount(2000),
                    new ExpenseDate(newDate),
                    CategoryType.TRANSPORT);

            expense.update(update);

            assertThat(expense.getDescription()).isEqualTo("新しい説明");
            assertThat(expense.getAmount().getAmount()).isEqualTo(2000);
            assertThat(expense.getDate().getDate()).isEqualTo(newDate);
            assertThat(expense.getCategory().getDisplayName()).isEqualTo("交通費");
        }
    }

    @Nested
    @DisplayName("ExpenseUpdate（異常系・説明）")
    class ExpenseUpdateFailureDescription {
        @Test
        @DisplayName("説明が空文字列の場合は例外が発生する")
        void createWithEmptyDescription() {
            assertExpenseUpdateThrows(
                    "", defaultAmount(), defaultDate(), defaultCategory(),
                    IllegalArgumentException.class, "説明は空文字列であってはなりません。");
        }

        @Test
        @DisplayName("説明が空白のみの場合は例外が発生する")
        void createWithBlankDescription() {
            assertExpenseUpdateThrows(
                    "   ", defaultAmount(), defaultDate(), defaultCategory(),
                    IllegalArgumentException.class, "説明は空文字列であってはなりません。");
        }

        @Test
        @DisplayName("金額がnullの場合は例外が発生する")
        void createWithNullAmount() {
            assertExpenseUpdateThrows(
                    "テスト支出", null, defaultDate(), defaultCategory(),
                    NullPointerException.class, "金額はnullであってはなりません。");
        }
    }
}
