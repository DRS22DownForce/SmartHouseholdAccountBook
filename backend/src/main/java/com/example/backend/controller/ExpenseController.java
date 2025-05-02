package com.example.backend.controller;

import com.example.backend.entity.Expense;
import com.example.backend.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import com.example.backend.dto.ExpenseForm;
import java.util.List;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping
    public String listExpenses(Model model) {
        // Serviceを使って全ての支出を取得
        List<Expense> expenses = expenseService.getAllExpenses();
        model.addAttribute("expenses", expenses);
        return "expenses/list";
    }

    /**
     * 新規支出を追加するエンドポイント
     * 
     * @param date               支出日
     * @param category           カテゴリー
     * @param description        説明
     * @param amount             金額
     * @param redirectAttributes リダイレクト時の属性
     * @return リダイレクト先
     */
    @PostMapping("/add")
    public String addExpense(
            @Valid @ModelAttribute ExpenseForm expenseForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            redirectAttributes.addFlashAttribute("InputErrorMessage", "入力内容に誤りがあります。");
            redirectAttributes.addFlashAttribute("InputErrorList", errorMessages);
            return "redirect:/expenses";
        }
        try {
            // Serviceを使って支出を追加
            expenseService.addExpense(expenseForm);

            // 成功メッセージの設定
            redirectAttributes.addFlashAttribute("successMessage", "支出を追加しました。");
        } catch (Exception e) {
            // エラーメッセージの設定
            redirectAttributes.addFlashAttribute("errorMessage", "支出の追加に失敗しました。");
        }

        // 一覧ページにリダイレクト
        return "redirect:/expenses";
    }
}