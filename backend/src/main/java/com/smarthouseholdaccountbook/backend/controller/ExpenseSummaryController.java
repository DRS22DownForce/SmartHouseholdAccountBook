package com.smarthouseholdaccountbook.backend.controller;

import com.smarthouseholdaccountbook.backend.application.mapper.MonthlySummaryMapper;
import com.smarthouseholdaccountbook.backend.application.service.ExpenseApplicationService;
import com.smarthouseholdaccountbook.backend.controller.support.MonthParameterParser;
import com.smarthouseholdaccountbook.backend.generated.api.ExpenseSummaryApi;
import com.smarthouseholdaccountbook.backend.generated.model.MonthlySummaryDto;
import com.smarthouseholdaccountbook.backend.valueobject.MonthlySummary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 支出の月別集計と利用可能月の取得を担当する REST API コントローラー。
 */
@RestController
public class ExpenseSummaryController implements ExpenseSummaryApi {

    private final ExpenseApplicationService expenseApplicationService;
    private final MonthlySummaryMapper monthlySummaryMapper;

    public ExpenseSummaryController(
            ExpenseApplicationService expenseApplicationService,
            MonthlySummaryMapper monthlySummaryMapper) {
        this.expenseApplicationService = expenseApplicationService;
        this.monthlySummaryMapper = monthlySummaryMapper;
    }

    @Override
    public ResponseEntity<MonthlySummaryDto> apiExpensesSummaryGet(String month) {
        MonthlySummary summary =
                expenseApplicationService.getMonthlySummary(MonthParameterParser.parse(month));
        return ResponseEntity.ok(monthlySummaryMapper.toDto(summary));
    }

    @Override
    public ResponseEntity<List<MonthlySummaryDto>> apiExpensesSummaryRangeGet(
            String startMonth,
            String endMonth) {
        List<MonthlySummaryDto> summaries = expenseApplicationService.getMonthlySummaryRange(
                        MonthParameterParser.parse(startMonth),
                        MonthParameterParser.parse(endMonth))
                .stream()
                .map(monthlySummaryMapper::toDto)
                .toList();
        return ResponseEntity.ok(summaries);
    }

    @Override
    public ResponseEntity<List<String>> apiExpensesMonthsGet() {
        return ResponseEntity.ok(expenseApplicationService.getAvailableMonths());
    }
}
