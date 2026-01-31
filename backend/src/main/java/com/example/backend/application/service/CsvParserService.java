package com.example.backend.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV解析サービス
 * 
 * このサービスはCSVファイルの内容を解析し、生のデータ（CsvParsedExpense）のリストに変換します。
 * 三井住友カードの利用明細CSVフォーマットに対応しています。
 * 
 * 対応するCSVフォーマット（2つの形式に対応）:
 * 
 * 【三井住友カード 旧形式（2025/12以前）】
 * - ヘッダー行: ご利用日,ご利用店名 ※,ご利用金額,支払区分,今回回数,お支払い金額　現地通貨額,略称,換算レート,換算日
 * - 金額は3列目（インデックス2）
 * 
 * 【三井住友カード 新形式（2026/1以降）】
 * - ヘッダー行: ご利用日,ご利用店名 ※,カード,支払区分,分割回数,支払予定月,ご利用金額　現地通貨額　略称,換算レート,換算日
 * - 金額は7列目（インデックス6）以降
 * 
 * 【共通】
 * - カード情報行（スキップ）
 * 
 * 将来的に他のカード会社を追加する際は、このサービスの拡張を検討してください。
 */
@Service
public class CsvParserService {

    private static final Logger logger = LoggerFactory.getLogger(CsvParserService.class);

    // 日付フォーマッター（1桁または2桁の月・日に対応）
    // M: 1桁または2桁の月、d: 1桁または2桁の日
    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d");

    /**
     * CSV形式の列挙型
     * 
     * 三井住友カードのクレジットカード明細のCSVフォーマットが変更されたため、2つの形式に対応します。
     * 将来的に他のカード会社を追加する際は、この列挙型を拡張するか、カード会社と形式を組み合わせた新しい列挙型を検討します。
     */
    public enum CsvFormat {
        /**
         * 三井住友カード 旧形式（2025/12以前）
         * 列構成: ご利用日, ご利用店名, ご利用金額, 支払区分, 今回回数, ...
         * 金額は3列目（インデックス2）
         */
        MITSUISUMITOMO_OLD_FORMAT,
        
        /**
         * 三井住友カード 新形式（2026/1以降）
         * 列構成: ご利用日, ご利用店名, カード, 支払区分, 分割回数, 支払予定月, ご利用金額, ...
         * 金額は7列目（インデックス6）以降
         */
        MITSUISUMITOMO_NEW_FORMAT
    }

    /**
     * CSVファイルを解析してCsvParsedExpenseのリストに変換
     * 
     * UTF-8とShift-JISの両方のエンコーディングに対応しています。
     * 日本のクレジットカード明細はShift-JISで作成されることが多いため、
     * まずShift-JISで試し、次にUTF-8で試します。
     * より多くのデータが成功し、文字化けが少ない方の結果を返します。
     * 
     * @param inputStream CSVファイルの入力ストリーム
     * @param csvFormat CSV形式（MITSUISUMITOMO_OLD_FORMAT: 三井住友カード 2025/12以前、MITSUISUMITOMO_NEW_FORMAT: 三井住友カード 2026/1以降）
     * @return 解析結果（成功したデータとエラー情報を含む）
     */
    public CsvParseResult parseCsv(InputStream inputStream, CsvFormat csvFormat) {
        // ストリームを読み込んでバイト配列に変換（複数のエンコーディングを試すため）
        byte[] bytes;
        try {
            bytes = inputStream.readAllBytes();
        } catch (Exception e) {
            // CSVファイルの読み込みに失敗した場合、エラーログを出力
            // logger.error()は重大なエラーを記録します
            // 第1引数: メッセージ、第2引数: 例外オブジェクト（スタックトレースも記録されます）
            logger.error("CSVファイルの読み込みに失敗しました", e);
            return new CsvParseResult(
                new ArrayList<>(),
                List.of(new CsvParseError(0, "", "CSVファイルの読み込みに失敗しました: " + e.getMessage()))
            );
        }

        // まずShift-JISで試す（日本のクレジットカード明細はShift-JISが多い）
        CsvParseResult shiftJisResult = null;
        try {
            Charset shiftJis = Charset.forName("Shift_JIS");
            shiftJisResult = parseCsvWithEncoding(new ByteArrayInputStream(bytes), shiftJis, csvFormat);
        } catch (Exception e) {
            // Shift-JISがサポートされていない場合は無視
        }

        // UTF-8で試す
        CsvParseResult utf8Result = parseCsvWithEncoding(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8, csvFormat);

        // どちらの結果が良いか判定
        // 1. 成功したデータが多い方を選ぶ
        // 2. 同じ場合は、文字化けが少ない方（日本語文字が含まれている方）を選ぶ
        if (shiftJisResult != null) {
            int shiftJisScore = calculateScore(shiftJisResult);
            int utf8Score = calculateScore(utf8Result);
            
            if (shiftJisScore > utf8Score) {
                return shiftJisResult;
            } else if (utf8Score > shiftJisScore) {
                return utf8Result;
            } else {
                // スコアが同じ場合は、成功したデータが多い方を選ぶ
                if (shiftJisResult.validExpenses().size() >= utf8Result.validExpenses().size()) {
                    return shiftJisResult;
                }
            }
        }

        // Shift-JISの結果が取得できなかった場合はUTF-8の結果を返す
        return utf8Result;
    }

