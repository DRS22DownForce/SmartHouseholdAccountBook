package com.example.backend.controller;

import com.example.backend.application.mapper.ExpenseMapper;
import com.example.backend.application.service.CsvExpenseService;
import com.example.backend.application.service.csv.CsvFormat;
import com.example.backend.application.service.ExpenseApplicationService;
import com.example.backend.domain.valueobject.MonthlySummary;
import com.example.backend.generated.api.ExpensesApi;
import com.example.backend.generated.model.CsvUploadResponseDto;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import com.example.backend.generated.model.MonthlySummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 支出に関するREST APIコントローラー
 * 
 * このコントローラーは支出のCRUD操作を提供します。
 * アプリケーションサービスを呼び出してビジネスロジックを実行します。
 */
@RestController
public class ExpenseController implements ExpensesApi {
    private final ExpenseApplicationService expenseApplicationService;
    private final CsvExpenseService csvExpenseService;
    private final ExpenseMapper expenseMapper;

    /**
     * コンストラクタ
     * 
     * @param expenseApplicationService 支出アプリケーションサービス
     * @param csvExpenseService         CSV支出処理サービス
     * @param expenseMapper             支出マッパー
     */
    public ExpenseController(
            ExpenseApplicationService expenseApplicationService,
            CsvExpenseService csvExpenseService,
            ExpenseMapper expenseMapper) {
        this.expenseApplicationService = expenseApplicationService;
        this.csvExpenseService = csvExpenseService;
        this.expenseMapper = expenseMapper;
    }

    @Override
    public ResponseEntity<List<ExpenseDto>> apiExpensesGet(String month) {
        // monthパラメータが指定されている場合は月別フィルタリング、指定されていない場合は全データ取得
        if (month != null && !month.isEmpty()) {
            // TODO 現状ではページネーションに対応できていない。今後対応予定。
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<ExpenseDto> expensePage = expenseApplicationService.getExpensesByMonth(month, pageable);
            return ResponseEntity.ok(expensePage.getContent());
        } else {
            // 全データ取得（既存の機能を維持）
            List<ExpenseDto> expenseDtos = expenseApplicationService.getExpenses();
            return ResponseEntity.ok(expenseDtos);
        }
    }

    @Override
    public ResponseEntity<ExpenseDto> apiExpensesPost(ExpenseRequestDto expenseRequestDto) {
        ExpenseDto expenseDto = expenseApplicationService.addExpense(expenseRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseDto);
    }

    @Override
    public ResponseEntity<Void> apiExpensesIdDelete(Long id) {
        expenseApplicationService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ExpenseDto> apiExpensesIdPut(Long id, ExpenseRequestDto expenseRequestDto) {
        ExpenseDto expenseDto = expenseApplicationService.updateExpense(id, expenseRequestDto);
        return ResponseEntity.ok(expenseDto);
    }

    /**
     * 月別サマリー取得エンドポイント
     * 
     * DDDの原則に従い、ServiceからMonthlySummary値オブジェクトを取得し、MapperでDTOに変換します。
     * 
     * @param month 対象月（YYYY-MM形式）
     * @return 月別サマリーDTO
     */
    @Override
    public ResponseEntity<MonthlySummaryDto> apiExpensesSummaryGet(String month) {
        // 1. ServiceからMonthlySummary値オブジェクトを取得
        MonthlySummary monthlySummary = expenseApplicationService.getMonthlySummary(month);

        // 2. MapperでDTOに変換
        MonthlySummaryDto dto = expenseMapper.toDto(monthlySummary);

        // 3. レスポンスを返す
        return ResponseEntity.ok(dto);
    }

    /**
     * 範囲指定で月別サマリー取得エンドポイント
     * 
     * DDDの原則に従い、ServiceからMonthlySummary値オブジェクトのリストを取得し、MapperでDTOのリストに変換します。
     * 
     * @param startMonth 開始月（YYYY-MM形式）
     * @param endMonth   終了月（YYYY-MM形式）
     * @return 月別サマリーDTOのリスト
     */
    @Override
    public ResponseEntity<List<MonthlySummaryDto>> apiExpensesSummaryRangeGet(String startMonth, String endMonth) {
        // 1. ServiceからMonthlySummary値オブジェクトのリストを取得
        List<MonthlySummary> monthlySummaries = expenseApplicationService.getMonthlySummaryRange(startMonth, endMonth);

        // 2. MapperでDTOのリストに変換
        List<MonthlySummaryDto> dtos = monthlySummaries.stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());

        // 3. レスポンスを返す
        return ResponseEntity.ok(dtos);
    }

    /**
     * 利用可能な月のリスト取得エンドポイント
     * 
     * @return 利用可能な月のリスト（YYYY-MM形式）
     */
    @Override
    public ResponseEntity<List<String>> apiExpensesMonthsGet() {
        // 1. Serviceから利用可能な月のリストを取得
        List<String> months = expenseApplicationService.getAvailableMonths();

        // 2. レスポンスを返す
        return ResponseEntity.ok(months);
    }

    /**
     * CSVファイルアップロードエンドポイント
     * 
     * このメソッドはコントローラー層として、HTTPリクエストの受け取りとレスポンスの返却に専念します。
     * 例外処理はService層で行われるため、ここでは例外をキャッチする必要はありません。
     * 
     * バリデーションエラー（処理開始前）はIllegalArgumentExceptionをスローし、
     * GlobalExceptionHandlerでErrorResponseを返します。
     * 部分成功の場合はCsvUploadResponseDtoを返します。
     * 処理全体が失敗した場合はService層でCsvUploadExceptionがスローされ、
     * GlobalExceptionHandlerでErrorResponseを返します。
     * 
     * @param file      アップロードされたCSVファイル
     * @param csvFormat CSV形式（MITSUISUMITOMO_OLD_FORMAT: 三井住友カード
     *                  2025/12以前、MITSUISUMITOMO_NEW_FORMAT: 三井住友カード 2026/1以降）
     * @return CSVアップロード結果（成功件数、エラー件数、エラー詳細）
     * @throws IllegalArgumentException バリデーションエラー（ファイルが空、形式が不正など）
     */
    @Override
    public ResponseEntity<CsvUploadResponseDto> apiExpensesUploadCsvPost(
            @RequestPart("file") MultipartFile file,
            @RequestParam("csvFormat") String csvFormat) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("ファイルが空です");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("CSVファイルを選択してください");
        }

        // CSV形式のバリデーション
        if (csvFormat == null || csvFormat.isEmpty()) {
            throw new IllegalArgumentException("CSV形式を指定してください");
        }

        if (!csvFormat.equals("MITSUISUMITOMO_OLD_FORMAT") && !csvFormat.equals("MITSUISUMITOMO_NEW_FORMAT")) {
            throw new IllegalArgumentException(
                    "無効なCSV形式です。MITSUISUMITOMO_OLD_FORMAT（三井住友カード 2025/12以前）またはMITSUISUMITOMO_NEW_FORMAT（三井住友カード 2026/1以降）を指定してください");
        }

        CsvFormat format = CsvFormat.valueOf(csvFormat);

        // CSV処理を実行（部分成功をサポート）
        CsvExpenseService.CsvUploadResult result = csvExpenseService.uploadCsvAndAddExpenses(file, format);

        // MapperでDTOに変換
        CsvUploadResponseDto response = expenseMapper.toDto(result);

        return ResponseEntity.ok(response);
    }

}