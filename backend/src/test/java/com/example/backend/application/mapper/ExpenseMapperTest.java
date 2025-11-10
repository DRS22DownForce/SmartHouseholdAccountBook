package com.example.backend.application.mapper;

import com.example.backend.domain.valueobject.Category;
import com.example.backend.domain.valueobject.ExpenseAmount;
import com.example.backend.domain.valueobject.ExpenseDate;
import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExpenseMapperのテストクラス
 * 
 * エンティティとDTO間の変換をテストします。
 */
class ExpenseMapperTest {

    private final ExpenseMapper mapper = new ExpenseMapper();

    @Test
    void toDto_正常系() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1234);
        ExpenseDate date = new ExpenseDate(LocalDate.of(2024, 6, 1));
        Category category = new Category("食費");
        Expense expense = new Expense("説明", amount, date, category, user);
        
        // テスト実行
        ExpenseDto dto = mapper.toDto(expense);

        // 検証
        assertNotNull(dto);
        assertEquals("説明", dto.getDescription());
        assertEquals(1234, dto.getAmount());
        assertEquals(LocalDate.of(2024, 6, 1), dto.getDate());
        assertEquals("食費", dto.getCategory());
    }

    @Test
    void toDto_nullならnull() {
        // テスト実行と検証
        assertNull(mapper.toDto(null));
    }

    @Test
    void toEntity_ExpenseDtoからEntityへ変換() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseDto dto = new ExpenseDto();
        dto.setDescription("テスト");
        dto.setAmount(500);
        dto.setDate(LocalDate.of(2024, 5, 1));
        dto.setCategory("交通費");

        // テスト実行
        Expense entity = mapper.toEntity(dto, user);

        // 検証
        assertNotNull(entity);
        assertEquals("テスト", entity.getDescription());
        assertEquals(500, entity.getAmountValue());
        assertEquals(LocalDate.of(2024, 5, 1), entity.getDateValue());
        assertEquals("交通費", entity.getCategoryValue());
    }

    @Test
    void toEntity_ExpenseDto_nullならnull() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        
        // テスト実行と検証
        assertNull(mapper.toEntity((ExpenseDto) null, user));
    }

    @Test
    void toEntity_ExpenseRequestDtoからEntityへ変換() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseRequestDto req = new ExpenseRequestDto();
        req.setDescription("リクエスト");
        req.setAmount(999);
        req.setDate(LocalDate.of(2024, 4, 1));
        req.setCategory("日用品");

        // テスト実行
        Expense entity = mapper.toEntity(req, user);

        // 検証
        assertNotNull(entity);
        assertEquals("リクエスト", entity.getDescription());
        assertEquals(999, entity.getAmountValue());
        assertEquals(LocalDate.of(2024, 4, 1), entity.getDateValue());
        assertEquals("日用品", entity.getCategoryValue());
    }

    @Test
    void toEntity_ExpenseRequestDto_nullならnull() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        
        // テスト実行と検証
        assertNull(mapper.toEntity((ExpenseRequestDto) null, user));
    }
}