    /**
     * 解析結果のスコアを計算
     * 
     * 成功したデータの数と、日本語文字が含まれているかを考慮してスコアを計算します。
     * 
     * @param result 解析結果
     * @return スコア（高いほど良い）
     */
    private int calculateScore(CsvParseResult result) {
        int score = result.validExpenses().size() * 10; // 成功したデータ1件につき10点
        
        // 日本語文字が含まれているデータの数をカウント
        int japaneseCount = 0;
        for (CsvParsedExpense expense : result.validExpenses()) {
            String description = expense.description();
            if (description != null && containsJapanese(description)) {
                japaneseCount++;
            }
        }
        
        score += japaneseCount * 5; // 日本語が含まれているデータ1件につき5点追加
        score -= result.errors().size(); // エラー1件につき1点減点
        
        return score;
    }

    /**
     * 文字列に日本語文字（ひらがな、カタカナ、漢字）が含まれているかチェック
     * 
     * @param text チェックする文字列
     * @return 日本語文字が含まれている場合true
     */
    private boolean containsJapanese(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // 文字化け文字（）が含まれている場合は文字化けしていると判断
        if (text.contains("")) {
            return false;
        }
        
        // ひらがな、カタカナ、漢字の範囲をチェック
        for (char c : text.toCharArray()) {
            if ((c >= 0x3040 && c <= 0x309F) || // ひらがな
                (c >= 0x30A0 && c <= 0x30FF) || // カタカナ
                (c >= 0x4E00 && c <= 0x9FAF)) { // 漢字
                return true;
            }
        }
        return false;
    }

