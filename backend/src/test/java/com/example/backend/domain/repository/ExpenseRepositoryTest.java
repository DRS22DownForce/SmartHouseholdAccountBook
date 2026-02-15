package com.example.backend.domain.repository;

import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.domain.valueobject.CategoryType;
import com.example.backend.domain.valueobject.ExpenseAmount;
import com.example.backend.domain.valueobject.ExpenseDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExpenseRepositoryのテストクラス
 * 
 * 支出リポジトリのクエリメソッドをテストします。
 * @DataJpaTestアノテーションにより、JPAレイヤーのみをテストします（実際のデータベースを使用）。
 */
@DataJpaTest
@ActiveProfiles("test")
class ExpenseRepositoryTest {

    // @Autowired: Springの依存性注入により、リポジトリの実装が自動的に注入されます
    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // テスト前にデータベースをクリア（各テストが独立して実行されるように）
        expenseRepository.deleteAll();
        userRepository.deleteAll();

        // テスト用のユーザーを作成して保存
        testUser = new User("cognitoSub", "test@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("ユーザーを指定して支出を取得できる")
    void findByUser_正常に取得() {
        // テストデータの準備: 支出データを作成して保存
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 15));
        CategoryType category1 = CategoryType.FOOD;
        Expense expense1 = new Expense("支出1", amount1, date1, category1, testUser);
        expenseRepository.save(expense1);

        ExpenseAmount amount2 = new ExpenseAmount(2000);
        ExpenseDate date2 = new ExpenseDate(LocalDate.of(2024, 1, 20));
        CategoryType category2 = CategoryType.TRANSPORT;
        Expense expense2 = new Expense("支出2", amount2, date2, category2, testUser);
        expenseRepository.save(expense2);

        // テスト実行: ユーザーを指定して支出を取得
        List<Expense> expenses = expenseRepository.findByUser(testUser);

