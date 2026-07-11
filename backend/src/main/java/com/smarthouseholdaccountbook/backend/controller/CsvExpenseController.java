package com.smarthouseholdaccountbook.backend.controller;

import com.smarthouseholdaccountbook.backend.application.mapper.CsvExpenseMapper;
import com.smarthouseholdaccountbook.backend.application.service.CsvExpenseService;
import com.smarthouseholdaccountbook.backend.application.service.csv.CsvFormat;
import com.smarthouseholdaccountbook.backend.generated.api.CsvExpensesApi;
import com.smarthouseholdaccountbook.backend.generated.model.CsvUploadResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

/**
 * 支出 CSV のインポートを担当する REST API コントローラー。
 */
@RestController
public class CsvExpenseController implements CsvExpensesApi {

    private final CsvExpenseService csvExpenseService;
    private final CsvExpenseMapper csvExpenseMapper;

    public CsvExpenseController(
            CsvExpenseService csvExpenseService,
            CsvExpenseMapper csvExpenseMapper) {
        this.csvExpenseService = csvExpenseService;
        this.csvExpenseMapper = csvExpenseMapper;
    }

    @Override
    public ResponseEntity<CsvUploadResponseDto> apiExpensesUploadCsvPost(
            MultipartFile file,
            String csvFormat) {
        validateFile(file);
        CsvFormat format = parseFormat(csvFormat);

        CsvExpenseService.CsvUploadResult result =
                csvExpenseService.uploadCsvAndAddExpenses(file, format);
        return ResponseEntity.ok(csvExpenseMapper.toDto(result));
    }

    /**
     * OpenAPI の Bean Validation に加えて、直接呼び出された場合にも安全な入力検証を行う。
     */
    private static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("ファイルが空です");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null
                || !originalFilename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new IllegalArgumentException("CSVファイルを選択してください");
        }
    }

    private static CsvFormat parseFormat(String csvFormat) {
        if (csvFormat == null || csvFormat.isBlank()) {
            throw new IllegalArgumentException("CSV形式を指定してください");
        }

        // 内部 enum に値が増えても、OpenAPI で公開した形式だけを許可する。
        return switch (csvFormat) {
            case "MITSUISUMITOMO_OLD_FORMAT" -> CsvFormat.MITSUISUMITOMO_OLD_FORMAT;
            case "MITSUISUMITOMO_NEW_FORMAT" -> CsvFormat.MITSUISUMITOMO_NEW_FORMAT;
            default -> throw new IllegalArgumentException(
                    "無効なCSV形式です。MITSUISUMITOMO_OLD_FORMAT（三井住友カード 確定月）"
                            + "またはMITSUISUMITOMO_NEW_FORMAT（三井住友カード 未確定月）を指定してください");
        };
    }
}
