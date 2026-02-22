package com.example.backend.controller;

import com.example.backend.application.mapper.ExpenseMapper;
import com.example.backend.application.service.CsvExpenseService;
import com.example.backend.application.service.ExpenseApplicationService;
import com.example.backend.application.service.csv.CsvFormat;
import com.example.backend.application.service.csv.model.CsvParseError;
import com.example.backend.exception.CsvUploadException;
import com.example.backend.entity.Expense;
import com.example.backend.entity.ExpenseUpdate;
import com.example.backend.entity.User;
import com.example.backend.generated.model.CsvUploadResponseDto;
import com.example.backend.generated.model.CsvUploadResponseDtoErrorsInner;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import com.example.backend.generated.model.MonthlySummaryDto;
import com.example.backend.valueobject.CategoryType;
import com.example.backend.valueobject.ExpenseAmount;
import com.example.backend.valueobject.ExpenseDate;
import com.example.backend.valueobject.MonthlySummary;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ExpenseControllerのユニットテストクラス
 * 各エンドポイントの動作をテストする。依存はモック化し、コントローラー単体の振る舞いを検証する。
 */
@ExtendWith(MockitoExtension.class)
class ExpenseControllerTest {

    @Mock
    private ExpenseApplicationService expenseApplicationService;

    @Mock
    private CsvExpenseService csvExpenseService;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private ExpenseController expenseController;

    @Nested
    @DisplayName("apiExpensesGet")
    class ApiExpensesGet {

        @Test
        @DisplayName("monthがnullのとき、全件取得して200で一覧を返す")
        void returnsAllExpensesWhenMonthIsNull() {
            Expense expense = createExpense("ダミー", 1000, LocalDate.of(2024, 1, 10), CategoryType.FOOD);
            when(expenseApplicationService.getExpenses()).thenReturn(List.of(expense));
            ExpenseDto expectedDto = new ExpenseDto();
            expectedDto.setId(1L);
            expectedDto.setDescription("テスト");
            expectedDto.setAmount(1000);
            expectedDto.setDate(LocalDate.of(2024, 1, 15));
            expectedDto.setCategory("食費");
            when(expenseMapper.toDto(any(Expense.class))).thenReturn(expectedDto);

            ResponseEntity<List<ExpenseDto>> response = expenseController.apiExpensesGet(null);

            assertOkWithBodyList(response, List.of(expectedDto));
            verify(expenseApplicationService).getExpenses();
            verify(expenseApplicationService, never()).getExpensesByMonth(any(), any(Pageable.class));
        }

        @Test
        @DisplayName("monthを指定したとき、その月の一覧を200で返す")
        void returnsExpensesByMonthWhenMonthGiven() {
            Expense expense = createExpense("月指定ダミー", 1200, LocalDate.of(2024, 1, 20), CategoryType.TRANSPORT);
            PageImpl<Expense> page = new PageImpl<>(List.of(expense), PageRequest.of(0, Integer.MAX_VALUE), 1);
            when(expenseApplicationService.getExpensesByMonth(eq("2024-01"), any(Pageable.class))).thenReturn(page);
            ExpenseDto expectedDto = new ExpenseDto();
            expectedDto.setId(2L);
            expectedDto.setDescription("月指定");
            when(expenseMapper.toDto(any(Expense.class))).thenReturn(expectedDto);

            ResponseEntity<List<ExpenseDto>> response = expenseController.apiExpensesGet("2024-01");

            assertOkWithBodyList(response, List.of(expectedDto));
            verify(expenseApplicationService).getExpensesByMonth("2024-01", PageRequest.of(0, Integer.MAX_VALUE));
            verify(expenseApplicationService, never()).getExpenses();
        }
    }

    @Nested
    @DisplayName("apiExpensesPost")
    class ApiExpensesPost {

