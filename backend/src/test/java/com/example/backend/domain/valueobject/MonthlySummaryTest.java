package com.example.backend.domain.valueobject;

import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MonthlySummary値オブジェクトのテストクラス
 *
 * createMonthlySummaryFromExpenses の振る舞いと、月別サマリーの不変性をテストします。
 */
class MonthlySummaryTest {

    private static User testUser() {
        return new User("cognitoSub", "test@example.com");
    }

    private static Expense expense(String description, int amount, CategoryType category) {
        return new Expense(
                description,
                new ExpenseAmount(amount),
                new ExpenseDate(LocalDate.now()),
                category,
                testUser());
    }

    @Nested
    @DisplayName("createMonthlySummaryFromExpenses（正常系）")
    class CreateFromExpensesSuccess {

        @Test
        @DisplayName("支出リストから月別サマリーを作成できる")
        void createWithExpenses() {
            // given
            List<Expense> expenses = List.of(
                    expense("食費1", 3000, CategoryType.FOOD),
                    expense("食費2", 2000, CategoryType.FOOD),
                    expense("交通費", 2000, CategoryType.TRANSPORT));

            // when
            MonthlySummary summary = MonthlySummary.createMonthlySummaryFromExpenses(expenses);

            // then
            assertThat(summary).isNotNull();
            assertThat(summary.getTotal()).isEqualTo(7000);
            assertThat(summary.getCount()).isEqualTo(3);
            assertThat(summary.getCategorySummaries()).hasSize(2);
            assertThat(summary.getCategorySummaries().get(0).getCategory()).isEqualTo(CategoryType.FOOD);
            assertThat(summary.getCategorySummaries().get(0).getAmount()).isEqualTo(5000);
            assertThat(summary.getCategorySummaries().get(1).getCategory()).isEqualTo(CategoryType.TRANSPORT);
            assertThat(summary.getCategorySummaries().get(1).getAmount()).isEqualTo(2000);
        }

        @Test
        @DisplayName("カテゴリ別集計は金額の降順でソートされる")
        void categorySummariesIsSortedByAmountDesc() {
            // given
            List<Expense> expenses = List.of(
                    expense("小", 1000, CategoryType.FOOD),
                    expense("大", 5000, CategoryType.TRANSPORT),
                    expense("中", 3000, CategoryType.UTILITIES));

            // when
            MonthlySummary summary = MonthlySummary.createMonthlySummaryFromExpenses(expenses);

            // then
            assertThat(summary.getCategorySummaries())
                    .extracting(CategorySummary::getAmount)
                    .containsExactly(5000, 3000, 1000);
        }

    }

    @Nested
    @DisplayName("createMonthlySummaryFromExpenses（境界・空リスト）")
    class CreateFromExpensesEdge {

        @Test
        @DisplayName("空リストの場合は合計0・件数0・カテゴリ空で作成される")
        void createWithEmptyList() {
            // given
            List<Expense> expenses = Collections.emptyList();

            // when
            MonthlySummary summary = MonthlySummary.createMonthlySummaryFromExpenses(expenses);

            // then
            assertThat(summary).isNotNull();
            assertThat(summary.getTotal()).isZero();
            assertThat(summary.getCount()).isZero();
            assertThat(summary.getCategorySummaries()).isEmpty();
        }
    }

    @Nested
    @DisplayName("createMonthlySummaryFromExpenses（異常系）")
    class CreateFromExpensesFailure {

        @Test
        @DisplayName("支出リストがnullの場合はNullPointerExceptionが発生する")
        void createWithNull() {
            // given, when, then
            assertThatThrownBy(() -> MonthlySummary.createMonthlySummaryFromExpenses(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("不変性")
    class Immutability {

        @Test
        @DisplayName("byCategoryは不変リストであり変更すると例外が発生する")
        void byCategoryIsUnmodifiable() {
            // given
            List<Expense> expenses = List.of(expense("テスト", 1000, CategoryType.FOOD));
            MonthlySummary summary = MonthlySummary.createMonthlySummaryFromExpenses(expenses);

            // when, then
            assertThatThrownBy(() ->
                    summary.getCategorySummaries().add(new CategorySummary(CategoryType.FOOD, 2000)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
