package com.smarthouseholdaccountbook.backend.controller;

import com.smarthouseholdaccountbook.backend.application.mapper.MonthlySummaryMapper;
import com.smarthouseholdaccountbook.backend.application.service.ExpenseApplicationService;
import com.smarthouseholdaccountbook.backend.generated.model.MonthlySummaryDto;
import com.smarthouseholdaccountbook.backend.valueobject.MonthlySummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 支出集計 Controller の単体テスト。
 */
@ExtendWith(MockitoExtension.class)
class ExpenseSummaryControllerTest {

    @Mock
    private ExpenseApplicationService expenseApplicationService;

    @Mock
    private MonthlySummaryMapper monthlySummaryMapper;

    @InjectMocks
    private ExpenseSummaryController controller;

    @Test
    void 指定月のサマリーを返す() {
        MonthlySummary summary = emptySummary("2024-04");
        MonthlySummaryDto dto = new MonthlySummaryDto();
        when(expenseApplicationService.getMonthlySummary(YearMonth.of(2024, 4)))
                .thenReturn(summary);
        when(monthlySummaryMapper.toDto(summary)).thenReturn(dto);

        assertThat(controller.apiExpensesSummaryGet("2024-04").getBody()).isSameAs(dto);
    }

    @Test
    void 範囲内のサマリーを返す() {
        MonthlySummary january = emptySummary("2024-01");
        MonthlySummary february = emptySummary("2024-02");
        MonthlySummaryDto januaryDto = new MonthlySummaryDto();
        MonthlySummaryDto februaryDto = new MonthlySummaryDto();
        when(expenseApplicationService.getMonthlySummaryRange(
                YearMonth.of(2024, 1), YearMonth.of(2024, 2)))
                .thenReturn(List.of(january, february));
        when(monthlySummaryMapper.toDto(january)).thenReturn(januaryDto);
        when(monthlySummaryMapper.toDto(february)).thenReturn(februaryDto);

        ResponseEntity<List<MonthlySummaryDto>> response =
                controller.apiExpensesSummaryRangeGet("2024-01", "2024-02");

        assertThat(response.getBody()).containsExactly(januaryDto, februaryDto);
    }

    @Test
    void 利用可能月を返す() {
        List<String> months = List.of("2024-02", "2024-01");
        when(expenseApplicationService.getAvailableMonths()).thenReturn(months);

        assertThat(controller.apiExpensesMonthsGet().getBody()).isSameAs(months);
        verify(expenseApplicationService).getAvailableMonths();
    }

    @Test
    void 不正な範囲月は拒否する() {
        assertThatThrownBy(() ->
                controller.apiExpensesSummaryRangeGet("2024-01", "2024-13"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static MonthlySummary emptySummary(String month) {
        return MonthlySummary.createMonthlySummaryFromExpenses(List.of(), month);
    }
}
