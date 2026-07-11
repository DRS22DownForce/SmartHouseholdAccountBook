package com.smarthouseholdaccountbook.backend.controller;

import com.smarthouseholdaccountbook.backend.application.mapper.ExpenseMapper;
import com.smarthouseholdaccountbook.backend.application.service.ExpenseApplicationService;
import com.smarthouseholdaccountbook.backend.entity.Expense;
import com.smarthouseholdaccountbook.backend.entity.ExpenseUpdate;
import com.smarthouseholdaccountbook.backend.entity.User;
import com.smarthouseholdaccountbook.backend.generated.model.ExpenseDto;
import com.smarthouseholdaccountbook.backend.generated.model.ExpensePageDto;
import com.smarthouseholdaccountbook.backend.generated.model.ExpenseRequestDto;
import com.smarthouseholdaccountbook.backend.valueobject.CategoryType;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseAmount;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 支出 CRUD Controller の単体テスト。
 */
@ExtendWith(MockitoExtension.class)
class ExpenseControllerTest {

    @Mock
    private ExpenseApplicationService expenseApplicationService;

    @Mock
    private ExpenseMapper expenseMapper;

    @InjectMocks
    private ExpenseController controller;

    @Test
    void 指定月の支出ページを返す() {
        Expense expense = createExpense("電車", 1200, CategoryType.TRANSPORT);
        when(expenseApplicationService.getExpensesByMonth(
                YearMonth.of(2024, 1), PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(expense), PageRequest.of(0, 20), 1));
        ExpenseDto expenseDto = new ExpenseDto();
        when(expenseMapper.toDto(expense)).thenReturn(expenseDto);

        ResponseEntity<ExpensePageDto> response =
                controller.apiExpensesGet("2024-01", 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).containsExactly(expenseDto);
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    void 存在しない月は拒否する() {
        assertThatThrownBy(() -> controller.apiExpensesGet("2024-99", 0, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("月の形式が不正です");
    }

    @Test
    void 支出を追加すると201を返す() {
        ExpenseRequestDto request = new ExpenseRequestDto();
        ExpenseUpdate update = new ExpenseUpdate(
                "食事", new ExpenseAmount(1000),
                new ExpenseDate(LocalDate.of(2024, 1, 1)), CategoryType.FOOD);
        Expense expense = createExpense("食事", 1000, CategoryType.FOOD);
        ExpenseDto dto = new ExpenseDto();
        when(expenseMapper.toExpenseUpdate(request)).thenReturn(update);
        when(expenseApplicationService.addExpense(update)).thenReturn(expense);
        when(expenseMapper.toDto(expense)).thenReturn(dto);

        ResponseEntity<ExpenseDto> response = controller.apiExpensesPost(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void 支出を更新する() {
        ExpenseRequestDto request = new ExpenseRequestDto();
        ExpenseUpdate update = new ExpenseUpdate(
                "更新", new ExpenseAmount(2000),
                new ExpenseDate(LocalDate.of(2024, 1, 2)), CategoryType.OTHER);
        Expense expense = createExpense("更新", 2000, CategoryType.OTHER);
        ExpenseDto dto = new ExpenseDto();
        when(expenseMapper.toExpenseUpdate(request)).thenReturn(update);
        when(expenseApplicationService.updateExpense(3L, update)).thenReturn(expense);
        when(expenseMapper.toDto(expense)).thenReturn(dto);

        assertThat(controller.apiExpensesIdPut(3L, request).getBody()).isSameAs(dto);
    }

    @Test
    void 支出を削除すると204を返す() {
        ResponseEntity<Void> response = controller.apiExpensesIdDelete(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(expenseApplicationService).deleteExpense(5L);
    }

    private static Expense createExpense(
            String description,
            int amount,
            CategoryType category) {
        return new Expense(
                description,
                new ExpenseAmount(amount),
                new ExpenseDate(LocalDate.of(2024, 1, 1)),
                category,
                new User("controller-test-user"));
    }
}