        // 検証: 2件の支出が取得できることを確認
        assertEquals(2, expenses.size());
    }

    @Test
    @DisplayName("ユーザーを指定して支出を取得する際、他のユーザーの支出は含まれない")
    void findByUser_他のユーザーの支出は含まれない() {
        // テストデータの準備: 別のユーザーを作成
        User otherUser = new User("otherCognitoSub", "other@example.com");
        otherUser = userRepository.save(otherUser);

        // テストユーザーの支出を作成
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 15));
        CategoryType category1 = CategoryType.FOOD;
        Expense expense1 = new Expense("テストユーザーの支出", amount1, date1, category1, testUser);
        expenseRepository.save(expense1);

        // 別のユーザーの支出を作成
        ExpenseAmount amount2 = new ExpenseAmount(2000);
        ExpenseDate date2 = new ExpenseDate(LocalDate.of(2024, 1, 20));
        CategoryType category2 = CategoryType.TRANSPORT;
        Expense expense2 = new Expense("別ユーザーの支出", amount2, date2, category2, otherUser);
        expenseRepository.save(expense2);

        // テスト実行: テストユーザーの支出のみを取得
        List<Expense> expenses = expenseRepository.findByUser(testUser);

        // 検証: テストユーザーの支出のみが取得できることを確認
        assertEquals(1, expenses.size());
        assertEquals("テストユーザーの支出", expenses.get(0).getDescription());
    }

    @Test
    @DisplayName("ユーザーと日付範囲を指定して支出を取得できる")
    void findByUserAndDateBetween_正常に取得() {
        // テストデータの準備: 異なる日付の支出を作成
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 10));
        CategoryType category1 = CategoryType.FOOD;
        Expense expense1 = new Expense("支出1", amount1, date1, category1, testUser);
        expenseRepository.save(expense1);

        ExpenseAmount amount2 = new ExpenseAmount(2000);
        ExpenseDate date2 = new ExpenseDate(LocalDate.of(2024, 1, 20));
        CategoryType category2 = CategoryType.TRANSPORT;
        Expense expense2 = new Expense("支出2", amount2, date2, category2, testUser);
        expenseRepository.save(expense2);

        ExpenseAmount amount3 = new ExpenseAmount(3000);
        ExpenseDate date3 = new ExpenseDate(LocalDate.of(2024, 2, 5));
        CategoryType category3 = CategoryType.HOUSING;
        Expense expense3 = new Expense("支出3", amount3, date3, category3, testUser);
        expenseRepository.save(expense3);

        // テスト実行: 1月の範囲で支出を取得
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);
        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(testUser, start, end);

        // 検証: 1月の支出のみが取得できることを確認（降順でソートされている）
        assertEquals(2, expenses.size());
        // 降順でソートされていることを確認（新しい日付が先頭）
        assertTrue(expenses.get(0).getDate().getDate().isAfter(expenses.get(1).getDate().getDate()) ||
                   expenses.get(0).getDate().getDate().equals(expenses.get(1).getDate().getDate()));
    }

    @Test
    @DisplayName("利用可能な月のリストを取得できる")
    void findDistinctDatesByUser_正常に取得() {
        // テストデータの準備: 異なる月の支出を作成
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 15));
        CategoryType category1 = CategoryType.FOOD;
        Expense expense1 = new Expense("支出1", amount1, date1, category1, testUser);
        expenseRepository.save(expense1);

        ExpenseAmount amount2 = new ExpenseAmount(2000);
        ExpenseDate date2 = new ExpenseDate(LocalDate.of(2024, 2, 10));
        CategoryType category2 = CategoryType.TRANSPORT;
        Expense expense2 = new Expense("支出2", amount2, date2, category2, testUser);
        expenseRepository.save(expense2);

        ExpenseAmount amount3 = new ExpenseAmount(3000);
        ExpenseDate date3 = new ExpenseDate(LocalDate.of(2024, 2, 20));
        CategoryType category3 = CategoryType.HOUSING;
        Expense expense3 = new Expense("支出3", amount3, date3, category3, testUser);
        expenseRepository.save(expense3);

        // テスト実行: 利用可能な日付のリストを取得
        List<LocalDate> distinctDates = expenseRepository.findDistinctDatesByUser(testUser);

        // 検証: 3つの異なる日付が取得できることを確認（降順でソートされている）
        assertEquals(3, distinctDates.size());
        // 降順でソートされていることを確認
        assertTrue(distinctDates.get(0).isAfter(distinctDates.get(1)) ||
                   distinctDates.get(0).equals(distinctDates.get(1)));
    }

    @Test
    @DisplayName("ユーザーと月を指定して支出を取得できる（ページネーション対応）")
    void findByUserAndDateRange_正常に取得() {
        // テストデータの準備: 同じ月の複数の支出を作成
        for (int i = 1; i <= 5; i++) {
            ExpenseAmount amount = new ExpenseAmount(1000 * i);
            ExpenseDate date = new ExpenseDate(LocalDate.of(2024, 1, i * 5));
            CategoryType category = CategoryType.FOOD;
            Expense expense = new Expense("支出" + i, amount, date, category, testUser);
            expenseRepository.save(expense);
        }

        // テスト実行: 1月の範囲で支出を取得（ページネーション: 1ページ目、2件）
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        Pageable pageable = PageRequest.of(0, 2);
        Page<Expense> expensePage = expenseRepository.findByUserAndDateRange(testUser, startDate, endDate, pageable);

        // 検証: ページネーションが正しく動作することを確認
        assertEquals(5, expensePage.getTotalElements()); // 全件数
        assertEquals(2, expensePage.getContent().size()); // 現在のページの件数
        assertEquals(3, expensePage.getTotalPages()); // 総ページ数
        // 降順でソートされていることを確認
        assertTrue(expensePage.getContent().get(0).getDate().getDate()
                .isAfter(expensePage.getContent().get(1).getDate().getDate()) ||
                expensePage.getContent().get(0).getDate().getDate()
                        .equals(expensePage.getContent().get(1).getDate().getDate()));
    }

    @Test
    @DisplayName("支出が存在しない場合は空のリストが返される")
    void findByUser_支出が存在しない場合() {
        // テスト実行: 支出が存在しないユーザーで取得
        List<Expense> expenses = expenseRepository.findByUser(testUser);

        // 検証: 空のリストが返されることを確認
        assertTrue(expenses.isEmpty());
    }
}

