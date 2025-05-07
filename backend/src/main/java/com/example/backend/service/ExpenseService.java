package com.example.backend.service;

import com.example.backend.entity.Expense;
import com.example.backend.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;

/**
 * 支出に関するビジネスロジックを担当するサービスクラス
 */
@Service
public class ExpenseService {
    private ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    /**
     * 全ての支出を取得します
     * 
     * @return 支出リスト
     */
    public List<ExpenseDto> getAllExpenses() {
        List<Expense> expenses = expenseRepository.findAll();

        List<ExpenseDto> expenseDtos = expenses.stream()
                .map(expense -> {
                    ExpenseDto expenseDto = new ExpenseDto();
                    expenseDto.setId(expense.getId());
                    expenseDto.setDate(expense.getDate());
                    expenseDto.setCategory(expense.getCategory());
                    expenseDto.setAmount(expense.getAmount());
                    expenseDto.setDescription(expense.getDescription());
                    return expenseDto;
                })
                .collect(Collectors.toList());
        return expenseDtos;
    }

    /**
     * 新しい支出を追加します
     * 
     * @param expenseRequestDto 支出リクエストDTO
     * @return 追加した支出エンティティ
     */
    public Expense addExpense(ExpenseRequestDto expenseRequestDto) {
        Expense expense = new Expense(expenseRequestDto.getDescription(), expenseRequestDto.getAmount(),
                expenseRequestDto.getDate(), expenseRequestDto.getCategory());
        return expenseRepository.save(expense);
    }

    /**
     * 支出を削除します
     * 
     * @param id 支出ID
     */
    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

}