        @Test
        @DisplayName("支出を追加すると、201で作成したDTOを返す")
        void returnsCreatedAndDto() {
            ExpenseRequestDto requestDto = new ExpenseRequestDto();
            requestDto.setDescription("追加テスト");
            requestDto.setAmount(2000);
            requestDto.setDate(LocalDate.of(2024, 2, 1));
            requestDto.setCategory("食費");
            ExpenseUpdate update = createExpenseUpdate("追加テスト", 2000, LocalDate.of(2024, 2, 1), CategoryType.FOOD);
            when(expenseMapper.toExpenseUpdate(requestDto)).thenReturn(update);
            Expense saved = createExpense("追加テスト", 2000, LocalDate.of(2024, 2, 1), CategoryType.FOOD);
            when(expenseApplicationService.addExpense(update)).thenReturn(saved);
            ExpenseDto expectedDto = new ExpenseDto();
            expectedDto.setId(10L);
            expectedDto.setDescription("追加テスト");
            when(expenseMapper.toDto(saved)).thenReturn(expectedDto);

            ResponseEntity<ExpenseDto> response = expenseController.apiExpensesPost(requestDto);

            assertCreatedWithBody(response, expectedDto);
            verify(expenseApplicationService).addExpense(update);
        }
    }

    @Nested
    @DisplayName("apiExpensesIdDelete")
    class ApiExpensesIdDelete {

        @Test
        @DisplayName("指定IDの支出を削除すると、204を返す")
        void returnsNoContentAfterDelete() {
            Long id = 5L;

            ResponseEntity<Void> response = expenseController.apiExpensesIdDelete(id);

            assertNoContent(response);
            verify(expenseApplicationService).deleteExpense(id);
        }
    }

    @Nested
    @DisplayName("apiExpensesIdPut")
    class ApiExpensesIdPut {

        @Test
        @DisplayName("指定IDの支出を更新すると、200でDTOを返す")
        void returnsOkAndUpdatedDto() {
            Long id = 3L;
            ExpenseRequestDto requestDto = new ExpenseRequestDto();
            requestDto.setDescription("更新後");
            requestDto.setAmount(3000);
            requestDto.setDate(LocalDate.of(2024, 3, 10));
            requestDto.setCategory("交通費");
            ExpenseUpdate update = createExpenseUpdate("更新後", 3000, LocalDate.of(2024, 3, 10), CategoryType.TRANSPORT);
            when(expenseMapper.toExpenseUpdate(requestDto)).thenReturn(update);
            Expense updated = createExpense("更新後", 3000, LocalDate.of(2024, 3, 10), CategoryType.TRANSPORT);
            when(expenseApplicationService.updateExpense(id, update)).thenReturn(updated);
            ExpenseDto expectedDto = new ExpenseDto();
            expectedDto.setId(id);
            expectedDto.setDescription("更新後");
            when(expenseMapper.toDto(updated)).thenReturn(expectedDto);

            ResponseEntity<ExpenseDto> response = expenseController.apiExpensesIdPut(id, requestDto);

            assertOkWithBody(response, expectedDto);
            verify(expenseApplicationService).updateExpense(id, update);
        }
    }

    @Nested
    @DisplayName("apiExpensesSummaryGet")
    class ApiExpensesSummaryGet {

        @Test
        @DisplayName("指定月のサマリーを取得すると、200でサマリーを返す")
        void returnsMonthlySummary() {
            MonthlySummary summary = MonthlySummary.createMonthlySummaryFromExpenses(List.of(
                    createExpense("サマリー1", 1000, LocalDate.of(2024, 4, 1), CategoryType.FOOD)));
            when(expenseApplicationService.getMonthlySummary("2024-04")).thenReturn(summary);
            MonthlySummaryDto expectedDto = new MonthlySummaryDto();
            expectedDto.setTotal(50000);
            expectedDto.setCount(10);
            expectedDto.setByCategory(new ArrayList<>());
            when(expenseMapper.toDto(summary)).thenReturn(expectedDto);

            ResponseEntity<MonthlySummaryDto> response = expenseController.apiExpensesSummaryGet("2024-04");

            assertOkWithBody(response, expectedDto);
            verify(expenseApplicationService).getMonthlySummary("2024-04");
        }
    }

    @Nested
    @DisplayName("apiExpensesSummaryRangeGet")
    class ApiExpensesSummaryRangeGet {

