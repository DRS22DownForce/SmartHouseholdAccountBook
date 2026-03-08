# バックエンド技術スタック資料

> このドキュメントは、Smart Household Account Bookプロジェクトのバックエンドで使用されている技術を学習用にまとめたものです。

## 📋 目次

1. [概要](#概要)
2. [コア技術](#コア技術)
3. [フレームワーク・ライブラリ](#フレームワークライブラリ)
   - [Resilience4j](#resilience4jレート制限リトライサーキットブレーカー)
4. [データベース・インフラ](#データベースインフラ)
5. [認証・セキュリティ](#認証セキュリティ)
6. [アーキテクチャパターン](#アーキテクチャパターン)
7. [実際のコード例](#実際のコード例)

> **📌 Springフレームワークについて**: Spring Boot、Spring Data JPA、Spring Securityなどの詳細は、[Springフレームワーク詳細資料](./spring-framework.md)を参照してください。

---

## 概要

このプロジェクトのバックエンドは、**Spring Boot 3.5.0**を基盤としたモダンなJavaアプリケーションです。ドメイン駆動設計（DDD）の原則に従って設計され、保守性と拡張性を重視しています。

### 主な特徴

- **Java 21**: 最新のJava機能を活用
- **Spring Boot 3.5.0**: エンタープライズレベルのアプリケーションフレームワーク
- **ドメイン駆動設計（DDD）**: ビジネスロジックを明確に表現
- **RESTful API**: OpenAPI 3.0仕様に基づく型安全なAPI設計
- **JWT認証**: AWS Cognitoによるセキュアな認証・認可

---

## コア技術

### 1. Java 21

**役割**: プログラミング言語。バックエンドの全コードがJavaで記述されています。

**学習ポイント**:
- **レコードクラス**: 不変データクラス（Java 14以降）
- **パターンマッチング**: switch式の拡張（Java 17以降）

### 2. Spring Boot 3.5.0

**役割**: Javaアプリケーション開発のためのフレームワーク。設定の自動化、依存性注入、アスペクト指向プログラミングなどの機能を提供します。

> **詳細はこちら**: [Spring Boot 3.5.0の詳細](./spring-framework.md#spring-boot-350)を参照してください。

---

## フレームワーク・ライブラリ

> **📌 ライブラリの詳細**: OpenAPI Generator、Lombok、Jakarta Validationなどの詳細は、[バックエンドライブラリ詳細資料](./libraries.md)を参照してください。

---

### Resilience4j（レート制限・リトライ・サーキットブレーカー）

**役割**: 外部API（OpenAI）呼び出しの耐障害性を高めるライブラリ。**レート制限**（送信回数の上限）、**リトライ**（一時障害時の再試行）、**サーキットブレーカー**（連続失敗時に呼び出しを一時停止）をアノテーションで適用できる。

**このプロジェクトでの使用箇所**:
- `OpenAiClient.java`（`callText` / `callJson` に適用）
- `application.properties`（インスタンス名 `openai` の設定）
- `GlobalExceptionHandler.java`（レート制限超過・OpenAI クォータ超過の例外処理）

#### 3つの機能の役割

| 機能 | 役割 |
|------|------|
| **Rate Limiter** | アプリからOpenAIへ送るリクエスト数を「一定期間あたり○回まで」に制限する。超えた場合は呼び出し前に `RequestNotPermitted` がスローされ、API利用者には 429 を返す。OpenAIの利用枠に達する前の「クライアント側のガード」になる。 |
| **Retry** | 一時的なネットワーク障害やサーバー過負荷で失敗した場合に、指定回数・間隔で再試行する。このプロジェクトでは `ResourceAccessException` と `RestClientException` のときだけリトライする。OpenAIが 429 を返した場合は `QuotaExceededException` に変換してスローするため、リトライ対象外となる。 |
| **Circuit Breaker** | 直近の呼び出しで失敗率が閾値を超えると「サーキットを開く」。開いている間は実際のAPI呼び出しを行わず、フォールバックメソッドが呼ばれて `AiServiceException` をスローする。連続失敗時の無駄な呼び出しを防ぎ、外部サービスの回復を待つ。 |

#### 処理の流れ（OpenAI呼び出し時）

```
callText / callJson が呼ばれる
      ↓
Rate Limiter（openai）
  → 許可数以内なら通過。超えていれば RequestNotPermitted をスロー（ここで 429 に変換）
      ↓
Retry（openai）
  → リトライ対象例外のときだけ待機後に再実行
      ↓
Circuit Breaker（openai）
  → サーキットが閉じていれば通過。開いていればフォールバック実行（AiServiceException）
      ↓
callForContent（実際の HTTP 呼び出し）
  → 成功なら結果を返す。OpenAI が 429 を返した場合は QuotaExceededException（429 に変換）
```

#### アノテーションの適用例（OpenAiClient）

`callText` と `callJson` の両方に、同じ3つのアノテーションを付与している。名前 `openai` で `application.properties` の設定（`resilience4j.ratelimiter.instances.openai` など）が紐づく。

| アノテーション | 意味 |
|----------------|------|
| **@RateLimiter(name = "openai")** | 上記の「発信レート制限」をこのメソッドに適用する。 |
| **@Retry(name = "openai")** | リトライ設定をこのメソッドに適用する。 |
| **@CircuitBreaker(name = "openai", fallbackMethod = "…")** | サーキットブレーカーを適用し、サーキットが開いているときは指定したフォールバックメソッドを呼ぶ。 |

#### application.properties の設定（本番・開発）

| プロパティ | 意味 |
|------------|------|
| **Rate Limiter** | |
| `resilience4j.ratelimiter.instances.openai.limit-for-period=50` | 1期間あたり許可する呼び出し数。 |
| `resilience4j.ratelimiter.instances.openai.limit-refresh-period=600s` | 上記の「1期間」の長さ。600秒で50回まで。 |
| `resilience4j.ratelimiter.instances.openai.timeout-duration=0` | 制限に達したときに待機する時間（秒）。0 のため待たずに即 `RequestNotPermitted` をスロー。 |
| **Retry** | |
| `resilience4j.retry.instances.openai.max-attempts=3` | 最大試行回数（初回＋リトライ2回）。 |
| `resilience4j.retry.instances.openai.wait-duration=1s` | リトライまでの待機時間。 |
| `resilience4j.retry.instances.openai.retry-exceptions[0/1]` | この例外のときだけリトライ。429 で投げる `QuotaExceededException` は含めないため、クォータ超過ではリトライしない。 |
| **Circuit Breaker** | |
| `resilience4j.circuitbreaker.instances.openai.failure-rate-threshold=50` | スライディングウィンドウ内の失敗率が50%を超えるとサーキットを開く。 |
| `resilience4j.circuitbreaker.instances.openai.sliding-window-size=5` | 直近の呼び出し何件を失敗率計算に使うか。 |
| `resilience4j.circuitbreaker.instances.openai.wait-duration-in-open-state=30s` | サーキットを開いたままにする時間。経過後に「半開」となり、再度呼び出しを試す。 |
| `resilience4j.circuitbreaker.instances.openai.minimum-number-of-calls=5` | 少なくともこの回数呼ばれた後に失敗率を判定する。 |

テスト用（`application-test.properties`）では、レート制限を緩和（`limit-for-period=10000`）し、リトライ回数を 1 にしているため、テストがレート制限やリトライ待ちに影響されない。

#### 例外ハンドリング（GlobalExceptionHandler）

レート制限とOpenAIクォータ超過の両方を、API では **429 Too Many Requests** として返す。

| 例外 | 発生タイミング | ハンドラーでの処理 |
|------|----------------|---------------------|
| **RequestNotPermitted** | Resilience4j の Rate Limiter が「許可数超過」と判断したとき。 | 429 と「リクエスト数が上限を超えました」メッセージを返す。 |
| **QuotaExceededException** | OpenAI API が 429 を返したとき。`OpenAiClient` が `HttpClientErrorException.TooManyRequests` をキャッチし、この例外に変換してスロー。 | 429 と「利用枠を超過しました」メッセージを返す。 |

---

## データベース・インフラ

> **📌 データベース・インフラの詳細**: MySQL、Docker、Mavenなどの詳細は、[Maven・MySQL・Docker詳細資料](./maven-mysql-docker.md)を参照してください。

> **📌 テスト用データベースの詳細**: H2 Databaseの詳細は、[テスト関連技術資料](./testing.md)を参照してください。

---

## 認証・セキュリティ

### 1. AWS Cognito

**役割**: マネージド認証サービス。ユーザー認証・認可を管理します。

**このプロジェクトでの使用箇所**:
- JWTトークンの発行元
- ユーザー認証

**学習ポイント**:
- **OAuth2**: 認証・認可の標準プロトコル
- **JWT**: JSON Web Token。認証情報を含むトークン
- **JWK Set**: JWTの署名を検証するための公開鍵セット

**参考資料**:
- [AWS Cognito公式ドキュメント](https://docs.aws.amazon.com/cognito/)

---

### 2. Nimbus JOSE + JWT

**役割**: JWTトークンの検証を行うライブラリ。

**このプロジェクトでの使用箇所**:
- `JwtAuthFilter.java`でJWTトークンを検証

**実際のコード例**:

`backend/src/main/java/com/example/backend/auth/filter/JwtAuthFilter.java` (43-66行目):

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    @VisibleForTesting
    ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private final JWKSource<SecurityContext> remoteJwkSet;

    public JwtAuthFilter(JwtProperties jwtProperties) {
        try {
            DefaultResourceRetriever resourceRetriever = new DefaultResourceRetriever(5000, 5000, 1024 * 1024);
            @SuppressWarnings("deprecation")
            RemoteJWKSet<SecurityContext> remoteJWKSet = new RemoteJWKSet<>(
                    URI.create(jwtProperties.getJwkSetUrl()).toURL(), resourceRetriever);
            this.remoteJwkSet = remoteJWKSet;

            this.jwtProcessor = new DefaultJWTProcessor<>();
            this.jwtProcessor.setJWSKeySelector(
                    new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, this.remoteJwkSet));
            logger.info("JWT認証フィルターを初期化しました。JWK URL: {}", jwtProperties.getJwkSetUrl());
        } catch (Exception e) {
            logger.error("JWT認証フィルターの初期化に失敗しました", e);
            throw new RuntimeException("JWT認証フィルターの初期化に失敗しました", e);
        }
    }
}
```

**学習ポイント**:
- **JWT検証**: トークンの署名を検証して、改ざんされていないことを確認
- **RS256**: RSA署名アルゴリズム。公開鍵で検証、秘密鍵で署名

**参考資料**:
- [Nimbus JOSE + JWT公式ドキュメント](https://connect2id.com/products/nimbus-jose-jwt)

---

## アーキテクチャパターン

### ドメイン駆動設計（DDD）

**役割**: ビジネスロジックを明確に表現する設計手法。ドメイン（ビジネスの核心）を中心に設計します。

**このプロジェクトでの使用箇所**:
- バックエンド全体の構造

**レイヤー構造**:

1. **ドメイン層** (`domain/`)
   - **値オブジェクト**: 不変な値（`ExpenseAmount`, `Category`, `ExpenseDate`など）
   - **リポジトリインターフェース**: データアクセスの抽象化

2. **アプリケーション層** (`application/`)
   - **サービス**: ユースケースの実装（`ExpenseApplicationService`など）
   - **マッパー**: ドメインオブジェクトとDTOの変換

3. **プレゼンテーション層** (`controller/`)
   - **コントローラー**: REST APIエンドポイントの実装

4. **インフラストラクチャ層** (`repository/`, `entity/`)
   - **リポジトリ実装**: JPAを使用したデータアクセス
   - **エンティティ**: データベースのテーブルに対応

**実際のコード例（値オブジェクト）**:

`backend/src/main/java/com/example/backend/domain/valueobject/ExpenseAmount.java` (16-41行目):

```java
@Embeddable
@Getter
@ToString
@EqualsAndHashCode
public class ExpenseAmount{

    @Column(name = "amount", nullable = false)
    private final Integer value;

    /**
     * JPA用のデフォルトコンストラクタ
     */
    protected ExpenseAmount() {
        this.value = null;
    }

    /**
     * コンストラクタ
     * 
     * @param value 金額（1以上でなければならない）
     * @throws IllegalArgumentException 金額がnullまたは0以下の場合
     */
    public ExpenseAmount(Integer value) {
        validate(value);
        this.value = value;
    }
}
```

**値オブジェクトの特徴**:
- **不変性**: 一度作成されたら変更できない
- **バリデーション**: コンストラクタで不正な値を拒否
- **ドメイン表現**: ビジネス概念をコードで明確に表現

**学習ポイント**:
- **レイヤー分離**: 各レイヤーの責務を明確にする
- **値オブジェクト**: プリミティブ型の代わりに値オブジェクトを使用
- **リポジトリパターン**: データアクセスを抽象化

**参考資料**:
- [ドメイン駆動設計入門](https://www.shoeisha.co.jp/book/detail/9784798150727)
- [Eric Evans - Domain-Driven Design](https://www.domainlanguage.com/ddd/)

---

## 実際のコード例

### コントローラーの実装

`backend/src/main/java/com/example/backend/controller/ExpenseController.java` (26-42行目):

```java
@RestController
public class ExpenseController implements ExpensesApi {
    private final ExpenseApplicationService expenseApplicationService;
    private final ExpenseMapper expenseMapper;

    /**
     * コンストラクタ
     * 
     * @param expenseApplicationService 支出アプリケーションサービス
     * @param expenseMapper             支出マッパー
     */
    public ExpenseController(
            ExpenseApplicationService expenseApplicationService,
            ExpenseMapper expenseMapper) {
        this.expenseApplicationService = expenseApplicationService;
        this.expenseMapper = expenseMapper;
    }
}
```

**解説**:
- `@RestController`: REST APIコントローラーであることを示す
- `implements ExpensesApi`: OpenAPI Generatorで生成されたインターフェースを実装
- **コンストラクタインジェクション**: 依存関係をコンストラクタで注入（推奨）

---

### エンティティの実装

`backend/src/main/java/com/example/backend/entity/Expense.java` (35-72行目):

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主キー（識別子）

    @Column(nullable = false)
    private String description; // 支出の説明

    /**
     * 支出金額（値オブジェクト）
     * 
     * @Embeddedアノテーションにより、値オブジェクトがエンティティに埋め込まれます。
     * データベースには値オブジェクトのフィールドが直接マッピングされます。
     */
    @Embedded
    private ExpenseAmount amount; // 金額（値オブジェクト）

    /**
     * 支出日付（値オブジェクト）
     */
    @Embedded
    private ExpenseDate date; // 日付（値オブジェクト）

    /**
     * 支出カテゴリ（値オブジェクト）
     */
    @Embedded
    private Category category; // カテゴリー（値オブジェクト）

    // UserEntityのidを外部キーとして参照
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

**解説**:
- `@Entity`: このクラスがJPAエンティティであることを示す
- `@Id`: 主キー（識別子）
- `@Embedded`: 値オブジェクトをエンティティに埋め込む
- `@ManyToOne`: 多対一の関係（複数の支出が1つのユーザーに属する）

---

### グローバル例外ハンドラー

`backend/src/main/java/com/example/backend/exception/GlobalExceptionHandler.java` (14-36行目):

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExpenseNotFoundException(ExpenseNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage(), OffsetDateTime.now()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage(), OffsetDateTime.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("不正な引数が渡されました: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage(), OffsetDateTime.now()));
    }

}
```

**解説**:
- `@ControllerAdvice`: すべてのコントローラーで発生する例外を処理
- `@ExceptionHandler`: 特定の例外を処理するメソッド
- **統一されたエラーレスポンス**: すべてのエラーを同じ形式で返す

---

## まとめ

このプロジェクトのバックエンドは、以下の技術スタックで構成されています：

### コア技術
- **Java 21**: プログラミング言語
- **Spring Boot 3.5.0**: アプリケーションフレームワーク
  - 詳細: [Spring Boot 3.5.0の詳細](./spring-framework.md#spring-boot-350)

### データアクセス
- **Spring Data JPA**: データベースアクセス
  - 詳細: [Spring Data JPAの詳細](./spring-framework.md#spring-data-jpa)
- **MySQL 8.0**: リレーショナルデータベース
  - 詳細: [MySQLの詳細](./maven-mysql-docker.md#mysqlデータベース)

### セキュリティ
- **Spring Security**: 認証・認可
  - 詳細: [Spring Security + OAuth2の詳細](./spring-framework.md#spring-security--oauth2)
- **AWS Cognito**: マネージド認証サービス
- **JWT**: トークンベース認証

### 耐障害・外部API呼び出し
- **Resilience4j**: レート制限・リトライ・サーキットブレーカー（OpenAI API 呼び出しに適用）
  - 詳細: [Resilience4jの説明（本文）](#resilience4jレート制限リトライサーキットブレーカー)

### 開発ツール
- **Maven**: ビルドツール
  - 詳細: [Mavenの詳細](./maven-mysql-docker.md#mavenビルドツール)
- **Lombok**: ボイラープレート削減
  - 詳細: [Lombokの詳細](./libraries.md#lombok)
- **OpenAPI Generator**: コード自動生成
  - 詳細: [OpenAPI Generatorの詳細](./libraries.md#openapi-generator)
- **JaCoCo**: テストカバレッジ測定
  - 詳細: [JaCoCoの詳細](./testing.md#6-jacoco)
- **Spring Boot DevTools**: 開発時の生産性向上
  - 詳細: [Spring Boot DevToolsの詳細](./spring-framework.md#spring-boot-devtools)

### アーキテクチャ
- **ドメイン駆動設計（DDD）**: 設計手法

これらの技術を組み合わせることで、保守性と拡張性の高いアプリケーションを実現しています。

> **📌 Springフレームワークについて**: Spring関連の技術の詳細は、[Springフレームワーク詳細資料](./spring-framework.md)を参照してください。

---

## 学習の進め方

1. **基礎から学ぶ**: Java、Spring Bootの基礎を理解する
2. **実際のコードを読む**: このプロジェクトのコードを読んで理解を深める
3. **手を動かす**: 小さな機能を追加して実践する
4. **ドキュメントを読む**: 公式ドキュメントで詳細を確認する

---

## 参考資料

### 公式ドキュメント
- [Spring Boot公式ドキュメント](https://spring.io/projects/spring-boot)
- [Spring Data JPA公式ドキュメント](https://spring.io/projects/spring-data-jpa)
- [Spring Security公式ドキュメント](https://spring.io/projects/spring-security)

### 書籍
- [Spring Boot実践ガイド](https://www.shoeisha.co.jp/book/detail/9784798162477)
- [ドメイン駆動設計入門](https://www.shoeisha.co.jp/book/detail/9784798150727)

### オンラインコース
- [Spring Boot公式チュートリアル](https://spring.io/guides)

---

**最終更新日**: 2024年

