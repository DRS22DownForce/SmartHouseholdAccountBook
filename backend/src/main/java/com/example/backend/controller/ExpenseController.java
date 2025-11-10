package com.example.backend.controller;

import com.example.backend.application.service.ExpenseApplicationService;
import com.example.backend.generated.api.DefaultApi;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 支出に関するREST APIコントローラー
 * 
 * このコントローラーは支出のCRUD操作を提供します。
 * アプリケーションサービスを呼び出してビジネスロジックを実行します。
 */
@RestController
public class ExpenseController implements DefaultApi {
    private final ExpenseApplicationService expenseApplicationService;

    /**
     * コンストラクタ
     * 
     * @param expenseApplicationService 支出アプリケーションサービス
     */
    public ExpenseController(ExpenseApplicationService expenseApplicationService) {
        this.expenseApplicationService = expenseApplicationService;
    }

    @Override
    public ResponseEntity<List<ExpenseDto>> apiExpensesGet() {
        List<ExpenseDto> expenseDtos = expenseApplicationService.getExpenses();
        return ResponseEntity.ok(expenseDtos);
    }

    @Override
    public ResponseEntity<ExpenseDto> apiExpensesPost(ExpenseRequestDto expenseRequestDto) {
        ExpenseDto expenseDto = expenseApplicationService.addExpense(expenseRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseDto);
    }

    @Override
    public ResponseEntity<Void> apiExpensesIdDelete(Long id) {
        expenseApplicationService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ExpenseDto> apiExpensesIdPut(Long id, ExpenseRequestDto expenseRequestDto) {
        ExpenseDto expenseDto = expenseApplicationService.updateExpense(id, expenseRequestDto);
        return ResponseEntity.ok(expenseDto);
    }

}