        @Test
        @DisplayName("開始月〜終了月のサマリーを取得すると、200でサマリー一覧を返す")
        void returnsMonthlySummaryRange() {
            MonthlySummary s1 = MonthlySummary.createMonthlySummaryFromExpenses(List.of(
                    createExpense("1月分", 30000, LocalDate.of(2024, 1, 1), CategoryType.FOOD)));
            MonthlySummary s2 = MonthlySummary.createMonthlySummaryFromExpenses(List.of(
                    createExpense("2月分", 25000, LocalDate.of(2024, 2, 1), CategoryType.TRANSPORT)));
            when(expenseApplicationService.getMonthlySummaryRange("2024-01", "2024-02"))
                    .thenReturn(List.of(s1, s2));
            MonthlySummaryDto expectedDto1 = new MonthlySummaryDto();
            expectedDto1.setTotal(30000);
            MonthlySummaryDto expectedDto2 = new MonthlySummaryDto();
            expectedDto2.setTotal(25000);
            when(expenseMapper.toDto(s1)).thenReturn(expectedDto1);
            when(expenseMapper.toDto(s2)).thenReturn(expectedDto2);

            ResponseEntity<List<MonthlySummaryDto>> response =
                    expenseController.apiExpensesSummaryRangeGet("2024-01", "2024-02");

            assertOkWithBodyList(response, List.of(expectedDto1, expectedDto2));
            verify(expenseApplicationService).getMonthlySummaryRange("2024-01", "2024-02");
        }
    }

    @Nested
    @DisplayName("apiExpensesMonthsGet")
    class ApiExpensesMonthsGet {

        @Test
        @DisplayName("利用可能な月のリストを取得すると、200で月リストを返す")
        void returnsAvailableMonths() {
            List<String> expectedMonths = List.of("2024-02", "2024-01");
            when(expenseApplicationService.getAvailableMonths()).thenReturn(expectedMonths);

            ResponseEntity<List<String>> response = expenseController.apiExpensesMonthsGet();

            assertOkWithBodyList(response, expectedMonths);
            verify(expenseApplicationService).getAvailableMonths();
        }
    }

    @Nested
    @DisplayName("apiExpensesUploadCsvPost - 正常系")
    class ApiExpensesUploadCsvPostSuccess {

        @Test
        @DisplayName("OLD形式のCSVをアップロードすると、200でアップロード結果を返す")
        void returnsOkWhenValidCsvUploaded() throws IOException {
            givenValidCsvFile();
            CsvExpenseService.CsvUploadResult result = new CsvExpenseService.CsvUploadResult(2, 0, new ArrayList<>());
            when(csvExpenseService.uploadCsvAndAddExpenses(any(MultipartFile.class), eq(CsvFormat.MITSUISUMITOMO_OLD_FORMAT)))
                    .thenReturn(result);
            CsvUploadResponseDto expectedDto = new CsvUploadResponseDto();
            expectedDto.setSuccessCount(2);
            expectedDto.setErrorCount(0);
            expectedDto.setErrors(new ArrayList<>());
            when(expenseMapper.toDto(result)).thenReturn(expectedDto);

            ResponseEntity<CsvUploadResponseDto> response = expenseController.apiExpensesUploadCsvPost(
                    mockFile, "MITSUISUMITOMO_OLD_FORMAT");

            assertOkWithBody(response, expectedDto);
            verify(csvExpenseService, times(1)).uploadCsvAndAddExpenses(eq(mockFile), eq(CsvFormat.MITSUISUMITOMO_OLD_FORMAT));
        }

