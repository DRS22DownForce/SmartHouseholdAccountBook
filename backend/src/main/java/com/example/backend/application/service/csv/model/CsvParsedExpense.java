package com.example.backend.application.service.csv.model;

import java.time.LocalDate;

/**
 * CSVから解析された支出データを保持するレコード
 *
 * カテゴリはCSVには含まれないため、AI分類で決定される。
 *
 * @param description 支出の説明（店名など）
 * @param date        支出日
 * @param amount      支出金額
 */
public record CsvParsedExpense(
        String description,
        LocalDate date,
        Integer amount) {
}
