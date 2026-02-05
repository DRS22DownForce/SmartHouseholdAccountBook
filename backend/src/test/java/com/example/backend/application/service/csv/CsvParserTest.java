package com.example.backend.application.service.csv;

import com.example.backend.application.service.csv.model.CsvParseResult;
import com.example.backend.application.service.csv.model.CsvParsedExpense;
import com.example.backend.application.service.csv.mitsuisumitomo.MitsuiSumitomoNewCsvParser;
import com.example.backend.application.service.csv.mitsuisumitomo.MitsuiSumitomoOldCsvParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

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
                2025/11/01,やよい軒大阪店,1220,１,１,1220,
                2025/11/01,マツモトキヨシ大阪駅前店,480,１,１,480,
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertThat(result.validExpenses()).hasSize(2);
            assertThat(result.errors()).isEmpty();
            CsvParsedExpense expense1 = result.validExpenses().get(0);
            assertThat(expense1.description()).isEqualTo("やよい軒大阪店");
            assertThat(expense1.date()).isEqualTo(LocalDate.of(2025, 11, 1));
            assertThat(expense1.amount()).isEqualTo(1220);
            CsvParsedExpense expense2 = result.validExpenses().get(1);
            assertThat(expense2.description()).isEqualTo("マツモトキヨシ大阪駅前店");
            assertThat(expense2.date()).isEqualTo(LocalDate.of(2025, 11, 1));
            assertThat(expense2.amount()).isEqualTo(480);
        }

        @Test
        @DisplayName("日付の形式が不正な場合はエラーを返す")
        void parse_日付が不正な場合はエラーを返す() throws IOException {
            String csvContent = """
                2025-11-01,やよい軒大阪店,1220,１,１,1220,
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertThat(result.validExpenses()).isEmpty();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().get(0).message()).isEqualTo("日付の形式が不正です: 2025-11-01");
        }

        @Test
        @DisplayName("店名が空の場合はエラーを返す")
        void parse_店名が空の場合はエラーを返す() throws IOException {
            String csvContent = """
                2025/11/01,,1220,１,１,1220,
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertThat(result.validExpenses()).isEmpty();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().get(0).message()).isEqualTo("店名が空です");
        }

        @Test
        @DisplayName("空行はスキップされる")
        void parse_空行はスキップされる() throws IOException {
            String csvContent = """

                2025/11/01,やよい軒大阪店,1220,１,１,1220,

                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertThat(result.validExpenses()).hasSize(1);
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("カード情報行はスキップされる")
        void parse_カード情報行はスキップされる() throws IOException {
            String csvContent = """
                テスト　ユーザー　様,1234-56**-****-****,三井住友ゴールドＶＩＳＡ（ＮＬ）
                2025/11/01,やよい軒大阪店,1220,１,１,1220,
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertThat(result.validExpenses()).hasSize(1);
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("店名にカンマが含まれる場合も正常に解析できる")
        void parse_店名にカンマが含まれる場合も解析できる() throws IOException {
            String csvContent = """
                2025/11/06,CURSOR, AI POWERED IDE (CURSOR.COM ),3200,１,１,3200,20.00　USD　160.042　11 06
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertThat(result.validExpenses()).hasSize(1);
            assertThat(result.errors()).isEmpty();
            assertThat(result.validExpenses().get(0).description()).isEqualTo("CURSOR, AI POWERED IDE (CURSOR.COM )");
            assertThat(result.validExpenses().get(0).amount()).isEqualTo(3200);
        }

        @Test
        @DisplayName("有効なデータとエラーが混在する場合")
        void parse_有効なデータとエラーが混在する場合() throws IOException {
            String csvContent = """
                2025/11/01,やよい軒大阪店,1220,１,１,1220,
                2025-11-02,マクドナルド,630,１,１,630,
                2025/11/03,セブン－イレブン,195,１,１,195,
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertThat(result.validExpenses()).hasSize(2);
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().get(0).message()).isEqualTo("日付の形式が不正です: 2025-11-02");
        }

        @Test
        @DisplayName("複数のカード情報行がある場合も正常に解析できる")
        void parse_複数のカード情報行がある場合も解析できる() throws IOException {
            String csvContent = """
                テスト　ユーザー　様,1234-56**-****-****,三井住友ゴールドＶＩＳＡ（ＮＬ）
                2025/11/01,やよい軒大阪店,1220,１,１,1220,
                テスト　ユーザー　様,1234-56**-****-****,ＡｐｐｌｅＰａｙ／ｉＤ
                2025/11/01,駅北口駐輪場／ｉＤ,120,１,１,120,
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertThat(result.validExpenses()).hasSize(2);
            assertThat(result.errors()).isEmpty();
            assertThat(result.validExpenses().get(0).description()).isEqualTo("やよい軒大阪店");
            assertThat(result.validExpenses().get(1).description()).isEqualTo("駅北口駐輪場／ｉＤ");
        }

        @Test
        @DisplayName("合計行はスキップされる")
        void parse_合計行はスキップされる() throws IOException {
            String csvContent = """
                2025/11/01,やよい軒大阪店,1220,１,１,1220,
                ,,,,,302155,
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertThat(result.validExpenses()).hasSize(1);
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("実際のCSVデータ形式で正常に解析できる")
        void parse_実際のCSVデータ形式で解析できる() throws IOException {
            String csvContent = """
                テスト　ユーザー　様,1234-56**-****-****,三井住友ゴールドＶＩＳＡ（ＮＬ）
                2025/11/01,やよい軒大阪店,1220,１,１,1220,
                2025/11/01,マツモトキヨシ大阪駅前店,480,１,１,480,
                2025/11/01,マクドナルド,630,１,１,630,
                2025/11/06,CURSOR, AI POWERED IDE (CURSOR.COM ),3200,１,１,3200,20.00　USD　160.042　11 06
                テスト　ユーザー　様,1234-56**-****-****,ＡｐｐｌｅＰａｙ／ｉＤ
                2025/11/01,駅北口駐輪場／ｉＤ,120,１,１,120,
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertThat(result.validExpenses()).hasSize(5);
            assertThat(result.errors()).isEmpty();
            assertThat(result.validExpenses().get(0).description()).isEqualTo("やよい軒大阪店");
            assertThat(result.validExpenses().get(0).amount()).isEqualTo(1220);
            assertThat(result.validExpenses().get(3).description()).isEqualTo("CURSOR, AI POWERED IDE (CURSOR.COM )");
            assertThat(result.validExpenses().get(3).amount()).isEqualTo(3200);
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
                2026/1/31,ＪＲ東日本モバイルＳｕｉｃａ,ご本人,1回払い,,'26/02,5000,5000,,,,,
                2026/1/31,セブンーイレブン,ご本人,1回払い,,'26/02,754,754,,,,,
                2026/1/28,ＡＰＰＬＥ  ＣＯＭ  ＢＩＬＬ,ご本人,1回払い,,'26/02,1450,1450,,,,,
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertThat(result.validExpenses()).hasSize(3);
            assertThat(result.errors()).isEmpty();
            CsvParsedExpense expense1 = result.validExpenses().get(0);
            assertThat(expense1.description()).isEqualTo("ＪＲ東日本モバイルＳｕｉｃａ");
            assertThat(expense1.date()).isEqualTo(LocalDate.of(2026, 1, 31));
            assertThat(expense1.amount()).isEqualTo(5000);
            CsvParsedExpense expense2 = result.validExpenses().get(1);
            assertThat(expense2.description()).isEqualTo("セブンーイレブン");
            assertThat(expense2.amount()).isEqualTo(754);
        }

        @Test
        @DisplayName("店名にカンマとスペースが含まれる場合も正常に解析できる")
        void parse_店名にカンマとスペースが含まれる場合も解析できる() throws IOException {
            String csvContent = """
                2026/1/6,ＣＵＲＳＯＲ，  ＡＩ  ＰＯＷＥＲＥＤ  Ｉ,ご本人,1回払い,,'26/02,3256,,,20.00,USD,162.822,01/06
                """;
            InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(SHIFT_JIS));

            CsvParseResult result = parser.parse(inputStream);

            assertThat(result.validExpenses()).hasSize(1);
            assertThat(result.errors()).isEmpty();
            assertThat(result.validExpenses().get(0).description()).isEqualTo("ＣＵＲＳＯＲ，  ＡＩ  ＰＯＷＥＲＥＤ  Ｉ");
            assertThat(result.validExpenses().get(0).amount()).isEqualTo(3256);
        }
    }
}
