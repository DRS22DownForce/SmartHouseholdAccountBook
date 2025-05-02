package com.example.backend.service;

import com.example.backend.entity.Expense;
import com.example.backend.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.backend.dto.ExpenseForm;

/**
 * 支出に関するビジネスロジックを担当するサービスクラス
 */
@Service
public class ExpenseService {
    @Autowired
    private ExpenseRepository expenseRepository;

    /**
     * 全ての支出を取得します
     * 
     * @return 支出リスト
     */
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    /**
     * 新しい支出を追加します
     * 
     * @param date        日付
     * @param category    カテゴリー
     * @param description 説明
     * @param amount      金額
     * @return 追加した支出エンティティ
     */
    public Expense addExpense(ExpenseForm expenseForm) {
        Expense expense = Expense.create(expenseForm.getDescription(), expenseForm.getAmount(),
                expenseForm.getDate(), expenseForm.getCategory());
        return expenseRepository.save(expense);
    }
}