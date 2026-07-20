package com.smarthouseholdaccountbook.backend.controller;

import com.smarthouseholdaccountbook.backend.application.mapper.CsvExpenseMapper;
import com.smarthouseholdaccountbook.backend.application.service.CsvExpenseService;
import com.smarthouseholdaccountbook.backend.application.service.csv.CsvFormat;
import com.smarthouseholdaccountbook.backend.generated.model.CsvUploadResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CSV インポート Controller の単体テスト。
 */
@ExtendWith(MockitoExtension.class)
class CsvExpenseControllerTest {

    @Mock
    private CsvExpenseService csvExpenseService;

    @Mock
    private CsvExpenseMapper csvExpenseMapper;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private CsvExpenseController controller;

    @BeforeEach
    void validFile() {
        // lenient にして、ファイル検証まで到達しないテストでも不要なスタブ扱いにしない。
        org.mockito.Mockito.lenient().when(file.isEmpty()).thenReturn(false);
        org.mockito.Mockito.lenient().when(file.getOriginalFilename()).thenReturn("expenses.CSV");
    }

    @Test
    void 正常なCSVを取り込む() {
        CsvExpenseService.CsvUploadResult result =
                new CsvExpenseService.CsvUploadResult(2, 0, 0, List.of());
        CsvUploadResponseDto dto = new CsvUploadResponseDto();
        when(csvExpenseService.uploadCsvAndAddExpenses(
                file, CsvFormat.MITSUISUMITOMO_OLD_FORMAT)).thenReturn(result);
        when(csvExpenseMapper.toDto(result)).thenReturn(dto);

        assertThat(controller.apiExpensesUploadCsvPost(
                file, "MITSUISUMITOMO_OLD_FORMAT").getBody()).isSameAs(dto);
    }

    @Test
    void 空ファイルを拒否する() {
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> controller.apiExpensesUploadCsvPost(
                file, "MITSUISUMITOMO_OLD_FORMAT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ファイルが空です");
        verify(csvExpenseService, never()).uploadCsvAndAddExpenses(file, CsvFormat.MITSUISUMITOMO_OLD_FORMAT);
    }

    @Test
    void nullファイルを拒否する() {
        assertThatThrownBy(() -> controller.apiExpensesUploadCsvPost(
                null, "MITSUISUMITOMO_OLD_FORMAT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ファイルが空です");
    }

    @Test
    void CSV以外の拡張子を拒否する() {
        when(file.getOriginalFilename()).thenReturn("expenses.txt");

        assertThatThrownBy(() -> controller.apiExpensesUploadCsvPost(
                file, "MITSUISUMITOMO_OLD_FORMAT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CSVファイルを選択してください");
    }

    @Test
    void 空のCSV形式を拒否する() {
        assertThatThrownBy(() -> controller.apiExpensesUploadCsvPost(file, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CSV形式を指定してください");
    }

    @Test
    void 未対応のCSV形式を拒否する() {
        assertThatThrownBy(() -> controller.apiExpensesUploadCsvPost(file, "UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("無効なCSV形式です");
    }
}
