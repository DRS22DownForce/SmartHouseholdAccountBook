package com.example.backend.controller;

import com.example.backend.application.mapper.ExpenseMapper;
import com.example.backend.application.service.CsvParserService;
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
        
        // アプリケーションサービスのモック設定
        ExpenseApplicationService.CsvUploadResult result = 
            new ExpenseApplicationService.CsvUploadResult(2, 0, new ArrayList<>());
        when(expenseApplicationService.uploadCsvAndAddExpenses(
            any(MultipartFile.class), 
            eq(CsvParserService.CsvFormat.OLD_FORMAT)
        )).thenReturn(result);

        // テスト実行
        ResponseEntity<CsvUploadResponseDto> response = 
            expenseController.apiExpensesUploadCsvPost(mockFile, "OLD_FORMAT");

        // 検証
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());
        assertTrue(response.getBody().getErrors().isEmpty());
        
        // アプリケーションサービスが正しく呼ばれたことを確認
        verify(expenseApplicationService, times(1)).uploadCsvAndAddExpenses(
            eq(mockFile), 
            eq(CsvParserService.CsvFormat.OLD_FORMAT)
        );
    }

    @Test
    @DisplayName("NEW_FORMAT形式のCSVファイルが正常にアップロードできる")
    void apiExpensesUploadCsvPost_NEW_FORMATで正常にアップロードできる() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");
        
        // アプリケーションサービスのモック設定
        ExpenseApplicationService.CsvUploadResult result = 
            new ExpenseApplicationService.CsvUploadResult(3, 0, new ArrayList<>());
        when(expenseApplicationService.uploadCsvAndAddExpenses(
            any(MultipartFile.class), 
            eq(CsvParserService.CsvFormat.NEW_FORMAT)
        )).thenReturn(result);

        // テスト実行
        ResponseEntity<CsvUploadResponseDto> response = 
            expenseController.apiExpensesUploadCsvPost(mockFile, "NEW_FORMAT");

        // 検証
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());
        
        // アプリケーションサービスが正しく呼ばれたことを確認
        verify(expenseApplicationService, times(1)).uploadCsvAndAddExpenses(
            eq(mockFile), 
            eq(CsvParserService.CsvFormat.NEW_FORMAT)
        );
    }

    @Test
    @DisplayName("CSVファイルが空の場合は400エラーを返す")
    void apiExpensesUploadCsvPost_ファイルが空の場合は400エラー() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(true);

        // テスト実行
        ResponseEntity<CsvUploadResponseDto> response = 
            expenseController.apiExpensesUploadCsvPost(mockFile, "OLD_FORMAT");

        // 検証
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());
        assertEquals(1, response.getBody().getErrors().size());
        assertEquals("ファイルが空です", response.getBody().getErrors().get(0).getMessage());
        
        // アプリケーションサービスは呼ばれないことを確認
        verify(expenseApplicationService, never()).uploadCsvAndAddExpenses(any(), any());
    }

    @Test
    @DisplayName("CSVファイルがnullの場合は400エラーを返す")
    void apiExpensesUploadCsvPost_ファイルがnullの場合は400エラー() throws IOException {
        // テスト実行
        ResponseEntity<CsvUploadResponseDto> response = 
            expenseController.apiExpensesUploadCsvPost(null, "OLD_FORMAT");

        // 検証
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());
        assertEquals(1, response.getBody().getErrors().size());
        assertEquals("ファイルが空です", response.getBody().getErrors().get(0).getMessage());
        
        // アプリケーションサービスは呼ばれないことを確認
        verify(expenseApplicationService, never()).uploadCsvAndAddExpenses(any(), any());
    }

    @Test
    @DisplayName("CSVファイル以外のファイル形式の場合は400エラーを返す")
    void apiExpensesUploadCsvPost_CSVファイル以外の場合は400エラー() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.txt");

        // テスト実行
        ResponseEntity<CsvUploadResponseDto> response = 
            expenseController.apiExpensesUploadCsvPost(mockFile, "OLD_FORMAT");

        // 検証
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());
        assertEquals(1, response.getBody().getErrors().size());
        assertEquals("CSVファイルを選択してください", response.getBody().getErrors().get(0).getMessage());
        
        // アプリケーションサービスは呼ばれないことを確認
        verify(expenseApplicationService, never()).uploadCsvAndAddExpenses(any(), any());
    }

    @Test
    @DisplayName("ファイル名がnullの場合は400エラーを返す")
    void apiExpensesUploadCsvPost_ファイル名がnullの場合は400エラー() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(null);

        // テスト実行
        ResponseEntity<CsvUploadResponseDto> response = 
            expenseController.apiExpensesUploadCsvPost(mockFile, "OLD_FORMAT");

        // 検証
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());
        assertEquals(1, response.getBody().getErrors().size());
        assertEquals("CSVファイルを選択してください", response.getBody().getErrors().get(0).getMessage());
        
        // アプリケーションサービスは呼ばれないことを確認
        verify(expenseApplicationService, never()).uploadCsvAndAddExpenses(any(), any());
    }

    @Test
    @DisplayName("CSV形式がnullの場合は400エラーを返す")
    void apiExpensesUploadCsvPost_CSV形式がnullの場合は400エラー() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");

        // テスト実行
        ResponseEntity<CsvUploadResponseDto> response = 
            expenseController.apiExpensesUploadCsvPost(mockFile, null);

        // 検証
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());
        assertEquals(1, response.getBody().getErrors().size());
        assertEquals("CSV形式を指定してください", response.getBody().getErrors().get(0).getMessage());
        
        // アプリケーションサービスは呼ばれないことを確認
        verify(expenseApplicationService, never()).uploadCsvAndAddExpenses(any(), any());
    }

    @Test
    @DisplayName("CSV形式が空文字列の場合は400エラーを返す")
    void apiExpensesUploadCsvPost_CSV形式が空文字列の場合は400エラー() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");

        // テスト実行
        ResponseEntity<CsvUploadResponseDto> response = 
            expenseController.apiExpensesUploadCsvPost(mockFile, "");

        // 検証
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());
        assertEquals(1, response.getBody().getErrors().size());
        assertEquals("CSV形式を指定してください", response.getBody().getErrors().get(0).getMessage());
        
        // アプリケーションサービスは呼ばれないことを確認
        verify(expenseApplicationService, never()).uploadCsvAndAddExpenses(any(), any());
    }

    @Test
    @DisplayName("無効なCSV形式の場合は400エラーを返す")
    void apiExpensesUploadCsvPost_無効なCSV形式の場合は400エラー() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");

        // テスト実行
        ResponseEntity<CsvUploadResponseDto> response = 
            expenseController.apiExpensesUploadCsvPost(mockFile, "INVALID_FORMAT");

        // 検証
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getErrorCount());
        assertEquals(1, response.getBody().getErrors().size());
        assertTrue(response.getBody().getErrors().get(0).getMessage().contains("無効なCSV形式です"));
        
        // アプリケーションサービスは呼ばれないことを確認
        verify(expenseApplicationService, never()).uploadCsvAndAddExpenses(any(), any());
    }

    @Test
    @DisplayName("CSVアップロード時にエラーが発生した場合はエラー情報を返す")
    void apiExpensesUploadCsvPost_エラーが発生した場合はエラー情報を返す() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");
        
        // エラーを含む解析結果を準備
        List<CsvParserService.CsvParseError> errors = new ArrayList<>();
        errors.add(new CsvParserService.CsvParseError(2, "2024/1/1,テスト店,1000", "日付の形式が不正です"));
        
        ExpenseApplicationService.CsvUploadResult result = 
            new ExpenseApplicationService.CsvUploadResult(1, 1, errors);
        when(expenseApplicationService.uploadCsvAndAddExpenses(
            any(MultipartFile.class), 
            eq(CsvParserService.CsvFormat.OLD_FORMAT)
        )).thenReturn(result);

        // テスト実行
        ResponseEntity<CsvUploadResponseDto> response = 
            expenseController.apiExpensesUploadCsvPost(mockFile, "OLD_FORMAT");

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
        
        // IOExceptionをスローするように設定
        when(expenseApplicationService.uploadCsvAndAddExpenses(
            any(MultipartFile.class), 
            any(CsvParserService.CsvFormat.class)
        )).thenThrow(new IOException("ファイルの読み込みに失敗しました"));

        // テスト実行と検証
        assertThrows(com.example.backend.exception.CsvUploadException.class, () -> {
            expenseController.apiExpensesUploadCsvPost(mockFile, "OLD_FORMAT");
        });
    }

    @Test
    @DisplayName("CSV処理中の予期しないエラーが発生した場合はCsvUploadExceptionをスロー")
    void apiExpensesUploadCsvPost_予期しないエラーで例外をスロー() throws IOException {
        // テストデータの準備
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");
        
        // RuntimeExceptionをスローするように設定
        when(expenseApplicationService.uploadCsvAndAddExpenses(
            any(MultipartFile.class), 
            any(CsvParserService.CsvFormat.class)
        )).thenThrow(new RuntimeException("予期しないエラー"));

        // テスト実行と検証
        assertThrows(com.example.backend.exception.CsvUploadException.class, () -> {
            expenseController.apiExpensesUploadCsvPost(mockFile, "OLD_FORMAT");
        });
    }
}
