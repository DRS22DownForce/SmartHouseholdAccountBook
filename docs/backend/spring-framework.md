# Springフレームワーク詳細資料

> このドキュメントは、Smart Household Account Bookプロジェクトで使用されているSpringフレームワーク関連の技術について、学習用にまとめたものです。

## 📋 目次

1. [Spring Boot 3.5.0](#spring-boot-350)
2. [Spring Data JPA](#spring-data-jpa)
3. [Spring Security + OAuth2](#spring-security--oauth2)
4. [Spring Boot DevTools](#spring-boot-devtools)

---

## Spring Boot 3.5.0

**役割**: Javaアプリケーション開発のフレームワーク。設定の自動化・依存性注入（DI）・コンポーネントスキャンを提供。

**主な機能**:
- **自動設定**: クラスパス上の依存（例: JPA、JDBCドライバー）を検出し、データソース・EntityManager等を自動設定
- **依存性注入**: `@Autowired`やコンストラクタインジェクションで依存関係を管理
- **プロファイル**: `application.properties`で環境ごとの設定を切り替え

**`@SpringBootApplication`**: `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`の複合。`main`から`SpringApplication.run(BackendApplication.class, args)`で起動し、このクラスを引数に渡すのが一般的。

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
- `@Column`: カラムの詳細設定（`nullable = false`：NULLを強要しない。`columnDefinition = "TEXT"`：DB上の型をテキストにする。`updatable = false` INSERT時だけ値が設定されUPDATEでは変更されない）
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
- インデックスをつけると検索が速くなるので、WHERE句やOREDER BYをよく利用する列につけると効果的。ただし更新系の処理は遅くなるのでやり過ぎは注意。

---

#### @Embeddedアノテーション

- **`@Embedded`とは**: エンティティにテーブルに別オブジェクトを埋め込むアノテーションです。別オブジェクトのフィールドがエンティティのテーブルのカラムとして直接マッピングされる
- **`@Embeddable`とは**：エンティティのテーブルに埋め込み可能なクラスであることを宣言する

---

#### リポジトリパターン

`JpaRepository`を継承するだけでCRUDが可能。実装クラスは不要で、Spring Data JPAが実行時に**動的プロキシ**を生成し、メソッド名からSQLを生成してHibernate経由で実行する。役割分担：動的プロキシ（メソッド解析）→ Hibernate（SQL生成）→ JDBCドライバー（DB接続）→ DB。動的プロキシの動作はH2/MySQLで同じ。

---

#### JDBCドライバー・H2とMySQL

- **JDBCドライバー**: JavaとDBの橋渡し。`pom.xml`で`mysql-connector-j`（本番）や`h2`（テスト）を依存追加。`spring.datasource.url`のプロトコル（`jdbc:mysql`等）からSpring Bootがドライバーを自動検出。
- **H2とMySQLの違い**: Dialect（`H2Dialect` / `MySQLDialect`）と接続URL・保存先（メモリ/ディスク）が異なる。H2はインメモリのためサーバー不要でテスト終了後に自動削除。
- **H2の起動**: テスト時は`application-test.properties`で`jdbc:h2:mem:testdb`を指定すると、Spring Boot起動時にインメモリDBが自動作成される。

---

#### クエリメソッド・引数の型

- **クエリメソッド**: メソッド名（例: `findByUser`）からSQLを自動生成。`@Query`でJPQLを直接記述可能。`@Query`を使う場合は引数に`@Param`を付ける。
- **引数の型**: Entity、`String`/`Long`等、`LocalDate`、`Pageable`/`Sort`、`List`（IN句）などが使える。Entityを引数にするとそのIDが条件に使われる。

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