package com.smarthouseholdaccountbook.backend.application.service;

import com.smarthouseholdaccountbook.backend.application.service.csv.CsvFormat;
import com.smarthouseholdaccountbook.backend.application.service.csv.CsvParserFactory;
import com.smarthouseholdaccountbook.backend.application.service.csv.model.CsvParseError;
import com.smarthouseholdaccountbook.backend.application.service.csv.model.CsvParsedExpense;
import com.smarthouseholdaccountbook.backend.application.service.csv.model.CsvParseResult;
import com.smarthouseholdaccountbook.backend.entity.Expense;
import com.smarthouseholdaccountbook.backend.entity.User;
import com.smarthouseholdaccountbook.backend.exception.AiServiceException;
import com.smarthouseholdaccountbook.backend.exception.CsvUploadException;
import com.smarthouseholdaccountbook.backend.exception.QuotaExceededException;
import com.smarthouseholdaccountbook.backend.valueobject.CategoryType;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseAmount;
import com.smarthouseholdaccountbook.backend.valueobject.ExpenseDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV支出処理サービス
 *
 * CSV 解析・重複除外・AI 分類・保存のユースケースを指揮する。
 * DB アクセスは {@link CsvExpensePersistenceService} に委譲し、OpenAI 呼び出し中は TX を張らない。
 */
@Service
public class CsvExpenseService {
    private static final Logger logger = LoggerFactory.getLogger(CsvExpenseService.class);

    private final UserApplicationService userApplicationService;
    private final CsvParserFactory csvParserFactory;
    private final AiCategoryService aiCategoryService;
    private final CsvExpensePersistenceService persistenceService;

    public CsvExpenseService(
            UserApplicationService userApplicationService,
            CsvParserFactory csvParserFactory,
            AiCategoryService aiCategoryService,
            CsvExpensePersistenceService persistenceService) {
        this.userApplicationService = userApplicationService;
        this.csvParserFactory = csvParserFactory;
        this.aiCategoryService = aiCategoryService;
        this.persistenceService = persistenceService;
    }

    /**
     * CSVファイルから支出を一括追加するユースケース
     *
     * 処理フロー:
     * 1. CSV 解析（TX なし）
     * 2. 重複除外（短い readOnly TX）
     * 3. AI カテゴリ分類（TX なし）
     * 4. DB 保存（短い write TX
     * 
     * @param file      CSVファイル
     * @param csvFormat CSV形式（MITSUISUMITOMO_OLD_FORMAT: 三井住友カード 確定月、
     *                  MITSUISUMITOMO_NEW_FORMAT: 三井住友カード 未確定月）
     * @return CSVアップロード結果（成功件数、スキップ件数、エラー件数、エラー詳細）
     * @throws CsvUploadException ファイルの読み込みに失敗した場合、または処理中にエラーが発生した場合
     */
    public CsvUploadResult uploadCsvAndAddExpenses(MultipartFile file, CsvFormat csvFormat) {
        CsvParseResult parseResult;
        try {
            parseResult = csvParserFactory.getParser(csvFormat).parse(file.getInputStream());
        } catch (IOException e) {
            logger.error("CSVファイルの読み込みに失敗しました", e);
            throw new CsvUploadException(
                    "ファイルの読み込みに失敗しました: " + e.getMessage(),
                    e,
                    HttpStatus.BAD_REQUEST);
        }

        if (!parseResult.errors().isEmpty()) {
            logger.warn("CSV解析で{}件のエラーが発生しました", parseResult.errors().size());
        }

        if (parseResult.validExpenses().isEmpty()) {
            logger.warn("CSV解析結果: 有効なデータが0件でした。エラー件数: {}", parseResult.errors().size());
            return new CsvUploadResult(
                    0,
                    parseResult.errors().size(),
                    0,
                    parseResult.errors());
        }

        User user = userApplicationService.getUser();

        CsvExpensePersistenceService.FilterNewExpensesResult filterResult =
                persistenceService.filterNewExpenses(parseResult.validExpenses(), user);

        if (filterResult.skippedCount() > 0) {
            logger.info("CSV重複除外: {}件をスキップしました", filterResult.skippedCount());
        }

        if (filterResult.newExpenses().isEmpty()) {
            return new CsvUploadResult(
                    0,
                    parseResult.errors().size(),
                    filterResult.skippedCount(),
                    parseResult.errors());
        }

        List<Expense> expenses = buildExpensesWithAiClassification(filterResult.newExpenses(), user);
        int savedCount = persistenceService.saveExpenses(expenses);

        return new CsvUploadResult(
                savedCount,
                parseResult.errors().size(),
                filterResult.skippedCount(),
                parseResult.errors());
    }

    /**
     * AI カテゴリ分類を適用してエンティティを作成する（TX 外）。
     */
    private List<Expense> buildExpensesWithAiClassification(
            List<CsvParsedExpense> parsedExpenses,
            User user) {
        if (parsedExpenses.isEmpty()) {
            throw new IllegalArgumentException("解析された支出データのリストは空です");
        }

        try {
            List<String> descriptions = parsedExpenses.stream()
                    .map(CsvParsedExpense::description)
                    .filter(desc -> desc != null && !desc.trim().isEmpty())
                    .toList();

            Map<String, CategoryType> categoryMap = new HashMap<>();
            if (!descriptions.isEmpty()) {
                categoryMap = aiCategoryService.predictCategoriesBatch(descriptions);
            }

            return createExpenseEntities(parsedExpenses, user, categoryMap, false);

        } catch (QuotaExceededException | AiServiceException e) {
            logger.error("AIカテゴリ分類の適用に失敗しました: データ件数={}, すべて「その他」を設定して処理を続行",
                    parsedExpenses.size(), e);
            return createExpenseEntities(parsedExpenses, user, null, true);
        }
    }

    private List<Expense> createExpenseEntities(
            List<CsvParsedExpense> parsedExpenses,
            User user,
            Map<String, CategoryType> categoryMap,
            boolean isFallback) {
        List<Expense> expenses = new ArrayList<>();

        for (CsvParsedExpense parsed : parsedExpenses) {
            ExpenseAmount amount = new ExpenseAmount(parsed.amount());
            ExpenseDate date = new ExpenseDate(parsed.date());

            CategoryType category;
            if (isFallback) {
                category = CategoryType.OTHER;
            } else {
                category = categoryMap.getOrDefault(parsed.description(), CategoryType.OTHER);
            }

            expenses.add(new Expense(
                    parsed.description(),
                    amount,
                    date,
                    category,
                    user));
        }

        return expenses;
    }

    /**
     * CSVアップロード結果を保持するレコード
     */
    public record CsvUploadResult(
            int successCount,
            int errorCount,
            int skippedCount,
            List<CsvParseError> errors) {
    }
}
