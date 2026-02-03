package com.example.backend.application.service.csv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CSVパーサーのユニットテスト
 */
class CsvParserTest {

    private static final Charset SHIFT_JIS = Charset.forName("Shift_JIS");

    @Nested
    @DisplayName("MitsuiSumitomoOldCsvParser")
    class MitsuiSumitomoOldCsvParserTest {

        private final MitsuiSumitomoOldCsvParser parser = new MitsuiSumitomoOldCsvParser();

        @Test
        @DisplayName("旧形式のCSVを正常に解析できる")
        void parse_正常に解析できる() throws IOException {
            String csvContent = """
                ご利用日,ご利用店名 ※,ご利用金額,支払区分,今回回数,お支払い金額　現地通貨額,略称,換算レート,換算日
                2024/1/1,テスト店1,1000,一括,1,1000,JPY,1.0,2024/1/1
                2024/1/2,テスト店2,2000,一括,1,2000,JPY,1.0,2024/1/2
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertEquals(2, result.validExpenses().size());
            assertEquals(0, result.errors().size());
            CsvParsedExpense expense1 = result.validExpenses().get(0);
            assertEquals("テスト店1", expense1.description());
            assertEquals(LocalDate.of(2024, 1, 1), expense1.date());
            assertEquals(1000, expense1.amount());
            CsvParsedExpense expense2 = result.validExpenses().get(1);
            assertEquals("テスト店2", expense2.description());
            assertEquals(LocalDate.of(2024, 1, 2), expense2.date());
            assertEquals(2000, expense2.amount());
        }

        @Test
        @DisplayName("日付の形式が不正な場合はエラーを返す")
        void parse_日付が不正な場合はエラーを返す() throws IOException {
            String csvContent = """
                ご利用日,ご利用店名 ※,ご利用金額,支払区分,今回回数,お支払い金額　現地通貨額,略称,換算レート,換算日
                2024-1-1,テスト店1,1000,一括,1,1000,JPY,1.0,2024/1/1
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertEquals(0, result.validExpenses().size());
            assertEquals(1, result.errors().size());
            assertTrue(result.errors().get(0).message().contains("日付の形式が不正です"));
        }

        @Test
        @DisplayName("店名が空の場合はエラーを返す")
        void parse_店名が空の場合はエラーを返す() throws IOException {
            String csvContent = """
                ご利用日,ご利用店名 ※,ご利用金額,支払区分,今回回数,お支払い金額　現地通貨額,略称,換算レート,換算日
                2024/1/1,,1000,一括,1,1000,JPY,1.0,2024/1/1
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertEquals(0, result.validExpenses().size());
            assertEquals(1, result.errors().size());
            assertTrue(result.errors().get(0).message().contains("店名が空です"));
        }

        @Test
        @DisplayName("空行はスキップされる")
        void parse_空行はスキップされる() throws IOException {
            String csvContent = """
                ご利用日,ご利用店名 ※,ご利用金額,支払区分,今回回数,お支払い金額　現地通貨額,略称,換算レート,換算日

                2024/1/1,テスト店1,1000,一括,1,1000,JPY,1.0,2024/1/1

                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertEquals(1, result.validExpenses().size());
            assertEquals(0, result.errors().size());
        }

        @Test
        @DisplayName("カード情報行はスキップされる")
        void parse_カード情報行はスキップされる() throws IOException {
            String csvContent = """
                ご利用日,ご利用店名 ※,ご利用金額,支払区分,今回回数,お支払い金額　現地通貨額,略称,換算レート,換算日
                テスト　ユーザー　様,4980-00**-****-****,三井住友ゴールドＶＩＳＡ（ＮＬ）
                2024/1/1,テスト店1,1000,一括,1,1000,JPY,1.0,2024/1/1
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertEquals(1, result.validExpenses().size());
            assertEquals(0, result.errors().size());
        }

        @Test
        @DisplayName("店名にカンマが含まれる場合も正常に解析できる")
        void parse_店名にカンマが含まれる場合も解析できる() throws IOException {
            String csvContent = """
                ご利用日,ご利用店名 ※,ご利用金額,支払区分,今回回数,お支払い金額　現地通貨額,略称,換算レート,換算日
                2024/1/6,CURSOR, AI POWERED IDE (CURSOR.COM ),3200,１,１,3200,
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertEquals(1, result.validExpenses().size());
            assertEquals(0, result.errors().size());
            assertEquals("CURSOR,AI POWERED IDE (CURSOR.COM )", result.validExpenses().get(0).description());
            assertEquals(3200, result.validExpenses().get(0).amount());
        }

        @Test
        @DisplayName("有効なデータとエラーが混在する場合")
        void parse_有効なデータとエラーが混在する場合() throws IOException {
            String csvContent = """
                ご利用日,ご利用店名 ※,ご利用金額,支払区分,今回回数,お支払い金額　現地通貨額,略称,換算レート,換算日
                2024/1/1,テスト店1,1000,一括,1,1000,JPY,1.0,2024/1/1
                2024-1-2,テスト店2,2000,一括,1,2000,JPY,1.0,2024/1/2
                2024/1/3,テスト店3,3000,一括,1,3000,JPY,1.0,2024/1/3
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertEquals(2, result.validExpenses().size());
            assertEquals(1, result.errors().size());
        }
    }

    @Nested
    @DisplayName("MitsuiSumitomoNewCsvParser")
    class MitsuiSumitomoNewCsvParserTest {

        private final MitsuiSumitomoNewCsvParser parser = new MitsuiSumitomoNewCsvParser();

        @Test
        @DisplayName("新形式のCSVを正常に解析できる")
        void parse_正常に解析できる() throws IOException {
            String csvContent = """
                ご利用日,ご利用店名 ※,カード,支払区分,分割回数,支払予定月,ご利用金額　現地通貨額　略称,換算レート,換算日
                2024/1/1,テスト店1,4980-00**-****-****,一括,1,'24/01,1000,JPY,1.0,2024/1/1
                2024/1/2,テスト店2,4980-00**-****-****,一括,1,'24/01,2000,JPY,1.0,2024/1/2
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertEquals(2, result.validExpenses().size());
            assertEquals(0, result.errors().size());
            CsvParsedExpense expense1 = result.validExpenses().get(0);
            assertEquals("テスト店1", expense1.description());
            assertEquals(LocalDate.of(2024, 1, 1), expense1.date());
            assertEquals(1000, expense1.amount());
        }

        @Test
        @DisplayName("金額が7列目にある場合を正常に解析できる")
        void parse_金額が7列目にある場合を解析できる() throws IOException {
            String csvContent = """
                ご利用日,ご利用店名 ※,カード,支払区分,分割回数,支払予定月,ご利用金額　現地通貨額　略称,換算レート,換算日
                2024/1/1,テスト店1,4980-00**-****-****,一括,1,'24/01,5000,JPY,1.0,2024/1/1
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertEquals(1, result.validExpenses().size());
            assertEquals(0, result.errors().size());
            assertEquals(5000, result.validExpenses().get(0).amount());
        }
    }
}
