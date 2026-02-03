package com.example.backend.application.service.csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * 三井住友カード 新形式（2026/1以降）のCSVパーサー
 *
 * 列構成: ご利用日,ご利用店名,カード,支払区分,分割回数,支払予定月,ご利用金額,...
 * 店名と金額の間に4列（カード・支払区分・分割回数・支払予定月）が固定で存在する。
 * 店名にカンマが含まれる場合、列6以降で金額を探し、その手前から逆算して店名範囲を決める。
 * 1行目はヘッダーのためスキップする。
 */
@Component
public class MitsuiSumitomoNewCsvParser implements CsvParser {

    private static final Logger logger = LoggerFactory.getLogger(MitsuiSumitomoNewCsvParser.class);

    private static final MitsuiSumitomoCsvParseUtil.Config CONFIG = new MitsuiSumitomoCsvParseUtil.Config(
            0,  // dateColumn
            1,  // descriptionColumn
            6,  // amountStartColumn
            7,  // minColumnCount
            4,  // columnsBetweenDescriptionAndAmount
            true // skipFirstLine
    );

    @Override
    public CsvParseResult parse(InputStream inputStream) throws IOException {
        return MitsuiSumitomoCsvParseUtil.parse(inputStream, CONFIG, logger);
    }
}
