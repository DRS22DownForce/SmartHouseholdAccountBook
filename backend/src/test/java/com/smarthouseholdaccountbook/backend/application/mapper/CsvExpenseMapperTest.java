package com.smarthouseholdaccountbook.backend.application.mapper;

import com.smarthouseholdaccountbook.backend.application.service.CsvExpenseService;
import com.smarthouseholdaccountbook.backend.application.service.csv.model.CsvParseError;
import com.smarthouseholdaccountbook.backend.generated.model.CsvUploadResponseDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvExpenseMapperTest {

    private final CsvExpenseMapper mapper = new CsvExpenseMapper();

    @Test
    void 件数とエラー詳細を変換する() {
        CsvExpenseService.CsvUploadResult result =
                new CsvExpenseService.CsvUploadResult(
                        3, 1, 20,
                        List.of(new CsvParseError(5, "line", "message")));

        CsvUploadResponseDto dto = mapper.toDto(result);

        assertThat(dto.getSuccessCount()).isEqualTo(3);
        assertThat(dto.getSkippedCount()).isEqualTo(20);
        assertThat(dto.getErrorCount()).isEqualTo(1);
        assertThat(dto.getErrors()).singleElement().satisfies(error -> {
            assertThat(error.getLineNumber()).isEqualTo(5);
            assertThat(error.getLineContent()).isEqualTo("line");
            assertThat(error.getMessage()).isEqualTo("message");
        });
    }

    @Test
    void nullならnullを返す() {
        assertThat(mapper.toDto(null)).isNull();
    }
}
