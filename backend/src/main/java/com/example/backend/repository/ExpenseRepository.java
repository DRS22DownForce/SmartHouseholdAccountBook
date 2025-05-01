package com.example.backend.repository;

import com.example.backend.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // 基本的なCRUD操作はJpaRepositoryから継承されます
} 