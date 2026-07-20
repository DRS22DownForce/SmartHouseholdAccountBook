package com.smarthouseholdaccountbook.backend.controller;

import com.smarthouseholdaccountbook.backend.application.mapper.MonthlyReportMapper;
import com.smarthouseholdaccountbook.backend.application.service.MonthlyReportService;
import com.smarthouseholdaccountbook.backend.controller.support.MonthParameterParser;
import com.smarthouseholdaccountbook.backend.entity.MonthlyReport;
import com.smarthouseholdaccountbook.backend.exception.AiServiceException;
import com.smarthouseholdaccountbook.backend.generated.api.MonthlyReportApi;
import com.smarthouseholdaccountbook.backend.generated.model.MonthlyReportResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * 月次 AI レポートの取得・生成を担当する REST API コントローラー。
 */
@RestController
public class MonthlyReportController implements MonthlyReportApi {

    private final MonthlyReportService monthlyReportService;
    private final MonthlyReportMapper monthlyReportMapper;

    public MonthlyReportController(
            MonthlyReportService monthlyReportService,
            MonthlyReportMapper monthlyReportMapper) {
        this.monthlyReportService = monthlyReportService;
        this.monthlyReportMapper = monthlyReportMapper;
    }

    @Override
    public ResponseEntity<MonthlyReportResponse> apiExpensesReportGet(
            String month,
            Boolean generate) {
        // 正規表現だけでは検出できない「2024-99」なども API 境界で拒否する。
        String validatedMonth = MonthParameterParser.parse(month).toString();

        if (!Boolean.TRUE.equals(generate)) {
            return monthlyReportService.generateReport(validatedMonth, false)
                    .map(monthlyReportMapper::toDto)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.noContent().build());
        }

        MonthlyReport report = monthlyReportService.generateReport(validatedMonth, true)
                .orElseThrow(() -> new AiServiceException("月次レポートの生成に失敗しました"));
        return ResponseEntity.ok(monthlyReportMapper.toDto(report));
    }
}
