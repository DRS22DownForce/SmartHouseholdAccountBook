package com.example.backend.controller;

import com.example.backend.application.service.UserApplicationService;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.repository.ExpenseRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.valueobject.CategoryType;
import com.example.backend.valueobject.ExpenseAmount;
import com.example.backend.valueobject.ExpenseDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ExpenseControllerの統合テストクラス
 * 
 * コントローラーからデータベースまでの一貫した動作をテストします。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@SuppressWarnings("null")
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private UserApplicationService userApplicationService;

    private User user;

    @BeforeEach
    void setUp() {
        // 毎回DBをクリア
        expenseRepository.deleteAll(); // 外部キー制約のため、expenseを先に削除
        userRepository.deleteAll();
        
        // テスト用のユーザーを作成し、DBに保存
        String cognitoSub = "cognitoSub";
        String email = "test@example.com";
        user = new User(cognitoSub, email);
        userRepository.save(user);
        when(userApplicationService.getUser()).thenReturn(user);
    }

    @Test
    @DisplayName("家計簿追加API→DB保存→取得APIまで一貫テスト")
    void testAddAndGetExpense() throws Exception {
        // 1. POSTで家計簿データを追加
        String json = """
                {
                  "date": "%s",
                  "category": "食費",
                  "amount": 1000,
                  "description": "テスト"
                }
                """.formatted(LocalDate.now());

        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        // 2. DBに保存されたか確認
        assertThat(expenseRepository.count()).isEqualTo(1);
        Expense saved = expenseRepository.findByUser(user).get(0);
        assertThat(saved.getCategory().getDisplayName()).isEqualTo("食費");

        // 3. GETで家計簿データ一覧を取得
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("食費"));
    }

    @Test
    @DisplayName("家計簿削除API→DB削除まで一貫テスト")
    void testDeleteExpense() throws Exception {
        // 事前にデータを登録
        ExpenseAmount amount = new ExpenseAmount(500);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.TRANSPORT;
        Expense expense = new Expense("バス代", amount, date, category, user);
        expense = expenseRepository.save(expense);

        // 1. DELETEで削除
        mockMvc.perform(delete("/api/expenses/" + expense.getId()))
                .andExpect(status().isNoContent());

        // 2. DBから消えていることを確認
        assertThat(expenseRepository.existsById(expense.getId())).isFalse();
    }

    @Test
    @DisplayName("家計簿更新API→DB更新まで一貫テスト")
    void testUpdateExpense() throws Exception {
        // 事前にデータを登録
        ExpenseAmount amount = new ExpenseAmount(500);
        ExpenseDate date = new ExpenseDate(LocalDate.now());
        CategoryType category = CategoryType.TRANSPORT;
        Expense expense = new Expense("バス代", amount, date, category, user);
        expense = expenseRepository.save(expense);

        // 1. PUTで更新
        mockMvc.perform(put("/api/expenses/" + expense.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "date": "%s",
                            "category": "食費",
                            "amount": 1000,
                            "description": "テスト"
                        }
                        """.formatted(LocalDate.now())))
                .andExpect(status().isOk());

        // 2. DBに更新されたことを確認
        Expense updated = expenseRepository.findById(expense.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getCategory().getDisplayName()).isEqualTo("食費");
        assertThat(updated.getAmount().getAmount()).isEqualTo(1000);
        assertThat(updated.getDescription()).isEqualTo("テスト");
        assertThat(updated.getDate().getDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("月別サマリー取得API→DB集計→レスポンスまで一貫テスト")
    void testGetMonthlySummary() throws Exception {
        // 事前にデータを登録
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 1));
        CategoryType category1 = CategoryType.FOOD;
        Expense expense1 = new Expense("支出1", amount1, date1, category1, user);
        expenseRepository.save(expense1);

        ExpenseAmount amount2 = new ExpenseAmount(2000);
        ExpenseDate date2 = new ExpenseDate(LocalDate.of(2024, 1, 2));
        CategoryType category2 = CategoryType.TRANSPORT;
        Expense expense2 = new Expense("支出2", amount2, date2, category2, user);
        expenseRepository.save(expense2);

        // 1. GETで月別サマリーを取得
        mockMvc.perform(get("/api/expenses/summary")
                .param("month", "2024-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3000))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.byCategory").isArray())
                .andExpect(jsonPath("$.byCategory[0].category").exists())
                .andExpect(jsonPath("$.byCategory[0].amount").exists());
    }

    @Test
    @DisplayName("範囲指定で月別サマリー取得API→DB集計→レスポンスまで一貫テスト")
    void testGetMonthlySummaryRange() throws Exception {
        // 事前にデータを登録（1月と2月）
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 1));
        CategoryType category1 = CategoryType.FOOD;
        Expense expense1 = new Expense("支出1", amount1, date1, category1, user);
        expenseRepository.save(expense1);

        ExpenseAmount amount2 = new ExpenseAmount(2000);
        ExpenseDate date2 = new ExpenseDate(LocalDate.of(2024, 2, 1));
        CategoryType category2 = CategoryType.TRANSPORT;
        Expense expense2 = new Expense("支出2", amount2, date2, category2, user);
        expenseRepository.save(expense2);

        // 1. GETで範囲指定で月別サマリーを取得
        mockMvc.perform(get("/api/expenses/summary/range")
                .param("startMonth", "2024-01")
                .param("endMonth", "2024-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].total").exists())
                .andExpect(jsonPath("$[1].total").exists());
    }

    @Test
    @DisplayName("利用可能な月のリスト取得API→DB取得→レスポンスまで一貫テスト")
    void testGetAvailableMonths() throws Exception {
        // 事前にデータを登録（異なる月）
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 1));
        CategoryType category1 = CategoryType.FOOD;
        Expense expense1 = new Expense("支出1", amount1, date1, category1, user);
        expenseRepository.save(expense1);

        ExpenseAmount amount2 = new ExpenseAmount(2000);
        ExpenseDate date2 = new ExpenseDate(LocalDate.of(2024, 2, 1));
        CategoryType category2 = CategoryType.TRANSPORT;
        Expense expense2 = new Expense("支出2", amount2, date2, category2, user);
        expenseRepository.save(expense2);

        // 1. GETで利用可能な月のリストを取得
        mockMvc.perform(get("/api/expenses/months"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("2024-02"))
                .andExpect(jsonPath("$[1]").value("2024-01"));
    }

    @Test
    @DisplayName("月別フィルタリングで支出取得API→DB取得→レスポンスまで一貫テスト")
    void testGetExpensesByMonth() throws Exception {
        // 事前にデータを登録（異なる月）
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 1));
        CategoryType category1 = CategoryType.FOOD;
        Expense expense1 = new Expense("支出1", amount1, date1, category1, user);
        expenseRepository.save(expense1);

        ExpenseAmount amount2 = new ExpenseAmount(2000);
        ExpenseDate date2 = new ExpenseDate(LocalDate.of(2024, 2, 1));
        CategoryType category2 = CategoryType.TRANSPORT;
        Expense expense2 = new Expense("支出2", amount2, date2, category2, user);
        expenseRepository.save(expense2);

        // 1. GETで月別フィルタリングで支出を取得
        mockMvc.perform(get("/api/expenses")
                .param("month", "2024-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].category").value("食費"))
                .andExpect(jsonPath("$[0].amount").value(1000));
    }
}
