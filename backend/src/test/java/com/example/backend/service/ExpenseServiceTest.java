package com.example.backend.service;

import com.example.backend.entity.Expense;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import com.example.backend.repository.ExpenseRepository;
import com.example.backend.service.ExpenseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;

    //ExpenseMapperはテスト対象クラスではないのでMock化
    @Mock
    private ExpenseMapper expenseMapper;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void getAllExpenses_リポジトリに2件あれば2件返す() {
        Expense expense1 = new Expense("支出1", 1000, LocalDate.of(2024, 1, 1), "食費");
        Expense expense2 = new Expense("支出2", 2000, LocalDate.of(2024, 1, 2), "交通費");
        ExpenseDto dto1 = new ExpenseDto();
        dto1.setDescription("支出1");
        dto1.setAmount(1000);
        dto1.setDate(LocalDate.of(2024, 1, 1));
        dto1.setCategory("食費");
        ExpenseDto dto2 = new ExpenseDto();
        dto2.setDescription("支出2");
        dto2.setAmount(2000);
        dto2.setDate(LocalDate.of(2024, 1, 2));
        dto2.setCategory("交通費");

        when(expenseRepository.findAll()).thenReturn(Arrays.asList(expense1, expense2));
        when(expenseMapper.toDto(expense1)).thenReturn(dto1);
        when(expenseMapper.toDto(expense2)).thenReturn(dto2);

        List<ExpenseDto> result = expenseService.getAllExpenses();

        assertEquals(2, result.size());
        assertEquals("支出1", result.get(0).getDescription());
        assertEquals("支出2", result.get(1).getDescription());
    }

    @Test
    void addExpense_支出追加() {
        ExpenseRequestDto requestDto = new ExpenseRequestDto();
        requestDto.setDescription("テスト支出");
        requestDto.setAmount(1000);
        requestDto.setDate(LocalDate.of(2024, 1, 1));
        requestDto.setCategory("食費");

        Expense expense = new Expense("テスト支出", 1000, LocalDate.of(2024, 1, 1), "食費");
        when(expenseMapper.toEntity(requestDto)).thenReturn(expense);

        ExpenseDto expenseDto = new ExpenseDto();
        expenseDto.setDescription("テスト支出");
        expenseDto.setAmount(1000);
        expenseDto.setDate(LocalDate.of(2024, 1, 1));
        expenseDto.setCategory("食費");
        when(expenseMapper.toDto(expense)).thenReturn(expenseDto);
        
        when(expenseRepository.save(expense)).thenReturn(expense);

        ExpenseDto result = expenseService.addExpense(requestDto);

        assertNotNull(result);
        assertEquals("テスト支出", result.getDescription());
        assertEquals(1000, result.getAmount());
        assertEquals(LocalDate.of(2024, 1, 1), result.getDate());
        assertEquals("食費", result.getCategory());
        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    void addExpense_リクエストがnullなら例外() {
        assertThrows(NullPointerException.class, () -> expenseService.addExpense(null));
    }
}