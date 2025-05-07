package com.example.backend.controller;

import com.example.backend.generated.api.DefaultApi;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import com.example.backend.service.ExpenseService;
import com.example.backend.entity.Expense;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ExpenseController implements DefaultApi {
    private final ExpenseService expenseService;

    // コンストラクタインジェクション
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Override
    public ResponseEntity<List<ExpenseDto>> apiExpensesGet() {
        List<ExpenseDto> expenseDtos = expenseService.getAllExpenses();
        return ResponseEntity.ok(expenseDtos);
    }

    @Override
    public ResponseEntity<ExpenseDto> apiExpensesPost(ExpenseRequestDto expenseRequestDto) {
        Expense expense = expenseService.addExpense(expenseRequestDto);
        ExpenseDto expenseDto = new ExpenseDto();
        expenseDto.setId(expense.getId());
        return ResponseEntity.ok(expenseDto);
    }

    @Override
    public ResponseEntity<Void> apiExpensesIdDelete(Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

}