package com.smarthouseholdaccountbook.backend.application.mapper;

import com.smarthouseholdaccountbook.backend.application.service.CsvExpenseService;
import com.smarthouseholdaccountbook.backend.application.service.csv.model.CsvParseError;
import com.smarthouseholdaccountbook.backend.generated.model.CsvUploadResponseDto;
import com.smarthouseholdaccountbook.backend.generated.model.CsvUploadResponseDtoErrorsInner;
import org.springframework.stereotype.Component;

/**
 * CSV インポート結果を API レスポンスへ変換するマッパー。
 */
@Component
public class CsvExpenseMapper {

    /**
     * CSV インポート結果を DTO へ変換する。
     *
     * @param result 成功・スキップ・エラー件数を含む処理結果
     * @return API レスポンス DTO。引数が {@code null} の場合は {@code null}
     */
    public CsvUploadResponseDto toDto(CsvExpenseService.CsvUploadResult result) {
        if (result == null) {
            return null;
        }

        return new CsvUploadResponseDto(
                result.successCount(),
                result.skippedCount(),
                result.errorCount(),
                result.errors().stream()
                        .map(this::toErrorDto)
                        .toList());
    }

    private CsvUploadResponseDtoErrorsInner toErrorDto(CsvParseError error) {
        CsvUploadResponseDtoErrorsInner dto = new CsvUploadResponseDtoErrorsInner(
                error.lineNumber(),
                error.message());
        dto.setLineContent(error.lineContent());
        return dto;
    }
}
