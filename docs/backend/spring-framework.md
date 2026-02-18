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

#### 動的プロキシとJDBCドライバーの関係

**動的プロキシとは**:

動的プロキシは、実行時に生成される「代理オブジェクト」です。インターフェースの実装クラスを自動生成し、メソッド呼び出しを横取り（インターセプト）して独自の処理を実行します。

**動作の流れ（詳細版）**:

```
1. コードでメソッドを呼び出す
   expenseRepository.findByUser(user)
   ↓
2. 動的プロキシがメソッド呼び出しを「横取り（インターセプト）」
   「findByUserというメソッドが呼ばれたな。メソッド名を解析しよう」
   ↓
3. Spring Data JPAがメソッド名を解析
   「findByUser」→ 「Userで検索する」という意味だと理解
   ↓
4. Hibernate（JPA実装）がSQLを生成
   SELECT * FROM expenses WHERE user_id = ?
   ↓
5. HibernateがJDBCドライバーを使用してデータベースに接続
   ↓
6. JDBCドライバーがSQLをデータベースに送信
   ↓
7. データベースがSQLを実行
   ↓
8. 結果をJDBCドライバーが受け取る
   ↓
9. Hibernateが結果をJavaオブジェクト（List<Expense>）に変換
   ↓
10. 動的プロキシが結果を返す
    ↓
11. 元のコードに結果が返る
    List<Expense> expenses = ...（結果）
```

**役割分担**:

| 役割 | 担当者 | 説明 |
|------|--------|------|
| **メソッド呼び出しの横取り** | 動的プロキシ（Spring Data JPA） | メソッド名を解析して、何をしたいのか理解する |
| **SQLの生成** | Hibernate（JPA実装） | メソッド名から適切なSQLを生成 |
| **データベース接続** | JDBCドライバー | 実際にデータベースに接続してSQLを送信 |
| **SQLの実行** | データベース（MySQL/H2など） | SQLを実行して結果を返す |

**重要なポイント**:
- **動的プロキシの実装はJDBCドライバーの種類によって変化しない**: 動的プロキシの生成方法や動作は、H2でもMySQLでも同じです
- **動的プロキシはHibernateに直接依頼するのではなく、内部実装の一部としてHibernateを使う**: 動的プロキシ（Spring Data JPAの実装）が、内部でHibernateを使ってSQLを生成・実行します

---

#### JDBCドライバーの設定

**JDBCドライバーとは**:

JDBCドライバーは、Javaアプリケーションとデータベースを接続するための橋渡し役です。データベースごとに専用のJDBCドライバーが必要です。

**pom.xmlでの指定（必須）**:

JDBCドライバーは、`pom.xml`で依存関係として指定する必要があります。

```xml
<!-- MySQL用のJDBCドライバー -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- H2用のJDBCドライバー（テスト用） -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

**動作の仕組み**:

1. **依存関係の追加**: `pom.xml`にJDBCドライバーの依存関係を追加
2. **自動ダウンロード**: Mavenが初回ビルド時にJDBCドライバーのJARファイルを自動的にダウンロード（`~/.m2/repository/`に保存）
3. **クラスパスに追加**: ビルド時にJDBCドライバーのJARファイルがクラスパスに自動的に追加される
4. **自動検出**: Spring Bootがクラスパス上のJDBCドライバーを自動的に検出して使用

**application.propertiesでの指定（オプション）**:

通常は`spring.datasource.url`から自動検出されますが、明示的に指定することもできます：

```properties
# データベース接続URL（JDBCドライバーを自動検出）
spring.datasource.url=jdbc:mysql://localhost:3306/mydb

# または、明示的にJDBCドライバークラスを指定（通常は不要）
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

**自動検出の仕組み**:

Spring Bootは、`spring.datasource.url`のプロトコル部分（`jdbc:mysql`、`jdbc:h2`など）からデータベースの種類を判断し、クラスパス上にある対応するJDBCドライバーを自動的に検出・使用します。

```
1. URLのプロトコル部分を解析
   jdbc:mysql://... → MySQL用のJDBCドライバーが必要
   jdbc:h2:... → H2用のJDBCドライバーが必要
   ↓
2. クラスパス上にあるJDBCドライバーを検索
   → 対応するJDBCドライバーのJARファイルが見つかる
   ↓
3. 適切なJDBCドライバークラスを自動的に選択
   → com.mysql.cj.jdbc.Driver や org.h2.Driver を使用
```

**重要なポイント**:
- **pom.xmlで取得するJARファイルがJDBCドライバーに該当する**: `mysql-connector-j`や`h2`のJARファイルに、JDBCドライバークラスが含まれています
- **どのJDBCドライバーを使うかはpom.xmlで指定する**: 依存関係として追加することで、JDBCドライバーのJARファイルがクラスパスに追加されます

---

#### H2とMySQLの違い

**動的プロキシの実装は変わらない**:

動的プロキシの生成方法や動作は、H2でもMySQLでも同じです。違いは、その内部で使われる設定とコンポーネントです。

**主な違い**:

| 項目 | H2（テスト環境） | MySQL（本番環境） |
|------|----------------|-----------------|
| **JDBCドライバー** | `org.h2.Driver` | `com.mysql.cj.jdbc.Driver` |
| **接続URL** | `jdbc:h2:mem:testdb` | `jdbc:mysql://localhost:3306/...` |
| **Hibernate Dialect** | `H2Dialect` | `MySQLDialect` |
| **データの保存場所** | メモリ内（揮発性） | ディスク（永続化） |
| **用途** | テスト実行時のみ | 本番・開発環境 |

**設定の違い**:

**H2（テスト環境）**:
```properties
# JDBCドライバー
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1

# HibernateのDialect（SQL方言）
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

**MySQL（本番環境）**:
```properties
# JDBCドライバー
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=${SPRING_DATASOURCE_URL_DEV}

# HibernateのDialect（SQL方言）
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# タイムゾーン設定（MySQL特有）
spring.jpa.properties.hibernate.jdbc.time_zone=Asia/Tokyo
```

**重要なポイント**:
- **動的プロキシの実装**: H2でもMySQLでも同じ
- **HibernateのDialect**: データベースの種類に応じて異なるSQLが生成される
- **JDBCドライバー**: データベースごとに異なるドライバーが必要

---

#### データベースの起動方法

**H2（テスト環境）: 自動起動（サーバー不要）**:

H2はインメモリデータベースのため、別途サーバーを起動する必要はありません。

**起動の仕組み**:
```
1. テストクラスが実行される
   ↓
2. Spring Bootが起動
   ↓
3. application-test.propertiesが読み込まれる
   spring.datasource.url=jdbc:h2:mem:testdb
   ↓
4. H2のJDBCドライバーが検出される（クラスパス上にあるため）
   ↓
5. JDBCドライバーが自動的にインメモリデータベースを作成
   → データベースサーバーを起動する必要がない！
   ↓
6. テストが実行される
   ↓
7. テスト終了後、メモリから自動的に削除される
```

**重要なポイント**:
- **サーバー不要**: データベースサーバーを起動する必要がない
- **自動起動**: Spring Bootが自動的にインメモリデータベースを作成
- **自動削除**: テスト終了後に自動的にメモリから削除される

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