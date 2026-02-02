package com.example.backend.application.service.csv;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * CSV形式に応じて適切なパーサーを返すファクトリ
 */
@Component
public class CsvParserFactory {

    private final Map<CsvFormat, CsvParser> parsers;

    public CsvParserFactory(
            MitsuiSumitomoOldCsvParser oldParser,
            MitsuiSumitomoNewCsvParser newParser) {
        this.parsers = new EnumMap<>(CsvFormat.class);
        this.parsers.put(CsvFormat.MITSUISUMITOMO_OLD_FORMAT, oldParser);
        this.parsers.put(CsvFormat.MITSUISUMITOMO_NEW_FORMAT, newParser);
    }

    public CsvParser getParser(CsvFormat format) {
        return parsers.get(format);
    }
}
