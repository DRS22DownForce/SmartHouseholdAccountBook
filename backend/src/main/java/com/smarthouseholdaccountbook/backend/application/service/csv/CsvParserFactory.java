package com.smarthouseholdaccountbook.backend.application.service.csv;

import com.smarthouseholdaccountbook.backend.application.service.csv.mitsuisumitomo.MitsuiSumitomoConfirmedMonthCsvParser;
import com.smarthouseholdaccountbook.backend.application.service.csv.mitsuisumitomo.MitsuiSumitomoUnconfirmedMonthCsvParser;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * CSV形式に応じて適切なパーサーを返すファクトリ。
 *
 * 呼び出し側は {@link CsvFormat} だけを渡せばよく、
 * 確定月 / 未確定月の列の違いを意識しなくてよい。
 */
@Component
public class CsvParserFactory {

    private final Map<CsvFormat, CsvParser> parsers;

    public CsvParserFactory(
            MitsuiSumitomoConfirmedMonthCsvParser confirmedMonthParser,
            MitsuiSumitomoUnconfirmedMonthCsvParser unconfirmedMonthParser) {
        this.parsers = new EnumMap<>(CsvFormat.class);
        this.parsers.put(CsvFormat.MITSUISUMITOMO_CONFIRMED_MONTH, confirmedMonthParser);
        this.parsers.put(CsvFormat.MITSUISUMITOMO_UNCONFIRMED_MONTH, unconfirmedMonthParser);
    }

    public CsvParser getParser(CsvFormat format) {
        return parsers.get(format);
    }
}
