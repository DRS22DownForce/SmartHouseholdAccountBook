package com.example.backend.controller;

import com.example.backend.application.mapper.ExpenseMapper;
import com.example.backend.application.service.CsvExpenseService;
import com.example.backend.application.service.ExpenseApplicationService;
import com.example.backend.application.service.csv.CsvFormat;
import com.example.backend.entity.Expense;
import com.example.backend.entity.ExpenseUpdate;
import com.example.backend.generated.api.ExpensesApi;
import com.example.backend.generated.model.CsvUploadResponseDto;
import com.example.backend.generated.model.ExpenseDto;
import com.example.backend.generated.model.ExpensePageDto;
import com.example.backend.generated.model.ExpenseRequestDto;
import com.example.backend.generated.model.MonthlySummaryDto;
import com.example.backend.valueobject.MonthlySummary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private static final String CSV_FORMAT_PATTERN = "MITSUISUMITOMO_OLD_FORMAT|MITSUISUMITOMO_NEW_FORMAT";  //csvFormatで許可する値の正規表現
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

    /**
     * 支出一覧取得エンドポイント（ページネーション対応）
     * OpenAPIはPage, Pageable型をサポートしていないので、変換処理が必要
     *
     * @param month 対象月（YYYY-MM形式、必須）
     * @param page  ページ番号（0始まり）
     * @param size  1ページあたりの件数（最大50）
     * @return 支出のページDTO
     */
    @Override
    public ResponseEntity<ExpensePageDto> apiExpensesGet(String month, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Expense> expensePage = expenseApplicationService.getExpensesByMonth(month, pageable);

        List<ExpenseDto> content = expensePage.getContent().stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
        
        ExpensePageDto dto = new ExpensePageDto(
                content,
                expensePage.getTotalElements(), //全体の件数
                expensePage.getTotalPages(), //総ページ数
                expensePage.getNumber(), //現在のページ番号（0始まり）
                expensePage.getSize()); //1ページあたりの件数
        return ResponseEntity.ok(dto);
    }

    /**
     * 支出追加エンドポイント
     * 
     * @param expenseRequestDto 支出追加リクエストDTO
     * @return 追加後の支出DTO
     */
    @Override
    public ResponseEntity<ExpenseDto> apiExpensesPost(ExpenseRequestDto expenseRequestDto) {
        ExpenseUpdate creation = expenseMapper.toExpenseUpdate(expenseRequestDto);
        Expense expense = expenseApplicationService.addExpense(creation);
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseMapper.toDto(expense));
    }

    /**
     * 支出削除エンドポイント
     * 
     * @param id 支出ID
     * @return 削除後の支出DTO
     */
    @Override
    public ResponseEntity<Void> apiExpensesIdDelete(Long id) {
        expenseApplicationService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 支出更新エンドポイント
     * 
     * @param id 支出ID
     * @param expenseRequestDto 支出更新リクエストDTO
     * @return 更新後の支出DTO
     */
    @Override
    public ResponseEntity<ExpenseDto> apiExpensesIdPut(Long id, ExpenseRequestDto expenseRequestDto) {
        ExpenseUpdate update = expenseMapper.toExpenseUpdate(expenseRequestDto);
        Expense expense = expenseApplicationService.updateExpense(id, update);
        return ResponseEntity.ok(expenseMapper.toDto(expense));
    }

    /**
     * 月別サマリー取得エンドポイント
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
     * 
     * @param file      アップロードされたCSVファイル
     * @param csvFormat CSV形式（MITSUISUMITOMO_OLD_FORMAT: 三井住友カード
     *                  2025/12以前、MITSUISUMITOMO_NEW_FORMAT: 三井住友カード 2026/1以降）
     * @return CSVアップロード結果（成功件数、エラー件数、エラー詳細）
     */
    @Override
    public ResponseEntity<CsvUploadResponseDto> apiExpensesUploadCsvPost(
            MultipartFile file,
            String csvFormat) {
        validateCsvUploadRequest(file, csvFormat);

        CsvFormat format = CsvFormat.valueOf(csvFormat);

        // CSV処理を実行（部分成功をサポート）
        CsvExpenseService.CsvUploadResult result = csvExpenseService.uploadCsvAndAddExpenses(file, format);

        // MapperでDTOに変換
        CsvUploadResponseDto response = expenseMapper.toDto(result);

        return ResponseEntity.ok(response);
    }
  
    /**
     * CSVアップロードのリクエストを検証する。
     * インターフェースのバリデーションのオーバライドができないので、メソッド内で追加で検証する。
     * 
     * @param file      アップロードファイル
     * @param csvFormat CSV形式
     * @throws IllegalArgumentException 検証エラー時（GlobalExceptionHandlerで400に変換される）
     */
    private void validateCsvUploadRequest(MultipartFile file, String csvFormat) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("ファイルが空です");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("CSVファイルを選択してください");
        }
        if (csvFormat.isBlank()) {
            throw new IllegalArgumentException("CSV形式を指定してください");
        }
        if (!csvFormat.matches(CSV_FORMAT_PATTERN)) {
            throw new IllegalArgumentException(
                    "無効なCSV形式です。MITSUISUMITOMO_OLD_FORMAT（三井住友カード 2025/12以前）またはMITSUISUMITOMO_NEW_FORMAT（三井住友カード 2026/1以降）を指定してください");
        }
    }

}