package com.example.backend.application.service.csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
 * 三井住友カード 旧形式（2025/12以前）のCSVパーサー
 * 
 * 列構成: ご利用日,ご利用店名,ご利用金額,支払区分,今回回数,お支払い金額,...
 * 店名にカンマが含まれる場合があるため、金額は検索開始列（2）以降で有効な数値を探す。
 */
@Component
public class MitsuiSumitomoOldCsvParser implements CsvParser {

    private static final Logger logger = LoggerFactory.getLogger(MitsuiSumitomoOldCsvParser.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d");
    private static final int DATE_COLUMN = 0;
    private static final int DESCRIPTION_COLUMN = 1;
    private static final int AMOUNT_START_COLUMN = 2;
    private static final int MIN_COLUMN_COUNT = 3;
    private static final Charset CHARSET = Charset.forName("Shift_JIS");

    @Override
    public CsvParseResult parse(InputStream inputStream) throws IOException {
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
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (isCardInfoLine(line)) {
                    continue;
                }

                try {
                    validExpenses.add(parseLine(line, lineNumber));
                } catch (IllegalArgumentException e) {
                    logger.warn("CSV行の解析に失敗: 行番号={}, 行内容={}, エラー={}",
                            lineNumber, line, e.getMessage());
                    errors.add(new CsvParseError(lineNumber, line, e.getMessage()));
                }
            }
        }

        if (!errors.isEmpty()) {
            logger.warn("CSV解析で{}件のエラーが発生しました", errors.size());
        }

        return new CsvParseResult(validExpenses, errors);
    }

    private CsvParsedExpense parseLine(String line, int lineNumber) {
        String[] columns = line.split(",", -1);

        if (columns.length < MIN_COLUMN_COUNT) {
            throw new IllegalArgumentException("列数が不足しています（最低" + MIN_COLUMN_COUNT + "列必要）");
        }

        String dateStr = columns[DATE_COLUMN].trim();
        if (dateStr.isEmpty()) {
            throw new IllegalArgumentException("日付が空です");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日付の形式が不正です: " + dateStr);
        }

        String description = columns[DESCRIPTION_COLUMN].trim();
        if (description.isEmpty()) {
            throw new IllegalArgumentException("店名が空です");
        }

        Integer amount = null;
        for (int i = AMOUNT_START_COLUMN; i < columns.length; i++) {
            Optional<Integer> opt = tryParseAmount(columns[i].trim());
            if (opt.isPresent()) {
                amount = opt.get();
                break;
            }
        }

        if (amount == null) {
            logger.warn("金額解析失敗: 行番号={}, 列内容={}", lineNumber, java.util.Arrays.toString(columns));
            throw new IllegalArgumentException("有効な金額が見つかりません");
        }

        return new CsvParsedExpense(description, date, amount, "その他");
    }

    private Optional<Integer> tryParseAmount(String amountStr) {
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

    private boolean isCardInfoLine(String line) {
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
