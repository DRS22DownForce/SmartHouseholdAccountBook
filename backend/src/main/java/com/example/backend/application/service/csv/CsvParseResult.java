package com.example.backend.application.service.csv;

import java.util.List;

/**
 * CSV解析結果を保持するレコード
 *
 * @param validExpenses 正常に解析された支出データのリスト
 * @param errors        解析エラーのリスト
 */
public record CsvParseResult(
        List<CsvParsedExpense> validExpenses,
        List<CsvParseError> errors) {
}
