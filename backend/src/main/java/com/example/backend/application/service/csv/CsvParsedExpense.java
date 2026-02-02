package com.example.backend.application.service.csv;

import java.time.LocalDate;

/**
 * CSVから解析された支出データを保持するレコード
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