    /**
     * 指定されたエンコーディングでCSVファイルを解析
     * 
     * @param inputStream CSVファイルの入力ストリーム
     * @param charset 文字エンコーディング
     * @param csvFormat CSV形式（MITSUISUMITOMO_OLD_FORMAT: 三井住友カード 2025/12以前、MITSUISUMITOMO_NEW_FORMAT: 三井住友カード 2026/1以降）
     * @return 解析結果（成功したデータとエラー情報を含む）
     */
    private CsvParseResult parseCsvWithEncoding(InputStream inputStream, Charset charset, CsvFormat csvFormat) {
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

                // 空行をスキップ
                if (line.isEmpty()) {
                    continue;
                }

                // ヘッダー行をスキップ
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // カード情報行をスキップ（カンマ区切りで2列目にカード番号のような形式がある行）
                if (isCardInfoLine(line)) {
                    continue;
                }

                // 合計行や空の日付の行をスキップ（日付が空の行は合計行の可能性が高い）
                String[] columns = line.split(",", -1);
                if (columns.length > 0 && columns[0].trim().isEmpty()) {
                    continue;
                }

                // CSV行を解析（指定された形式を使用）
                try {
                    CsvParsedExpense expense = parseCsvLine(line, lineNumber, csvFormat);
                    if (expense != null) {
                        validExpenses.add(expense);
                    }
                } catch (Exception e) {
                    // 行の解析に失敗した場合、警告ログを出力
                    // logger.warn()は処理は続行できるが注意が必要な場合に使用します
                    // 行番号、行内容、エラーメッセージを記録してデバッグしやすくします
                    logger.warn("CSV行の解析に失敗しました: 行番号={}, 行内容={}, エラー={}", 
                        lineNumber, line, e.getMessage());
                    errors.add(new CsvParseError(lineNumber, line, e.getMessage()));
                }
            }
        } catch (Exception e) {
            // エンコーディングでの読み込みに失敗した場合、警告ログを出力
            // エンコーディング名とエラーメッセージを記録します
            logger.warn("CSVファイルの読み込みに失敗しました（エンコーディング: {}）: {}", 
                charset.name(), e.getMessage(), e);
            errors.add(new CsvParseError(0, "", "CSVファイルの読み込みに失敗しました（エンコーディング: " + charset.name() + "）: " + e.getMessage()));
        }

        // エラーが発生した場合、サマリーをログに出力
        // エラー件数とエンコーディング情報を記録して、どのエンコーディングで問題が発生したかを把握できます
        if (!errors.isEmpty()) {
            logger.warn("CSV解析で{}件のエラーが発生しました（エンコーディング: {}）", 
                errors.size(), charset.name());
        }

        return new CsvParseResult(validExpenses, errors);
    }

    /**
     * CSV行を解析してCsvParsedExpenseに変換
     * 
     * @param line CSV行
     * @param lineNumber 行番号（エラーメッセージ用）
     * @param format CSV形式（旧形式または新形式）
     * @return CsvParsedExpense（解析に失敗した場合はnull）
     * @throws IllegalArgumentException 解析エラー
     */
    private CsvParsedExpense parseCsvLine(String line, int lineNumber, CsvFormat format) {
        // CSV行をカンマで分割
        String[] columns = line.split(",", -1);

        // 最低限の列数チェック（日付、店名、金額は必須）
        if (columns.length < 3) {
            throw new IllegalArgumentException("列数が不足しています（最低3列必要）");
        }

        // 日付を取得（1列目）
        String dateStr = columns[0].trim();
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

        // 店名を取得（2列目）→ 説明として使用
        String description = columns[1].trim();
        if (description.isEmpty()) {
            throw new IllegalArgumentException("店名が空です");
        }

        // 金額を取得（形式に応じて適切な列から取得）
        Integer amount = null;
        
        if (format == CsvFormat.MITSUISUMITOMO_OLD_FORMAT) {
            // 旧形式: 金額は通常3列目（インデックス2）にあるが、
            // 店名にカンマが含まれている場合など、列の位置がずれる可能性がある
            // そのため、2列目以降を順に確認して、最初に見つかった有効な金額を使用
            
            // まず、通常の位置（3列目、インデックス2）を確認
            if (columns.length > 2) {
                String amountStr = columns[2].trim();
                amount = tryParseAmount(amountStr, 2);
            }
            
            // 3列目で金額が見つからない場合、他の列も順に確認（フォールバック）
            // 店名にカンマが含まれている場合など、列の位置がずれる可能性があるため
            if (amount == null) {
                // 2列目以降を順に確認（店名の列はスキップするため、2列目から開始）
                // tryParseAmountメソッドが適切に判定するため、ここでは全ての列を確認
                for (int i = 2; i < columns.length; i++) {
                    String candidateStr = columns[i].trim();
                    if (!candidateStr.isEmpty()) {
                        Integer candidateAmount = tryParseAmount(candidateStr, i);
                        if (candidateAmount != null && candidateAmount > 0) {
                            amount = candidateAmount;
                            break; // 最初に見つかった有効な金額を使用
                        }
                    }
                }
            }
            
            // デバッグログ: 金額が見つからない場合の詳細情報
            if (amount == null) {
                logger.warn("旧形式の金額解析失敗: 行番号={}, 列数={}, 列内容={}", 
                    lineNumber, columns.length, java.util.Arrays.toString(columns));
            }
        } else {
            // 新形式: 金額は7列目（インデックス6）以降
            // 6列目以降を順に確認して、最初に見つかった有効な金額を使用
            for (int i = 6; i < columns.length; i++) {
                String amountStr = columns[i].trim();
                if (!amountStr.isEmpty()) {
                    Integer candidateAmount = tryParseAmount(amountStr, i);
                    if (candidateAmount != null && candidateAmount > 0) {
                        amount = candidateAmount;
                        break; // 最初に見つかった有効な金額を使用
                    }
                }
            }
        }
        
        // 金額が見つからない場合
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("金額が見つかりません。形式に応じた列を確認しましたが、有効な金額が見つかりませんでした。");
        }

        // カテゴリーは自動分類できないため、デフォルトで「その他」を設定
        // 将来的にAI分類を追加することも可能
        String category = "その他";

        // CsvParsedExpenseを作成（生のデータを保持）
        return new CsvParsedExpense(description, date, amount, category);
    }

    /**
     * 金額文字列を数値に変換を試みる
     * 
     * カンマや通貨記号を除去して数値に変換します。
     * 全角数字も半角数字に変換して処理します。
     * 変換に失敗した場合はnullを返します。
     * 小数点を含む金額（例：162.822）は除外します（外貨建ての金額や換算レートの可能性があるため）。
     * 日付形式（'26/02など）は除外します。
     * 
     * 注意: 以前は100未満の金額を無視していましたが、実際には2円などの小額取引も存在するため、
     * 1円以上の金額を有効な金額として扱います。
     * 
     * @param amountStr 金額文字列
     * @param columnIndex 列のインデックス（デバッグ用、現在は使用していない）
     * @return 変換された金額（変換に失敗した場合はnull）
     */
    private Integer tryParseAmount(String amountStr, int columnIndex) {
        if (amountStr == null || amountStr.isEmpty()) {
            return null;
        }
        
        // 日付形式を除外（'で始まり/を含む文字列）
        // 例：「'26/02」など（支払予定月の形式）
        if (amountStr.startsWith("'") && amountStr.contains("/")) {
            return null;
        }
        
        // 小数点を含む文字列は除外する（外貨建ての金額や換算レートの可能性がある）
        // 例：「162.822」「20.00」など
        // ただし、小数点の前後が数字のみで、かつ整数部分が有効な金額の可能性がある場合は考慮する
        // しかし、外貨建ての金額や換算レートの可能性が高いため、小数点を含む場合は除外
        if (amountStr.contains(".")) {
            return null;
        }
        
        try {
            // 全角数字を半角数字に変換
            String normalizedAmount = normalizeFullWidthNumbers(amountStr);
            
            // カンマや通貨記号を除去して数値に変換
            String cleanedAmount = normalizedAmount.replaceAll("[^0-9-]", "");
            if (cleanedAmount.isEmpty()) {
                return null;
            }
            int parsedAmount = Integer.parseInt(cleanedAmount);
            
            // 負の値も正の値として扱う（支出なので）
            parsedAmount = Math.abs(parsedAmount);
            
            // 0以下の場合はnullを返す（有効な金額ではない）
            if (parsedAmount <= 0) {
                return null;
            }
            
            // 1円以上の金額を有効な金額として扱う
            // 以前は100未満を無視していましたが、実際には2円などの小額取引も存在するため、
            // 1円以上の金額を有効な金額として扱います
            return parsedAmount;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 全角数字を半角数字に変換
     * 
     * 三井住友カードのCSVでは、金額が全角数字で記載される場合があります。
     * 全角数字（０-９）を半角数字（0-9）に変換します。
     * 
     * @param str 変換する文字列
     * @return 全角数字を半角数字に変換した文字列
     */
    private String normalizeFullWidthNumbers(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        
        // 全角数字を半角数字に変換
        return str.replace('０', '0')
                  .replace('１', '1')
                  .replace('２', '2')
                  .replace('３', '3')
                  .replace('４', '4')
                  .replace('５', '5')
                  .replace('６', '6')
                  .replace('７', '7')
                  .replace('８', '8')
                  .replace('９', '9');
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
     * @param errors 解析エラーのリスト
     */
    public record CsvParseResult(
        List<CsvParsedExpense> validExpenses,
        List<CsvParseError> errors
    ) {}

    /**
     * CSV解析エラーを保持するレコード
     * 
     * @param lineNumber エラーが発生した行番号
     * @param lineContent エラーが発生した行の内容
     * @param message エラーメッセージ
     */
    public record CsvParseError(
        int lineNumber,
        String lineContent,
        String message
    ) {}

    /**
     * CSVから解析された支出データを保持するレコード
     * 
     * DTOに依存せず、生のデータを保持します。
     * これにより、アプリケーション層のサービスがプレゼンテーション層のDTOに依存しなくなります。
     * 
     * @param description 支出の説明（店名など）
     * @param date 支出日
     * @param amount 支出金額
     * @param category カテゴリ（デフォルトで「その他」）
     */
    public record CsvParsedExpense(
        String description,
        LocalDate date,
        Integer amount,
        String category
    ) {}
}
