package com.smarthouseholdaccountbook.backend.application.mapper;

import com.smarthouseholdaccountbook.backend.entity.MonthlyReport;
import com.smarthouseholdaccountbook.backend.entity.User;
import com.smarthouseholdaccountbook.backend.generated.model.MonthlyReportResponse;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyReportMapperTest {

    private final MonthlyReportMapper mapper = new MonthlyReportMapper();

    @Test
    void 月次レポートを変換する() {
        MonthlyReport report = new MonthlyReport(
                new User("report-mapper-test-user"),
                "2024-01",
                "総評",
                List.of("提案1", "提案2"));

        MonthlyReportResponse dto = mapper.toDto(report);

        assertThat(dto.getMonth()).isEqualTo("2024-01");
        assertThat(dto.getSummary()).isEqualTo("総評");
        assertThat(dto.getSuggestions()).containsExactly("提案1", "提案2");
        assertThat(dto.getGeneratedAt())
                .isEqualTo(report.getGeneratedAt().atOffset(ZoneOffset.UTC));
    }

    @Test
    void nullならnullを返す() {
        assertThat(mapper.toDto(null)).isNull();
    }
}
