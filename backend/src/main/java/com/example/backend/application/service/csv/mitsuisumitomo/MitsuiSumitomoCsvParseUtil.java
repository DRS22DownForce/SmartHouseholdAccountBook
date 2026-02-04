package com.example.backend.application.service.csv.mitsuisumitomo;

import com.example.backend.application.service.csv.model.CsvParseError;
import com.example.backend.application.service.csv.model.CsvParseResult;
import com.example.backend.application.service.csv.model.CsvParsedExpense;
import org.slf4j.Logger;

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
 * 三井住友カードCSVの解析で共通して使う処理をまとめたユーティリティ
 */
public final class MitsuiSumitomoCsvParseUtil {

    /**
     * 形式ごとの列設定。拡張性のため、列インデックスなどは共通でも各形式で全て指定する。
     */
    public record Config(
            int dateColumn,
            int descriptionColumn,
            int amountStartColumn,
            int minColumnCount,
            int columnsBetweenDescriptionAndAmount,
            boolean skipFirstLine
    ) {
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d");
    private static final Charset CHARSET = Charset.forName("Shift_JIS");
    private static final int ILLEGAL_AMOUNT_COLUMN = -1;

    private MitsuiSumitomoCsvParseUtil() {
    }

    /**
     * ストリーム全体を解析し、有効な明細とエラー一覧を返す
     */
    public static CsvParseResult parse(
            InputStream inputStream,
            Config config,
            Logger logger) throws IOException {
        List<CsvParsedExpense> validExpenses = new ArrayList<>();
        List<CsvParseError> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, CHARSET))) {

            String line;
            int lineNumber = 0;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }
                if (config.skipFirstLine() && isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (isCardInfoLine(line)) {
                    continue;
                }

                try {
                    validExpenses.add(parseLine(line, lineNumber, config, logger));
                } catch (IllegalArgumentException e) {
                    logger.warn("CSV行の解析に失敗: 行番号={}, 行内容={}, エラー={}",
                            lineNumber, line, e.getMessage());
                    errors.add(new CsvParseError(lineNumber, line, e.getMessage()));
                }
            }
        }

        return new CsvParseResult(validExpenses, errors);
    }

    /**
     * 1行を解析して CsvParsedExpense に変換する
     */
    public static CsvParsedExpense parseLine(String line, int lineNumber, Config config, Logger logger) {
        String[] columns = line.split(",", -1);

        if (columns.length < config.minColumnCount()) {
            throw new IllegalArgumentException("列数が不足しています（最低" + config.minColumnCount() + "列必要）");
        }

        String dateStr = columns[config.dateColumn()].trim();
        if (dateStr.isEmpty()) {
            throw new IllegalArgumentException("日付が空です");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日付の形式が不正です: " + dateStr);
        }

        // 金額を取得する
        // 店名にカンマが含まれている場合を考慮し、金額を先に取得。
        int amountColumnIndex = ILLEGAL_AMOUNT_COLUMN;
        Integer amount = null;
        for (int i = config.amountStartColumn(); i < columns.length; i++) {
            Optional<Integer> opt = tryParseAmount(columns[i].trim());
            if (opt.isPresent()) {
                amount = opt.get();
                amountColumnIndex = i;
                break;
            }
        }

        if (amount == null || amountColumnIndex == ILLEGAL_AMOUNT_COLUMN) {
            logger.warn("金額解析失敗: 行番号={}, 列内容={}", lineNumber, java.util.Arrays.toString(columns));
            throw new IllegalArgumentException("有効な金額が見つかりません");
        }

        // 店名を取得する
        int descriptionEndIndex = amountColumnIndex - config.columnsBetweenDescriptionAndAmount() - 1;
        if (descriptionEndIndex < config.descriptionColumn()) {
            throw new IllegalArgumentException("列構成が不正です（店名の範囲を特定できません）");
        }
        StringBuilder descriptionSb = new StringBuilder();
        for (int i = config.descriptionColumn(); i <= descriptionEndIndex; i++) {
            if (descriptionSb.length() > 0) {
                descriptionSb.append(',');
            }
            descriptionSb.append(columns[i].trim());
        }
        if (descriptionSb.length() == 0) {
            throw new IllegalArgumentException("店名が空です");
        }
        String description = descriptionSb.toString();

        return new CsvParsedExpense(description, date, amount);
    }

    /**
     * 文字列を金額として解釈できる場合はその値を返す
     */
    public static Optional<Integer> tryParseAmount(String amountStr) {
        if (amountStr.isEmpty()) {
            return Optional.empty();
        }
        String cleaned = amountStr.replaceAll("[^0-9-]", "");
        if (cleaned.isEmpty()) {
            return Optional.empty();
        }
        try {
            int amount = Math.abs(Integer.parseInt(cleaned));
            return amount > 0 ? Optional.of(amount) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * カード情報行かどうかを判定する
     */
    public static boolean isCardInfoLine(String line) {
        String[] columns = line.split(",", -1);
        if (columns.length < 2) {
            return false;
        }
        String first = columns[0].trim();
        if (first.isEmpty() || first.matches("\\d{4}/\\d{1,2}/\\d{1,2}")) {
            return false;
        }
        String second = columns[1].trim();
        return second.matches(".*\\d{4}-\\d{2}\\*{2}-\\*{4}-\\*{4}.*");
    }
}
