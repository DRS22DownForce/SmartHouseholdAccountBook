# テスト関連技術資料

> このドキュメントは、Smart Household Account Bookプロジェクトのバックエンドで使用されているテスト関連の技術について、学習用にまとめたものです。

## 📋 目次

1. [概要](#概要)
2. [テストフレームワーク](#テストフレームワーク)
3. [モック・スタブライブラリ](#モックスタブライブラリ)
4. [アサーションライブラリ](#アサーションライブラリ)
5. [テスト用データベース](#テスト用データベース)
6. [Spring Boot テストサポート](#spring-boot-テストサポート)
7. [テストカバレッジ](#テストカバレッジ)
8. [テストの種類と実装例](#テストの種類と実装例)
9. [テストの実行方法](#テストの実行方法)

---

## 概要

このプロジェクトでは、**JUnit 5**を中心とした包括的なテスト環境を構築しています。Spring Bootのテストサポート機能を活用し、ユニットテストから統合テストまで、様々なレベルのテストを実装しています。

### 主な特徴

- **JUnit 5**: 最新のJavaテストフレームワーク
- **Mockito**: モックオブジェクトの作成と検証
- **AssertJ**: 読みやすいアサーション
- **H2 Database**: テスト用インメモリデータベース
- **JaCoCo**: テストカバレッジの測定
- **Spring Boot Test**: Springアプリケーションのテストサポート

---

## テストフレームワーク

### 1. JUnit 5

**役割**: Javaアプリケーションのテストを実行するためのフレームワーク。テストメソッドを定義し、実行結果を検証します。

**このプロジェクトでの使用箇所**:
- すべてのテストクラス（`*Test.java`）

**主な機能**:
- **`@Test`アノテーション**: テストメソッドを定義
- **`@DisplayName`アノテーション**: テストの表示名を日本語で指定
- **`@BeforeEach`アノテーション**: 各テストメソッドの前に実行される初期化処理
- **アサーション**: 期待値と実際の値を比較（`assertEquals`, `assertTrue`, `assertThrows`など）

**実際のコード例**:

`backend/src/test/java/com/example/backend/domain/valueobject/ExpenseAmountTest.java` (16-29行目):

```java
@Test
@DisplayName("正常な金額を作成できる")
void createExpenseAmount_正常な値() {
    // テストデータの準備: 1以上の正の整数
    Integer value = 1000;

    // テスト実行: ExpenseAmountオブジェクトを作成
    ExpenseAmount amount = new ExpenseAmount(value);

    // 検証: 正常に作成され、値が正しく設定されていることを確認
    assertNotNull(amount);
    assertEquals(1000, amount.getValue());
    assertEquals(1000, amount.toInteger());
}
```

**JUnit 5の主なアノテーション**:

| アノテーション | 説明 | 使用例 |
|--------------|------|--------|
| `@Test` | テストメソッドであることを示す | `@Test void testMethod() {}` |
| `@DisplayName` | テストの表示名を指定（日本語可） | `@DisplayName("正常な金額を作成できる")` |
| `@BeforeEach` | 各テストメソッドの前に実行 | `@BeforeEach void setUp() {}` |
| `@AfterEach` | 各テストメソッドの後に実行 | `@AfterEach void tearDown() {}` |
| `@BeforeAll` | すべてのテストメソッドの前に1回だけ実行 | `@BeforeAll static void setUpAll() {}` |
| `@AfterAll` | すべてのテストメソッドの後に1回だけ実行 | `@AfterAll static void tearDownAll() {}` |

**JUnit 5の主なアサーションメソッド**:

| メソッド | 説明 | 使用例 |
|---------|------|--------|
| `assertEquals(expected, actual)` | 期待値と実際の値が等しいか確認 | `assertEquals(1000, amount.getValue())` |
| `assertNotEquals(expected, actual)` | 期待値と実際の値が異なるか確認 | `assertNotEquals(0, amount.getValue())` |
| `assertTrue(condition)` | 条件がtrueか確認 | `assertTrue(amount.getValue() > 0)` |
| `assertFalse(condition)` | 条件がfalseか確認 | `assertFalse(amount.getValue() < 0)` |
| `assertNull(object)` | オブジェクトがnullか確認 | `assertNull(expense)` |
| `assertNotNull(object)` | オブジェクトがnullでないか確認 | `assertNotNull(expense)` |
| `assertThrows(exceptionClass, executable)` | 例外がスローされるか確認 | `assertThrows(IllegalArgumentException.class, () -> new ExpenseAmount(null))` |

**学習ポイント**:
- **テストの構造**: 準備（Arrange）→ 実行（Act）→ 検証（Assert）の3段階で構成
- **テストの独立性**: 各テストは独立して実行され、他のテストに影響を与えない
- **テストの可読性**: `@DisplayName`で日本語の説明を付けることで、テストの意図が明確になる

---

## モック・スタブライブラリ

### 2. Mockito

**役割**: テスト対象の依存関係をモック（偽装）オブジェクトに置き換えるライブラリ。外部システムやデータベースへのアクセスをシミュレートします。

**このプロジェクトでの使用箇所**:
- サービス層のテスト（`ExpenseApplicationServiceTest.java`など）
- フィルターのテスト（`JwtAuthFilterTest.java`など）

---

#### 基本的な使い方

**実際のコード例**:

`backend/src/test/java/com/example/backend/application/service/ExpenseApplicationServiceTest.java` (31-57行目):

```java
@ExtendWith(MockitoExtension.class)
class ExpenseApplicationServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;
    
    @Mock
    private ExpenseMapper expenseMapper;
    
    @Mock
    private UserApplicationService userApplicationService;
    
    @InjectMocks
    private ExpenseApplicationService expenseApplicationService;

    @Test
    void getAllExpenses_リポジトリに2件あれば2件返す() {
        // モックの設定
        when(expenseRepository.findByUser(user)).thenReturn(Arrays.asList(expense1, expense2));
        when(userApplicationService.getUser()).thenReturn(user);
        
        // テスト実行
        List<ExpenseDto> result = expenseApplicationService.getExpenses();

        // 検証
        assertEquals(2, result.size());
        verify(expenseRepository, times(1)).findByUser(user);
    }
}
```

---

#### アノテーションの説明

##### `@ExtendWith(MockitoExtension.class)`

**役割**: JUnit 5の拡張機能を有効化し、Mockitoのアノテーション（`@Mock`、`@InjectMocks`など）を動作させます。

**動作の仕組み**:
1. テストクラスに`@ExtendWith(MockitoExtension.class)`を付ける
2. JUnit 5がテスト実行前に`MockitoExtension`を呼び出す
3. `MockitoExtension`が`@Mock`アノテーションを検出して、モックオブジェクトを作成
4. `MockitoExtension`が`@InjectMocks`アノテーションを検出して、モックを注入したオブジェクトを作成

**なぜ必要か**: JUnit 5では、拡張機能を明示的に有効化する必要があります。これがないと、`@Mock`や`@InjectMocks`が無視されます。

##### `@Mock`

**役割**: モックオブジェクトを作成します。依存関係を偽装オブジェクトに置き換えます。

```java
@Mock
private ExpenseRepository expenseRepository;
```

##### `@InjectMocks`

**役割**: `@Mock`で作成したモックオブジェクトを、テスト対象のオブジェクトに自動的に注入します。

**注入の方法**（優先順位順）:

1. **コンストラクタインジェクション**（最優先）
2. **セッターインジェクション**（コンストラクタがない場合）
3. **フィールドインジェクション**（コンストラクタとセッターがない場合）

**このプロジェクトでの使用例**:

`backend/src/main/java/com/example/backend/application/service/ExpenseApplicationService.java` (43-50行目):

```java
// コンストラクタインジェクションを使用
public ExpenseApplicationService(
        ExpenseRepository expenseRepository,
        ExpenseMapper expenseMapper,
        UserApplicationService userApplicationService) {
    this.expenseRepository = expenseRepository;
    this.expenseMapper = expenseMapper;
    this.userApplicationService = userApplicationService;
}
```

テストクラスでは、`@InjectMocks`がこのコンストラクタにモックを注入します：

```java
@ExtendWith(MockitoExtension.class)
class ExpenseApplicationServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;
    
    @Mock
    private ExpenseMapper expenseMapper;
    
    @Mock
    private UserApplicationService userApplicationService;
    
    @InjectMocks
    private ExpenseApplicationService expenseApplicationService;
    // 内部的に以下が実行される:
    // new ExpenseApplicationService(expenseRepository, expenseMapper, userApplicationService)
}
```

---

#### 主な機能

1. **モックオブジェクトの作成**:
   ```java
   @Mock
   private ExpenseRepository expenseRepository;
   ```

2. **戻り値の設定**:
   ```java
   when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
   ```

3. **例外のスロー**:
   ```java
   when(expenseRepository.findById(999L))
       .thenThrow(new ExpenseNotFoundException("見つかりません"));
   ```

4. **メソッド呼び出しの検証**:
   ```java
   verify(expenseRepository, times(1)).save(expense);
   verify(expenseRepository, never()).delete(any());
   ```

---

#### なぜモックを使うのか

- **テストの独立性**: データベースや外部システムに依存せずにテストできる
- **テストの高速化**: 実際のデータベースアクセスが不要なため、テストが高速に実行される
- **テストの安定性**: 外部システムの状態に左右されない、安定したテストを実現
- **依存関係の分離**: テスト対象のクラスだけをテストし、依存関係はモックで置き換える

---

#### モックとスタブの違い

**スタブ（Stub）**: メソッドの戻り値を設定するだけ。呼び出しの検証は行わない。

```java
// スタブ: 戻り値を設定するだけ
when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
// このメソッドが呼ばれたかどうかは検証しない
```

**モック（Mock）**: メソッドの呼び出しを検証する。戻り値の設定も可能。

```java
// モック: 戻り値を設定し、かつ呼び出しを検証する
when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
// テスト実行...
verify(expenseRepository, times(1)).findById(1L); // 呼び出しを検証
```

**実際の使い分け**:
- **スタブ**: テスト対象のメソッドが依存関係から値を取得する必要がある場合に使用
- **モック**: テスト対象のメソッドが依存関係のメソッドを呼び出すことを確認したい場合に使用

**注意**: Mockitoでは、`@Mock`で作成したオブジェクトは「モック」と呼ばれますが、実際にはスタブとしてもモックとしても使用できます。`when().thenReturn()`で戻り値を設定するだけなら「スタブ」、`verify()`で呼び出しを検証するなら「モック」として機能します。

---

## アサーションライブラリ

### 3. AssertJ

**役割**: 読みやすいアサーション（検証）を提供するライブラリ。JUnitの標準アサーションよりも、より表現力豊かで読みやすいコードを書けます。

**このプロジェクトでの使用箇所**:
- 統合テスト（`ExpenseControllerIntegrationTest.java`など）

**主な機能**:
- **流れるようなAPI（Fluent API）**: メソッドチェーンで読みやすいアサーション
- **豊富なアサーションメソッド**: 様々な検証パターンに対応

**実際のコード例**:

`backend/src/test/java/com/example/backend/controller/ExpenseControllerIntegrationTest.java` (89-92行目):

```java
// AssertJを使用したアサーション
import static org.assertj.core.api.Assertions.assertThat;

// 検証: DBに保存されたか確認
assertThat(expenseRepository.count()).isEqualTo(1);
Expense saved = expenseRepository.findByUser(user).get(0);
assertThat(saved.getCategoryValue()).isEqualTo("食費");
```

**AssertJとJUnit標準アサーションの比較**:

| JUnit標準 | AssertJ | 説明 |
|----------|---------|------|
| `assertEquals(1000, amount)` | `assertThat(amount).isEqualTo(1000)` | 等しいか確認 |
| `assertTrue(amount > 0)` | `assertThat(amount).isGreaterThan(0)` | より大きいか確認 |
| `assertNotNull(expense)` | `assertThat(expense).isNotNull()` | nullでないか確認 |
| `assertTrue(list.isEmpty())` | `assertThat(list).isEmpty()` | 空か確認 |

**AssertJの主なアサーションメソッド**:

```java
// 数値の比較
assertThat(amount).isEqualTo(1000);
assertThat(amount).isGreaterThan(500);
assertThat(amount).isLessThan(2000);
assertThat(amount).isBetween(500, 2000);

// 文字列の検証
assertThat(description).isEqualTo("食費");
assertThat(description).contains("費");
assertThat(description).startsWith("食");
assertThat(description).isNotEmpty();

// コレクションの検証
assertThat(list).hasSize(2);
assertThat(list).isEmpty();
assertThat(list).contains(expense1);
assertThat(list).doesNotContain(expense2);

// オブジェクトの検証
assertThat(expense).isNotNull();
assertThat(expense).isEqualTo(otherExpense);
```

**学習ポイント**:
- **読みやすさ**: メソッドチェーンで自然な日本語のように読める
- **エラーメッセージ**: 失敗時のエラーメッセージが分かりやすい
- **柔軟性**: 様々な検証パターンに対応

## テスト用データベース

### 4. H2 Database

**役割**: テスト用のインメモリ（メモリ内）データベース。テスト実行時にのみ使用され、テスト終了後にデータは消去されます。

**このプロジェクトでの使用箇所**:
- すべてのテスト（`application-test.properties`で設定）

**主な特徴**:
- **インメモリ**: ディスクに保存されず、メモリ上で動作
- **高速**: ディスクI/Oが不要なため、テストが高速に実行される
- **自動クリーンアップ**: テスト終了後に自動的にデータが消去される

---

#### インストールと起動の仕組み

H2はMavenの依存関係として定義されており、**明示的なインストールは不要**です。

`backend/pom.xml` (89-93行目):

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

**動作の仕組み**:

1. **依存関係の追加**: `pom.xml`にH2の依存関係を追加
2. **自動ダウンロード**: Mavenが初回ビルド時にH2のJARファイルを自動的にダウンロード（`~/.m2/repository/`に保存）
3. **クラスパスに追加**: テスト実行時にH2のJARファイルがクラスパスに自動的に追加される
4. **自動起動**: Spring Bootがテスト実行時に自動的にH2データベースを起動

**重要なポイント**:
- **インストール不要**: 手動でH2をインストールする必要はありません
- **起動不要**: データベースサーバーを起動する必要はありません
- **自動管理**: Spring Bootがテスト実行時に自動的にH2を使用します

**テスト実行時の流れ**:

```
1. テストクラスが実行される
   ↓
2. @ActiveProfiles("test")により、application-test.propertiesが読み込まれる
   ↓
3. Spring BootがH2のJDBCドライバーを検出（クラスパス上にあるため）
   ↓
4. spring.datasource.url=jdbc:h2:mem:testdb の設定により、H2インメモリデータベースが自動的に作成される
   ↓
5. テストが実行される（H2データベースを使用）
   ↓
6. テスト終了後、H2データベースは自動的に破棄される（メモリから削除）
```

---

#### 設定と動作

**設定ファイル**: `backend/src/test/resources/application-test.properties`

```properties
# H2インメモリDBを使用
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
#アプリ起動時にテーブルを作成し、アプリ終了時にテーブルを削除
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
#HibernateのデータベースプラットフォームをH2に設定
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

#Cognitoの設定
cognito.jwk-set-url=https://dummy-url-for-test

# OpenAI設定 (テスト用ダミー)
openai.api.key=dummy-key-for-test
openai.api.url=https://dummy-url-for-test
```

**設定の説明**:
- **`jdbc:h2:mem:testdb`**: メモリ内に`testdb`という名前のデータベースを作成
- **`DB_CLOSE_DELAY=-1`**: データベース接続が閉じられても、データベースを保持（テスト中に接続が切れないようにする）
- **`spring.jpa.hibernate.ddl-auto=create-drop`**: 
  - Springコンテキストの起動時にテーブルを自動作成（`create`）
  - Springコンテキストの終了時にテーブルを自動削除（`drop`）
- **`spring.jpa.show-sql=true`**: 実行されるSQLをコンソールに表示（デバッグ用）

**設定が有効になる条件**:
- テストクラスに`@ActiveProfiles("test")`アノテーションを付ける
- Spring Bootが`application-test.properties`を読み込む
- テスト実行時のみH2が使用される（本番環境には影響しない）

**テーブルの作成・削除のタイミング**:

`create-drop`の動作は、**Springコンテキストのライフサイクル**に依存します：

1. **Springコンテキストの起動時**: テーブルが自動作成される
2. **テストメソッドの実行**: 作成されたテーブルを使用してテストが実行される
3. **Springコンテキストの終了時**: テーブルが自動削除される

**重要なポイント**:
- **テストクラスごとではない**: テーブルの作成・削除は「テストクラスごと」ではなく、「Springコンテキストごと」に行われます
- **コンテキストの共有**: 同じ設定（`@SpringBootTest`、`@ActiveProfiles("test")`など）のテストクラスは、同じSpringコンテキストを共有する場合があります
- **データの独立性**: 複数のテストクラスが同じデータベースを共有する可能性があるため、各テストメソッドの前にデータをクリアする必要があります

**データの独立性を保つ方法**:

```java
@BeforeEach
void setUp() {
    // テスト前にデータベースをクリア（各テストが独立して実行されるように）
    expenseRepository.deleteAll();
    userRepository.deleteAll();
}
```

---

#### 環境別の使い分け

| 環境 | データベース | 設定ファイル | 説明 |
|------|------------|------------|------|
| **テスト環境** | H2（インメモリ） | `backend/src/test/resources/application-test.properties` | `@ActiveProfiles("test")`が付いたテストクラスで自動的に読み込まれる |
| **開発環境** | MySQL | `backend/src/main/resources/application.properties` | ローカル開発時に使用 |
| **本番環境** | MySQL | 環境変数（`docker-compose.yaml`） | Dockerコンテナ起動時に使用 |

**H2 Databaseを使うメリット**:
- **テストの独立性**: 本番環境のデータベースに影響を与えない
- **テストの高速化**: ディスクI/Oが不要なため、テストが高速に実行される
- **セットアップの簡素化**: データベースサーバーの起動が不要

**学習ポイント**:
- **テストプロファイル**: `@ActiveProfiles("test")`でテスト用の設定ファイル（`application-test.properties`）を使用
- **データのクリーンアップ**: `@BeforeEach`で各テストの前にデータをクリアすることで、テストの独立性を保つ

---

## Spring Boot テストサポート

### 5. Spring Boot Test

**役割**: Spring Bootアプリケーションのテストを支援する機能。Springコンテキストの起動、依存性注入、モックMVCなどの機能を提供します。

**このプロジェクトでの使用箇所**:
- 統合テスト（`@SpringBootTest`を使用）
- リポジトリテスト（`@DataJpaTest`を使用）
- Web層のテスト（`MockMvc`を使用）

---

#### Springコンテキストとは

**Springコンテキスト（ApplicationContext）**: Spring Frameworkが管理するオブジェクト（Bean）のコンテナです。アプリケーション内のすべてのコンポーネント（サービス、リポジトリ、コントローラーなど）を管理し、依存性注入を実現します。

**主な役割**:
1. **Beanの管理**: `@Service`、`@Repository`、`@Controller`などのアノテーションが付いたクラスをBeanとして登録・管理
2. **依存性注入**: Bean間の依存関係を自動的に解決し、必要なオブジェクトを注入
3. **ライフサイクル管理**: Beanの作成、初期化、破棄を管理

**テストでのSpringコンテキスト**:

テストでは、`@SpringBootTest`や`@DataJpaTest`などのアノテーションにより、Springコンテキストが起動されます：

```java
@SpringBootTest  // Springコンテキストを起動
@ActiveProfiles("test")
class ExpenseControllerIntegrationTest {
    @Autowired  // SpringコンテキストからBeanを取得
    private ExpenseRepository expenseRepository;
}
```

**Springコンテキストのライフサイクル**:
1. **起動**: テストクラスが実行されると、Springコンテキストが起動される
2. **Beanの作成**: `@Service`、`@Repository`などのBeanが作成される
3. **依存性注入**: Bean間の依存関係が解決され、注入される
4. **テスト実行**: テストメソッドが実行される
5. **終了**: テストが終了すると、Springコンテキストが終了し、Beanが破棄される

**重要なポイント**:
- **コンテキストの共有**: 同じ設定のテストクラスは、同じSpringコンテキストを共有する場合があります（パフォーマンス向上のため）
- **コンテキストの分離**: 異なる設定のテストクラスは、異なるSpringコンテキストを使用します
- **`create-drop`との関係**: `spring.jpa.hibernate.ddl-auto=create-drop`は、Springコンテキストの起動時にテーブルを作成し、終了時に削除します

---

#### 複数の`@SpringBootTest`がある場合の動作

**Springコンテキストのキャッシング**:

Spring Bootは、**同じ設定のテストクラス間でSpringコンテキストをキャッシュ（再利用）**します。これにより、テストの実行速度が大幅に向上します。

**キャッシュの条件**:

以下の設定が**すべて同じ**場合、同じSpringコンテキストが再利用されます：
1. **アノテーション**: `@SpringBootTest`、`@DataJpaTest`など
2. **プロファイル**: `@ActiveProfiles("test")`など
3. **インポート**: `@Import(TestSecurityConfig.class)`など
4. **その他の設定**: `@AutoConfigureMockMvc`など

**実際の動作例**:

このプロジェクトには、以下のような`@SpringBootTest`が付いたテストクラスがあります：

```java
// テストクラス1
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ExpenseControllerIntegrationTest { ... }

// テストクラス2
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class HomeControllerTest { ... }

// テストクラス3
@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests { ... }
```

**実行時の動作**:

```
1. ExpenseControllerIntegrationTestが実行される
   ↓ Springコンテキストが起動（初回）
   ↓ テストが実行される
   ↓ Springコンテキストは終了せず、キャッシュされる

2. HomeControllerTestが実行される
   ↓ 同じ設定なので、キャッシュされたSpringコンテキストを再利用
   ↓ テストが実行される
   ↓ Springコンテキストは終了せず、キャッシュされる

3. BackendApplicationTestsが実行される
   ↓ @Import(TestSecurityConfig.class)がないため、異なる設定
   ↓ 新しいSpringコンテキストが起動
   ↓ テストが実行される
   ↓ すべてのテストが終了すると、すべてのSpringコンテキストが終了
```

**重要なポイント**:
- **1回だけ起動**: 同じ設定のテストクラスが複数あっても、Springコンテキストは**1回だけ起動**されます
- **パフォーマンス向上**: Springコンテキストの起動は時間がかかるため、キャッシュによりテストの実行速度が向上します
- **設定が異なる場合**: 異なる設定のテストクラスは、別のSpringコンテキストを使用します
- **`create-drop`への影響**: 
  - 同じSpringコンテキストを共有する場合: テーブルは1回だけ作成され、すべてのテストクラスの実行が終了した後に削除されます
  - 異なるSpringコンテキストを使用する場合: 各Springコンテキストごとにテーブルが作成・削除されます

---

#### 主なアノテーション

#### `@SpringBootTest`

**役割**: Spring Bootアプリケーション全体を起動してテストします。統合テストで使用します。

**実際のコード例**:

`backend/src/test/java/com/example/backend/controller/ExpenseControllerIntegrationTest.java` (36-41行目):

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ExpenseControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ExpenseRepository expenseRepository;
}
```

**`@SpringBootTest`の特徴**:
- **アプリケーション全体を起動**: コントローラー、サービス、リポジトリなど、すべてのコンポーネントが起動される
- **依存性注入**: `@Autowired`でSpring Beanを注入できる
- **統合テスト**: 複数のレイヤーをまたいだテストが可能

**重要なポイント: 本番環境と同じBeanが生成されるのか？**

`@SpringBootTest`を実行すると、**アプリケーションコンテキストが起動し、ほぼ本番環境と同じBeanが生成されます**。しかし、**完全に同じではありません**。テスト環境では以下の違いがあります：

1. **プロファイルの違い**: `@ActiveProfiles("test")`により、`application-test.properties`が使用されます
   - 本番環境: `application.properties`（MySQLデータベースなど）
   - テスト環境: `application-test.properties`（H2インメモリデータベースなど）

2. **データベースの違い**:
   ```properties
   # 本番環境（application.properties）
   spring.datasource.url=${SPRING_DATASOURCE_URL_DEV}  # MySQL
   spring.jpa.hibernate.ddl-auto=update
   
   # テスト環境（application-test.properties）
   spring.datasource.url=jdbc:h2:mem:testdb  # H2インメモリDB
   spring.jpa.hibernate.ddl-auto=create-drop  # テスト終了時に削除
   ```

3. **セキュリティ設定の違い**:
   - 本番環境: `SecurityConfig`（認証が必要）
   - テスト環境: `TestSecurityConfig`（認証を無効化、`@Import(TestSecurityConfig.class)`で読み込み）

4. **一部のBeanがモックに置き換えられる場合**:
   ```java
   @MockitoBean  // このBeanはモックに置き換えられる
   private UserApplicationService userApplicationService;
   ```

**まとめ**:
- ✅ **アプリケーションコンテキストは起動される**: 本番環境と同様に、すべてのコンポーネントが起動される
- ✅ **基本的なBeanは同じ**: `@Service`、`@Repository`、`@Controller`などのBeanは本番環境と同じように生成される
- ⚠️ **設定が異なる**: プロファイル、データベース、セキュリティ設定などがテスト用に変更される
- ⚠️ **一部のBeanが置き換えられる**: `@MockitoBean`などで一部のBeanがモックに置き換えられる場合がある

#### `@DataJpaTest`

**役割**: JPAレイヤー（リポジトリ）のみをテストします。コントローラーやサービスは起動されません。

**実際のコード例**:

`backend/src/test/java/com/example/backend/domain/repository/ExpenseRepositoryTest.java` (29-35行目):

```java
@DataJpaTest
@ActiveProfiles("test")
class ExpenseRepositoryTest {
    // @Autowired: Springの依存性注入により、リポジトリの実装が自動的に注入されます
    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;
}
```

**`@DataJpaTest`の特徴**:
- **JPAレイヤーのみ起動**: リポジトリとデータベースのみが起動される
- **高速**: アプリケーション全体を起動しないため、テストが高速に実行される
- **トランザクション**: 各テストは自動的にトランザクションで実行され、テスト終了後にロールバックされる

#### `@AutoConfigureMockMvc`

**役割**: コントローラー層のテスト用に`MockMvc`を自動設定します。HTTPリクエストをモック化してテストできます。

**実際のコード例**:

`backend/src/test/java/com/example/backend/controller/ExpenseControllerIntegrationTest.java` (84-97行目):

```java
@Test
@DisplayName("家計簿追加API→DB保存→取得APIまで一貫テスト")
void testAddAndGetExpense() throws Exception {
    // 1. POSTで家計簿データを追加
    String json = """
            {
              "date": "%s",
              "category": "食費",
              "amount": 1000,
              "description": "テスト"
            }
            """.formatted(LocalDate.now());

    mockMvc.perform(post("/api/expenses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isCreated());
}
```

**MockMvcの主な機能**:
- **HTTPリクエストの送信**: `mockMvc.perform(get("/api/expenses"))`でGETリクエストを送信
- **レスポンスの検証**: `.andExpect(status().isOk())`でHTTPステータスコードを検証
- **JSONの検証**: `.andExpect(jsonPath("$[0].category").value("食費"))`でJSONレスポンスを検証

**MockMvcの主なメソッド**:

```java
// GETリクエスト
mockMvc.perform(get("/api/expenses"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$[0].category").value("食費"));

// POSTリクエスト
mockMvc.perform(post("/api/expenses")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
    .andExpect(status().isCreated());

// PUTリクエスト
mockMvc.perform(put("/api/expenses/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
    .andExpect(status().isOk());

// DELETEリクエスト
mockMvc.perform(delete("/api/expenses/1"))
    .andExpect(status().isNoContent());
```

#### `@ActiveProfiles("test")`

**役割**: テスト用のプロファイル（`application-test.properties`）を有効化します。

**学習ポイント**:
- **テストプロファイル**: 本番環境とは異なる設定（H2 Databaseなど）を使用
- **環境の分離**: テスト環境と本番環境を分離することで、テストが本番環境に影響を与えない

---

## テストカバレッジ

### 6. JaCoCo

**役割**: テストカバレッジ（テストでカバーされているコードの割合）を測定するツール。どのコードがテストされているかを可視化します。

**このプロジェクトでの使用箇所**:
- `pom.xml`のMavenプラグイン設定

**実際の設定例**:

`backend/pom.xml` (308-326行目):

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

**JaCoCoの動作の仕組み**:

1. **`prepare-agent`**: テスト実行時に、コードの実行を追跡するエージェントを準備
2. **テスト実行**: テストが実行され、どのコードが実行されたかが記録される
3. **`report`**: テスト実行後、カバレッジレポートを生成

**カバレッジレポートの確認方法**:

```bash
# テストを実行
mvn test

# カバレッジレポートが生成される
# レポートの場所: backend/target/site/jacoco/index.html
```

**カバレッジの種類**:

| カバレッジ | 説明 |
|----------|------|
| **行カバレッジ** | 実行されたコード行の割合 |
| **ブランチカバレッジ** | 実行された分岐（if文など）の割合 |
| **メソッドカバレッジ** | 実行されたメソッドの割合 |
| **クラスカバレッジ** | 実行されたクラスの割合 |

**学習ポイント**:
- **品質管理**: テストが不足している箇所を特定できる
- **目標設定**: プロジェクトごとにカバレッジの目標を設定（例: 80%以上）
- **継続的な改善**: カバレッジレポートを確認して、テストを追加

**参考資料**:
- [JaCoCo公式ドキュメント](https://www.jacoco.org/jacoco/)

---

## テストの種類と実装例

### ユニットテスト

**役割**: 個々のクラスやメソッドを独立してテストします。依存関係はモックで置き換えます。

**実際のコード例**:

`backend/src/test/java/com/example/backend/domain/valueobject/ExpenseAmountTest.java` (16-29行目):

```java
/**
 * ExpenseAmount値オブジェクトのテストクラス
 * 
 * 支出金額を表現する値オブジェクトのバリデーションとビジネスロジックをテストします。
 * 値オブジェクトは不変（immutable）であり、バリデーションによって不正な値の作成を防ぎます。
 */
class ExpenseAmountTest {

    @Test
    @DisplayName("正常な金額を作成できる")
    void createExpenseAmount_正常な値() {
        // テストデータの準備: 1以上の正の整数
        Integer value = 1000;

        // テスト実行: ExpenseAmountオブジェクトを作成
        ExpenseAmount amount = new ExpenseAmount(value);

        // 検証: 正常に作成され、値が正しく設定されていることを確認
        assertNotNull(amount);
        assertEquals(1000, amount.getValue());
        assertEquals(1000, amount.toInteger());
    }
}
```

**ユニットテストの特徴**:
- **独立性**: 他のクラスに依存しない
- **高速**: モックを使用するため、テストが高速に実行される
- **単一責任**: 1つのクラスやメソッドのみをテスト

### 統合テスト

**役割**: 複数のレイヤー（コントローラー、サービス、リポジトリ、データベース）をまたいだ動作をテストします。

**実際のコード例**:

`backend/src/test/java/com/example/backend/controller/ExpenseControllerIntegrationTest.java` (71-98行目):

```java
/**
 * ExpenseControllerの統合テストクラス
 * 
 * コントローラーからデータベースまでの一貫した動作をテストします。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Test
    @DisplayName("家計簿追加API→DB保存→取得APIまで一貫テスト")
    void testAddAndGetExpense() throws Exception {
        // 1. POSTで家計簿データを追加
        String json = """
                {
                  "date": "%s",
                  "category": "食費",
                  "amount": 1000,
                  "description": "テスト"
                }
                """.formatted(LocalDate.now());

        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        // 2. DBに保存されたか確認
        assertThat(expenseRepository.count()).isEqualTo(1);
        Expense saved = expenseRepository.findByUser(user).get(0);
        assertThat(saved.getCategoryValue()).isEqualTo("食費");

        // 3. GETで家計簿データ一覧を取得
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("食費"));
    }
}
```

**統合テストの特徴**:
- **一貫性**: 複数のレイヤーをまたいだ動作を確認
- **実際の環境**: 実際のデータベース（H2）を使用
- **エンドツーエンド**: APIからデータベースまでの一連の流れをテスト

### リポジトリテスト

**役割**: リポジトリ（データアクセス層）のクエリメソッドをテストします。

**実際のコード例**:

`backend/src/test/java/com/example/backend/domain/repository/ExpenseRepositoryTest.java` (53-74行目):

```java
/**
 * ExpenseRepositoryのテストクラス
 * 
 * 支出リポジトリのクエリメソッドをテストします。
 * @DataJpaTestアノテーションにより、JPAレイヤーのみをテストします（実際のデータベースを使用）。
 */
@DataJpaTest
@ActiveProfiles("test")
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Test
    @DisplayName("ユーザーを指定して支出を取得できる")
    void findByUser_正常に取得() {
        // テストデータの準備: 支出データを作成して保存
        ExpenseAmount amount1 = new ExpenseAmount(1000);
        ExpenseDate date1 = new ExpenseDate(LocalDate.of(2024, 1, 15));
        Category category1 = new Category("食費");
        Expense expense1 = new Expense("支出1", amount1, date1, category1, testUser);
        expenseRepository.save(expense1);

        // テスト実行: ユーザーを指定して支出を取得
        List<Expense> expenses = expenseRepository.findByUser(testUser);

        // 検証: 2件の支出が取得できることを確認
        assertEquals(2, expenses.size());
    }
}
```

**リポジトリテストの特徴**:
- **JPAレイヤーのみ**: リポジトリとデータベースのみが起動される
- **高速**: アプリケーション全体を起動しないため、テストが高速に実行される
- **クエリの検証**: データベースクエリが正しく動作することを確認

---

## テストの実行方法

### Mavenコマンドでテストを実行

```bash
# すべてのテストを実行
mvn test

# 特定のテストクラスのみ実行
mvn test -Dtest=ExpenseAmountTest

# 特定のテストメソッドのみ実行
mvn test -Dtest=ExpenseAmountTest#createExpenseAmount_正常な値

# テストをスキップしてビルド
mvn package -DskipTests
```

### IDE（Cursor/IntelliJ IDEA）でテストを実行

1. **テストクラスを開く**: `ExpenseAmountTest.java`を開く
2. **テストメソッドの横の実行ボタンをクリック**: 個別のテストメソッドを実行
3. **テストクラス名の横の実行ボタンをクリック**: クラス内のすべてのテストを実行

### テストの結果の確認

- **成功**: テストが正常に完了した場合、緑色のチェックマークが表示される
- **失敗**: テストが失敗した場合、赤色の×マークが表示され、エラーメッセージが表示される
- **カバレッジレポート**: `backend/target/site/jacoco/index.html`をブラウザで開く

---

## まとめ

このプロジェクトのテスト環境は、以下の技術で構成されています：

### テストフレームワーク
- **JUnit 5**: テストの実行とアサーション
- **Mockito**: モックオブジェクトの作成と検証
- **AssertJ**: 読みやすいアサーション

### テスト用インフラ
- **H2 Database**: テスト用インメモリデータベース
- **Spring Boot Test**: Springアプリケーションのテストサポート
- **MockMvc**: Web層のテスト

### テストカバレッジ
- **JaCoCo**: テストカバレッジの測定とレポート生成

### テストの種類
- **ユニットテスト**: 個々のクラスやメソッドをテスト
- **統合テスト**: 複数のレイヤーをまたいだ動作をテスト
- **リポジトリテスト**: データアクセス層をテスト

これらの技術を組み合わせることで、保守性と信頼性の高いアプリケーションを実現しています。

---

## 学習の進め方

1. **基礎から学ぶ**: JUnit 5の基本的な使い方を理解する
2. **実際のテストを読む**: このプロジェクトのテストコードを読んで理解を深める
3. **手を動かす**: 小さなテストを書いて実践する
4. **カバレッジを確認**: テストカバレッジレポートを確認して、テストが不足している箇所を特定する

---

## 参考資料

### 公式ドキュメント
- [JUnit 5公式ドキュメント](https://junit.org/junit5/)
- [Mockito公式ドキュメント](https://site.mockito.org/)
- [AssertJ公式ドキュメント](https://assertj.github.io/doc/)
- [Spring Boot Test公式ドキュメント](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JaCoCo公式ドキュメント](https://www.jacoco.org/jacoco/)

### 書籍
- [JUnit実践入門](https://www.shoeisha.co.jp/book/detail/9784798126706)
- [テスト駆動開発](https://www.ohmsha.co.jp/book/9784274217883/)

---

**最終更新日**: 2024年

