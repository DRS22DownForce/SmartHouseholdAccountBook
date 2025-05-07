package com.example.backend.service;

import com.example.backend.entity.Expense;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseMapperTest {

    private final ExpenseMapper mapper = new ExpenseMapper();

    @Test
    void toDto_正常系() {
        Expense expense = new Expense("説明", 1234, LocalDate.of(2024, 6, 1), "食費");
        ExpenseDto dto = mapper.toDto(expense);

        assertNotNull(dto);
        assertEquals("説明", dto.getDescription());
        assertEquals(1234, dto.getAmount());
        assertEquals(LocalDate.of(2024, 6, 1), dto.getDate());
        assertEquals("食費", dto.getCategory());
    }

    @Test
    void toDto_nullならnull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toEntity_ExpenseDtoからEntityへ変換() {
        ExpenseDto dto = new ExpenseDto();
        dto.setDescription("テスト");
        dto.setAmount(500);
        dto.setDate(LocalDate.of(2024, 5, 1));
        dto.setCategory("交通費");

        Expense entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("テスト", entity.getDescription());
        assertEquals(500, entity.getAmount());
        assertEquals(LocalDate.of(2024, 5, 1), entity.getDate());
        assertEquals("交通費", entity.getCategory());
    }

    @Test
    void toEntity_ExpenseDto_nullならnull() {
        assertNull(mapper.toEntity((ExpenseDto) null));
    }

    @Test
    void toEntity_ExpenseRequestDtoからEntityへ変換() {
        ExpenseRequestDto req = new ExpenseRequestDto();
        req.setDescription("リクエスト");
        req.setAmount(999);
        req.setDate(LocalDate.of(2024, 4, 1));
        req.setCategory("日用品");

        Expense entity = mapper.toEntity(req);

        assertNotNull(entity);
        assertEquals("リクエスト", entity.getDescription());
        assertEquals(999, entity.getAmount());
        assertEquals(LocalDate.of(2024, 4, 1), entity.getDate());
        assertEquals("日用品", entity.getCategory());
    }

    @Test
    void toEntity_ExpenseRequestDto_nullならnull() {
        assertNull(mapper.toEntity((ExpenseRequestDto) null));
    }
}