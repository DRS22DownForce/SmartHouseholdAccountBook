package com.example.backend.controller;

import com.example.backend.entity.Expense;
import com.example.backend.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @GetMapping
    public String listExpenses(Model model) {
        // データベースから全ての支出を取得
        List<Expense> expenses = expenseRepository.findAll();
        
        // モデルに支出リストを追加
        model.addAttribute("expenses", expenses);
        
        // Thymeleafテンプレートの名前を返す
        return "expenses/list";
    }
} 