package com.example.backend.service;

import com.example.backend.dto.ExpenseForm;
import com.example.backend.entity.Expense;
import com.example.backend.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ExpenseServiceの単体テストクラス
 * サービスのビジネスロジックが正しく動作するかを検証します
 */
class ExpenseServiceTest {
    @Mock
    private ExpenseRepository expenseRepository; // モック化したリポジトリ

    @InjectMocks
    private ExpenseService expenseService; // テスト対象のサービス

    @BeforeEach
    void setUp() {
        
        // 各テスト実行前にモックの初期化を行う
        MockitoAnnotations.openMocks(this);
    }
    
    /**
     * getAllExpensesメソッドが正しく全ての支出を取得できるかのテスト
     */
    @Test
    void getAllExpenses_正常系_全ての支出が取得できる() {
        // テスト用の支出データ
        Expense expense1 = Expense.create("支出1", 1000, LocalDate.of(2024, 1, 1), "食費");
        Expense expense2 = Expense.create("支出2", 2000, LocalDate.of(2024, 1, 2), "交通費");

        // モックの振る舞いの設定
        when(expenseRepository.findAll()).thenReturn(Arrays.asList(expense1, expense2));

        // サービスのメソッドを呼び出す
        List<Expense> result = expenseService.getAllExpenses();

        // 結果の検証
        assertEquals(2, result.size(), "支出の数が正しいこと");
        assertEquals(expense1, result.get(0), "支出1が正しいこと");
        assertEquals(expense2, result.get(1), "支出2が正しいこと");
    }

    /**
     * addExpenseメソッドが正しく支出を追加できるかのテスト
     */
    @Test
    void addExpense_正常系_支出が追加される() {
        // テスト用の固定日付
        LocalDate testDate = LocalDate.of(2024, 1, 1);

        // テスト用フォームの作成
        ExpenseForm expenseForm = new ExpenseForm();
        expenseForm.setDescription("テスト支出");
        expenseForm.setAmount(1000);
        expenseForm.setDate(testDate);
        expenseForm.setCategory("食費");

        // 期待されるExpenseエンティティ
        Expense expense = Expense.create("テスト支出", 1000, testDate, "食費");

        // expenseRepository.saveが呼ばれたとき、expenseを返すように設定
        when(expenseRepository.save(ArgumentMatchers.any(Expense.class))).thenReturn(expense);

        // 実際にサービスを呼び出す
        Expense result = expenseService.addExpense(expenseForm);

        // 結果の検証
        assertNotNull(result, "結果がnullでないこと");
        assertEquals(1000, result.getAmount(), "金額が正しいこと");
        assertEquals("テスト支出", result.getDescription(), "説明が正しいこと");
        assertEquals(testDate, result.getDate(), "日付が正しいこと");
        assertEquals("食費", result.getCategory(), "カテゴリが正しいこと");

        // saveメソッドが1回だけ呼ばれたことを検証
        verify(expenseRepository, times(1)).save(ArgumentMatchers.any(Expense.class));
    }
}