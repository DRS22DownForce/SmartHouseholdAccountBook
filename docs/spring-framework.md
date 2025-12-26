# Springフレームワーク詳細資料

> このドキュメントは、Smart Household Account Bookプロジェクトで使用されているSpringフレームワーク関連の技術について、学習用にまとめたものです。

## 📋 目次

1. [Spring Boot 3.5.0](#spring-boot-350)
2. [Spring Data JPA](#spring-data-jpa)
3. [Spring Security + OAuth2](#spring-security--oauth2)
4. [SpringDoc OpenAPI](#springdoc-openapi)
5. [Spring Boot DevTools](#spring-boot-devtools)

---

## Spring Boot 3.5.0

**役割**: Javaアプリケーション開発のためのフレームワーク。設定の自動化、依存性注入、アスペクト指向プログラミングなどの機能を提供します。

**主な機能**:
- **自動設定（Auto Configuration）**: 必要な設定を自動で行う
  - クラスパス上の依存関係を検出して、必要なBeanを自動的に作成・設定します
  - 例：`spring-boot-starter-data-jpa`がクラスパスにあると、データソース、EntityManager、トランザクションマネージャーなどを自動設定
  - 例：`application.properties`に`spring.datasource.url`を設定すると、その値を使ってデータソースBeanを自動生成
  - 例：MySQLのJDBCドライバーがクラスパスにあると、MySQL用のDialect(データベースごとに異なるSQLの方言)を自動選択
  - 手動で設定する必要がなくなり、開発効率が大幅に向上します
- **依存性注入（DI）**: `@Autowired`やコンストラクタインジェクションで依存関係を管理
- **プロファイル**: 環境ごとに異なる設定を管理（`application.properties`）

**実際のコード例**:

`backend/src/main/java/com/example/backend/BackendApplication.java` (1-13行目):

```java
package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
```

**`@SpringBootApplication`アノテーションの意味**:
- `@Configuration`: このクラスが設定クラスであることを示す
- `@EnableAutoConfiguration`: Spring Bootの自動設定を有効化
- `@ComponentScan`: コンポーネント（`@Service`, `@Repository`など）を自動検出

**`@SpringBootApplication`と`main`関数の関係**:
- `SpringApplication.run(BackendApplication.class, args)`の引数として、`@SpringBootApplication`が付いたクラスを指定する必要があります
- そのため、`main`関数があるクラスに`@SpringBootApplication`を付けるのが一般的で推奨されます

---

## Spring Data JPA

**役割**: データベースアクセスを簡単にするフレームワーク。リポジトリパターンを実装し、SQLを書かずにデータ操作が可能。

**このプロジェクトでの使用箇所**:
- リポジトリインターフェース（`ExpenseRepository`, `UserRepository`など）
- エンティティクラス（`Expense`, `User`など）

---

#### Entityクラス（エンティティ）とは

**Entity（エンティティ）**: データベースのテーブルに対応するJavaクラスです。データベースの1行（レコード）をJavaオブジェクトとして表現します。

**主な特徴**:
- **`@Entity`アノテーション**: このクラスがエンティティであることを示す
- **識別子（ID）を持つ**: `@Id`アノテーションで主キーを指定（例: `Long id`）
- **状態が変わる（可変）**: エンティティは変更可能です（値オブジェクトとは異なる）
- **データベースマッピング**: クラスのフィールドがデータベースのカラムに対応

**Entityと値オブジェクトの違い**:

| 特徴 | Entity（エンティティ） | 値オブジェクト |
|------|---------------------|--------------|
| **識別子** | IDを持つ（`@Id`） | IDを持たない |
| **可変性** | 状態が変わる（可変） | 不変（immutable） |
| **等価性** | IDで比較 | 値で比較 |
| **例** | `Expense`, `User` | `ExpenseAmount`, `Category` |

**Entityの役割**:
1. **データベースとのマッピング**: Javaオブジェクトとデータベースのテーブルを対応付ける
2. **状態管理**: データの変更を追跡し、データベースに反映する
3. **リレーション管理**: 他のエンティティとの関係を定義（`@ManyToOne`, `@OneToMany`など）

**実際のコード例**:

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

**主なアノテーション**:
- `@Entity`: このクラスがエンティティであることを示す
- `@Table(name = "expenses")`: データベースのテーブル名を指定（省略時はクラス名）
- `@Id`: 主キー（識別子）を指定
- `@GeneratedValue`: IDの自動生成方法を指定（`IDENTITY`はデータベースに任せる）
- `@Column`: カラムの詳細設定（`nullable = false`は必須項目）
- `@ManyToOne`: 多対一の関係（複数の支出が1つのユーザーに属する）
- `@Embedded`: 値オブジェクトをエンティティに埋め込む

---

#### @Embeddedアノテーション

**`@Embedded`とは**: 値オブジェクトをエンティティに埋め込むアノテーションです。値オブジェクトのフィールドがエンティティのテーブルのカラムとして直接マッピングされます。

**動作の仕組み**:

1. **値オブジェクト側**: `@Embeddable`アノテーションを付ける
   ```java
   @Embeddable  // このクラスが埋め込み可能であることを示す
   public class ExpenseAmount {
       @Column(name = "amount", nullable = false)  // カラム名を指定
       private final Integer value;
   }
   ```

2. **エンティティ側**: `@Embedded`アノテーションを付ける
   ```java
   @Entity
   public class Expense {
       @Embedded  // 値オブジェクトを埋め込む
       private ExpenseAmount amount;
   }
   ```

3. **データベースの結果**: 値オブジェクトのフィールドがエンティティのテーブルのカラムになる
   - `Expense`テーブルに`amount`カラムが作成される
   - **別のテーブルは作成されない**

**実際の例**:

`backend/src/main/java/com/example/backend/domain/valueobject/ExpenseAmount.java` (16-23行目):

```java
@Embeddable
@Getter
@ToString
@EqualsAndHashCode
public class ExpenseAmount{

    @Column(name = "amount", nullable = false)
    private final Integer value;
}
```

`backend/src/main/java/com/example/backend/entity/Expense.java` (54-55行目):

```java
    @Embedded
    private ExpenseAmount amount; // 金額（値オブジェクト）
```

**データベーステーブルの構造**:

`expenses`テーブル:
- `id` (BIGINT) - 主キー
- `description` (VARCHAR) - 説明
- `amount` (INTEGER) - **値オブジェクト`ExpenseAmount`の`value`フィールドがこのカラムにマッピング**
- `date` (DATE) - **値オブジェクト`ExpenseDate`のフィールドがこのカラムにマッピング**
- `category` (VARCHAR) - **値オブジェクト`Category`の`value`フィールドがこのカラムにマッピング**
- `user_id` (BIGINT) - 外部キー

**`@Embedded`を使うメリット**:
- **ドメインロジックの集約**: 値オブジェクトにビジネスロジックを集約できる
- **テーブル構造の簡素化**: 別テーブルを作らずに済む
- **型安全性**: プリミティブ型ではなく、意味のある型を使える（例: `ExpenseAmount` vs `Integer`）

**比較例**:

```java
// @Embeddedを使わない場合（値オブジェクトを使わない）
@Entity
public class Expense {
    @Column(name = "amount")
    private Integer amount;  // 単なるInteger型
}

// @Embeddedを使う場合（値オブジェクトを使う）
@Entity
public class Expense {
    @Embedded
    private ExpenseAmount amount;  // 意味のある型、バリデーション付き
}
```

どちらもデータベースには`amount`カラムが作成されますが、`@Embedded`を使うと値オブジェクトのバリデーションやビジネスロジックを活用できます。

---

#### リポジトリパターン

**リポジトリパターン**: `JpaRepository`を継承するだけでCRUD操作が可能です。

**自動実装の仕組み**:
- インターフェースを定義するだけで、Spring Data JPAが実行時に**動的プロキシ（Proxy）**を生成します
- 実装クラスは不要です

**動作の流れ**:
1. アプリケーション起動時に、`JpaRepository`を継承したインターフェースを検出
2. 実行時に動的プロキシを生成（メソッド呼び出しをインターセプト）
3. メソッド名やアノテーションからSQLを生成して実行
4. 結果を返す

**使用例**: 
```java
// インターフェースのみ定義（実装クラスは不要）
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUser(User user);
}

// サービスで使用（Springが自動的に実装を注入）
@Service
public class ExpenseApplicationService {
    private final ExpenseRepository expenseRepository; // 実装クラスは不要
    
    public ExpenseApplicationService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository; // 動的プロキシが注入される
    }
}
```

---

#### クエリメソッド

**クエリメソッド**: メソッド名から自動的にSQLを生成します。

**主な機能**:
- **メソッド名から自動生成**: `findByUser`のようにメソッド名から自動的にSQLを生成
- **カスタムクエリ**: `@Query`アノテーションでJPQL（Java Persistence Query Language）を記述

**実際のコード例**:

`backend/src/main/java/com/example/backend/domain/repository/ExpenseRepository.java` (1-50行目):

```java
package com.example.backend.domain.repository;

import com.example.backend.entity.Expense;
import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 支出エンティティのリポジトリインターフェース
 * 
 * DDDの原則に従い、ドメイン層のリポジトリインターフェースとして定義します。
 * Spring Data JPAの命名規則に従ってメソッドを定義します。
 */
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    /**
     * ユーザーを指定して支出を取得
     * 
     * @param user ユーザーエンティティ
     * @return 該当ユーザーの支出リスト
     */
    List<Expense> findByUser(User user);

    /**
     * ユーザーと日付範囲を指定して支出を取得
     * 
     * @param user ユーザーエンティティ
     * @param start 開始日（含む）
     * @param end 終了日（含む）
     * @return 該当ユーザーの指定期間内の支出リスト(降順でソート)
     */
    @Query("SELECT e FROM Expense e WHERE e.user = :user AND e.date.value >= :start AND e.date.value <= :end ORDER BY e.date.value DESC")
    List<Expense> findByUserAndDateBetween(
        @Param("user") User user,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    /**
     * ユーザーを指定して、利用可能な月（YYYY-MM形式）のリストを取得
     * 
     * @param user ユーザーエンティティ
     * @return 利用可能な月のリスト（YYYY-MM形式、降順でソート済み）
     */
    @Query("SELECT DISTINCT e.date.value FROM Expense e WHERE e.user = :user ORDER BY e.date.value DESC")
    List<LocalDate> findDistinctDatesByUser(@Param("user") User user);
}
```

---

#### リポジトリメソッドの引数の型

リポジトリのメソッドの引数には、以下のような型を使用できます：

1. **Entityクラス**: エンティティオブジェクトを引数として受け取る
   ```java
   List<Expense> findByUser(User user); // Userエンティティを引数に
   ```

2. **プリミティブ型とラッパークラス**: `String`, `Integer`, `Long`, `Boolean`など
   ```java
   Optional<User> findByCognitoSub(String cognitoSub); // String型
   ```

3. **日付・時刻型**: `LocalDate`, `LocalDateTime`, `Date`など
   ```java
   List<Expense> findByUserAndDateBetween(
       User user,
       LocalDate start,  // 日付型
       LocalDate end
   );
   ```

4. **Spring Data JPAの特殊型**: `Pageable`, `Sort`など（ページネーションやソート用）
   ```java
   Page<Expense> findByUserAndDateRange(
       User user,
       LocalDate startDate,
       LocalDate endDate,
       Pageable pageable  // ページネーション情報
   );
   ```

5. **コレクション型**: `List`, `Set`など（IN句で使用）
   ```java
   List<Expense> findByIdIn(List<Long> ids); // 複数のIDで検索
   ```

**注意点**:
- `@Query`アノテーションを使用する場合、引数に`@Param`アノテーションを付ける必要があります
- メソッド名から自動生成する場合（`findByUser`など）は、`@Param`は不要です
- Entityクラスを引数にすると、そのEntityのIDが自動的に使用されます

---

#### 学習ポイント

- **エンティティ**: `@Entity`アノテーションでデータベースのテーブルに対応
- **リポジトリ**: `JpaRepository<Entity, ID>`を継承するだけで基本的な操作が可能
- **クエリメソッド**: `findByUser`のようにメソッド名から自動的にSQLを生成
- **JPQL**: Javaオブジェクトを対象としたクエリ言語
- **引数の型**: Entityクラス、プリミティブ型、日付型、Spring Data JPAの特殊型など、様々な型が使用可能
- **@Embedded**: 値オブジェクトのフィールドがエンティティのテーブルのカラムとして直接マッピングされる

---

## Spring Security + OAuth2

**役割**: アプリケーションのセキュリティを管理するフレームワーク。認証と認可を制御します。

**このプロジェクトでの使用箇所**:
- セキュリティ設定（`SecurityConfig.java`）
- JWT認証フィルター（`JwtAuthFilter.java`）
- ユーザー登録フィルター（`UserRegistrationFilter.java`）

---

#### セキュリティ設定（SecurityConfig）

**セキュリティ設定（`SecurityConfig.java`）とは**:
セキュリティ設定は、アプリケーション全体のセキュリティポリシーを定義するクラスです。具体的には以下の設定を行います：

1. **CORS設定**: 異なるオリジン（ドメイン）からのリクエストを許可する設定
   - 例：フロントエンド（`http://localhost:3000`）からバックエンド（`http://localhost:8080`）へのリクエストを許可

2. **CSRF保護の無効化**: REST APIでJWT認証を使用する場合、CSRF保護は不要なため無効化
   - CSRF（Cross-Site Request Forgery）は、セッションクッキーを使用する場合に有効な保護機能
   - JWT認証では`Authorization`ヘッダーを使用するため、CSRF攻撃の対象にならない

3. **セッション管理の設定**: ステートレス（セッションを作成しない）に設定
   - JWT認証では、サーバー側でセッションを管理する必要がない
   - 各リクエストにJWTトークンを含めることで認証を行う

4. **認可ルールの設定**: どのパスにアクセスするために認証が必要かを定義
   - `/api/**`で始まるパスは認証必須（`authenticated()`）
   - それ以外のパスは誰でもアクセス可能（`permitAll()`）

5. **認証フィルターの登録**: JWT認証フィルターとユーザー登録フィルターを登録
   - リクエストが来たときに、これらのフィルターが順番に実行される

**コード例**:

`backend/src/main/java/com/example/backend/config/security/SecurityConfig.java` (27-70行目):

```java
@Configuration
@Profile("!test") // test環境では無効化する
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final UserRegistrationFilter userRegistrationFilter;
    private final CorsProperties corsProperties;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            UserRegistrationFilter userRegistrationFilter,
            CorsProperties corsProperties) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userRegistrationFilter = userRegistrationFilter;
        this.corsProperties = corsProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS設定を有効化
                .cors(cors -> {
                })

                // CSRF保護を無効化
                // REST APIでJWTを使用する場合、CSRF保護は不要です
                // CSRFは主にセッションクッキーを使用する場合に有効です
                // Authorizationヘッダーはブラウザから自動送信されないため、CSRF保護は不要
                .csrf(csrf -> csrf.disable())

                // セッション管理の設定
                // STATELESS: セッションを作成しない（JWT認証ではステートレスが推奨）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 認可ルールの設定
                .authorizeHttpRequests(authz -> authz
                        // /api/** で始まるパスは認証が必要
                        // authenticated()は認証済みユーザーのみアクセス可能にする
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(userRegistrationFilter, JwtAuthFilter.class);

        return http.build();
    }
}
```

---

#### Securityフィルターチェーン

**Securityフィルターチェーンとは**:

Securityフィルターチェーンは、リクエストがコントローラーに到達する前に、複数のフィルターを順番に通過させる仕組みです。各フィルターが特定のセキュリティ処理を行い、リクエストを検証・加工します。

**このプロジェクトでのフィルターチェーンの構成**:

```
リクエスト
   ↓
1. CORSフィルター（Spring Securityが自動的に追加）
   - 異なるオリジンからのリクエストを許可するかチェック
   ↓
2. JwtAuthFilter（JWT認証フィルター）
   - JWTトークンを取得・検証
   - 認証情報をセキュリティコンテキストに設定
   ↓
3. UserRegistrationFilter（ユーザー登録フィルター）
   - JWT認証が成功した後、認証済みユーザーがデータベースに登録されているか確認
   - 未登録の場合は自動的にデータベースに登録（`/api/**`パスのみ動作）
   - これにより、初回ログイン時に自動的にユーザー情報がデータベースに保存される
   ↓
4. 認可チェック（Spring Securityが自動的に実行）
   - `/api/**`パスは認証必須かチェック
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
- `addFilterAfter(userRegistrationFilter, JwtAuthFilter.class)`: ユーザー登録フィルターをJWT認証フィルターの後に配置

**なぜフィルターチェーンを使うのか**:
- **責務の分離**: 各フィルターが特定のセキュリティ処理に専念できる
- **再利用性**: フィルターを他のエンドポイントでも再利用できる
- **拡張性**: 新しいセキュリティ要件が発生した場合、新しいフィルターを追加するだけで対応できる

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
  - `aud`（Audience）: トークンの受信者（クライアントID）

- **Cognito固有のクレーム**:
  - `cognito:username`: Cognitoのユーザー名
  - `token_use`: トークンの種類（`id`、`access`など）

**このプロジェクトで使用しているクレーム**:
- `sub`: ユーザーを識別するために使用（データベースの`cognitoSub`として保存）
- `email`: ユーザーのメールアドレスを取得するために使用

**クレームの取得方法**:
```java
// セキュリティコンテキストからJWTを取得
Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

// クレームを取得
String sub = jwt.getClaimAsString("sub");        // CognitoユーザーID
String email = jwt.getClaimAsString("email");     // メールアドレス
```

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
5. AWS CognitoのJWKセットで署名を検証
   ↓ 検証成功
6. Spring Securityのセキュリティコンテキストに認証情報を設定
   ↓
7. コントローラーで認証済みユーザーとして処理
```

**JWT認証のメリット**:
- **ステートレス**: サーバー側でセッションを管理する必要がない
- **スケーラブル**: 複数のサーバーで同じトークンを使用できる
- **セキュア**: 署名検証により、トークンの改ざんを防げる


**学習ポイント**:
- **フィルターチェーン**: リクエストが複数のフィルターを通過する
- **JWT認証**: トークンベースの認証方式（ステートレス）
- **CORS**: 異なるオリジンからのリクエストを許可する設定

---
## Spring Boot DevTools

**役割**: 開発時の生産性を向上させるツール。コード変更を検知して自動的に再起動します。

**このプロジェクトでの使用箇所**:
- 開発環境でのホットリロード

**実際の設定例**:

`backend/src/main/resources/application.properties` (42-51行目):

```properties
# ========================================
# DevTools設定（開発環境のみ有効）
# ========================================
# DevToolsによる自動再起動を有効化
spring.devtools.restart.enabled=true

# ファイル変更の監視間隔（ミリ秒）
# デフォルトは1秒。この間隔でクラスパスの変更をチェック
spring.devtools.restart.poll-interval=1000

# ファイル変更が検知された後、この時間変更がなければ再起動を実行
spring.devtools.restart.quiet-period=400
```

**DevToolsが有効になる条件**:

Spring Boot DevToolsは、以下の条件を満たす場合にのみ有効になります：

1. **依存関係が含まれている**
   - `pom.xml`に`spring-boot-devtools`の依存関係が定義されている必要があります
   - このプロジェクトでは`backend/pom.xml`の139-145行目に定義されています

2. **開発環境で実行している**
   - IDE（Cursor/VS Code）から直接実行する場合 → ✅ **有効**
   - `mvn spring-boot:run`で実行する場合（JARファイルを作成せずに起動） → ✅ **有効**
   - ローカルでJARファイルを実行する場合（`java -jar app.jar`） → ❌ **無効**（通常のJARにはDevToolsが含まれない）

3. **本番環境では自動的に無効化される**
   - `pom.xml`で`<optional>true</optional>`が設定されているため、`mvn package`でJARファイルを作成する際にはDevToolsが除外されます
   - Dockerコンテナで実行する場合 → ❌ **無効**（本番用JARには含まれない）
   - パッケージ化されたJARファイルを実行する場合 → ❌ **無効**

**なぜJARファイル実行時にDevToolsが無効になるのか？**

`mvn package`コマンドでJARファイルを作成する際、Spring Boot Mavenプラグインは以下の処理を行います：

1. **依存関係の収集**
   - `pom.xml`に記載されているすべての依存関係を収集します
   - **重要**: `<optional>true</optional>`が設定されている依存関係は**自動的に除外**されます

2. **DevToolsの除外**
   - DevTools（`spring-boot-devtools`）は`optional=true`が設定されているため、JARファイルに含まれません
   - そのため、`java -jar app.jar`で実行してもDevToolsは動作しません

3. **結果**
   - 作成されたJARファイルにはDevToolsが含まれていない
   - JARファイルを実行しても、DevToolsの機能（ホットリロードなど）は使用できない