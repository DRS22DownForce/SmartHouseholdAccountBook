package com.example.backend.service;

import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
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
        User user = Objects.requireNonNull(userService.getUser(), "ユーザー情報の取得に失敗しました");
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
        Objects.requireNonNull(expenseRequestDto, "支出リクエストDTOはnullであってはなりません");
        User user = Objects.requireNonNull(userService.getUser(), "ユーザー情報の取得に失敗しました");
        Expense expense = Objects.requireNonNull(
            expenseMapper.toEntity(expenseRequestDto, user),
            "エンティティの生成に失敗しました"
        );
        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toDto(savedExpense);
    }

    /**
     * 支出を削除します
     * 
     * @param id 支出ID
     */
    public void deleteExpense(Long id) {
        Objects.requireNonNull(id, "支出IDはnullであってはなりません");
        expenseRepository.deleteById(id);
    }

    /**
     * 支出を更新します
     * 
     * @param id                支出ID
     * @param expenseRequestDto 更新する支出リクエストDTO
     * @return 更新された支出DTO
     */
    public ExpenseDto updateExpense(Long id, ExpenseRequestDto expenseRequestDto) {
        Objects.requireNonNull(expenseRequestDto, "支出リクエストDTOはnullであってはなりません");
        Objects.requireNonNull(id, "支出IDはnullであってはなりません");
        // 既存の支出を取得
        Expense existingExpense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID " + id + " の支出が見つかりません"));

        // 既存のエンティティを直接更新（IDは変更しない）
        existingExpense.changeDescription(expenseRequestDto.getDescription());
        existingExpense.changeAmount(expenseRequestDto.getAmount());
        existingExpense.changeDate(expenseRequestDto.getDate());
        existingExpense.changeCategory(expenseRequestDto.getCategory());
        // ユーザーは既存のものを保持（通常は変更しない）

        // データベースに保存（UPDATE文が実行される）
        Expense savedExpense = expenseRepository.save(existingExpense);
        return expenseMapper.toDto(savedExpense);
    }

}