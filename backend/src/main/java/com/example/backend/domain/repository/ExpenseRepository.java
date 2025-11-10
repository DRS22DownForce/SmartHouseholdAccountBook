package com.example.backend.domain.repository;

import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 支出エンティティのリポジトリインターフェース
 */
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    /**
     * ユーザーを指定して支出を取得
     * 
     * @param user ユーザーエンティティ
     * @return 該当ユーザーの支出リスト
     */
    List<Expense> findByUser(User user);
}

