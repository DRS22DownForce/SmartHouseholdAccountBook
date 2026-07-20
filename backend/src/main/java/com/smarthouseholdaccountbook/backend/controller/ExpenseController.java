package com.smarthouseholdaccountbook.backend.controller;

import com.smarthouseholdaccountbook.backend.application.mapper.ExpenseMapper;
import com.smarthouseholdaccountbook.backend.application.service.ExpenseApplicationService;
import com.smarthouseholdaccountbook.backend.controller.support.MonthParameterParser;
import com.smarthouseholdaccountbook.backend.entity.Expense;
import com.smarthouseholdaccountbook.backend.entity.ExpenseUpdate;
import com.smarthouseholdaccountbook.backend.generated.api.ExpensesApi;
import com.smarthouseholdaccountbook.backend.generated.model.ExpenseDto;
import com.smarthouseholdaccountbook.backend.generated.model.ExpensePageDto;
import com.smarthouseholdaccountbook.backend.generated.model.ExpenseRequestDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

/**
 * 支出の一覧・追加・更新・削除を担当する REST API コントローラー。
 *
 * <p>集計、CSV インポート、月次レポートは、それぞれ専用の Controller が担当します。</p>
 */
@RestController
public class ExpenseController implements ExpensesApi {

    private final ExpenseApplicationService expenseApplicationService;
    private final ExpenseMapper expenseMapper;

    /**
     * コンストラクタ
     *
     * @param expenseApplicationService 支出アプリケーションサービス
     * @param expenseMapper             支出マッパー
     */
    public ExpenseController(
            ExpenseApplicationService expenseApplicationService,
            ExpenseMapper expenseMapper) {
        this.expenseApplicationService = expenseApplicationService;
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
        // HTTP の文字列パラメータを YearMonth に変換してから Service へ渡す
        YearMonth yearMonth = MonthParameterParser.parse(month);
        Pageable pageable = PageRequest.of(page, size);
        Page<Expense> expensePage = expenseApplicationService.getExpensesByMonth(yearMonth, pageable);

        List<ExpenseDto> content = expensePage.getContent().stream()
                .map(expenseMapper::toDto)
                .toList();
        
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

}