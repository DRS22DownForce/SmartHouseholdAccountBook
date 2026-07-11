package com.smarthouseholdaccountbook.backend.application.mapper;

import com.smarthouseholdaccountbook.backend.entity.MonthlyReport;
import com.smarthouseholdaccountbook.backend.generated.model.MonthlyReportResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

/**
 * 月次 AI レポートを API レスポンスへ変換するマッパー。
 */
@Component
public class MonthlyReportMapper {

    /**
     * 永続化された月次レポートを DTO へ変換する。
     *
     * @param report 月次レポート
     * @return API レスポンス DTO。引数が {@code null} の場合は {@code null}
     */
    public MonthlyReportResponse toDto(MonthlyReport report) {
        if (report == null) {
            return null;
        }

        MonthlyReportResponse response = new MonthlyReportResponse();
        response.setMonth(report.getReportMonth());
        response.setSummary(report.getSummary());
        response.setSuggestions(report.getSuggestions());
        response.setGeneratedAt(report.getGeneratedAt().atOffset(ZoneOffset.UTC));
        return response;
    }
}
