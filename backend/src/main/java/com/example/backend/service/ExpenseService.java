package com.example.backend.service;

import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
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
    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final UserService userService;

    public ExpenseService(ExpenseRepository expenseRepository, ExpenseMapper expenseMapper, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
        this.userService = userService;
    }

    /**
     * 全ての支出を取得します
     * 
     * @return 支出リスト
     */
    public List<ExpenseDto> getExpenses() {
        User user = userService.getUser();
        List<Expense> expenses = expenseRepository.findByUser(user);
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
    public ExpenseDto addExpense(ExpenseRequestDto expenseRequestDto) {
        if (expenseRequestDto == null)
            throw new NullPointerException("requestDto is null");
        User user = userService.getUser();
        Expense expense = expenseMapper.toEntity(expenseRequestDto);
        expense.setUser(user);

        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toDto(savedExpense);
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