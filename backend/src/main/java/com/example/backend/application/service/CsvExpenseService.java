package com.example.backend.application.service;

import com.example.backend.application.service.csv.CsvFormat;
import com.example.backend.application.service.csv.CsvParseError;
import com.example.backend.application.service.csv.CsvParsedExpense;
import com.example.backend.application.service.csv.CsvParseResult;
import com.example.backend.application.service.csv.CsvParserFactory;
import com.example.backend.domain.repository.ExpenseRepository;
import com.example.backend.domain.valueobject.CategoryType;
import com.example.backend.domain.valueobject.ExpenseAmount;
import com.example.backend.domain.valueobject.ExpenseDate;
import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import com.example.backend.exception.AiServiceException;
import com.example.backend.exception.CsvUploadException;
import com.example.backend.exception.QuotaExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV支出処理サービス
 * 
 * このサービスはCSVファイルから支出データを一括追加する処理を担当します。
 * CSVファイルの解析、AIカテゴリ分類の適用、エンティティの作成、データベースへの保存までを一貫して処理します。
 * 
 */
@Service
@Transactional
public class CsvExpenseService {
    private static final Logger logger = LoggerFactory.getLogger(CsvExpenseService.class);

    private final ExpenseRepository expenseRepository;
    private final UserApplicationService userApplicationService;
    private final CsvParserFactory csvParserFactory;
    private final AiCategoryService aiCategoryService;

    public CsvExpenseService(
            ExpenseRepository expenseRepository,
            UserApplicationService userApplicationService,
            CsvParserFactory csvParserFactory,
            AiCategoryService aiCategoryService) {
        this.expenseRepository = expenseRepository;
        this.userApplicationService = userApplicationService;
        this.csvParserFactory = csvParserFactory;
        this.aiCategoryService = aiCategoryService;
    }

    /**
     * CSVファイルから支出を一括追加するユースケース
     * 
     * CSVファイルを解析し、AIカテゴリ分類を適用してから、支出エンティティを作成してデータベースに保存します。
     * 部分成功をサポートし、一部の行でエラーが発生しても、正常な行は保存されます。
     * 
     * 処理フロー:
     * 1. CSVファイルを解析（CsvParserFactoryで取得したパーサーを使用）
     * 2. ユーザー情報を取得
     * 3. AIカテゴリ分類を適用してエンティティを作成
     * 4. データベースに一括保存
     * 5. 結果（成功件数、エラー件数、エラー詳細）を返す
     * 
     * @param file      CSVファイル
     * @param csvFormat CSV形式（MITSUISUMITOMO_OLD_FORMAT: 三井住友カード
     *                  2025/12以前、MITSUISUMITOMO_NEW_FORMAT: 三井住友カード 2026/1以降）
     * @return CSVアップロード結果（成功件数、エラー件数、エラー詳細）
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
                    parseResult.errors());
        }

        User user = userApplicationService.getUser();

        // AIカテゴリ分類を適用してエンティティを作成
        List<Expense> expenses = applyAiCategoryClassificationAndCreateEntities(
                parseResult.validExpenses(),
                user);

        // 一括保存
        List<Expense> savedExpenses = expenseRepository.saveAll(expenses);

        // 結果を返す
        return new CsvUploadResult(
                savedExpenses.size(),
                parseResult.errors().size(),
                parseResult.errors());
    }

    /**
     * AIカテゴリ分類を適用してエンティティを作成
     * 
     * CSVから解析された支出データに対して、AIカテゴリ分類を適用し、支出エンティティのリストを作成します。
     * AI分類が失敗した場合は、すべて「その他」カテゴリを設定してフォールバック処理を行います。
     * 
     * 処理フロー:
     * 1. 説明文を収集（nullや空文字列を除外）
     * 2. AIカテゴリ分類をバッチ処理で実行
     * 3. 分類結果を使用してエンティティを作成
     * 
     * @param parsedExpenses CSVから解析された支出データのリスト（nullまたは空リストは想定されていない）
     * @param user           ユーザーエンティティ
     * @return エンティティのリスト
     * @throws IllegalArgumentException parsedExpensesがnullまたは空の場合
     */
    private List<Expense> applyAiCategoryClassificationAndCreateEntities(
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

            // AIカテゴリ分類をバッチ処理で実行
            Map<String, CategoryType> categoryMap = new LinkedHashMap<String, CategoryType>();
            if (!descriptions.isEmpty()) {
                categoryMap = aiCategoryService.predictCategoriesBatch(descriptions);
            }

            // エンティティを作成
            return createExpenseEntities(parsedExpenses, user, categoryMap, false);

        } catch (QuotaExceededException | AiServiceException e) {
            // AI分類が失敗した場合、エラーログを出力してフォールバック処理に移行
            logger.error("AIカテゴリ分類の適用に失敗しました: データ件数={}, すべて「その他」を設定して処理を続行",
                    parsedExpenses.size(), e);

            // フォールバック処理: すべて「その他」を設定
            return createExpenseEntities(parsedExpenses, user, null, true);
        }
    }

    /**
     * エンティティを作成する共通処理
     * 
     * 解析された支出データとカテゴリ分類結果から、支出エンティティのリストを作成します。
     * カテゴリマップがnullの場合や、説明文がマップに存在しない場合は「その他」カテゴリを使用します。
     * 
     * 値オブジェクトの作成:
     * - ExpenseAmount: 金額を値オブジェクトとしてラップ（バリデーションを含む）
     * - ExpenseDate: 日付を値オブジェクトとしてラップ（バリデーションを含む）
     * - Category: カテゴリを値オブジェクトとしてラップ（バリデーションを含む）
     * 
     * @param parsedExpenses 解析された支出データのリスト
     * @param user           ユーザーエンティティ
     * @param categoryMap    AI分類結果のマップ（nullの場合は「その他」を使用）
     * @param isFallback     フォールバック処理かどうか
     * @return エンティティのリスト
     */
    private List<Expense> createExpenseEntities(
            List<CsvParsedExpense> parsedExpenses,
            User user,
            Map<String, CategoryType> categoryMap,
            boolean isFallback) {
        List<Expense> expenses = new ArrayList<>();

        for (CsvParsedExpense parsed : parsedExpenses) {
            // 値オブジェクトを作成
            ExpenseAmount amount = new ExpenseAmount(parsed.amount());
            ExpenseDate date = new ExpenseDate(parsed.date());

            // カテゴリを決定
            CategoryType category;
            if (isFallback) {
                // フォールバック処理
                category = CategoryType.OTHER;
            } else {
                // AI分類結果を使用
                category = categoryMap.getOrDefault(parsed.description(), CategoryType.OTHER);
            }

            // エンティティを作成
            Expense expense = new Expense(
                    parsed.description(),
                    amount,
                    date,
                    category,
                    user);
            expenses.add(expense);
        }

        return expenses;
    }

    /**
     * CSVアップロード結果を保持するレコード
     * 
     * CSVファイルのアップロード処理の結果を保持します。
     * 部分成功をサポートするため、成功件数とエラー件数を分けて管理します。
     * 
     * @param successCount 成功件数（データベースに保存された支出の件数）
     * @param errorCount   エラー件数（CSV解析でエラーが発生した行の件数）
     * @param errors       エラー詳細のリスト（行番号、行内容、エラーメッセージを含む）
     */
    public record CsvUploadResult(
            int successCount,
            int errorCount,
            List<CsvParseError> errors) {
    }
}
