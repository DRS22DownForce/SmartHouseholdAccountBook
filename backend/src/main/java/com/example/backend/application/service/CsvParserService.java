package com.example.backend.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CSV解析サービス
 * 
 * このサービスはCSVファイルの内容を解析し、生のデータ（CsvParsedExpense）のリストに変換します。
 * 三井住友カードの利用明細CSVフォーマットに対応しています。
 * 
 * 対応するCSVフォーマット（2つの形式に対応）:
 * 
 * 【三井住友カード 旧形式（2025/12以前）】
 * - ご利用日,ご利用店名,ご利用金額,支払区分,今回回数,お支払い金額 現地通貨額,略称,換算レート,換算日
 * 
 * 【三井住友カード 旧形式（2025/12以前）】
 * - ご利用日,ご利用店名,カード,支払区分,分割回数,支払予定月,ご利用金額,（お支払い総額）,（内手数料）, 現地通貨額, 略称,換算レート,換算日
 * 
 * 【共通】
 * - カード情報行（スキップ）
 * 
 * 将来的に他のカード会社を追加する際は、このサービスの拡張を検討してください。
 */
@Service
public class CsvParserService {

    private static final Logger logger = LoggerFactory.getLogger(CsvParserService.class);

    // 日付フォーマッター
    // M: 1桁または2桁の月、d: 1桁または2桁の日
    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d");

    /**
     * CSV形式の列挙型
     */
    public enum CsvFormat {
        /**
         * 三井住友カード 旧形式（2025/12以前）
         */
        MITSUISUMITOMO_OLD_FORMAT(Charset.forName("Shift_JIS"), 0, 1, 2, 3),

        /**
         * 三井住友カード 新形式（2026/1以降）
         */
        MITSUISUMITOMO_NEW_FORMAT(Charset.forName("Shift_JIS"), 0, 1, 6, 7);

        private final Charset charset;
        private final int dateColumnIndex;
        private final int descriptionColumnIndex;
        private final int amountColumnIndex;
        private final int minColumnCount;

        /**
         * charset: エンコーディング、dateColumnIndex: 日付列、descriptionColumnIndex:
         * 店名列、amountColumnIndex: 金額検索開始列、minColumnCount: 最低列数
         */
        CsvFormat(Charset charset, int dateColumnIndex, int descriptionColumnIndex,
                int amountColumnIndex, int minColumnCount) {
            this.charset = charset;
            this.dateColumnIndex = dateColumnIndex;
            this.descriptionColumnIndex = descriptionColumnIndex;
            this.amountColumnIndex = amountColumnIndex;
            this.minColumnCount = minColumnCount;
        }

        public Charset getCharset() {
            return charset;
        }

        public int getDateColumnIndex() {
            return dateColumnIndex;
        }

        public int getDescriptionColumnIndex() {
            return descriptionColumnIndex;
        }

        public int getAmountColumnIndex() {
            return amountColumnIndex;
        }

        public int getMinColumnCount() {
            return minColumnCount;
        }
    }

