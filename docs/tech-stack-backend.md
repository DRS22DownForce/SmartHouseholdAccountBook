# バックエンド技術スタック資料

> このドキュメントは、Smart Household Account Bookプロジェクトのバックエンドで使用されている技術を学習用にまとめたものです。

## 📋 目次

1. [概要](#概要)
2. [コア技術](#コア技術)
3. [フレームワーク・ライブラリ](#フレームワークライブラリ)
4. [データベース・インフラ](#データベースインフラ)
5. [認証・セキュリティ](#認証セキュリティ)
6. [開発ツール](#開発ツール)
7. [アーキテクチャパターン](#アーキテクチャパターン)
8. [実際のコード例](#実際のコード例)

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

### 5. OpenAPI Generator

**役割**: OpenAPI仕様書（`openapi.yaml`）からJavaのインターフェースとモデルクラスを自動生成するツール。

**このプロジェクトでの使用箇所**:
- `pom.xml`のMavenプラグイン設定
- 生成されたコード: `target/generated-sources/openapi/`配下

**主な機能**:
- **APIインターフェース生成**: `ExpensesApi`インターフェースを自動生成
- **モデルクラス生成**: `ExpenseDto`, `ExpenseRequestDto`などを自動生成
- **型安全性**: 仕様書とコードが常に同期

**実際のコード例**:

`backend/pom.xml` (236-265行目):

```xml
			<plugin>
				<groupId>org.openapitools</groupId>
				<artifactId>openapi-generator-maven-plugin</artifactId>
				<version>7.13.0</version>
				<executions>
					<execution>
						<id>generate-spring</id>
						<phase>generate-sources</phase>
						<goals>
							<goal>generate</goal>
						</goals>
						<configuration>
							<inputSpec>${openapi.file}</inputSpec>
							<generatorName>spring</generatorName>
							<output>${project.build.directory}/generated-sources/openapi</output>
							<apiPackage>com.example.backend.generated.api</apiPackage>
							<modelPackage>com.example.backend.generated.model</modelPackage>
							<configOptions>
								<!-- インターフェースのみ生成（実装クラスは生成しない） -->
								<interfaceOnly>true</interfaceOnly>
								<!-- タグを使用してAPIをグループ化 -->
								<useTags>true</useTags>
								<dateLibrary>java8</dateLibrary>
								<useJakartaEe>true</useJakartaEe>
								<useSpringBoot3>true</useSpringBoot3>
							</configOptions>
						</configuration>
					</execution>
				</executions>
			</plugin>
```

**学習ポイント**:
- **コード生成**: 仕様書からコードを自動生成することで、手動のミスを防ぐ
- **型安全性**: インターフェースを実装することで、API仕様に準拠したコードを書ける
- **Mavenプラグイン**: ビルド時に自動的にコードを生成

**参考資料**:
- [OpenAPI Generator公式ドキュメント](https://openapi-generator.tech/)
- [OpenAPI Specification](https://swagger.io/specification/)

---

### 6. Lombok

**役割**: ボイラープレートコード（繰り返し書く必要があるコード）を削減するライブラリ。アノテーションで自動的にコードを生成します。

**このプロジェクトでの使用箇所**:
- エンティティクラス（`@Getter`, `@NoArgsConstructor`など）
- 値オブジェクト（`@EqualsAndHashCode`, `@ToString`など）

**主なアノテーション**:
- `@Getter`: getterメソッドを自動生成
- `@Setter`: setterメソッドを自動生成
- `@NoArgsConstructor`: 引数なしコンストラクタを自動生成
- `@EqualsAndHashCode`: `equals()`と`hashCode()`を自動生成
- `@ToString`: `toString()`を自動生成

**実際のコード例**:

`backend/src/main/java/com/example/backend/entity/Expense.java` (35-39行目):

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "expenses")
public class Expense {
```

**学習ポイント**:
- **ボイラープレート削減**: 手動でgetter/setterを書く必要がない
- **可読性向上**: コードが簡潔になり、意図が明確になる
- **IDEサポート**: IDE（Cursor/IntelliJ IDEA）でLombokプラグインが必要

**参考資料**:
- [Lombok公式サイト](https://projectlombok.org/)
- [Lombokアノテーション一覧](https://projectlombok.org/features/all)

---

### 7. Jakarta Validation

**役割**: 入力値の検証を行うフレームワーク。アノテーションでバリデーションルールを定義します。

**このプロジェクトでの使用箇所**:
- DTOクラス（`@NotNull`, `@Min`, `@Max`など）
- 値オブジェクト（コンストラクタでバリデーション）

**主なアノテーション**:
- `@NotNull`: nullでないことを検証
- `@Min`, `@Max`: 数値の範囲を検証
- `@Size`: 文字列やコレクションのサイズを検証
- `@Email`: メールアドレスの形式を検証

**参考資料**:
- [Jakarta Bean Validation](https://beanvalidation.org/)

---

## データベース・インフラ

### 9. MySQL 8.0

**役割**: リレーショナルデータベース管理システム（RDBMS）。データを永続化します。

**このプロジェクトでの使用箇所**:
- 支出データ、ユーザーデータの保存
- `application.properties`で接続設定

**実際の設定例**:

`backend/src/main/resources/application.properties` (3-7行目):

```properties
# MySQLの接続設定（ローカル開発用）
spring.datasource.url=${SPRING_DATASOURCE_URL_DEV}
spring.datasource.username=${MYSQL_ROOT_USER}
spring.datasource.password=${MYSQL_ROOT_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

**学習ポイント**:
- **リレーショナルデータベース**: テーブル間の関係を定義
- **SQL**: データの取得、追加、更新、削除を行う言語
- **トランザクション**: 複数の操作を1つの単位として実行

**参考資料**:
- [MySQL公式ドキュメント](https://dev.mysql.com/doc/)

---

### 10. H2 Database（テスト用）

**役割**: メモリ内データベース。テスト時に使用します。

**このプロジェクトでの使用箇所**:
- ユニットテスト、統合テスト

**学習ポイント**:
- **テスト用データベース**: 本番環境のデータベースを使わずにテストできる
- **メモリ内**: ディスクに保存されないため、高速

---

### 11. Docker & Docker Compose

**役割**: アプリケーションをコンテナ化して実行環境を統一します。

**このプロジェクトでの使用箇所**:
- `docker-compose.yaml`: 本番環境用の設定
- `docker-compose.dev.yaml`: 開発環境用の設定（MySQLのみ）

**学習ポイント**:
- **コンテナ**: アプリケーションとその依存関係をパッケージ化
- **Docker Compose**: 複数のコンテナをまとめて管理

**参考資料**:
- [Docker公式ドキュメント](https://docs.docker.com/)
- [Docker Compose公式ドキュメント](https://docs.docker.com/compose/)

---

## 認証・セキュリティ

### 12. AWS Cognito

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

### 13. Nimbus JOSE + JWT

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

### 15. JaCoCo

**役割**: テストカバレッジ（テストでカバーされているコードの割合）を測定するツール。

**このプロジェクトでの使用箇所**:
- `pom.xml`のMavenプラグイン設定

**実際のコード例**:

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

**学習ポイント**:
- **テストカバレッジ**: どのコードがテストされているかを可視化
- **品質管理**: テストが不足している箇所を特定

**参考資料**:
- [JaCoCo公式ドキュメント](https://www.jacoco.org/jacoco/)

---

### 16. JUnit 5

**役割**: Javaのユニットテストフレームワーク。

**このプロジェクトでの使用箇所**:
- すべてのテストクラス（`*Test.java`）

**学習ポイント**:
- **ユニットテスト**: 個々のメソッドやクラスをテスト
- **アサーション**: 期待値と実際の値を比較

**参考資料**:
- [JUnit 5公式ドキュメント](https://junit.org/junit5/)

---

### 17. Maven

**役割**: Javaプロジェクトのビルドツール。依存関係の管理、コンパイル、テスト、パッケージングを行います。

**このプロジェクトでの使用箇所**:
- `pom.xml`: プロジェクトの設定と依存関係

**主な機能**:
- **依存関係管理**: 必要なライブラリを自動的にダウンロード
- **ビルド**: コンパイル、テスト、パッケージングを自動化
- **プラグイン**: OpenAPI Generator、JaCoCoなどのプラグインを使用

**学習ポイント**:
- **pom.xml**: プロジェクトの設定ファイル
- **ライフサイクル**: `mvn clean`, `mvn compile`, `mvn test`, `mvn package`など
- **依存関係**: `<dependencies>`セクションでライブラリを定義

**参考資料**:
- [Maven公式ドキュメント](https://maven.apache.org/)

---

## アーキテクチャパターン

### 18. ドメイン駆動設計（DDD）

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

### セキュリティ
- **Spring Security**: 認証・認可
  - 詳細: [Spring Security + OAuth2の詳細](./spring-framework.md#spring-security--oauth2)
- **AWS Cognito**: マネージド認証サービス
- **JWT**: トークンベース認証

### 開発ツール
- **Maven**: ビルドツール
- **Lombok**: ボイラープレート削減
- **OpenAPI Generator**: コード自動生成
- **JaCoCo**: テストカバレッジ測定
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

