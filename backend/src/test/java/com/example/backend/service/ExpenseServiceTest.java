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