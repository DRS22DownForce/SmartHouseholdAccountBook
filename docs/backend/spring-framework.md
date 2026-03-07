# Springフレームワーク詳細資料

> このドキュメントは、Smart Household Account Bookプロジェクトで使用されているSpringフレームワーク関連の技術について、学習用にまとめたものです。

## 📋 目次

1. [Spring Boot 3.5.0](#spring-boot-350)
   - [実行環境のレイヤー](#実行環境のレイヤー)
   - [HTTP リクエストの流れ](#http-リクエストの流れtomcat--controller)
   - [外部化設定](#外部化設定externalized-configuration)
   - [ステーター（Starter）の考え方](#ステーターstarterの考え方)
2. [Spring Data JPA](#spring-data-jpa)
   - [トランザクション](#トランザクション)
3. [Spring Security + OAuth2](#spring-security--oauth2)
4. [Spring Boot DevTools](#spring-boot-devtools)

---

## Spring Boot 3.5.0

**役割**: Javaアプリケーション開発のフレームワーク。設定の自動化・依存性注入（DI）・コンポーネントスキャンを提供。

**主な機能**:
- **AutoConfiguration**: クラスパス上の依存（例: JPA、JDBCドライバー）を検出し、データソース・EntityManager等を自動生成
- **依存性注入**: `@Autowired`やコンストラクタインジェクションで依存関係を管理
- **プロファイル**: `application.properties`で環境ごとの設定を切り替え

**`@SpringBootApplication`**: `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`の複合。`main`から`SpringApplication.run(BackendApplication.class, args)`で起動し、このクラスを引数に渡すのが一般的。

#### @SpringBootApplication を構成する3つのアノテーション

| アノテーション | 役割 |
|----------------|------|
| **@Configuration** | このクラスを「Bean を定義する設定クラス」として扱う。`@Bean` メソッドで Bean を登録できる。 |
| **@EnableAutoConfiguration** | クラスパスに応じてAutoConfigurationを有効にする。Starter 由来の Bean（DataSource や DispatcherServlet など）が自動作成される。 |
| **@ComponentScan** | このクラスがあるパッケージ以下をスキャンし、`@Component` / `@Service` / `@Controller` などを Bean として登録する。 |

起動の流れ
SpringApplication.run(...) でコンテナが起動 → @ComponentScan でアプリの Bean を登録 → @EnableAutoConfiguration で Starter 由来の Bean を登録 → 組み込み Tomcat 起動」

### 実行環境のレイヤー

下から上への積み重なりは次のとおりです。

```
┌─────────────────────────────────────────────────────────┐
│  Spring Boot アプリ (BackendApplication, Controller 等)   │  ← あなたのコード
├─────────────────────────────────────────────────────────┤
│  組み込み Web サーバー (Embedded Tomcat)                  │  ← JAR 内に同梱
├─────────────────────────────────────────────────────────┤
│  実行可能 JAR (backend-0.0.1-SNAPSHOT.jar)               │  ← 1 ファイルで配布
├─────────────────────────────────────────────────────────┤
│  JVM (Java Virtual Machine)                              │  ← バイトコード実行
├─────────────────────────────────────────────────────────┤
│  JDK 21 (java コマンド・標準ライブラリ等)                 │  ← 実行・開発環境
├─────────────────────────────────────────────────────────┤
│  OS (Linux)                                              │
└─────────────────────────────────────────────────────────┘
```

#### 各レイヤーの説明

| レイヤー | 役割 |
|----------|------|
| **JDK** | Java を「開発する」＋「実行する」ための一式。主なもの: **javac**（ソースをバイトコードにコンパイル）、**java**（JVM 起動）、**jar**（JAR の作成・解凍）。 |
| **JVM** | `.class`（バイトコード）を解釈・実行する実行エンジン。JDK の `java` コマンドが JVM を立ち上げる。 |
| **JAR** | Java Archive。クラスとリソースをまとめた配布単位。Spring Boot では**実行可能 JAR（fat JAR）**となり、アプリ＋依存（Spring、Tomcat 等）が 1 つに同梱される。`java -jar backend-0.0.1-SNAPSHOT.jar` でサーバー起動。 |
| **組み込み Tomcat** | `spring-boot-starter-web` に同梱。JVM 内で起動し HTTP を受信。Servlet コンテナ（Servlet API を実装）。 |
| **Spring Boot アプリ** | Controller や DI コンテナなど、あなたのコードと Spring の処理。 |

**WAR との違い（参考）**

- **WAR**: 従来型。外部の Web コンテナ（Tomcat 等）にデプロイする形式。コンテナが WAR を読み込み Servlet を動かす。
- **JAR（Spring Boot）**: Web サーバーが JAR 内に含まれる。外部 Tomcat は不要。

### HTTP リクエストの流れ（Tomcat → Controller）

Tomcat は Servlet コンテナ。Spring Boot は起動時に **DispatcherServlet** を 1 本だけ Tomcat に登録する。DispatcherServlet が URL や HTTP メソッドを見て、どの `@RestController` / `@Controller` に渡すか決める。流れは **Tomcat → DispatcherServlet → Controller**。

```
HTTP リクエスト
      ↓
┌─────────────────────────────────────────┐
│  Tomcat（Servlet コンテナ）              │  ← ソケットで HTTP を受信
│  「どの Servlet / Filter に渡すか」を管理  │
└─────────────────────────────────────────┘
      ↓
┌─────────────────────────────────────────┐
│  Filter チェーン（Servlet の前処理）      │  ← Servlet API を使用
│  JwtAuthFilter → UserRegistrationFilter  │     (HttpServletRequest 等)
└─────────────────────────────────────────┘
      ↓
┌─────────────────────────────────────────┐
│  DispatcherServlet（唯一の Servlet）     │  ← URL に応じて Controller を選択
└─────────────────────────────────────────┘
      ↓
┌─────────────────────────────────────────┐
│  @RestController / @Controller           │  ← あなたの API
└─────────────────────────────────────────┘
      ↓
HTTP レスポンス
```

### 外部化設定（Externalized Configuration）

設定をコードの外に置き、環境（開発・本番など）やデプロイ先で変えられるようにする仕組み。同じ JAR を、設定だけ変えて複数環境で動かせる。

```
設定の優先順位（上ほど優先される）
      ↑
┌─────────────────────────────────────────────────────────┐
│  1. コマンドライン引数                                    │  java -jar app.jar --server.port=9090
├─────────────────────────────────────────────────────────┤
│  2. 環境変数                                             │  SPRING_DATASOURCE_URL=jdbc:mysql://...
├─────────────────────────────────────────────────────────┤
│  3. application-{profile}.properties / .yml             │  application-prod.properties（プロファイル指定時）
├─────────────────────────────────────────────────────────┤
│  4. application.properties / application.yml            │  src/main/resources/ に配置
└─────────────────────────────────────────────────────────┘
```

#### 外部化設定のポイント

| 項目 | 説明 |
|------|------|
| **application.properties** | デフォルトの設定。`src/main/resources/` に置く。キーは `server.port` のようにドットで階層化。 |
| **プロファイル** | `spring.profiles.active=prod` と設置することで `application-prod.properties` が追加で読み込まれる。同じキーはプロファイル側が上書き。 |
| **環境変数** | 本番やコンテナでは環境変数で上書きすることが多い。`SPRING_DATASOURCE_URL` のように大文字・アンダースコアにすると、`spring.datasource.url` に対応する。そのため`openai.api.key=${OPENAI_API_KEY}`のような記載はなくてもいい |
| **利点** | コードを変えずに DB の URL や API キーを切り替えられる。秘密情報をソースに含めず、12-Factor の「設定を環境に持つ」に沿った運用がしやすい。 |

---

### ステーター（Starter）の考え方

**Starter** は、ある機能に必要な依存ライブラリと、それに合うデフォルト設定(Bean定義が含まれるJARがクラスパスに取り込まれる)をひとまとめにした Maven/Gradle の依存単位。`spring-boot-starter-xxx` を 1 つ追加するだけで、その機能に必要なものが一括で入る。

```
pom.xml に starter を追加
      ↓
┌─────────────────────────────────────────────────────────┐
│  spring-boot-starter-web を追加した場合                   │
│  → spring-webmvc, tomcat, jackson-databind, ... が       │
│     「互換の取れたバージョン」で自動的に入る               │
├─────────────────────────────────────────────────────────┤
│  自動設定（Auto-configuration）が有効になる               │
│  → クラスパスに JAR があるのを検知し、DispatcherServlet  │
│     or データソースなどを Bean として登録                 │
└─────────────────────────────────────────────────────────┘
```

#### このプロジェクトで使っている主な Starter

| Starter | 役割 |
|---------|------|
| **spring-boot-starter-parent** | 親 POM。Spring Boot のバージョン・プラグイン・依存のバージョン管理を一括で行う。Spring bootのバージョンを指定することで、その他Starterのバージョンを指定しなくても、適切なバージョンが自動で使われる |
| **spring-boot-starter-web** | Web API 用。組み込み Tomcat、Spring MVC、JSON 変換（Jackson）など。 |
| **spring-boot-starter-data-jpa** | JPA + Hibernate。リポジトリの実装やトランザクションまわりも含む。 |
| **spring-boot-starter-security** | 認証・認可の基盤。フィルターや SecurityContext の仕組みを提供。 |
| **spring-boot-starter-oauth2-resource-server** | JWT などでリソースサーバーとして動かすための検証機能。 |
| **spring-boot-starter-oauth2-client** | OAuth2 クライアント（Cognito 等への認証リクエスト）用。このプロジェクトでは主にリソースサーバー側で JWT 検証に利用。 |
| **spring-boot-starter-validation** | Bean Validation（@Valid, @NotNull 等）。リクエストの入力チェックに使う。 |
| **spring-boot-starter-cache** | キャッシュ抽象化。実装（Caffeine 等）は別依存で追加する。 |
| **spring-boot-starter-test** | テスト用。JUnit 5、Mockito、Spring TestContext など。 |

Starter 同士のバージョンは親 POM で揃えられるため、ライブラリの組み合わせ不整合を減らせる。

---

## Spring Data JPA

**役割**: データベースアクセスを簡単にするフレームワーク。リポジトリパターンを実装し、SQLを書かずにデータ操作が可能。

**使用箇所**: リポジトリ（`ExpenseRepository`, `UserRepository`等）、エンティティ（`Expense`, `User`等）。

---

#### Entityクラス（エンティティ）とは

**Entity（エンティティ）**: データベースのテーブルに対応するJavaクラスです。データベースの1行（レコード）をJavaオブジェクトとして表現します。

**Entityと値オブジェクトの違い**:

| 特徴       | Entity（エンティティ） | 値オブジェクト   |
|------------|------------------------|------------------|
| **識別子** | IDを持つ（`@Id`）      | IDを持たない     |
| **可変性** | 状態が変わる（可変）   | 不変（immutable）|
| **等価性** | IDで比較               | 値で比較         |
| **例**     | `Expense`, `User`      | `ExpenseAmount`, `Category` |

**Entityの役割**:

1. **データベースとのマッピング**: Javaオブジェクトとデータベースのテーブルを対応付ける
2. **状態管理**: データの変更を追跡し、データベースに反映する
3. **リレーション管理**: 他のエンティティとの関係を定義（`@ManyToOne`, `@OneToMany`など）

**主なアノテーション**:

- `@Entity`: このクラスがエンティティであることを示す
- `@Table(name = "テーブル名")`: データベースのテーブル名を指定（省略時はクラス名）
- `@Table(name="テーブル名", indexes ={@Index(name = "インデックス名", columnList = "インデックスを作成する列")})`で特定にカラムに対するインデックスを作成できる
- `@Id`: 主キー（識別子）を指定
- `@GeneratedValue`: IDの自動生成方法を指定（`@GeneratedValue(strategy = GenerationType.IDENTITY)`はDBの自動採番機能によって主キーを生成する方式）
- `@Column`: カラムの詳細設定（`nullable = false`：NULLを許容しない。`columnDefinition = "TEXT"`：DB上の型をテキストにする。`updatable = false` INSERT時だけ値が設定されUPDATEでは変更されない）
- `@ManyToOne`: 多対一の関係（複数の支出が1つのユーザーに属する）。`@ManyToOne(fetch = FetchType.LAZY)`とすると、関連のEnittyのフィールドに実際にアクセスするまで、そのEntityへのSELECTは発行されない。
- `@Embedded`: 値オブジェクトをエンティティに埋め込む
- `@Enumerated`: EnumをDBにどう保存するかの指定。`@Enumerated(EnumType.STRING)`は列挙子の名前を文字列で保存する設定。

**ポイント**:

- DBのテーブルがEnittyに合わせて自動生成されるかはapplication.propertiesの`spring.jpa.hibernate.ddl-auto`の設定に依存する。
- JPAの仕様でEntityには`protected`か`public`な引数なしコンストラクタが必要。
- `@Embeddable`を付けたクラスにも同様に引数なしコンストラクタが必要
- `@ManyToOne(fetch = FetchType.LAZY)`とするとN+1問題が発生する可能性がある
  - 例えばExpenseをリストで複数個取得し、それぞれにExpense.getUser()をするとそれぞれにUserを取得するクエリが実行される。
  - この対策として、Expenseと一緒に関連のUserも取得する`JOIN FETCH`や`@EntityGraph`を利用する。
- インデックスをつけると検索が速くなるので、WHERE句で絞り込んだりやOREDER BYでソートをよく利用する列につけると効果的。ただし更新系の処理は遅くなるのでやり過ぎは注意。

---

#### @Embeddedアノテーション

- **`@Embedded`とは**: エンティティにテーブルに別オブジェクトを埋め込むアノテーションです。別オブジェクトのフィールドがエンティティのテーブルのカラムとして直接マッピングされる
- **`@Embeddable`とは**：エンティティのテーブルに埋め込み可能なクラスであることを宣言する

---

#### リポジトリパターン

`JpaRepository`を継承するインターフェースを定義することでCRUDが可能。実装クラスは不要で、Spring Data JPAが実行時にインターフェースの実装クラスである**動的プロキシ**を生成し、これをDIする。動的プロキシ、Hibernateを通してSQLが発行される。
役割分担：動的プロキシ（メソッド解析）→ Hibernate（SQL生成）→ JDBCドライバー（DB接続）→ DB。動的プロキシの動作はH2/MySQLで同じ。

---

#### JDBCドライバー・H2とMySQL

- **JDBCドライバー**: JavaとDBの橋渡し。`pom.xml`で`mysql-connector-j`（本番）や`h2`（テスト）を依存追加。`spring.datasource.url`のプロトコル（`jdbc:mysql`等）からSpring Bootがドライバーを自動検出。SpringBootのAutoConfigurationにより、検出されたJDBCドライバからDataSource Beanが自動生成される。
- **H2とMySQLの違い**: Dialect（`H2Dialect` / `MySQLDialect`）と接続URL・保存先（メモリ/ディスク）が異なる。H2はインメモリのためサーバー不要でテスト終了後に自動削除。
- **H2の起動**: テスト時は`application-test.properties`で`spring.datasource.url=jdbc:h2:mem:testdb`を指定すると、Spring Boot起動時にインメモリDBが自動作成される。

---

#### クエリメソッド・引数の型

- **クエリメソッド**: メソッド名（例: `findByUser`）からSQLを自動生成。`@Query`でJPQLを直接記述可能。`@Query`を使う場合は引数に`@Param`を付ける。
- **引数の型**: Entity、`String`/`Long`等、`LocalDate`、`Pageable`/`Sort`、`List`（IN句）などが使える。Entityを引数にするとそのIDが条件に使われる。

**JPQL（Java Persistence Query Language）とは**:
- データベースのテーブル名・カラム名ではなく、**Entity クラス名とプロパティ名**を指定して検索するクエリ言語。JPA の標準仕様で、Hibernate が JPQL を解釈し、接続先 DB に合わせた SQL に変換して実行する。
- **SQL との違い**: SQL は `SELECT * FROM expense WHERE user_id = ?` のようにテーブル・列を書く。JPQL は `SELECT e FROM Expense e WHERE e.user = :user` のように Entity（`Expense`）とそのプロパティ（`e.user`）で書く。DB に依存しない書き方になるため、H2 と MySQL で同じ JPQL が使える。
- **`@Query` での使い方**: リポジトリのメソッドに `@Query("JPQL文")` を付け、引数は `:引数名` で JPQL 内にバインドする。メソッドの引数には `@Param("引数名")` を付けて、`:引数名` と対応させる。例: `@Query("SELECT e FROM Expense e WHERE e.user = :user AND e.date.date >= :start ...")` と `@Param("user")`, `@Param("start")`。

---

#### トランザクション

**トランザクションとは**: 複数のデータベース操作をひとまとまりとして扱い、「すべて成功する」か「すべて取り消す」のどちらかにする仕組み。途中で例外が起きた場合は、それまでに行った変更をロールバック（取り消し）し、データの整合性を保つ。

| 項目 | 説明 |
|------|------|
| **開始** | `@Transactional` が付いたメソッドを呼び出したときに、そのメソッドの先頭でトランザクションが開始される。 |
| **コミット** | メソッドが正常終了すると、トランザクションがコミットされ、変更がDBに確定する。 |
| **ロールバック** | メソッド内で未チェック例外（`RuntimeException` 等）が発生すると、トランザクションはロールバックされ、そのトランザクション内の変更はDBに反映されない。 |

**`@Transactional` の付け方**:
- **サービス層に付ける**: 複数のリポジトリ呼び出しや、1リクエストで複数テーブルを更新する処理を、1つのトランザクションにまとめたいときに、`@Service` クラスのメソッドに `@Transactional` を付ける。Spring Data JPA のリポジトリ単体のメソッドもトランザクション内で動くが、サービスでまとめることで「読んでから書く」までを一括でコミット・ロールバックできる。
- **読み取り専用**: 参照だけするメソッドでは `@Transactional(readOnly = true)` を付けると、書き込み用の準備を省略でき、パフォーマンスや接続の扱いの最適化が行われる場合がある。

**Entity の状態管理との関係**:
- トランザクション内で `findById` などで取得した Entity は「永続コンテキスト(トランザクションの間だけ存在するメモリ上のデータ構造)で管理されている」状態になる。
- その Entity のフィールドを変更すると、永続コンテキストに変更が保存され、コミット時にDBに反映される。つまり、明示的に `save()` を呼ばなくても、トランザクションがコミットされれば DB に反映される。
- 逆に、トランザクションがロールバックされると、その中で行った Entity の変更も DB には反映されない。

---

## Spring Security + OAuth2

**役割**: アプリケーションのセキュリティを管理するフレームワーク。認証と認可を制御します。

**このプロジェクトでの使用箇所**:
- セキュリティ設定（`SecurityConfig.java`）
- JWT認証フィルター（`JwtAuthFilter.java`）

---

#### セキュリティ設定（SecurityConfig）

**セキュリティ設定（`SecurityConfig.java`）とは**:
セキュリティ設定は、アプリケーション全体のセキュリティポリシーを定義するクラスです。具体的には以下の設定を行います：

1. **CORS設定**: 異なるオリジン（ドメイン）からのリクエストを許可する設定
   - 例：フロントエンドサーバ（`http://localhost:3000`）で取得したJSからバックエンド（`http://localhost:8080`）へのリクエストを許可
   - バックエンドがリプライ時に`Access-Control-Allow-Origin: http://localhost:3000`というヘッダーを付けて、このオリジン（フロントエンドサーバ）からリクエストは許可すると宣言する。
   - ブラウザはこのヘッダーが確認されたときのみJSにデータを渡す。確認できない場合はCORエラーになる。

2. **CSRF保護の無効化**: REST APIでJWT認証を使用する場合、CSRF保護は不要なため無効化
   - CSRF（Cross-Site Request Forgery）は、セッションクッキーを使用する場合に有効な保護機能
   - ブラウザは同一オリジンへのリクエストにクッキーを自動付与するため、あるサイトにログインしてセッションクッキーを取得した後、悪意ある別サイトからそのオリジンにリクエストが送られるとセッションクッキーが付与されてしまう。
   - フォームにサーバ発行のトークンを埋め込むなどして対策する。
   - JWT認証では`Authorization`ヘッダーを使用するため、CSRF攻撃の対象にならない。`Authorization`ヘッダーはブラウザによって自動付与されないため。

3. **セッション管理の設定**: ステートレス（セッションを作成しない）に設定
   - JWT認証では、サーバー側でセッションを管理する必要がない
   - 各リクエストにJWTトークンを含めることで認証を行う
   - JWTトークンにはユーザ情報が含まれているので、サーバーは署名の検証だけで、ユーザを判別できる。
   - ステートフルの場合、ログイン後セッションIDとユーザ情報のセッションストアがサーバ側で保存される。ログイン後のリクエストのクッキーヘッダーのセッションIDが付与される。

4. **認可ルールの設定**: どのパスにアクセスするために認証が必要かを定義
   - ウェルカムページ`/`は認証なしでアクセス可能。簡易的なヘルスチェックのため。将来的にAcutuatorに以移行する。(`permitALl()`)
   - `/api/**`で始まるパスは認証必須（`authenticated()`）
   - それ以外のパス全て拒否する（`denyAll()`）

5. **認証フィルターの登録**: JWT認証フィルターとユーザー登録フィルターを登録
   - リクエストが来たときに、これらのフィルターが順番に実行される

---

#### Securityフィルターチェーン

**Securityフィルターチェーンとは**:

Securityフィルターチェーンは、リクエストがコントローラーに到達する前に、複数のフィルターを順番に通過させる仕組みです。各フィルターが特定のセキュリティ処理を行い、リクエストを検証・加工します。

**このプロジェクトでのフィルターチェーンの構成（主要なもの）**:

```
リクエスト
   ↓
1. SecurityContextHolderFilte
　 - 各リクエストの最初に、そのリクエスト用の SecurityContext を SecurityContextHolder に紐づけ、リクエスト終了後にクリアする。
   ↓
2. CORSフィルター（Spring Securityが自動的に追加）
   - プリフライトへの応答
   - レスポンスにCORSヘッダーを付与する 
   ↓
3. JwtAuthFilter（JWT認証フィルター）
   - JWTトークンを取得・検証
   - 認証情報をセキュリティコンテキストに設定
   ↓
4. AuthorizationFilter（認可チェック）（Spring Securityが自動的に追加）
   - `http.authorizeHttpRequest()`のルールを検証
   - `/api/**`パスは認証必須
   - 認証されていない場合は401 Unauthorizedを返す
   ↓
5. コントローラー
   - リクエストを処理
```

**認証（Authentication）と認可（Authorization）の違い**:

- **認証（JwtAuthFilterで実行）**: 「ユーザーが誰であるか」を確認する処理
  - JWTトークンを検証して、ユーザーが正しいユーザーであることを確認
  - セキュリティコンテキストに認証情報を設定
- **認可（認可チェックで実行）**: 「認証済みユーザーが特定のリソースにアクセスする権限があるか」を確認する処理
  - 既に認証されているユーザーが、そのエンドポイントにアクセスする権限があるかをチェック
  - `/api/**`パスは認証必須というルールを適用
- **エラーレスポンスの違い**:
  - 認証されていない場合: 401 Unauthorized（認証フィルターで処理）
  - 認証されているが権限がない場合: 403 Forbidden（認可チェックで処理）

**フィルターチェーンの特徴**:
- **順次実行**: フィルターは登録された順番に実行される
- **早期終了**: いずれかのフィルターでエラーが発生した場合、後続のフィルターやコントローラーには到達しない
- **フィルターの追加方法**:
  - `addFilterBefore(filter, targetFilter)`: 指定したフィルターの前に追加
  - `addFilterAfter(filter, targetFilter)`: 指定したフィルターの後に追加

**このプロジェクトでのフィルター登録**:
- `addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)`: JWT認証フィルターをSpring Securityの標準認証フィルターの前に配置
  - **`UsernamePasswordAuthenticationFilter`とは**: Spring Securityが提供する標準的な認証フィルターです
  - **通常の動作**: ユーザー名とパスワードによるフォーム認証を行う（`/login`エンドポイントでPOSTリクエストを受け取り、認証を実行）
  - **このプロジェクトでの使用**: JWT認証を使用しているため、このフィルターは実際には使用されませんが、フィルターチェーン上の位置を指定するための基準点として使用しています
  - **なぜ前に配置するか**: JWT認証を先に実行することで、ユーザー名・パスワード認証が不要になるため、JWT認証が成功した場合は`UsernamePasswordAuthenticationFilter`をスキップできます
  - **スキップされる仕組み**:
    1. JWT認証フィルターが先に実行され、JWTトークンの検証が成功すると、`SecurityContextHolder`に認証情報が設定されます
    2. `UsernamePasswordAuthenticationFilter`は、セキュリティコンテキストに認証情報が既に設定されている場合、処理をスキップします
    3. これは、Spring Securityの「既に認証済みのリクエストに対しては再度認証を行わない」という仕組みによるものです
    4. 結果として、JWT認証が成功したリクエストでは、ユーザー名・パスワード認証は実行されません

---

#### JWT認証（JwtAuthFilter）

**JWT認証（`JwtAuthFilter.java`）で行っていること**:

JWT認証フィルターは、リクエストが来たときに以下の処理を順番に実行します：

1. **JWTトークンの取得**: リクエストヘッダーの`Authorization`からJWTトークンを取得
   - 形式: `Authorization: Bearer <JWTトークン>`
   - 例: `Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...`

**JWTトークンの構造**:

JWTトークンは3つの部分で構成されています（`.`で区切られています）：
- **ヘッダー（Header）**: トークンのタイプと署名アルゴリズム（例: `RS256`）
- **ペイロード（Payload）**: ユーザー情報や権限などのクレーム（claims）
- **署名（Signature）**: ヘッダーとペイロードを秘密鍵で署名したもの

**JWTトークンに含まれる情報（クレーム）**:

AWS Cognitoが発行するJWTトークンには、以下のような情報が含まれています：

- **標準クレーム（Standard Claims）**:
  - `sub`（Subject）: CognitoユーザーID（一意の識別子）
  - `email`: ユーザーのメールアドレス
  - `iat`（Issued At）: トークンが発行された時刻（Unixタイムスタンプ）
  - `exp`（Expiration Time）: トークンの有効期限（Unixタイムスタンプ）
  - `nbf`（Not Before）: トークンが有効になる時刻（Unixタイムスタンプ）
  - `iss`（Issuer）: トークンの発行者（AWS CognitoのURL）
  - `aud`（Audience）: どのアプリ向けに発行されたか

- **Cognito固有のクレーム**:
  - `cognito:username`: Cognitoのユーザー名
  - `token_use`: トークンの種類（`id`、`access`など）


2. **JWTトークンの署名検証**: AWS CognitoのJWKセット（公開鍵セット）を使用して、トークンの署名が正しいか検証
   - **署名検証の目的**: トークンが改ざんされていないことを確認
   - **RS256アルゴリズム**: RSA署名アルゴリズムを使用（公開鍵で検証、秘密鍵で署名）
   - **秘密鍵と公開鍵の関係**: 
     - AWS Cognitoが秘密鍵で署名したトークンは、対応する公開鍵でのみ検証できます
     - 秘密鍵はCognitoのみが保持（外部に公開されない）
     - 公開鍵はJWKセットとして公開され、誰でも検証に使用可能
   - **JWKセット**: AWS Cognitoが提供する公開鍵のセット（`https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json`）

3. **JWTトークンの有効性チェック**: トークンの有効期限や発行時刻を確認
   - **有効期限（exp）**: トークンが期限切れでないか確認
   - **発行時刻（iat）**: トークンがいつ発行されたか確認
   - **有効開始時刻（nbf）**: トークンが有効になる時刻を確認

4. **認証情報の設定**: 検証が成功した場合、Spring Securityのセキュリティコンテキストに認証情報を設定
   - JWTトークンからユーザー情報（`sub`、`cognito:username`など）を取得
   - `SecurityContextHolder`に認証情報を設定することで、後続の処理で認証済みユーザーとして扱われる

5. **エラーハンドリング**: 検証が失敗した場合、401 Unauthorizedを返す

**セキュリティコンテキスト（SecurityContext）とは**:
- 現在のリクエストを処理しているユーザーの認証情報を保持するオブジェクトです
- 例：誰がログインしているか、どのような権限を持っているかなどの情報
- リクエストごとに独立しており、1つのリクエスト処理中は同じセキュリティコンテキストが使用されます

**SecurityContextHolderとは**:
- セキュリティコンテキストを保持・管理するクラスです
- `SecurityContextHolder.getContext()`で現在のセキュリティコンテキストを取得できます
- スレッドローカル（ThreadLocal）を使用して、各スレッド（リクエスト）ごとに独立したセキュリティコンテキストを管理します

**なぜ必要か**:
- コントローラーやサービスで「現在ログインしているユーザーは誰か？」を簡単に取得できるようにするため
- 例：`SecurityContextHolder.getContext().getAuthentication()`で認証情報を取得し、ユーザーIDを取得してデータベースから該当ユーザーのデータを取得する

**JWT認証の流れ**:

```
1. ユーザーがログイン（フロントエンドからAWS Cognitoに認証情報を送信）
   ↓ 認証成功
2. AWS CognitoがJWTトークンを発行(AWS Cognitoが管理する秘密鍵（private key）を使用してJWTトークンに署名します)
   ↓ JWTトークンをクライアントに返す
3. クライアント（フロントエンド）
   ↓ Authorization: Bearer <JWTトークン> を送信
4. JwtAuthFilter
   ↓ JWTトークンを取得
5. AWS CognitoのJWKソース（公開鍵）で署名を検証
   ↓ 検証成功
6. Spring Securityのセキュリティコンテキストに認証情報を設定
   ↓
7. コントローラーで認証済みユーザーとして処理
```

**JWT認証のメリット**:
- **ステートレス**: サーバー側でセッションを管理する必要がない
- **スケーラブル**: 複数のサーバーで同じトークンを使用できる
- **セキュア**: 署名検証により、トークンの改ざんを防げる
- **SPA+REST APIの構成と相性がいい**：セッション方式だとオリジンが異なる場合、デフォルトではセッションクッキーが送られなくなるが、`Authorization`ヘッダーで送るJWT方式はその制約を受けない。


---
## Spring Boot DevTools

**役割**: コード変更を検知して自動再起動（ホットリロード）し、開発効率を上げる。

**有効条件**: `pom.xml`に`spring-boot-devtools`を追加し、IDEや`mvn spring-boot:run`で起動する場合。`mvn package`で作ったJARでは`<optional>true</optional>`によりDevToolsが除外されるため無効。