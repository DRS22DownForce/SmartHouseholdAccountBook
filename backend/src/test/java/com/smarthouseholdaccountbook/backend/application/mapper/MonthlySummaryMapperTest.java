package com.smarthouseholdaccountbook.backend.application.mapper;

import com.smarthouseholdaccountbook.backend.entity.Expense;
import com.smarthouseholdaccountbook.backend.entity.User;
import com.smarthouseholdaccountbook.backend.generated.model.MonthlySummaryDto;
import com.smarthouseholdaccountbook.backend.valueobject.CategoryType;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseAmount;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseDate;
import com.smarthouseholdaccountbook.backend.valueobject.MonthlySummary;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlySummaryMapperTest {

    private final MonthlySummaryMapper mapper = new MonthlySummaryMapper();

    @Test
    void 合計件数とカテゴリ別集計を変換する() {
        Expense expense = new Expense(
                "昼食",
                new ExpenseAmount(1200),
                new ExpenseDate(LocalDate.of(2024, 1, 10)),
                CategoryType.FOOD,
                new User("summary-mapper-test-user"));
        MonthlySummary summary =
                MonthlySummary.createMonthlySummaryFromExpenses(List.of(expense), "2024-01");

        MonthlySummaryDto dto = mapper.toDto(summary);

        assertThat(dto.getTotal()).isEqualTo(1200);
        assertThat(dto.getCount()).isEqualTo(1);
        assertThat(dto.getByCategory()).singleElement().satisfies(category -> {
            assertThat(category.getCategory()).isEqualTo("食費");
            assertThat(category.getAmount()).isEqualTo(1200);
        });
    }

    @Test
    void nullならnullを返す() {
        assertThat(mapper.toDto(null)).isNull();
    }
}