        @Test
        @DisplayName("NEW形式のCSVをアップロードすると、200でアップロード結果を返す")
        void returnsOkWhenNewFormatCsvUploaded() throws IOException {
            givenValidCsvFile();
            CsvExpenseService.CsvUploadResult result = new CsvExpenseService.CsvUploadResult(3, 0, new ArrayList<>());
            when(csvExpenseService.uploadCsvAndAddExpenses(any(MultipartFile.class), eq(CsvFormat.MITSUISUMITOMO_NEW_FORMAT)))
                    .thenReturn(result);
            CsvUploadResponseDto expectedDto = new CsvUploadResponseDto();
            expectedDto.setSuccessCount(3);
            expectedDto.setErrorCount(0);
            expectedDto.setErrors(new ArrayList<>());
            when(expenseMapper.toDto(result)).thenReturn(expectedDto);

            ResponseEntity<CsvUploadResponseDto> response = expenseController.apiExpensesUploadCsvPost(
                    mockFile, "MITSUISUMITOMO_NEW_FORMAT");

            assertOkWithBody(response, expectedDto);
            verify(csvExpenseService, times(1)).uploadCsvAndAddExpenses(eq(mockFile), eq(CsvFormat.MITSUISUMITOMO_NEW_FORMAT));
        }

        @Test
        @DisplayName("パースエラーがあるときも、200でエラー詳細付きの結果を返す")
        void returnsOkWithErrorDetailsWhenParseErrorsOccur() throws IOException {
            givenValidCsvFile();
            List<CsvParseError> errors = new ArrayList<>();
            errors.add(new CsvParseError(2, "2024/1/1,テスト店,1000", "日付の形式が不正です"));
            CsvExpenseService.CsvUploadResult result = new CsvExpenseService.CsvUploadResult(1, 1, errors);
            when(csvExpenseService.uploadCsvAndAddExpenses(any(MultipartFile.class), eq(CsvFormat.MITSUISUMITOMO_OLD_FORMAT)))
                    .thenReturn(result);
            CsvUploadResponseDto expectedDto = new CsvUploadResponseDto();
            expectedDto.setSuccessCount(1);
            expectedDto.setErrorCount(1);
            List<CsvUploadResponseDtoErrorsInner> errorDtos = new ArrayList<>();
            CsvUploadResponseDtoErrorsInner errorDto = new CsvUploadResponseDtoErrorsInner(2, "日付の形式が不正です");
            errorDto.setLineContent("2024/1/1,テスト店,1000");
            errorDtos.add(errorDto);
            expectedDto.setErrors(errorDtos);
            when(expenseMapper.toDto(result)).thenReturn(expectedDto);

            ResponseEntity<CsvUploadResponseDto> response = expenseController.apiExpensesUploadCsvPost(
                    mockFile, "MITSUISUMITOMO_OLD_FORMAT");

            assertOkWithBody(response, expectedDto);
            verify(csvExpenseService, times(1)).uploadCsvAndAddExpenses(eq(mockFile), eq(CsvFormat.MITSUISUMITOMO_OLD_FORMAT));
        }
    }

    @Nested
    @DisplayName("apiExpensesUploadCsvPost - バリデーションエラー")
    class ApiExpensesUploadCsvPostValidationError {

        @Test
        @DisplayName("アップロードファイルが空のとき、IllegalArgumentExceptionをスローする")
        void throwsWhenFileEmpty() {
            when(mockFile.isEmpty()).thenReturn(true);
            assertValidationErrorWithExactMessage(IllegalArgumentException.class, "MITSUISUMITOMO_OLD_FORMAT", "ファイルが空です");
        }

        @Test
        @DisplayName("拡張子が.csv以外のとき、IllegalArgumentExceptionをスローする")
        void throwsWhenFileIsNotCsv() {
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("test.txt");
            assertValidationErrorWithExactMessage(IllegalArgumentException.class, "MITSUISUMITOMO_OLD_FORMAT", "CSVファイルを選択してください");
        }

        @Test
        @DisplayName("ファイル名がnullのとき、IllegalArgumentExceptionをスローする")
        void throwsWhenFileNameIsNull() {
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn(null);
            assertValidationErrorWithExactMessage(IllegalArgumentException.class, "MITSUISUMITOMO_OLD_FORMAT", "CSVファイルを選択してください");
        }

        @Test
        @DisplayName("CSV形式が空または空白のとき、IllegalArgumentExceptionをスローする")
        void throwsWhenCsvFormatIsEmpty() {
            givenValidCsvFile();
            assertValidationErrorWithExactMessage(IllegalArgumentException.class, "", "CSV形式を指定してください");
        }

