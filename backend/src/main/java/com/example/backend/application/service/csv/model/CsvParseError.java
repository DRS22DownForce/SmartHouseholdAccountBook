package com.example.backend.application.service.csv.model;

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