    /**
     * CSVファイルを解析してCsvParsedExpenseのリストに変換
     * 
     * 各フォーマットに定義されたエンコーディング（Charset）で1回だけ解析します。
     * 三井住友カードはShift-JIS、将来的にUTF-8のフォーマットを追加する場合はenumにStandardCharsets.UTF_8を指定します。
     * 
     * @param inputStream CSVファイルの入力ストリーム
     * @param csvFormat CSV形式（フォーマットごとのエンコーディング・列構造で解析）
     * @return 解析結果（成功したデータとエラー情報を含む）
     */
    public CsvParseResult parseCsv(InputStream inputStream, CsvFormat csvFormat) throws IOException {
        Charset charset = csvFormat.getCharset();
        List<CsvParsedExpense> validExpenses = new ArrayList<>();
        List<CsvParseError> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, charset))) {

            String line;
            int lineNumber = 0;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (isCardInfoLine(line)) {
                    continue;
                }

                try {
                    validExpenses.add(parseCsvLine(line, lineNumber, csvFormat));
                } catch (IllegalArgumentException e) {
                    logger.warn("CSV行の解析に失敗しました: 行番号={}, 行内容={}, エラー={}",
                        lineNumber, line, e.getMessage());
                    errors.add(new CsvParseError(lineNumber, line, e.getMessage()));
                }
            }
        }

        if (!errors.isEmpty()) {
            logger.warn("CSV解析で{}件のエラーが発生しました（エンコーディング: {}）",
                errors.size(), charset.name());
        }

        return new CsvParseResult(validExpenses, errors);
    }

    /**
     * CSV行を解析してCsvParsedExpenseに変換
     *
     * @param line       CSV行
     * @param lineNumber 行番号（エラーメッセージ用）
     * @param format     CSV形式
     * @return CsvParsedExpense
     * @throws IllegalArgumentException 解析エラー
     */
    private CsvParsedExpense parseCsvLine(String line, int lineNumber, CsvFormat format) {
        // CSV行をカンマで分割
        String[] columns = line.split(",", -1);

        // フォーマットごとの最低列数チェック
        if (columns.length < format.getMinColumnCount()) {
            throw new IllegalArgumentException("列数が不足しています（最低" + format.getMinColumnCount() + "列必要）");
        }

        // 日付を取得（フォーマットごとの列インデックス）
        String dateStr = columns[format.getDateColumnIndex()].trim();
        if (dateStr.isEmpty()) {
            throw new IllegalArgumentException("日付が空です");
        }

        // 日付フォーマット変換（YYYY/M/D または YYYY/MM/DD → LocalDate）
        // 1桁または2桁の月・日に対応（例: "2026/1/10" または "2026/01/10"）
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, INPUT_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日付の形式が不正です（期待: YYYY/M/D または YYYY/MM/DD）: " + dateStr);
        }

        // 店名を取得（フォーマットごとの列インデックス）→ 説明として使用
        String description = columns[format.getDescriptionColumnIndex()].trim();
        if (description.isEmpty()) {
            throw new IllegalArgumentException("店名が空です");
        }

        // 金額を取得（検索開始列以降で有効な金額を探す）
        // 店名にカンマが含まれる場合、列がずれるため、検索開始列以降を順に確認して最初の有効な金額を取得
        Integer amount = null;
        int amountStart = format.getAmountColumnIndex();
        for (int i = amountStart; i < columns.length; i++) {
            Optional<Integer> amountOpt = tryParseAmount(columns[i].trim());
            if (amountOpt.isPresent()) {
                amount = amountOpt.get();
                break;
            }
        }

        if (amount == null) {
            logger.warn("金額解析失敗: 行番号={}, 列内容={}", lineNumber, java.util.Arrays.toString(columns));
            throw new IllegalArgumentException("有効な金額が見つかりません");
        }

        // カテゴリーは自動分類できないため、デフォルトで「その他」を設定
        String category = "その他";

        return new CsvParsedExpense(description, date, amount, category);
    }

    /**
     * 金額文字列を数値に変換を試みる
     * 
     * 数字のみで構成される文字列を金額として認識します。
     * 店名など数字以外の文字を含む場合はnullを返します。
     * 
     * @param amountStr 金額文字列
     * @return 変換された金額（正の整数）、無効な場合はnull
     */
    private Optional<Integer> tryParseAmount(String amountStr) {
        if (amountStr.isEmpty()) {
            return Optional.empty();
        }

        // 数字とマイナス記号以外を除去
        String cleanedAmount = amountStr.replaceAll("[^0-9-]", "");
        if (cleanedAmount.isEmpty()) {
            return Optional.empty();
        }

        try {
            int amount = Integer.parseInt(cleanedAmount);
            return Optional.of(amount);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * カード情報行かどうかを判定
     * 
     * カード情報行の特徴:
     * - 2列目にカード番号のような形式（数字とハイフン、アスタリスクを含む）がある
     * - 1列目が日付形式でない（名前などが含まれる）
     * 
     * @param line CSV行
     * @return カード情報行の場合true
     */
    private boolean isCardInfoLine(String line) {
        String[] columns = line.split(",", -1);
        if (columns.length < 2) {
            return false;
        }

        // 1列目が日付形式でない場合（カード情報行の可能性が高い）
        String firstColumn = columns[0].trim();
        if (!firstColumn.isEmpty() && !firstColumn.matches("\\d{4}/\\d{1,2}/\\d{1,2}")) {
            // 2列目にカード番号のような形式（数字、ハイフン、アスタリスクを含む）を検出
            String secondColumn = columns[1].trim();
            // より柔軟なパターン: 数字4桁-数字2桁**-****-**** または 数字4桁-数字2桁**-****-**** の形式
            if (secondColumn.matches(".*\\d{4}-\\d{2}\\*{2}-\\*{4}-\\*{4}.*")) {
                return true;
            }
        }

        return false;
    }

    /**
     * CSV解析結果を保持するレコード
     * 
     * @param validExpenses 正常に解析された支出データのリスト（生のデータ）
     * @param errors        解析エラーのリスト
     */
    public record CsvParseResult(
            List<CsvParsedExpense> validExpenses,
            List<CsvParseError> errors) {
    }

    /**
     * CSV解析エラーを保持するレコード
     * 
     * @param lineNumber  エラーが発生した行番号
     * @param lineContent エラーが発生した行の内容
     * @param message     エラーメッセージ
     */
    public record CsvParseError(
            int lineNumber,
            String lineContent,
            String message) {
    }

    /**
     * CSVから解析された支出データを保持するレコード
     * 
     * DTOに依存せず、生のデータを保持します。
     * これにより、アプリケーション層のサービスがプレゼンテーション層のDTOに依存しなくなります。
     * 
     * @param description 支出の説明（店名など）
     * @param date        支出日
     * @param amount      支出金額
     * @param category    カテゴリ（デフォルトで「その他」）
     */
    public record CsvParsedExpense(
            String description,
            LocalDate date,
            Integer amount,
            String category) {
    }
}
