package com.example.backend.application.service;

import com.example.backend.application.mapper.ExpenseMapper;
import com.example.backend.domain.repository.ExpenseRepository;
import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 支出に関するアプリケーションサービス
 * このクラスは支出の追加、取得、更新、削除というユースケースを実装します。
 */
@Service
@Transactional
public class ExpenseApplicationService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final UserApplicationService userApplicationService;

    /**
     * コンストラクタ
     * 
     * @param expenseRepository 支出リポジトリ
     * @param expenseMapper 支出マッパー
     * @param userApplicationService ユーザーアプリケーションサービス
     */
    public ExpenseApplicationService(
            ExpenseRepository expenseRepository,
            ExpenseMapper expenseMapper,
            UserApplicationService userApplicationService) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
        this.userApplicationService = userApplicationService;
    }

    /**
     * 全ての支出を取得するユースケース
     * 
     * 現在のユーザーの支出を取得し、DTOに変換して返します。
     * 
     * @return 支出DTOリスト
     */
    @Transactional(readOnly = true)
    public List<ExpenseDto> getExpenses() {
        // 1. 現在のユーザーを取得
        User user = Objects.requireNonNull(
            userApplicationService.getUser(),
            "ユーザー情報の取得に失敗しました"
        );
        
        // 2. ユーザーの支出を取得
        List<Expense> expenses = expenseRepository.findByUser(user);
        
        // 3. DTOに変換して返す
        return expenses.stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 新しい支出を追加するユースケース
     * 
     * リクエストDTOからエンティティを作成し、保存してDTOに変換して返します。
     * 
     * @param expenseRequestDto 支出リクエストDTO
     * @return 追加した支出DTO
     */
    public ExpenseDto addExpense(ExpenseRequestDto expenseRequestDto) {
        // 1. 入力検証
        Objects.requireNonNull(expenseRequestDto, "支出リクエストDTOはnullであってはなりません");
        
        // 2. 現在のユーザーを取得
        User user = Objects.requireNonNull(
            userApplicationService.getUser(),
            "ユーザー情報の取得に失敗しました"
        );
        
        // 3. DTOからエンティティへ変換（マッパーが値オブジェクトを作成）
        Expense expense = Objects.requireNonNull(
            expenseMapper.toEntity(expenseRequestDto, user),
            "エンティティの生成に失敗しました"
        );
        
        // 4. データベースに保存
        Expense savedExpense = expenseRepository.save(expense);
        
        // 5. DTOに変換して返す
        return expenseMapper.toDto(savedExpense);
    }

    /**
     * 支出を削除するユースケース
     * 
     * 指定されたIDの支出を削除します。
     * 
     * @param id 支出ID
     */
    public void deleteExpense(Long id) {
        // 1. 入力検証
        Objects.requireNonNull(id, "支出IDはnullであってはなりません");
        
        // 2. データベースから削除
        expenseRepository.deleteById(id);
    }

    /**
     * 支出を更新するユースケース
     * 
     * 既存の支出を取得し、リクエストDTOの内容で更新します。
     * 
     * @param id 支出ID
     * @param expenseRequestDto 更新する支出リクエストDTO
     * @return 更新された支出DTO
     */
    public ExpenseDto updateExpense(Long id, ExpenseRequestDto expenseRequestDto) {
        Objects.requireNonNull(expenseRequestDto, "支出リクエストDTOはnullであってはなりません");
        Objects.requireNonNull(id, "支出IDはnullであってはなりません");
        
        Expense existingExpense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID " + id + " の支出が見つかりません"));

        ExpenseMapper.ValueObjectsForUpdate valueObjectsForUpdate = 
            expenseMapper.toValueObjectsForUpdate(expenseRequestDto);

        existingExpense.update(
            expenseRequestDto.getDescription(),
            valueObjectsForUpdate.amount(),
            valueObjectsForUpdate.date(),
            valueObjectsForUpdate.category()
        );

        Expense savedExpense = expenseRepository.save(existingExpense);
        return expenseMapper.toDto(savedExpense);
    }
}