        @Test
        @DisplayName("対応外のCSV形式を指定したとき、IllegalArgumentExceptionをスローする")
        void throwsWhenCsvFormatIsInvalid() {
            givenValidCsvFile();
            assertValidationErrorWithMessageContaining(IllegalArgumentException.class, "INVALID_FORMAT", "無効なCSV形式です");
        }
    }

    @Nested
    @DisplayName("apiExpensesUploadCsvPost - 例外伝播")
    class ApiExpensesUploadCsvPostException {

        @Test
        @DisplayName("CSVの読み込みエラーが発生したとき、CsvUploadExceptionをスローする")
        void throwsCsvUploadExceptionWhenFileReadFails() {
            givenValidCsvFile();
            CsvUploadException csvException = new CsvUploadException(
                    "ファイルの読み込みに失敗しました: ファイルの読み込みに失敗しました",
                    new IOException("ファイルの読み込みに失敗しました"),
                    HttpStatus.BAD_REQUEST);
            when(csvExpenseService.uploadCsvAndAddExpenses(any(MultipartFile.class), any(CsvFormat.class)))
                    .thenThrow(csvException);

            assertThatThrownBy(() -> expenseController.apiExpensesUploadCsvPost(mockFile, "MITSUISUMITOMO_OLD_FORMAT"))
                    .isInstanceOf(CsvUploadException.class);
        }

        @Test
        @DisplayName("CSV処理中に予期しないエラーが発生したとき、CsvUploadExceptionをスローする")
        void throwsCsvUploadExceptionWhenUnexpectedErrorOccurs() {
            givenValidCsvFile();
            CsvUploadException csvException = new CsvUploadException(
                    "CSVの処理中にエラーが発生しました: 予期しないエラー",
                    new RuntimeException("予期しないエラー"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
            when(csvExpenseService.uploadCsvAndAddExpenses(any(MultipartFile.class), any(CsvFormat.class)))
                    .thenThrow(csvException);

            assertThatThrownBy(() -> expenseController.apiExpensesUploadCsvPost(mockFile, "MITSUISUMITOMO_OLD_FORMAT"))
                    .isInstanceOf(CsvUploadException.class);
        }
    }

    private void givenValidCsvFile() {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");
    }

    private Expense createExpense(String description, int amount, LocalDate date, CategoryType category) {
        User user = new User("unit-test-user", "unit-test@example.com");
        return new Expense(
                description,
                new ExpenseAmount(amount),
                new ExpenseDate(date),
                category,
                user);
    }

    private ExpenseUpdate createExpenseUpdate(String description, int amount, LocalDate date, CategoryType category) {
        return new ExpenseUpdate(
                description,
                new ExpenseAmount(amount),
                new ExpenseDate(date),
                category);
    }

    private <T> void assertOkWithBody(ResponseEntity<T> response, T expectedBody) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedBody);
    }

    private <T> void assertCreatedWithBody(ResponseEntity<T> response, T expectedBody) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedBody);
    }

    private <T> void assertOkWithBodyList(ResponseEntity<List<T>> response, List<T> expectedList) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedList);
    }

    private void assertNoContent(ResponseEntity<Void> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    private void assertValidationErrorWithExactMessage(Class<? extends Throwable> exceptionType, String csvFormat, String expectedMessage) {
        assertThatThrownBy(() -> expenseController.apiExpensesUploadCsvPost(mockFile, csvFormat))
                .isInstanceOf(exceptionType)
                .hasMessage(expectedMessage);
        verify(csvExpenseService, never()).uploadCsvAndAddExpenses(any(), any());
    }

    private void assertValidationErrorWithMessageContaining(Class<? extends Throwable> exceptionType, String csvFormat, String messageSubstring) {
        assertThatThrownBy(() -> expenseController.apiExpensesUploadCsvPost(mockFile, csvFormat))
                .isInstanceOf(exceptionType)
                .hasMessageContaining(messageSubstring);
        verify(csvExpenseService, never()).uploadCsvAndAddExpenses(any(), any());
    }
}
