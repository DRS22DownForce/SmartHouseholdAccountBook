package com.example.backend.application.service.csv;

import com.example.backend.application.service.csv.model.CsvParseResult;

import java.io.IOException;
import java.io.InputStream;

/**
 * CSV解析のインターフェース
 * 
 * カード種別ごとに専用の実装クラスがこのインターフェースを実装します。
 */
public interface CsvParser {

    /**
     * CSVファイルを解析してCsvParseResultに変換
     *
     * @param inputStream CSVファイルの入力ストリーム
     * @return 解析結果（成功したデータとエラー情報を含む）
     */
    CsvParseResult parse(InputStream inputStream) throws IOException;
}
