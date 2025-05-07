package com.example.backend.service;

import com.example.backend.entity.Expense;
import com.example.backend.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import com.example.backend.service.ExpenseMapper;

/**
 * 支出に関するビジネスロジックを担当するサービスクラス
 */
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    public ExpenseService(ExpenseRepository expenseRepository, ExpenseMapper expenseMapper) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
    }

    /**
     * 全ての支出を取得します
     * 
     * @return 支出リスト
     */
    public List<ExpenseDto> getAllExpenses() {
        List<Expense> expenses = expenseRepository.findAll();
        return expenses.stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 新しい支出を追加します
     * 
     * @param expenseRequestDto 支出リクエストDTO
     * @return 追加した支出エンティティ
     */
    public Expense addExpense(ExpenseRequestDto expenseRequestDto) {
        if (expenseRequestDto == null)
            throw new NullPointerException("requestDto is null");
        Expense expense = expenseMapper.toEntity(expenseRequestDto);
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