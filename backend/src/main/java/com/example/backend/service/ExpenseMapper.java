package com.example.backend.service;

import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {
    // Entity → DTO
    public ExpenseDto toDto(Expense expense) {
        if (expense == null)
            return null;
        ExpenseDto dto = new ExpenseDto();
        dto.setId(expense.getId());
        dto.setDescription(expense.getDescription());
        dto.setAmount(expense.getAmount());
        dto.setDate(expense.getDate());
        dto.setCategory(expense.getCategory());
        return dto;
    }

    // DTO → Entity
    public Expense toEntity(ExpenseDto dto, User user) {
        if (dto == null)
            return null;
        return new Expense(
                dto.getDescription(),
                dto.getAmount(),
                dto.getDate(),
                dto.getCategory(),
                user);
    }

    // RequestDto → Entity
    public Expense toEntity(ExpenseRequestDto dto, User user) {
        if (dto == null)
            return null;
        return new Expense(
                dto.getDescription(),
                dto.getAmount(),
                dto.getDate(),
                dto.getCategory(),
                user);
    }
}