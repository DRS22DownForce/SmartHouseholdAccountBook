package com.example.backend.controller;

import com.example.backend.application.mapper.ExpenseMapper;
import com.example.backend.application.service.CsvExpenseService;
import com.example.backend.application.service.csv.CsvFormat;
import com.example.backend.application.service.csv.model.CsvParseError;
import com.example.backend.application.service.ExpenseApplicationService;
import com.example.backend.generated.model.CsvUploadResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * ExpenseControllerのユニットテストクラス
 * 
 * CSVアップロードエンドポイントの動作をテストします。
 * Mockitoを使用して依存関係をモック化し、コントローラーのロジックのみをテストします。
 */
@ExtendWith(MockitoExtension.class)
class ExpenseControllerTest {

    @Mock
    private ExpenseApplicationService expenseApplicationService;

    @Mock
    private CsvExpenseService csvExpenseService;

    @Mock
    private ExpenseMapper expenseMapper;

    @InjectMocks
    private ExpenseController expenseController;

    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // テスト用のMultipartFileをモック化
        // lenientを使用して、すべてのテストで使用されない場合でもエラーにならないようにする
        mockFile = mock(MultipartFile.class);
        lenient().when(mockFile.getOriginalFilename()).thenReturn("test.csv");
    }

    @Test
    @DisplayName("CSVファイルが正常にアップロードできる")
    void apiExpensesUploadCsvPost_正常にアップロードできる() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");

        // CSV支出処理サービスのモック設定
        CsvExpenseService.CsvUploadResult result = new CsvExpenseService.CsvUploadResult(2, 0,
                new ArrayList<>());
        when(csvExpenseService.uploadCsvAndAddExpenses(
                any(MultipartFile.class),
                eq(CsvFormat.MITSUISUMITOMO_OLD_FORMAT))).thenReturn(result);

        // Mapperのモック設定
        CsvUploadResponseDto expectedDto = new CsvUploadResponseDto();
        expectedDto.setSuccessCount(2);
        expectedDto.setErrorCount(0);
        expectedDto.setErrors(new ArrayList<>());
        when(expenseMapper.toDto(result)).thenReturn(expectedDto);

        // テスト実行
        ResponseEntity<CsvUploadResponseDto> response = expenseController.apiExpensesUploadCsvPost(mockFile,
                "MITSUISUMITOMO_OLD_FORMAT");

        // 検証
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());
        assertTrue(response.getBody().getErrors().isEmpty());

        // CSV支出処理サービスが正しく呼ばれたことを確認
        verify(csvExpenseService, times(1)).uploadCsvAndAddExpenses(
                eq(mockFile),
                eq(CsvFormat.MITSUISUMITOMO_OLD_FORMAT));
    }

    @Test
    @DisplayName("MITSUISUMITOMO_NEW_FORMAT形式のCSVファイルが正常にアップロードできる")
    void apiExpensesUploadCsvPost_MITSUISUMITOMO_NEW_FORMATで正常にアップロードできる() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");

        // CSV支出処理サービスのモック設定
        CsvExpenseService.CsvUploadResult result = new CsvExpenseService.CsvUploadResult(3, 0,
                new ArrayList<>());
        when(csvExpenseService.uploadCsvAndAddExpenses(
                any(MultipartFile.class),
                eq(CsvFormat.MITSUISUMITOMO_NEW_FORMAT))).thenReturn(result);

        // Mapperのモック設定
        CsvUploadResponseDto expectedDto = new CsvUploadResponseDto();
        expectedDto.setSuccessCount(3);
        expectedDto.setErrorCount(0);
        expectedDto.setErrors(new ArrayList<>());
        when(expenseMapper.toDto(result)).thenReturn(expectedDto);

        // テスト実行
        ResponseEntity<CsvUploadResponseDto> response = expenseController.apiExpensesUploadCsvPost(mockFile,
                "MITSUISUMITOMO_NEW_FORMAT");

        // 検証
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());

        // CSV支出処理サービスが正しく呼ばれたことを確認
        verify(csvExpenseService, times(1)).uploadCsvAndAddExpenses(
                eq(mockFile),
                eq(CsvFormat.MITSUISUMITOMO_NEW_FORMAT));
    }

    @Test
    @DisplayName("CSVファイルが空の場合は400エラーを返す")
    void apiExpensesUploadCsvPost_ファイルが空の場合は400エラー() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(true);

        // テスト実行と検証
        // IllegalArgumentExceptionがスローされることを確認
        assertThrows(IllegalArgumentException.class, () -> {
            expenseController.apiExpensesUploadCsvPost(mockFile, "MITSUISUMITOMO_OLD_FORMAT");
        });

        // CSV支出処理サービスは呼ばれないことを確認
        verify(csvExpenseService, never()).uploadCsvAndAddExpenses(any(), any());
    }

    @Test
    @DisplayName("CSVファイル以外のファイル形式の場合は400エラーを返す")
    void apiExpensesUploadCsvPost_CSVファイル以外の場合は400エラー() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.txt");

        // テスト実行と検証
        // IllegalArgumentExceptionがスローされることを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            expenseController.apiExpensesUploadCsvPost(mockFile, "MITSUISUMITOMO_OLD_FORMAT");
        });
        assertEquals("CSVファイルを選択してください", exception.getMessage());

        // CSV支出処理サービスは呼ばれないことを確認
        verify(csvExpenseService, never()).uploadCsvAndAddExpenses(any(), any());
    }

    @Test
    @DisplayName("ファイル名がnullの場合は400エラーを返す")
    void apiExpensesUploadCsvPost_ファイル名がnullの場合は400エラー() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(null);

        // テスト実行と検証
        // IllegalArgumentExceptionがスローされることを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            expenseController.apiExpensesUploadCsvPost(mockFile, "MITSUISUMITOMO_OLD_FORMAT");
        });
        assertEquals("CSVファイルを選択してください", exception.getMessage());

        // CSV支出処理サービスは呼ばれないことを確認
        verify(csvExpenseService, never()).uploadCsvAndAddExpenses(any(), any());
    }

    @Test
    @DisplayName("CSV形式が空文字列の場合は400エラーを返す")
    void apiExpensesUploadCsvPost_CSV形式が空文字列の場合は400エラー() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");

        // テスト実行と検証
        // IllegalArgumentExceptionがスローされることを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            expenseController.apiExpensesUploadCsvPost(mockFile, "");
        });
        assertEquals("CSV形式を指定してください", exception.getMessage());

        // CSV支出処理サービスは呼ばれないことを確認
        verify(csvExpenseService, never()).uploadCsvAndAddExpenses(any(), any());
    }

    @Test
    @DisplayName("無効なCSV形式の場合は400エラーを返す")
    void apiExpensesUploadCsvPost_無効なCSV形式の場合は400エラー() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");

        // テスト実行と検証
        // IllegalArgumentExceptionがスローされることを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            expenseController.apiExpensesUploadCsvPost(mockFile, "INVALID_FORMAT");
        });
        assertTrue(exception.getMessage().contains("無効なCSV形式です"));

        // CSV支出処理サービスは呼ばれないことを確認
        verify(csvExpenseService, never()).uploadCsvAndAddExpenses(any(), any());
    }

    @Test
    @DisplayName("CSVアップロード時にエラーが発生した場合はエラー情報を返す")
    void apiExpensesUploadCsvPost_エラーが発生した場合はエラー情報を返す() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");

        // エラーを含む解析結果を準備
        List<CsvParseError> errors = new ArrayList<>();
        errors.add(new CsvParseError(2, "2024/1/1,テスト店,1000", "日付の形式が不正です"));

        CsvExpenseService.CsvUploadResult result = new CsvExpenseService.CsvUploadResult(1, 1, errors);
        when(csvExpenseService.uploadCsvAndAddExpenses(
                any(MultipartFile.class),
                eq(CsvFormat.MITSUISUMITOMO_OLD_FORMAT))).thenReturn(result);

        // Mapperのモック設定
        CsvUploadResponseDto expectedDto = new CsvUploadResponseDto();
        expectedDto.setSuccessCount(1);
        expectedDto.setErrorCount(1);
        List<com.example.backend.generated.model.CsvUploadResponseDtoErrorsInner> errorDtos = new ArrayList<>();
        com.example.backend.generated.model.CsvUploadResponseDtoErrorsInner errorDto = new com.example.backend.generated.model.CsvUploadResponseDtoErrorsInner(
                2, "日付の形式が不正です");
        errorDto.setLineContent("2024/1/1,テスト店,1000");
        errorDtos.add(errorDto);
        expectedDto.setErrors(errorDtos);
        when(expenseMapper.toDto(result)).thenReturn(expectedDto);

        // テスト実行
        ResponseEntity<CsvUploadResponseDto> response = expenseController.apiExpensesUploadCsvPost(mockFile,
                "MITSUISUMITOMO_OLD_FORMAT");

        // 検証
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getSuccessCount());
        assertEquals(1, response.getBody().getErrorCount());
        assertEquals(1, response.getBody().getErrors().size());
        assertEquals(2, response.getBody().getErrors().get(0).getLineNumber());
        assertEquals("日付の形式が不正です", response.getBody().getErrors().get(0).getMessage());
        assertEquals("2024/1/1,テスト店,1000", response.getBody().getErrors().get(0).getLineContent());
    }

    @Test
    @DisplayName("CSVファイルの読み込みエラーが発生した場合はCsvUploadExceptionをスロー")
    void apiExpensesUploadCsvPost_ファイル読み込みエラーで例外をスロー() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");

        // CsvUploadExceptionをスローするように設定（Service層でIOExceptionがCsvUploadExceptionに変換される）
        com.example.backend.exception.CsvUploadException csvException = new com.example.backend.exception.CsvUploadException(
                "ファイルの読み込みに失敗しました: ファイルの読み込みに失敗しました",
                new IOException("ファイルの読み込みに失敗しました"),
                org.springframework.http.HttpStatus.BAD_REQUEST);
        when(csvExpenseService.uploadCsvAndAddExpenses(
                any(MultipartFile.class),
                any(CsvFormat.class))).thenThrow(csvException);

        // テスト実行と検証
        assertThrows(com.example.backend.exception.CsvUploadException.class, () -> {
            expenseController.apiExpensesUploadCsvPost(mockFile, "MITSUISUMITOMO_OLD_FORMAT");
        });
    }

    @Test
    @DisplayName("CSV処理中の予期しないエラーが発生した場合はCsvUploadExceptionをスロー")
    void apiExpensesUploadCsvPost_予期しないエラーで例外をスロー() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");

        // CsvUploadExceptionをスローするように設定（Service層でRuntimeExceptionがCsvUploadExceptionに変換される）
        com.example.backend.exception.CsvUploadException csvException = new com.example.backend.exception.CsvUploadException(
                "CSVの処理中にエラーが発生しました: 予期しないエラー",
                new RuntimeException("予期しないエラー"),
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        when(csvExpenseService.uploadCsvAndAddExpenses(
                any(MultipartFile.class),
                any(CsvFormat.class))).thenThrow(csvException);

        // テスト実行と検証
        assertThrows(com.example.backend.exception.CsvUploadException.class, () -> {
            expenseController.apiExpensesUploadCsvPost(mockFile, "MITSUISUMITOMO_OLD_FORMAT");
        });
    }
}
