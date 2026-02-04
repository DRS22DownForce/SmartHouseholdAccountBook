package com.example.backend.application.service.csv.mitsuisumitomo;

import com.example.backend.application.service.csv.CsvParser;
import com.example.backend.application.service.csv.model.CsvParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * 三井住友カード 旧形式（2025/12以前）のCSVパーサー
 *
 * 列構成: ご利用日,ご利用店名,ご利用金額,支払区分,今回回数,お支払い金額,...
 * 店名にカンマが含まれる場合があるため、金額は列2以降で有効な数値を探し、
 * その手前までを店名として結合する。
 */
@Component
public class MitsuiSumitomoOldCsvParser implements CsvParser {

    private static final Logger logger = LoggerFactory.getLogger(MitsuiSumitomoOldCsvParser.class);

    private static final MitsuiSumitomoCsvParseUtil.Config CONFIG = new MitsuiSumitomoCsvParseUtil.Config(
            0,  // dateColumn
            1,  // descriptionColumn
            2,  // amountStartColumn
            3,  // minColumnCount
            0,  // columnsBetweenDescriptionAndAmount
            true // skipFirstLine
    );

    @Override
    public CsvParseResult parse(InputStream inputStream) throws IOException {
        return MitsuiSumitomoCsvParseUtil.parse(inputStream, CONFIG, logger);
    }
}
