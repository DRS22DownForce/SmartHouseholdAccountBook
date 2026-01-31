package com.example.backend.application.service;

import com.example.backend.application.mapper.ExpenseMapper;
import com.example.backend.application.service.AiCategoryService;
import com.example.backend.application.service.CsvParserService;
import com.example.backend.domain.repository.ExpenseRepository;
import com.example.backend.domain.valueobject.Category;
import com.example.backend.domain.valueobject.ExpenseAmount;
import com.example.backend.domain.valueobject.ExpenseDate;
import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.backend.exception.ExpenseNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * ExpenseApplicationServiceのテストクラス
 * 
 * アプリケーションサービスのユースケースをテストします。
 */
@ExtendWith(MockitoExtension.class)
class ExpenseApplicationServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;

    // ExpenseMapperはテスト対象クラスではないのでMock化
    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private UserApplicationService userApplicationService;

    @Mock
    private CsvParserService csvParserService;

    @Mock
    private AiCategoryService aiCategoryService;

    @InjectMocks
    private ExpenseApplicationService expenseApplicationService;

    @Test
    void getAllExpenses_リポジトリに2件あれば2件返す() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 1));
        Category category1 = new Category("食費");
        Expense expense1 = new Expense("支出1", amount1, date1, category1, user);
        
        ExpenseAmount amount2 = new ExpenseAmount(2000);
        ExpenseDate date2 = new ExpenseDate(LocalDate.of(2024, 1, 2));
        Category category2 = new Category("交通費");
        Expense expense2 = new Expense("支出2", amount2, date2, category2, user);
        
        ExpenseDto dto1 = new ExpenseDto();
        dto1.setDescription("支出1");
        dto1.setAmount(1000);
        dto1.setDate(LocalDate.of(2024, 1, 1));
        dto1.setCategory("食費");
        
        ExpenseDto dto2 = new ExpenseDto();
        dto2.setDescription("支出2");
        dto2.setAmount(2000);
        dto2.setDate(LocalDate.of(2024, 1, 2));
        dto2.setCategory("交通費");

        // モックの設定
        when(expenseRepository.findByUser(user)).thenReturn(Arrays.asList(expense1, expense2));
        when(expenseMapper.toDto(expense1)).thenReturn(dto1);
        when(expenseMapper.toDto(expense2)).thenReturn(dto2);
        when(userApplicationService.getUser()).thenReturn(user);
        
        // テスト実行
        List<ExpenseDto> result = expenseApplicationService.getExpenses();

        // 検証
        assertEquals(2, result.size());
        assertEquals("支出1", result.get(0).getDescription());
        assertEquals("支出2", result.get(1).getDescription());
    }

    @Test
    void addExpense_支出追加() {
        // テストデータの準備
        ExpenseRequestDto requestDto = new ExpenseRequestDto();
        requestDto.setDescription("テスト支出");
        requestDto.setAmount(1000);
        requestDto.setDate(LocalDate.of(2024, 1, 1));
        requestDto.setCategory("食費");

        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount = new ExpenseAmount(1000);
        ExpenseDate date = new ExpenseDate(LocalDate.of(2024, 1, 1));
        Category category = new Category("食費");
        Expense expense = new Expense("テスト支出", amount, date, category, user);
        
        when(expenseMapper.toEntity(requestDto, user)).thenReturn(expense);
        when(userApplicationService.getUser()).thenReturn(user);

        ExpenseDto expenseDto = new ExpenseDto();
        expenseDto.setDescription("テスト支出");
        expenseDto.setAmount(1000);
        expenseDto.setDate(LocalDate.of(2024, 1, 1));
        expenseDto.setCategory("食費");
        when(expenseMapper.toDto(expense)).thenReturn(expenseDto);

        when(expenseRepository.save(expense)).thenReturn(expense);

        // テスト実行
        ExpenseDto result = expenseApplicationService.addExpense(requestDto);

        // 検証
        assertNotNull(result);
        assertEquals("テスト支出", result.getDescription());
        assertEquals(1000, result.getAmount());
        assertEquals(LocalDate.of(2024, 1, 1), result.getDate());
        assertEquals("食費", result.getCategory());
        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    void addExpense_リクエストがnullなら例外() {
        // テスト実行と検証
        assertThrows(NullPointerException.class, () -> expenseApplicationService.addExpense(null));
    }

    @Test
    void updateExpense_正常に更新できる() {
        // テストデータの準備
        Long expenseId = 1L;
        ExpenseRequestDto requestDto = new ExpenseRequestDto();
        requestDto.setDescription("更新された支出");
        requestDto.setAmount(1500);
        requestDto.setDate(LocalDate.of(2024, 1, 15));
        requestDto.setCategory("娯楽費");

        // 既存の支出エンティティをモック
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount originalAmount = new ExpenseAmount(1000);
        ExpenseDate originalDate = new ExpenseDate(LocalDate.of(2024, 1, 1));
        Category originalCategory = new Category("食費");
        Expense existingExpense = new Expense("元の支出", originalAmount, originalDate, originalCategory, user);

        // 更新後の支出DTOをモック
        ExpenseDto updatedDto = new ExpenseDto();
        updatedDto.setDescription("更新された支出");
        updatedDto.setAmount(1500);
        updatedDto.setDate(LocalDate.of(2024, 1, 15));
        updatedDto.setCategory("娯楽費");

        // 更新用の値オブジェクトを準備
        ExpenseAmount updatedAmount = new ExpenseAmount(1500);
        ExpenseDate updatedDate = new ExpenseDate(LocalDate.of(2024, 1, 15));
        Category updatedCategory = new Category("娯楽費");
        ExpenseMapper.ValueObjectsForUpdate valueObjectsForUpdate = 
            new ExpenseMapper.ValueObjectsForUpdate(updatedAmount, updatedDate, updatedCategory);

        // モックの設定
        when(expenseRepository.findById(expenseId)).thenReturn(java.util.Optional.of(existingExpense));
        when(expenseRepository.save(existingExpense)).thenReturn(existingExpense);
        when(expenseMapper.toDto(existingExpense)).thenReturn(updatedDto);
        // toValueObjectsForUpdateメソッドのモック設定を追加
        // これがないと、updateExpenseメソッド内でnullが返されてNullPointerExceptionが発生します
        when(expenseMapper.toValueObjectsForUpdate(requestDto)).thenReturn(valueObjectsForUpdate);

        // テスト実行
        ExpenseDto result = expenseApplicationService.updateExpense(expenseId, requestDto);

        // 検証
        assertNotNull(result);
        assertEquals("更新された支出", result.getDescription());
        assertEquals(1500, result.getAmount());
        assertEquals(LocalDate.of(2024, 1, 15), result.getDate());
        assertEquals("娯楽費", result.getCategory());

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(expenseRepository, times(1)).findById(expenseId);
        verify(expenseRepository, times(1)).save(existingExpense);
        verify(expenseMapper, times(1)).toDto(existingExpense);
        // toValueObjectsForUpdateメソッドが呼ばれたことを確認
        verify(expenseMapper, times(1)).toValueObjectsForUpdate(requestDto);
    }

    @Test
    void updateExpense_存在しないIDなら例外() {
        // テストデータの準備
        Long nonExistentId = 999L;
        ExpenseRequestDto requestDto = new ExpenseRequestDto();
        requestDto.setDescription("テスト支出");

        // モックの設定（存在しないIDなので空のOptionalを返す）
        when(expenseRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        // テスト実行と検証
        ExpenseNotFoundException exception = assertThrows(ExpenseNotFoundException.class,
                () -> expenseApplicationService.updateExpense(nonExistentId, requestDto));

        assertEquals("ID: " + nonExistentId + " の支出が見つかりませんでした", exception.getMessage());
        verify(expenseRepository, times(1)).findById(nonExistentId);
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void updateExpense_リクエストがnullなら例外() {
        // テスト実行と検証
        assertThrows(NullPointerException.class,
                () -> expenseApplicationService.updateExpense(1L, null));

        // リポジトリは呼ばれないことを確認
        verify(expenseRepository, never()).findById(any());
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void getMonthlySummary_正常に取得できる() {
        // テストデータの準備
        String month = "2024-01";
        User user = new User("cognitoSub", "test@example.com");
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 1));
        Category category1 = new Category("食費");
        Expense expense1 = new Expense("支出1", amount1, date1, category1, user);
        
        ExpenseAmount amount2 = new ExpenseAmount(2000);
        ExpenseDate date2 = new ExpenseDate(LocalDate.of(2024, 1, 2));
        Category category2 = new Category("交通費");
        Expense expense2 = new Expense("支出2", amount2, date2, category2, user);

        // モックの設定
        when(userApplicationService.getUser()).thenReturn(user);
        when(expenseRepository.findByUserAndDateBetween(
            eq(user),
            eq(LocalDate.of(2024, 1, 1)),
            eq(LocalDate.of(2024, 1, 31))
        )).thenReturn(Arrays.asList(expense1, expense2));

        // テスト実行
        com.example.backend.domain.valueobject.MonthlySummary result = 
            expenseApplicationService.getMonthlySummary(month);

        // 検証
        assertNotNull(result);
        assertEquals(3000, result.getTotal());
        assertEquals(2, result.getCount());
        assertEquals(2, result.getByCategory().size());
        // 金額の降順でソートされていることを確認
        assertTrue(result.getByCategory().get(0).getAmount() >= 
                   result.getByCategory().get(1).getAmount());
    }

    @Test
    void getMonthlySummary_月の形式が不正なら例外() {
        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> expenseApplicationService.getMonthlySummary("2024-1"));

        assertTrue(exception.getMessage().contains("月の形式が不正です"));
    }

    @Test
    void getAvailableMonths_正常に取得できる() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        // H2とMySQLの両方で動作するように、LocalDateのリストを返すように変更
        List<LocalDate> distinctDates = Arrays.asList(
            LocalDate.of(2024, 3, 15),
            LocalDate.of(2024, 2, 10),
            LocalDate.of(2024, 1, 5),
            LocalDate.of(2024, 3, 20) // 同じ月の別の日付（重複除去のテスト用）
        );

        // モックの設定
        when(userApplicationService.getUser()).thenReturn(user);
        when(expenseRepository.findDistinctDatesByUser(user)).thenReturn(distinctDates);

        // テスト実行
        List<String> result = expenseApplicationService.getAvailableMonths();

        // 検証
        assertNotNull(result);
        assertEquals(3, result.size()); // 同じ月の重複が除去されるため、3つになる
        assertEquals("2024-03", result.get(0));
    }

    @Test
    void uploadCsvAndAddExpenses_正常にCSVをアップロードできる() throws IOException {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        
        // CSV解析結果を準備
        List<CsvParserService.CsvParsedExpense> parsedExpenses = new ArrayList<>();
        parsedExpenses.add(new CsvParserService.CsvParsedExpense(
            "テスト店1", 
            LocalDate.of(2024, 1, 1), 
            1000, 
            "その他"
        ));
        parsedExpenses.add(new CsvParserService.CsvParsedExpense(
            "テスト店2", 
            LocalDate.of(2024, 1, 2), 
            2000, 
            "その他"
        ));
        
        CsvParserService.CsvParseResult parseResult = new CsvParserService.CsvParseResult(
            parsedExpenses, 
            new ArrayList<>()
        );
        
        // AIカテゴリ分類の結果を準備
        Map<String, String> categoryMap = new LinkedHashMap<>();
        categoryMap.put("テスト店1", "食費");
        categoryMap.put("テスト店2", "交通費");
        
        // 保存されたエンティティを準備
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 1));
        Category category1 = new Category("食費");
        Expense expense1 = new Expense("テスト店1", amount1, date1, category1, user);
        
        ExpenseAmount amount2 = new ExpenseAmount(2000);
        ExpenseDate date2 = new ExpenseDate(LocalDate.of(2024, 1, 2));
        Category category2 = new Category("交通費");
        Expense expense2 = new Expense("テスト店2", amount2, date2, category2, user);
        
        // モックの設定
        when(userApplicationService.getUser()).thenReturn(user);
        when(csvParserService.parseCsv(any(), eq(CsvParserService.CsvFormat.MITSUISUMITOMO_OLD_FORMAT)))
            .thenReturn(parseResult);
        when(aiCategoryService.predictCategoriesBatch(anyList())).thenReturn(categoryMap);
        when(expenseRepository.saveAll(anyList())).thenReturn(Arrays.asList(expense1, expense2));
        
        // テスト実行
        ExpenseApplicationService.CsvUploadResult result = 
            expenseApplicationService.uploadCsvAndAddExpenses(mockFile, CsvParserService.CsvFormat.MITSUISUMITOMO_OLD_FORMAT);
        
        // 検証
        assertNotNull(result);
        assertEquals(2, result.successCount());
        assertEquals(0, result.errorCount());
        assertTrue(result.errors().isEmpty());
        
        // 各サービスが正しく呼ばれたことを確認
        verify(csvParserService, times(1)).parseCsv(any(), eq(CsvParserService.CsvFormat.MITSUISUMITOMO_OLD_FORMAT));
        verify(aiCategoryService, times(1)).predictCategoriesBatch(anyList());
        verify(expenseRepository, times(1)).saveAll(anyList());
    }

    @Test
    void uploadCsvAndAddExpenses_CSV解析でエラーが発生した場合() throws IOException {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        
        // エラーを含む解析結果を準備
        List<CsvParserService.CsvParseError> errors = new ArrayList<>();
        errors.add(new CsvParserService.CsvParseError(2, "2024/1/1,テスト店,1000", "日付の形式が不正です"));
        
        CsvParserService.CsvParseResult parseResult = new CsvParserService.CsvParseResult(
            new ArrayList<>(), 
            errors
        );
        
        // モックの設定
        // lenientを使用して、getUser()が呼ばれない場合でもエラーにならないようにする
        lenient().when(userApplicationService.getUser()).thenReturn(user);
        when(csvParserService.parseCsv(any(), eq(CsvParserService.CsvFormat.MITSUISUMITOMO_OLD_FORMAT)))
            .thenReturn(parseResult);
        
        // テスト実行
        ExpenseApplicationService.CsvUploadResult result = 
            expenseApplicationService.uploadCsvAndAddExpenses(mockFile, CsvParserService.CsvFormat.MITSUISUMITOMO_OLD_FORMAT);
        
        // 検証
        assertNotNull(result);
        assertEquals(0, result.successCount());
        assertEquals(1, result.errorCount());
        assertEquals(1, result.errors().size());
        assertEquals(2, result.errors().get(0).lineNumber());
        assertEquals("日付の形式が不正です", result.errors().get(0).message());
        
        // リポジトリは呼ばれないことを確認（有効なデータがないため）
        verify(expenseRepository, never()).saveAll(anyList());
    }

    @Test
    void uploadCsvAndAddExpenses_有効なデータがない場合は0件を返す() throws IOException {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        
        // 有効なデータがない解析結果を準備
        CsvParserService.CsvParseResult parseResult = new CsvParserService.CsvParseResult(
            new ArrayList<>(), 
            new ArrayList<>()
        );
        
        // モックの設定
        // lenientを使用して、getUser()が呼ばれない場合でもエラーにならないようにする
        lenient().when(userApplicationService.getUser()).thenReturn(user);
        when(csvParserService.parseCsv(any(), eq(CsvParserService.CsvFormat.MITSUISUMITOMO_OLD_FORMAT)))
            .thenReturn(parseResult);
        
        // テスト実行
        ExpenseApplicationService.CsvUploadResult result = 
            expenseApplicationService.uploadCsvAndAddExpenses(mockFile, CsvParserService.CsvFormat.MITSUISUMITOMO_OLD_FORMAT);
        
        // 検証
        assertNotNull(result);
        assertEquals(0, result.successCount());
        assertEquals(0, result.errorCount());
        assertTrue(result.errors().isEmpty());
        
        // リポジトリは呼ばれないことを確認
        verify(expenseRepository, never()).saveAll(anyList());
    }

    @Test
    void uploadCsvAndAddExpenses_ファイルがnullの場合は例外() {
        // テスト実行と検証
        assertThrows(NullPointerException.class, () -> {
            expenseApplicationService.uploadCsvAndAddExpenses(null, CsvParserService.CsvFormat.MITSUISUMITOMO_OLD_FORMAT);
        });
    }

    @Test
    void uploadCsvAndAddExpenses_CSV形式がnullの場合は例外() throws IOException {
        // テストデータの準備
        MultipartFile mockFile = mock(MultipartFile.class);
        // lenientを使用して、getInputStream()が呼ばれない場合でもエラーにならないようにする
        lenient().when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        
        // テスト実行と検証
        assertThrows(NullPointerException.class, () -> {
            expenseApplicationService.uploadCsvAndAddExpenses(mockFile, null);
        });
    }

    @Test
    void uploadCsvAndAddExpenses_AIカテゴリ分類が失敗した場合はフォールバック処理() throws IOException {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        
        // CSV解析結果を準備
        List<CsvParserService.CsvParsedExpense> parsedExpenses = new ArrayList<>();
        parsedExpenses.add(new CsvParserService.CsvParsedExpense(
            "テスト店1", 
            LocalDate.of(2024, 1, 1), 
            1000, 
            "その他"
        ));
        
        CsvParserService.CsvParseResult parseResult = new CsvParserService.CsvParseResult(
            parsedExpenses, 
            new ArrayList<>()
        );
        
        // 保存されたエンティティを準備（フォールバックで「その他」が設定される）
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 1));
        Category category1 = new Category("その他");
        Expense expense1 = new Expense("テスト店1", amount1, date1, category1, user);
        
        // モックの設定
        when(userApplicationService.getUser()).thenReturn(user);
        when(csvParserService.parseCsv(any(), eq(CsvParserService.CsvFormat.MITSUISUMITOMO_OLD_FORMAT)))
            .thenReturn(parseResult);
        // AIカテゴリ分類が例外をスローするように設定
        when(aiCategoryService.predictCategoriesBatch(anyList()))
            .thenThrow(new RuntimeException("AIサービスエラー"));
        when(expenseRepository.saveAll(anyList())).thenReturn(Arrays.asList(expense1));
        
        // テスト実行
        ExpenseApplicationService.CsvUploadResult result = 
            expenseApplicationService.uploadCsvAndAddExpenses(mockFile, CsvParserService.CsvFormat.MITSUISUMITOMO_OLD_FORMAT);
        
        // 検証
        assertNotNull(result);
        assertEquals(1, result.successCount());
        assertEquals(0, result.errorCount());
        
        // リポジトリが呼ばれたことを確認（フォールバック処理で「その他」が設定される）
        verify(expenseRepository, times(1)).saveAll(anyList());
    }
}

