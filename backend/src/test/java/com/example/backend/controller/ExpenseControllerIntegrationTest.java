package com.example.backend.controller;

import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.repository.ExpenseRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ExpenseService;
import com.example.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.example.backend.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // application-test.propertiesを使用
@Import(TestSecurityConfig.class) // test時にはセキュリティを無効化する
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    //SpringのBeanをMockに置き換える
    private UserService userService;

    @InjectMocks
    private ExpenseService expenseService;

    private User user;

    @BeforeEach
    void setUp() {
        // 毎回DBをクリア
        expenseRepository.deleteAll();//外部キー制約のため、expenseを先に削除
        userRepository.deleteAll();
        // テスト用のユーザーを作成し、DBに保存
        String cognitoSub = "cognitoSub";
        String email = "test@example.com";
        user = new User(cognitoSub, email);
        userRepository.save(user);
        when(userService.getUser()).thenReturn(user);
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
                .andExpect(status().isOk());

        // 2. DBに保存されたか確認
        assertThat(expenseRepository.count()).isEqualTo(1);
        Expense saved = expenseRepository.findByUser(user).get(0);
        assertThat(saved.getCategory()).isEqualTo("食費");

        // 3. GETで家計簿データ一覧を取得
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("食費"));
    }

    @Test
    @DisplayName("家計簿削除API→DB削除まで一貫テスト")
    void testDeleteExpense() throws Exception {
        // 事前にデータを登録
        Expense expense = new Expense("バス代", 500, LocalDate.now(), "交通費");
        expense.setUser(user);
        expense = expenseRepository.save(expense);

        // 1. DELETEで削除
        mockMvc.perform(delete("/api/expenses/" + expense.getId()))
                .andExpect(status().isNoContent());

        // 2. DBから消えていることを確認
        assertThat(expenseRepository.existsById(expense.getId())).isFalse();
    }
}