package com.smarthouseholdaccountbook.backend.controller.support;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * API の月パラメータを厳密に検証して {@link YearMonth} へ変換する。
 *
 * <p>OpenAPI の正規表現は文字の並びだけを確認するため、2024-99 のような
 * 存在しない月はこのクラスで拒否します。</p>
 */
public final class MonthParameterParser {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM").withResolverStyle(ResolverStyle.STRICT);

    private MonthParameterParser() {
        // インスタンスを持たないユーティリティクラス
    }

    /**
     * yyyy-MM 形式の文字列を年月へ変換する。
     *
     * @param month API から受け取った月
     * @return 変換した年月
     * @throws IllegalArgumentException null、形式不正、存在しない月の場合
     */
    public static YearMonth parse(String month) {
        try {
            return YearMonth.parse(month, FORMATTER);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new IllegalArgumentException(
                    "月の形式が不正です。yyyy-MM で指定してください: " + month,
                    exception);
        }
    }
}
