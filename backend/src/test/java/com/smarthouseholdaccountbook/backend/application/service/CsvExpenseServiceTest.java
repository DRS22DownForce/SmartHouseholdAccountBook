package com.smarthouseholdaccountbook.backend.application.service;

import com.smarthouseholdaccountbook.backend.application.service.csv.CsvFormat;
import com.smarthouseholdaccountbook.backend.application.service.csv.CsvParser;
import com.smarthouseholdaccountbook.backend.application.service.csv.CsvParserFactory;
import com.smarthouseholdaccountbook.backend.application.service.csv.model.CsvParsedExpense;
import com.smarthouseholdaccountbook.backend.application.service.csv.model.CsvParseResult;
import com.smarthouseholdaccountbook.backend.entity.Expense;
import com.smarthouseholdaccountbook.backend.entity.User;
import com.smarthouseholdaccountbook.backend.repository.ExpenseRepository;
import com.smarthouseholdaccountbook.backend.valueobject.CategoryType;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseAmount;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CsvExpenseService のユニットテスト（重複除外を含む）
 */
@ExtendWith(MockitoExtension.class)
class CsvExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserApplicationService userApplicationService;

    @Mock
    private CsvParserFactory csvParserFactory;

    @Mock
    private AiCategoryService aiCategoryService;

    @Mock
    private CsvParser csvParser;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private CsvExpenseService csvExpenseService;

    private User user;

    @BeforeEach
    void setUp() throws IOException {
        user = new User("cognito-sub", "test@example.com");
        when(userApplicationService.getUser()).thenReturn(user);
        when(csvParserFactory.getParser(any(CsvFormat.class))).thenReturn(csvParser);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    }

    private CsvParsedExpense expense(String description, LocalDate date, int amount) {
        return new CsvParsedExpense(description, date, amount);
    }

    private Expense existingExpense(String description, LocalDate date, int amount) {
        return new Expense(
                description,
                new ExpenseAmount(amount),
                new ExpenseDate(date),
                CategoryType.FOOD,
                user);
    }

    private void givenParseResult(List<CsvParsedExpense> validExpenses) throws IOException {
        when(csvParser.parse(any())).thenReturn(new CsvParseResult(validExpenses, List.of()));
    }

    @Nested
    @DisplayName("重複除外")
    class DuplicateFiltering {

        @Test
        @DisplayName("全行が新規のとき、全件保存し skippedCount は 0")
        void savesAllWhenAllRowsAreNew() throws IOException {
            List<CsvParsedExpense> parsed = List.of(
                    expense("店A", LocalDate.of(2025, 11, 1), 1000),
                    expense("店B", LocalDate.of(2025, 11, 2), 2000));
            givenParseResult(parsed);
            when(expenseRepository.findByUserAndDateBetween(
                    eq(user), eq(LocalDate.of(2025, 11, 1)), eq(LocalDate.of(2025, 11, 2))))
                    .thenReturn(Collections.emptyList());
            when(aiCategoryService.predictCategoriesBatch(any())).thenReturn(Map.of());
            when(expenseRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            CsvExpenseService.CsvUploadResult result = csvExpenseService.uploadCsvAndAddExpenses(
                    multipartFile, CsvFormat.MITSUISUMITOMO_OLD_FORMAT);

            assertThat(result.successCount()).isEqualTo(2);
            assertThat(result.skippedCount()).isZero();
            verify(expenseRepository, times(1)).saveAll(any());
        }

        @Test
        @DisplayName("全行がDB既存のとき、0件保存し AI は呼ばない")
        void skipsAllWhenAllRowsExistInDb() throws IOException {
            List<CsvParsedExpense> parsed = List.of(
                    expense("店A", LocalDate.of(2025, 11, 1), 1000),
                    expense("店B", LocalDate.of(2025, 11, 2), 2000));
            givenParseResult(parsed);
            when(expenseRepository.findByUserAndDateBetween(
                    eq(user), eq(LocalDate.of(2025, 11, 1)), eq(LocalDate.of(2025, 11, 2))))
                    .thenReturn(List.of(
                            existingExpense("店A", LocalDate.of(2025, 11, 1), 1000),
                            existingExpense("店B", LocalDate.of(2025, 11, 2), 2000)));

            CsvExpenseService.CsvUploadResult result = csvExpenseService.uploadCsvAndAddExpenses(
                    multipartFile, CsvFormat.MITSUISUMITOMO_OLD_FORMAT);

            assertThat(result.successCount()).isZero();
            assertThat(result.skippedCount()).isEqualTo(2);
            verify(aiCategoryService, never()).predictCategoriesBatch(any());
            verify(expenseRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("一部のみ新規のとき、差分のみ保存する")
        void savesOnlyNewRows() throws IOException {
            List<CsvParsedExpense> parsed = List.of(
                    expense("店A", LocalDate.of(2025, 11, 1), 1000),
                    expense("店B", LocalDate.of(2025, 11, 2), 2000),
                    expense("店C", LocalDate.of(2025, 11, 3), 3000));
            givenParseResult(parsed);
            when(expenseRepository.findByUserAndDateBetween(
                    eq(user), eq(LocalDate.of(2025, 11, 1)), eq(LocalDate.of(2025, 11, 3))))
                    .thenReturn(List.of(
                            existingExpense("店A", LocalDate.of(2025, 11, 1), 1000),
                            existingExpense("店B", LocalDate.of(2025, 11, 2), 2000)));
            when(aiCategoryService.predictCategoriesBatch(any())).thenReturn(Map.of());
            when(expenseRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            CsvExpenseService.CsvUploadResult result = csvExpenseService.uploadCsvAndAddExpenses(
                    multipartFile, CsvFormat.MITSUISUMITOMO_OLD_FORMAT);

            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.skippedCount()).isEqualTo(2);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Expense>> captor = ArgumentCaptor.forClass(List.class);
            verify(expenseRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(1);
            assertThat(captor.getValue().get(0).getDescription()).isEqualTo("店C");
        }

        @Test
        @DisplayName("同一CSV内に重複行があるとき、1件のみ保存する")
        void skipsDuplicatesWithinSameCsv() throws IOException {
            List<CsvParsedExpense> parsed = List.of(
                    expense("店A", LocalDate.of(2025, 11, 1), 1000),
                    expense("店A", LocalDate.of(2025, 11, 1), 1000));
            givenParseResult(parsed);
            when(expenseRepository.findByUserAndDateBetween(
                    eq(user), eq(LocalDate.of(2025, 11, 1)), eq(LocalDate.of(2025, 11, 1))))
                    .thenReturn(Collections.emptyList());
            when(aiCategoryService.predictCategoriesBatch(any())).thenReturn(Map.of());
            when(expenseRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            CsvExpenseService.CsvUploadResult result = csvExpenseService.uploadCsvAndAddExpenses(
                    multipartFile, CsvFormat.MITSUISUMITOMO_OLD_FORMAT);

            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.skippedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("手動登録済みと一致する行はスキップする")
        void skipsRowsMatchingManuallyAddedExpense() throws IOException {
            List<CsvParsedExpense> parsed = List.of(
                    expense("手動登録店", LocalDate.of(2025, 11, 5), 500));
            givenParseResult(parsed);
            when(expenseRepository.findByUserAndDateBetween(
                    eq(user), eq(LocalDate.of(2025, 11, 5)), eq(LocalDate.of(2025, 11, 5))))
                    .thenReturn(List.of(
                            existingExpense("手動登録店", LocalDate.of(2025, 11, 5), 500)));

            CsvExpenseService.CsvUploadResult result = csvExpenseService.uploadCsvAndAddExpenses(
                    multipartFile, CsvFormat.MITSUISUMITOMO_OLD_FORMAT);

            assertThat(result.successCount()).isZero();
            assertThat(result.skippedCount()).isEqualTo(1);
            verify(expenseRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("AI分類")
    class AiClassification {

        @Test
        @DisplayName("新規行のみ AI カテゴリ分類の対象になる")
        void callsAiOnlyForNewRows() throws IOException {
            List<CsvParsedExpense> parsed = List.of(
                    expense("既存店", LocalDate.of(2025, 11, 1), 1000),
                    expense("新店", LocalDate.of(2025, 11, 2), 2000));
            givenParseResult(parsed);
            when(expenseRepository.findByUserAndDateBetween(
                    eq(user), eq(LocalDate.of(2025, 11, 1)), eq(LocalDate.of(2025, 11, 2))))
                    .thenReturn(List.of(
                            existingExpense("既存店", LocalDate.of(2025, 11, 1), 1000)));
            Map<String, CategoryType> categoryMap = new HashMap<>();
            categoryMap.put("新店", CategoryType.FOOD);
            when(aiCategoryService.predictCategoriesBatch(List.of("新店"))).thenReturn(categoryMap);
            when(expenseRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            csvExpenseService.uploadCsvAndAddExpenses(multipartFile, CsvFormat.MITSUISUMITOMO_OLD_FORMAT);

            verify(aiCategoryService).predictCategoriesBatch(List.of("新店"));
        }
    }
}
