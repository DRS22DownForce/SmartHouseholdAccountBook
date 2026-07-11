package com.smarthouseholdaccountbook.backend.application.mapper;

import com.smarthouseholdaccountbook.backend.application.service.CsvExpenseService;
import com.smarthouseholdaccountbook.backend.application.service.csv.model.CsvParseError;
import com.smarthouseholdaccountbook.backend.entity.Expense;
import com.smarthouseholdaccountbook.backend.entity.User;
import com.smarthouseholdaccountbook.backend.generated.model.CsvUploadResponseDto;
import com.smarthouseholdaccountbook.backend.generated.model.ExpenseDto;
import com.smarthouseholdaccountbook.backend.valueobject.CategoryType;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseAmount;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseDate;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExpenseMapperのテストクラス
 *
 * 本番で使用している変換（Entity/値オブジェクト → DTO）を検証します。
 */
class ExpenseMapperTest {

    private final ExpenseMapper mapper = new ExpenseMapper();

    @Test
    void toDto_正常系() {
        // テストデータの準備
        User user = new User("cognitoSub");
        ExpenseAmount amount = new ExpenseAmount(1234);
        ExpenseDate date = new ExpenseDate(LocalDate.of(2024, 6, 1));
        CategoryType category = CategoryType.FOOD;
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
        // Expense型のnullを明示的に指定（MonthlySummaryのtoDtoメソッドとの曖昧さを回避）
        assertNull(mapper.toDto((Expense) null));
    }

    @Test
    void toDto_CsvUploadResult_skippedCountを含めて変換する() {
        CsvExpenseService.CsvUploadResult result = new CsvExpenseService.CsvUploadResult(
                3, 1, 20, List.of(new CsvParseError(5, "line", "msg")));

        CsvUploadResponseDto dto = mapper.toDto(result);

        assertNotNull(dto);
        assertEquals(3, dto.getSuccessCount());
        assertEquals(20, dto.getSkippedCount());
        assertEquals(1, dto.getErrorCount());
        assertEquals(1, dto.getErrors().size());
    }
}

