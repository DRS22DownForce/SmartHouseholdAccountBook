package com.example.backend.repository;

import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.valueobject.CategoryType;
import com.example.backend.valueobject.ExpenseAmount;
import com.example.backend.valueobject.ExpenseDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ExpenseRepositoryのテストクラス
 */
@DataJpaTest //各テストはトランザクションで実行され終了後にロールバックされるため、テスト前の明示的なデータクリアは不要。
@ActiveProfiles("test")
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(new User("cognitoSub", "test@example.com"));
    }

    private Expense createExpense(String description, int amountYen, LocalDate date, CategoryType category, User user) {
        return expenseRepository.save(
                new Expense(
                        description,
                        new ExpenseAmount(amountYen),
                        new ExpenseDate(date),
                        category,
                        user
                ));
    }

    @Nested
    @DisplayName("findByUser - ユーザー指定で支出取得")
    class FindByUser {

        @Test
        @DisplayName("指定ユーザーの支出が取得できる")
        void returnsExpensesForUser() {
            // given
            createExpense("支出1", 1000, LocalDate.of(2024, 1, 15), CategoryType.FOOD, testUser);
            createExpense("支出2", 2000, LocalDate.of(2024, 1, 20), CategoryType.TRANSPORT, testUser);

            // when
            List<Expense> expenses = expenseRepository.findByUser(testUser);

            // then
            assertThat(expenses).hasSize(2);
        }

        @Test
        @DisplayName("他ユーザーの支出は含まれない")
        void excludesOtherUsersExpenses() {
            // given
            User otherUser = userRepository.save(new User("otherCognitoSub", "other@example.com"));
            createExpense("テストユーザーの支出", 1000, LocalDate.of(2024, 1, 15), CategoryType.FOOD, testUser);
            createExpense("別ユーザーの支出", 2000, LocalDate.of(2024, 1, 20), CategoryType.TRANSPORT, otherUser);

            // when
            List<Expense> expenses = expenseRepository.findByUser(testUser);

            // then
            assertThat(expenses).hasSize(1);
            assertThat(expenses.get(0).getDescription()).isEqualTo("テストユーザーの支出");
        }

        @Test
        @DisplayName("支出が無い場合は空リストを返す")
        void returnsEmptyWhenNoExpenses() {
            // when
            List<Expense> expenses = expenseRepository.findByUser(testUser);

            // then
            assertThat(expenses).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserAndDateBetween - ユーザーと日付範囲指定で取得")
    class FindByUserAndDateBetween {

        @Test
        @DisplayName("指定期間内の支出のみ取得され、日付降順で返る")
        void returnsExpensesInRangeSortedByDateDesc() {
            // given
            createExpense("支出1", 1000, LocalDate.of(2024, 1, 10), CategoryType.FOOD, testUser);
            createExpense("支出2", 2000, LocalDate.of(2024, 1, 20), CategoryType.TRANSPORT, testUser);
            createExpense("支出3", 3000, LocalDate.of(2024, 2, 5), CategoryType.HOUSING, testUser);

            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 31);

            // when
            List<Expense> expenses = expenseRepository.findByUserAndDateBetween(testUser, start, end);

            // then
            assertThat(expenses).hasSize(2);
            assertThat(expenses)
                    .extracting(e -> e.getDate().getDate())
                    .isSortedAccordingTo(Comparator.reverseOrder());
        }
    }

    @Nested
    @DisplayName("findDistinctDatesByUser - 利用可能な日付一覧取得")
    class FindDistinctDatesByUser {

        @Test
        @DisplayName("ユーザーの支出がある日付の一覧が日付降順で返る")
        void returnsDistinctDatesSortedDesc() {
            // given
            createExpense("支出1", 1000, LocalDate.of(2024, 1, 15), CategoryType.FOOD, testUser);
            createExpense("支出2", 2000, LocalDate.of(2024, 2, 10), CategoryType.TRANSPORT, testUser);
            createExpense("支出3", 3000, LocalDate.of(2024, 2, 20), CategoryType.HOUSING, testUser);

            // when
            List<LocalDate> distinctDates = expenseRepository.findDistinctDatesByUser(testUser);

            // then
            assertThat(distinctDates).hasSize(3);
            assertThat(distinctDates).isSortedAccordingTo(Comparator.reverseOrder());
        }
    }

    @Nested
    @DisplayName("findByUserAndDateRange - ユーザー・日付範囲・ページネーションで取得")
    class FindByUserAndDateRange {

        @Test
        @DisplayName("ページネーションで取得でき、総件数・総ページ数・内容が正しい")
        void returnsPagedExpensesInRange() {
            // given
            for (int i = 1; i <= 5; i++) {
                createExpense("支出" + i, 1000 * i, LocalDate.of(2024, 1, i * 5), CategoryType.FOOD, testUser);
            }
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            Pageable pageable = PageRequest.of(0, 2);

            // when
            Page<Expense> expensePage = expenseRepository.findByUserAndDateRange(testUser, startDate, endDate, pageable);

            // then
            assertThat(expensePage.getTotalElements()).isEqualTo(5);
            assertThat(expensePage.getContent()).hasSize(2);
            assertThat(expensePage.getTotalPages()).isEqualTo(3);
            assertThat(expensePage.getContent())
                    .extracting(e -> e.getDate().getDate())
                    .isSortedAccordingTo(Comparator.reverseOrder());
        }
    }
}
