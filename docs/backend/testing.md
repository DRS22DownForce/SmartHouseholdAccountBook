# バックエンド テスト資料（要約）

このプロジェクトのバックエンドで使っているテスト技術の要点だけをまとめています。

---

## 目次

1. [概要](#概要)
2. [JUnit 5](#junit-5)
3. [Mockito](#mockito)
4. [AssertJ](#assertj)
5. [テスト用DB（H2）](#テスト用dbh2)
6. [Spring Boot テスト](#spring-boot-テスト)
7. [JaCoCo（カバレッジ）](#jacocoカバレッジ)
8. [テストの実行](#テストの実行)

---

## 概要

- **JUnit 5**: テスト実行・アサーション
- **Mockito**: 依存のモック化
- **AssertJ**: 読みやすいアサーション（主に統合テスト）
- **H2**: テスト用インメモリDB
- **Spring Boot Test**: `@SpringBootTest` / `@DataJpaTest` / MockMvc
- **JaCoCo**: カバレッジ測定

---

## JUnit 5

- テストメソッド: `@Test`
- 表示名: `@DisplayName("日本語")`
- 前処理: `@BeforeEach`（各テスト前）, `@BeforeAll`（全テスト前に1回、static）

よく使うアサーション: `assertEquals`, `assertTrue` / `assertFalse`, `assertNull` / `assertNotNull`, `assertThrows(例外クラス, () -> 処理)`.

テストは「準備 → 実行 → 検証」の流れで書く。

---

## Mockito

- 依存を偽装し、DBや外部に頼らずサービス層などをテストするために使う。
- **`@ExtendWith(MockitoExtension.class)`**: Mockito を有効化
- **`@Mock`**: モックオブジェクトの宣言
- **`@InjectMocks`**: テスト対象クラスにモックを注入（コンストラクタ → セッター → フィールドへの注入(リフレクション)の順で試す）
- 使用時は **`import static org.mockito.Mockito.*;`** および **`import static org.mockito.ArgumentMatchers.*;`** で static インポートすると読みやすい。

**スタブ（when）の例**

- 戻り値: `when(リポジトリ.findById(1L)).thenReturn(Optional.of(entity));`
- 例外: `when(リポジトリ.findById(999L)).thenThrow(new NotFoundException());`
- 引数に応じて返す: `when(リポジトリ.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));`（渡された引数をそのまま返すときなど）
- 引数マッチャー: `any()`, `any(クラス.class)`, `eq(値)` で「どんな引数でも」「この型なら」「この値のときだけ」を指定できる。複数引数のうち一部だけ揃えたいときは `eq(値)` と `any()` を組み合わせる。

**検証（verify）の例**

- 呼び出し回数: `verify(リポジトリ, times(1)).save(any());`, `verify(リポジトリ, never()).save(any());`
- 回数指定なし: `verify(リポジトリ).save(any());` は「1回呼ばれたこと」の検証になる。
- 呼ばれた引数: `verify(csvExpenseService, times(1)).uploadCsvAndAddExpenses(eq(mockFile), eq(CsvFormat.MITSUISUMITOMO_OLD_FORMAT));` で、どの引数で呼ばれたかまで検証できる。



---

## AssertJ

- JUnit の `assertEquals` などより読みやすく、失敗時のメッセージも分かりやすいアサーションライブラリ。
- 基本形は **`assertThat(実際の値).isEqualTo(期待値)`**。メソッドチェーンで「何を期待しているか」が自然な日本語のように読める。
- 使用時は **`import static org.assertj.core.api.Assertions.assertThat;`** で static インポートするとよい。
- よく使う例:
  - コレクション: `assertThat(list).hasSize(2);`, `assertThat(list).containsExactly(a, b);`
  - 文字列: `assertThat(s).contains("費");`, `assertThat(s).isNotBlank();`
  - 数値: `assertThat(n).isGreaterThan(0);`, `assertThat(n).isBetween(1, 10);`
  - Optional: `assertThat(opt).isPresent();`, `assertThat(opt).hasValue(期待値);`
- 例外の検証は `assertThatThrownBy(() -> 処理).isInstanceOf(例外クラス.class).hasMessage("メッセージ");` のように書ける。このプロジェクトではリポジトリ・統合テストで主に利用する。
- リストの要素の変換とリストの並び順の検証 `assertThat(messages).extracting(ChatMessage::getCreatedAt).isSortedAccordingTo(Comparator.reverseOrder());`

---

## テスト用DB（H2）

- テスト時だけ使うインメモリDB。
- @DataJpaTestや@SpringBootTestといったSpringコンテキスト(DIコンテナ)を起動するテストが実行されるとH2が起動される

- 設定は **`src/test/resources/application-test.properties`**。
  - `spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1` H2に接続時にtestdbというテーブルが作成される。`DB_CLOSE_DELAY=-1`よりプロセスが終了するまでDBを残す。(テストを実行するプロセスとH2を動かすプロセスは同じ)
  - `spring.jpa.hibernate.ddl-auto=create-drop` コンテキスト起動時にでテーブルを作成し終了で削除
- テストクラスで **`@ActiveProfiles("test")`** を付けるとこの設定が使われる。
- **`@DataJpaTest`** では各テストがトランザクション内で実行され、終了後にロールバックされるため、テスト前の `repository.deleteAll()` は不要です。`@SpringBootTest` などでトランザクションがロールバックされない場合は、必要に応じて `@BeforeEach` でクリアする。

---

## Spring Boot テスト

| アノテーション | 用途 |
|----------------|------|
| **@SpringBootTest** | アプリ全体を起動し、Controller, Service, RepositoryなどをBeanとして登録する。統合テスト用。`@ActiveProfiles("test")` で H2 使用。 |
| **@DataJpaTest** | JPAのリポジトリ、@Entityのクラス、テスト用のDataSource (+DIコンテナ)が起動する。各テストはトランザクションで実行され、終了後にロールバックされる。 |
| **@AutoConfigureMockMvc** | `MockMvc` を有効化。HTTP リクエストをシミュレートし、レスポンスを検証する。結合テストで利用する。 |
| **@ActiveProfiles("test")** | プロファイルをtestにする。これにより`application.properties`を読み込んだ後`application-test.properties` を読み込み、同じキーは上書きされる。また@Profile("test")がついたBeanが有効になり@Profile("!test")がついたBeanは無効になる |
| **@MockitoBean** | BeanをMockに差し替える|

---

## JaCoCo（カバレッジ）

- テスト実行でどの行・ブランチが実行されたかを計測する。
- **`mvn test`** のあと、**`target/site/jacoco/index.html`** を開くとレポートを確認できる。
- 行・ブランチ・メソッド・クラスごとのカバレッジが分かる。

---

## テストの実行

```bash
mvn test                                    # 全テスト
mvn test -Dtest=ExpenseAmountTest           # 指定クラスのみ
mvn test -Dtest=ExpenseAmountTest#createExpenseAmount_正常な値  # 指定メソッドのみ
mvn package -DskipTests                     # テストをスキップしてビルド
```

---

## まとめ

- **ユニットテスト**: 単一クラス・メソッド。JUnit + Mockito。
- **リポジトリテスト**: `@DataJpaTest` + `@ActiveProfiles("test")` で H2 に接続。
- **統合テスト**: `@SpringBootTest` + MockMvc で API から DB まで一括検証。

カバレッジは `mvn test` 後に `target/site/jacoco/index.html` で確認する。
