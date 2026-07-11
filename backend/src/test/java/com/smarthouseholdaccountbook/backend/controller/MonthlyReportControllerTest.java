package com.smarthouseholdaccountbook.backend.controller;

import com.smarthouseholdaccountbook.backend.application.mapper.MonthlyReportMapper;
import com.smarthouseholdaccountbook.backend.application.service.MonthlyReportService;
import com.smarthouseholdaccountbook.backend.entity.MonthlyReport;
import com.smarthouseholdaccountbook.backend.entity.User;
import com.smarthouseholdaccountbook.backend.exception.AiServiceException;
import com.smarthouseholdaccountbook.backend.generated.model.MonthlyReportResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 月次 AI レポート Controller の単体テスト。
 */
@ExtendWith(MockitoExtension.class)
class MonthlyReportControllerTest {

    @Mock
    private MonthlyReportService monthlyReportService;

    @Mock
    private MonthlyReportMapper monthlyReportMapper;

    @InjectMocks
    private MonthlyReportController controller;

    @Test
    void キャッシュされたレポートを返す() {
        MonthlyReport report = report();
        MonthlyReportResponse dto = new MonthlyReportResponse();
        when(monthlyReportService.generateReport("2024-01", false))
                .thenReturn(Optional.of(report));
        when(monthlyReportMapper.toDto(report)).thenReturn(dto);

        assertThat(controller.apiExpensesReportGet("2024-01", false).getBody())
                .isSameAs(dto);
    }

    @Test
    void キャッシュがない場合は204を返す() {
        when(monthlyReportService.generateReport("2024-01", false))
                .thenReturn(Optional.empty());

        assertThat(controller.apiExpensesReportGet("2024-01", false).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void trueならレポートを再生成する() {
        MonthlyReport report = report();
        MonthlyReportResponse dto = new MonthlyReportResponse();
        when(monthlyReportService.generateReport("2024-01", true))
                .thenReturn(Optional.of(report));
        when(monthlyReportMapper.toDto(report)).thenReturn(dto);

        assertThat(controller.apiExpensesReportGet("2024-01", true).getBody())
                .isSameAs(dto);
        verify(monthlyReportService).generateReport("2024-01", true);
    }

    @Test
    void 生成結果が空ならAIサービス例外を投げる() {
        when(monthlyReportService.generateReport("2024-01", true))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.apiExpensesReportGet("2024-01", true))
                .isInstanceOf(AiServiceException.class);
    }

    @Test
    void 存在しない月はサービス呼び出し前に拒否する() {
        assertThatThrownBy(() -> controller.apiExpensesReportGet("2024-13", false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static MonthlyReport report() {
        return new MonthlyReport(
                new User("report-controller-test-user"),
                "2024-01",
                "総評",
                List.of("提案"));
    }
}
