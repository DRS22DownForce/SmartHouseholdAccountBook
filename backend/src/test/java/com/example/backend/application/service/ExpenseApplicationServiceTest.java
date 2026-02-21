package com.example.backend.application.service;

import com.example.backend.domain.repository.ExpenseRepository;
import com.example.backend.domain.valueobject.CategoryType;
import com.example.backend.domain.valueobject.ExpenseAmount;
import com.example.backend.domain.valueobject.ExpenseDate;
import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.backend.exception.ExpenseNotFoundException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ExpenseApplicationServiceのテストクラス
 * 
 * アプリケーションサービスのユースケースをテストします。
 */
@ExtendWith(MockitoExtension.class)
class ExpenseApplicationServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserApplicationService userApplicationService;

    @InjectMocks
    private ExpenseApplicationService expenseApplicationService;

    @Test
    void getAllExpenses_リポジトリに2件あれば2件返す() {
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 1));
        CategoryType category1 = CategoryType.FOOD;
        Expense expense1 = new Expense("支出1", amount1, date1, category1, user);

        ExpenseAmount amount2 = new ExpenseAmount(2000);
        ExpenseDate date2 = new ExpenseDate(LocalDate.of(2024, 1, 2));
        CategoryType category2 = CategoryType.TRANSPORT;
        Expense expense2 = new Expense("支出2", amount2, date2, category2, user);

        when(expenseRepository.findByUser(user)).thenReturn(Arrays.asList(expense1, expense2));
        when(userApplicationService.getUser()).thenReturn(user);

        List<Expense> result = expenseApplicationService.getExpenses();

        assertEquals(2, result.size());
        assertEquals("支出1", result.get(0).getDescription());
        assertEquals("支出2", result.get(1).getDescription());
    }

    @Test
    void addExpense_支出追加() {
        Expense.ExpenseUpdate creation = new Expense.ExpenseUpdate(
                "テスト支出",
                new ExpenseAmount(1000),
                new ExpenseDate(LocalDate.of(2024, 1, 1)),
                CategoryType.FOOD);

        User user = new User("cognitoSub", "test@example.com");
        when(userApplicationService.getUser()).thenReturn(user);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Expense result = expenseApplicationService.addExpense(creation);

        assertNotNull(result);
        assertEquals("テスト支出", result.getDescription());
        assertEquals(1000, result.getAmount().getAmount());
        assertEquals(LocalDate.of(2024, 1, 1), result.getDate().getDate());
        assertEquals(CategoryType.FOOD, result.getCategory());
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    void updateExpense_正常に更新できる() {
        Long expenseId = 1L;
        Expense.ExpenseUpdate expenseUpdate = new Expense.ExpenseUpdate(
                "更新された支出",
                new ExpenseAmount(1500),
                new ExpenseDate(LocalDate.of(2024, 1, 15)),
                CategoryType.ENTERTAINMENT);

        User user = new User("cognitoSub", "test@example.com");
        Expense existingExpense = new Expense(
                "元の支出",
                new ExpenseAmount(1000),
                new ExpenseDate(LocalDate.of(2024, 1, 1)),
                CategoryType.FOOD,
                user);

        when(userApplicationService.getUser()).thenReturn(user);
        when(expenseRepository.findByIdAndUser(expenseId, user)).thenReturn(Optional.of(existingExpense));
        when(expenseRepository.save(existingExpense)).thenReturn(existingExpense);

        Expense result = expenseApplicationService.updateExpense(expenseId, expenseUpdate);

        assertNotNull(result);
        assertEquals("更新された支出", result.getDescription());
        assertEquals(1500, result.getAmount().getAmount());
        assertEquals(LocalDate.of(2024, 1, 15), result.getDate().getDate());
        assertEquals(CategoryType.ENTERTAINMENT, result.getCategory());
        verify(expenseRepository, times(1)).findByIdAndUser(expenseId, user);
        verify(expenseRepository, times(1)).save(existingExpense);
    }

    @Test
    void updateExpense_存在しないIDなら例外() {
        Long nonExistentId = 999L;
        Expense.ExpenseUpdate update = new Expense.ExpenseUpdate(
                "テスト支出",
                new ExpenseAmount(1),
                new ExpenseDate(LocalDate.EPOCH),
                CategoryType.OTHER);

        User user = new User("cognitoSub", "test@example.com");
        when(userApplicationService.getUser()).thenReturn(user);
        when(expenseRepository.findByIdAndUser(nonExistentId, user)).thenReturn(Optional.empty());

        ExpenseNotFoundException exception = assertThrows(ExpenseNotFoundException.class,
                () -> expenseApplicationService.updateExpense(nonExistentId, update));

        assertEquals("ID: " + nonExistentId + " の支出が見つかりませんでした。", exception.getMessage());
        verify(expenseRepository, times(1)).findByIdAndUser(nonExistentId, user);
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void getMonthlySummary_正常に取得できる() {
        // テストデータの準備
        String month = "2024-01";
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 1));
        CategoryType category1 = CategoryType.FOOD;
        Expense expense1 = new Expense("支出1", amount1, date1, category1, user);
        
        ExpenseAmount amount2 = new ExpenseAmount(2000);
        ExpenseDate date2 = new ExpenseDate(LocalDate.of(2024, 1, 2));
        CategoryType category2 = CategoryType.TRANSPORT;
        Expense expense2 = new Expense("支出2", amount2, date2, category2, user);

        // モックの設定
        when(userApplicationService.getUser()).thenReturn(user);
        when(expenseRepository.findByUserAndDateBetween(
            eq(user),
            eq(LocalDate.of(2024, 1, 1)),
            eq(LocalDate.of(2024, 1, 31))
        )).thenReturn(Arrays.asList(expense1, expense2));

        // テスト実行
        com.example.backend.domain.valueobject.MonthlySummary result = 
            expenseApplicationService.getMonthlySummary(month);

        // 検証
        assertNotNull(result);
        assertEquals(3000, result.getTotal());
        assertEquals(2, result.getCount());
        assertEquals(2, result.getCategorySummaries().size());
        // 金額の降順でソートされていることを確認
        assertTrue(result.getCategorySummaries().get(0).getAmount() >= 
                   result.getCategorySummaries().get(1).getAmount());
    }

    @Test
    void getMonthlySummary_月の形式が不正なら例外() {
        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> expenseApplicationService.getMonthlySummary("2024-1"));

        assertTrue(exception.getMessage().contains("月の形式が不正です"));
    }

    @Test
    void getAvailableMonths_正常に取得できる() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        // H2とMySQLの両方で動作するように、LocalDateのリストを返すように変更
        List<LocalDate> distinctDates = Arrays.asList(
            LocalDate.of(2024, 3, 15),
            LocalDate.of(2024, 2, 10),
            LocalDate.of(2024, 1, 5),
            LocalDate.of(2024, 3, 20) // 同じ月の別の日付（重複除去のテスト用）
        );

        // モックの設定
        when(userApplicationService.getUser()).thenReturn(user);
        when(expenseRepository.findDistinctDatesByUser(user)).thenReturn(distinctDates);

        // テスト実行
        List<String> result = expenseApplicationService.getAvailableMonths();

        // 検証
        assertNotNull(result);
        assertEquals(3, result.size()); // 同じ月の重複が除去されるため、3つになる
        assertEquals("2024-03", result.get(0));
    }

}

