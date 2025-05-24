package com.example.backend.repository;

import com.example.backend.entity.Expense;
import com.example.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // 基本的なCRUD操作はJpaRepositoryから継承されます
    // Userを指定して支出を取得
    List<Expense> findByUser(User user);
} 