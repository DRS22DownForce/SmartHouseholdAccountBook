package com.example.backend.service;

import com.example.backend.entity.Expense;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
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
        Expense expense1 = new Expense("支出1", 1000, LocalDate.of(2024, 1, 1), "食費");
        Expense expense2 = new Expense("支出2", 2000, LocalDate.of(2024, 1, 2), "交通費");

        // モックの振る舞いの設定
        when(expenseRepository.findAll()).thenReturn(Arrays.asList(expense1, expense2));

        // サービスのメソッドを呼び出す
        List<ExpenseDto> result = expenseService.getAllExpenses();

        // 結果の検証
        assertEquals(2, result.size(), "支出の数が正しいこと");

        // 1件目の内容を検証
        ExpenseDto dto1 = result.get(0);
        assertEquals("支出1", dto1.getDescription(), "1件目の説明が正しいこと");
        assertEquals(1000, dto1.getAmount(), "1件目の金額が正しいこと");
        assertEquals(LocalDate.of(2024, 1, 1), dto1.getDate(), "1件目の日付が正しいこと");
        assertEquals("食費", dto1.getCategory(), "1件目のカテゴリが正しいこと");

        // 2件目の内容も同様に検証
        ExpenseDto dto2 = result.get(1);
        assertEquals("支出2", dto2.getDescription(), "2件目の説明が正しいこと");
        assertEquals(2000, dto2.getAmount(), "2件目の金額が正しいこと");
        assertEquals(LocalDate.of(2024, 1, 2), dto2.getDate(), "2件目の日付が正しいこと");
        assertEquals("交通費", dto2.getCategory(), "2件目のカテゴリが正しいこと");
    }

    /**
     * addExpenseメソッドが正しく支出を追加できるかのテスト
     */
    @Test
    void addExpense_正常系_支出が追加される() {
        //テスト用データ
        String description = "テスト支出";  
        int amount = 1000;
        String category = "食費";
        LocalDate testDate = LocalDate.of(2024, 1, 1);

        // テスト用のリクエストDTOを作成
        ExpenseRequestDto requestDto = new ExpenseRequestDto();
        requestDto.setDescription(description);
        requestDto.setAmount(amount);
        requestDto.setDate(testDate);
        requestDto.setCategory(category);

        // 期待されるExpenseエンティティ
        Expense expense = new Expense(description, amount, testDate, category);

        // expenseRepository.saveが呼ばれたとき、expenseを返すように設定
        when(expenseRepository.save(ArgumentMatchers.any(Expense.class))).thenReturn(expense);

        // 実際にサービスを呼び出す
        Expense result = expenseService.addExpense(requestDto);

        // 結果の検証
        assertNotNull(result, "結果がnullでないこと");
        assertEquals(amount, result.getAmount(), "金額が正しいこと");
        assertEquals(description, result.getDescription(), "説明が正しいこと");
        assertEquals(testDate, result.getDate(), "日付が正しいこと");
        assertEquals(category, result.getCategory(), "カテゴリが正しいこと");

        // saveメソッドが1回だけ呼ばれたことを検証
        verify(expenseRepository, times(1)).save(ArgumentMatchers.any(Expense.class));
    }
}