package com.example.backend.integrationTest;

import com.example.backend.config.TestJwtAuthenticationFilter;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import com.example.backend.generated.model.MonthlySummaryDto;
import com.example.backend.repository.ExpenseRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.valueobject.CategoryType;
import com.example.backend.valueobject.ExpenseAmount;
import com.example.backend.valueobject.ExpenseDate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ExpenseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        // @SpringBootTest では各テストはロールバックは機能しないため、テスト前にデータをクリアする。
        expenseRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(new User(TestJwtAuthenticationFilter.TEST_SUB, TestJwtAuthenticationFilter.TEST_EMAIL));
        // キャッシュをクリアする。
        var usersCache = cacheManager.getCache("users");
        if (usersCache != null) {
            usersCache.clear();
        }
    }

    private Expense saveExpense(String description, int amount, LocalDate date, CategoryType category) {
        return expenseRepository.save(new Expense(
                description,
                new ExpenseAmount(amount),
                new ExpenseDate(date),
                category,
                user));
    }

    private static ExpenseRequestDto requestDto(LocalDate date, String category, int amount, String description) {
        ExpenseRequestDto dto = new ExpenseRequestDto();
        dto.setDate(date);
        dto.setCategory(category);
        dto.setAmount(amount);
        dto.setDescription(description);
        return dto;
    }

    @Nested
    @DisplayName("POST /api/expenses")
    class ApiExpensesPost {

        @Test
        @DisplayName("追加した支出が一覧で取得できる")
        void returnsCreatedAndExpenseInList() throws Exception {
            // given
            ExpenseRequestDto request = requestDto(LocalDate.now(), "食費", 1000, "テスト");

            // when
            mockMvc.perform(post("/api/expenses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // then
            var expenses = expenseRepository.findAll();
            assertThat(expenses).hasSize(1);
            var saved = expenses.get(0);
            assertThat(saved.getDescription()).isEqualTo("テスト");
            assertThat(saved.getAmount().getAmount()).isEqualTo(1000);
            assertThat(saved.getDate().getDate()).isEqualTo(request.getDate());
            assertThat(saved.getCategory().getDisplayName()).isEqualTo("食費");
            assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("不正JSONのとき400を返しDBに保存しない")
        void returnsBadRequestWhenJsonInvalid() throws Exception {
            // when
            mockMvc.perform(post("/api/expenses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ invalid json }"))
                    .andExpect(status().isBadRequest());

            // then
            assertThat(expenseRepository.count()).isZero();
        }
    }

    @Nested
    @DisplayName("DELETE /api/expenses/{id}")
    class ApiExpensesIdDelete {

        @Test
        @DisplayName("指定IDの支出を削除する")
        void deletesExpense() throws Exception {
            // given
            Expense expense = saveExpense("バス代", 500, LocalDate.now(), CategoryType.TRANSPORT);

            // when
            mockMvc.perform(delete("/api/expenses/" + expense.getId()))
                    .andExpect(status().isNoContent());

            // then
            assertThat(expenseRepository.existsById(expense.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("PUT /api/expenses/{id}")
    class ApiExpensesIdPut {

        @Test
        @DisplayName("支出更新がDBに反映される")
        void updatesExpense() throws Exception {
            // given
            Expense expense = saveExpense("バス代", 500, LocalDate.now(), CategoryType.TRANSPORT);
            ExpenseRequestDto request = requestDto(LocalDate.now(), "食費", 1000, "更新テスト");

            // when
            mockMvc.perform(put("/api/expenses/" + expense.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // then
            Expense updated = expenseRepository.findById(expense.getId()).orElseThrow();
            assertThat(updated.getCategory().getDisplayName()).isEqualTo("食費");
            assertThat(updated.getAmount().getAmount()).isEqualTo(1000);
            assertThat(updated.getDescription()).isEqualTo("更新テスト");
        }
    }

    @Nested
    @DisplayName("GET /api/expenses/summary")
    class ApiExpensesSummaryGet {

        @Test
        @DisplayName("月次サマリーの主要項目を返す")
        void returnsMonthlySummary() throws Exception {
            // given
            saveExpense("支出1", 1000, LocalDate.of(2024, 1, 1), CategoryType.FOOD);
            saveExpense("支出2", 2000, LocalDate.of(2024, 1, 2), CategoryType.TRANSPORT);

            // when
            String responseBody = mockMvc.perform(get("/api/expenses/summary").param("month", "2024-01"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            MonthlySummaryDto dto = objectMapper.readValue(responseBody, MonthlySummaryDto.class);

            // then
            assertThat(dto.getTotal()).isEqualTo(3000);
            assertThat(dto.getCount()).isEqualTo(2);
            assertThat(dto.getByCategory()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("GET /api/expenses/summary/range")
    class ApiExpensesSummaryRangeGet {

        @Test
        @DisplayName("範囲集計を配列で返す")
        void returnsRangeSummaries() throws Exception {
            // given
            saveExpense("1月支出", 1000, LocalDate.of(2024, 1, 1), CategoryType.FOOD);
            saveExpense("2月支出", 2000, LocalDate.of(2024, 2, 1), CategoryType.TRANSPORT);

            // when
            String responseBody = mockMvc.perform(get("/api/expenses/summary/range")
                            .param("startMonth", "2024-01")
                            .param("endMonth", "2024-02"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            List<MonthlySummaryDto> list = objectMapper.readValue(responseBody, new TypeReference<List<MonthlySummaryDto>>() {});

            // then
            assertThat(list).hasSize(2);
            assertThat(list.get(0).getTotal()).isEqualTo(1000);
            assertThat(list.get(1).getTotal()).isEqualTo(2000);
        }
    }

    @Nested
    @DisplayName("GET /api/expenses/months")
    class ApiExpensesMonthsGet {

        @Test
        @DisplayName("利用可能な月を新しい順で返す")
        void returnsAvailableMonthsSortedDesc() throws Exception {
            // given
            saveExpense("1月支出", 1000, LocalDate.of(2024, 1, 1), CategoryType.FOOD);
            saveExpense("2月支出", 2000, LocalDate.of(2024, 2, 1), CategoryType.TRANSPORT);

            // when
            String responseBody = mockMvc.perform(get("/api/expenses/months"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            List<String> months = objectMapper.readValue(responseBody, new TypeReference<List<String>>() {});

            // then
            assertThat(months).containsExactly("2024-02", "2024-01");
        }
    }

    @Nested
    @DisplayName("GET /api/expenses")
    class ApiExpensesGet {

        @Test
        @DisplayName("month指定でその月の支出だけ返す")
        void returnsExpensesFilteredByMonth() throws Exception {
            // given
            saveExpense("1月支出", 1000, LocalDate.of(2024, 1, 1), CategoryType.FOOD);
            saveExpense("2月支出", 2000, LocalDate.of(2024, 2, 1), CategoryType.TRANSPORT);

            // when
            String responseBody = mockMvc.perform(get("/api/expenses").param("month", "2024-01"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            List<ExpenseDto> list = objectMapper.readValue(responseBody, new TypeReference<List<ExpenseDto>>() {});

            // then
            assertThat(list).hasSize(1);
            assertThat(list.get(0).getCategory()).isEqualTo("食費");
            assertThat(list.get(0).getAmount()).isEqualTo(1000);
        }
    }
}
