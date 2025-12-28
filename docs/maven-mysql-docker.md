# Maven・MySQL・Docker 詳細資料

> このドキュメントは、Smart Household Account Bookプロジェクトで使用されているMaven、MySQL、Dockerに関する技術について、学習用にまとめたものです。

## 📋 目次

1. [Maven（ビルドツール）](#mavenビルドツール)
2. [MySQL（データベース）](#mysqlデータベース)
3. [Docker（コンテナ化技術）](#dockerコンテナ化技術)
4. [プロジェクトでの統合使用方法](#プロジェクトでの統合使用方法)

---

## Maven（ビルドツール）

### Mavenとは

**Maven**は、Javaプロジェクトのビルド、依存関係管理、プロジェクト管理を行うためのツールです。

**主な役割**:
- **依存関係管理**: 必要なライブラリを自動的にダウンロード・管理
- **ビルド自動化**: コンパイル、テスト、パッケージングを自動実行
- **プロジェクト構造の標準化**: プロジェクトの構造を統一
- **プラグインシステム**: 様々な機能をプラグインで追加可能

### プロジェクトでのMaven設定

このプロジェクトでは、`backend/pom.xml`でMavenの設定を行っています。

**`pom.xml`の主な構成要素**:

#### 1. プロジェクト基本情報

```1:17:backend/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.5.0</version>
		<relativePath />
	</parent>
	<groupId>com.example</groupId>
	<artifactId>backend</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>backend</name>
	<description>Backend of SmartHouseholdAccountBook</description>
```

**各要素の意味**:
- **`parent`**: Spring Bootの親POMを継承。標準設定が自動適用されます
- **`groupId`**: プロジェクトの組織識別子（例: `com.example`）
- **`artifactId`**: プロジェクト名（例: `backend`）
- **`version`**: バージョン（`SNAPSHOT`は開発中を意味）

#### 2. プロパティ設定

```34:39:backend/pom.xml
	<properties>
		<!-- Javaバージョン -->
		<java.version>21</java.version>
		<!-- OpenAPI仕様ファイルのパス -->
		<openapi.file>../openapi/openapi.yaml</openapi.file>
	</properties>
```

**意味**: Java 21を使用し、OpenAPIファイルのパスを定義（プラグインで使用）

#### 3. 依存関係（Dependencies）

Mavenの最も重要な機能の一つが、依存関係の管理です。

```119:128:backend/pom.xml
		<dependency>
			<groupId>com.mysql</groupId>
			<artifactId>mysql-connector-j</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>test</scope>
		</dependency>
```

**依存関係の構造**:
- **`groupId`**: ライブラリを提供している組織
- **`artifactId`**: ライブラリの名前
- **`version`**: バージョン（親POMで管理されている場合は省略可能）
- **`scope`**: 依存関係の適用範囲（後述）

### Mavenの依存関係スコープ（Scope）

Mavenの`scope`は、依存関係がどのフェーズ（コンパイル、テスト、実行）で必要かを指定します。

| スコープ | コンパイル時 | テスト時 | 実行時 | JARに含まれる | 説明 |
|---------|------------|---------|--------|------------|------|
| **`compile`**（デフォルト） | ✅ | ✅ | ✅ | ✅ | すべてのフェーズで必要 |
| **`runtime`** | ❌ | ✅ | ✅ | ✅ | 実行時のみ必要（JDBCドライバーなど） |
| **`test`** | ❌ | ✅ | ❌ | ❌ | テスト時のみ必要（JUnit、H2など） |
| **`provided`** | ✅ | ✅ | ❌ | ❌ | 実行環境で提供される（Servlet APIなど）、または実行時に不要（Lombokなど） | |

**使用例と理由**:

- **`runtime`**: MySQL JDBCドライバーは実行時にリフレクションで動的に読み込まれるため、コンパイル時にクラスパスに含める必要がない
- **`test`**: H2データベースやJUnitはテスト時のみ必要で、本番JARに含める必要がない
- **`provided`**: Lombokはコンパイル時にコードを生成するアノテーションプロセッサで、実行時には不要

**スコープの確認方法**:
```bash
cd backend
mvn dependency:tree  # スコープも表示される
```

#### 4. プロファイル（Profiles）

環境ごとに異なる設定を管理します。

```172:187:backend/pom.xml
	<profiles>
		<profile>
			<!-- backendフォルダ下でのmvnコマンド実行時に使用 -->
			<id>local</id>
			<properties>
				<openapi.file>../openapi/openapi.yaml</openapi.file>
			</properties>
		</profile>
		<profile>
			<!-- docker-compose.yamlでのmvnコマンド実行時に使用 -->
			<id>docker</id>
			<properties>
				<openapi.file>openapi/openapi.yaml</openapi.file>
			</properties>
		</profile>
	</profiles>
```

**使い方**: `mvn clean package -Plocal`（ローカル環境）または`-Pdocker`（Docker環境）

#### 5. ビルドプラグイン（Build Plugins）

**主要なプラグイン**:

1. **OpenAPI Generator Plugin**: OpenAPI仕様書からJavaインターフェースとモデルクラスを自動生成
2. **Build Helper Plugin**: 自動生成されたソースコードをビルドパスに追加
3. **Spring Boot Maven Plugin**: 実行可能なFat JARを作成

```296:302:backend/pom.xml
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<mainClass>com.example.backend.BackendApplication</mainClass>
				</configuration>
			</plugin>
```

### Mavenのライフサイクルとディレクトリ構造

**主要なフェーズ**:

| フェーズ | 説明 | 実行される処理 |
|---------|------|--------------|
| `compile` | コンパイル | ソースコードをコンパイル |
| `test` | テスト | ユニットテストを実行 |
| `package` | パッケージング | JARファイルなどにパッケージング |
| `install` | インストール | ローカルリポジトリにインストール(~/.m2/repositoryに生成したJarファイルをコピー) |

**ディレクトリ構造**:
```
backend/
├── pom.xml                    # Maven設定ファイル
├── src/
│   ├── main/
│   │   ├── java/              # メインのJavaソースコード
│   │   └── resources/         # リソースファイル（設定ファイルなど）
│   └── test/
│       ├── java/              # テスト用のJavaソースコード
│       └── resources/         # テスト用のリソースファイル
└── target/                    # ビルド成果物（コンパイル済みクラス、JARファイルなど）
```

### JARファイルの作成プロセス

**JARファイル**は、ZIP形式で圧縮されたアーカイブファイルで、コンパイル済みの`.class`ファイル、リソースファイル、依存関係のライブラリを含みます。

**`mvn package`の処理フロー**:

1. **ソースコード生成** (`generate-sources`): OpenAPI GeneratorがAPIインターフェースを生成
2. **ビルドパス追加**: Build Helperプラグインが生成されたソースをビルドパスに追加
3. **コンパイル** (`compile`): Javaソースコード（`.java`）をバイトコード（`.class`）に変換
4. **テスト** (`test`): ユニットテストを実行
5. **パッケージング** (`package`): Fat JARを作成（すべての依存関係を含む）

**Spring BootのFat JAR構造**:

```
target/backend-0.0.1-SNAPSHOT.jar
├── META-INF/
│   └── MANIFEST.MF              # メインクラス情報
├── BOOT-INF/
│   ├── classes/                  # アプリケーションのクラスファイル
│   └── lib/                      # すべての依存関係のJARファイル（100個以上）
└── org/springframework/boot/loader/  # Spring Bootローダー
```

**実行方法**: `java -jar target/backend-0.0.1-SNAPSHOT.jar`（単独で実行可能）

### ビルドパスとクラスパスの違い

| 項目 | ビルドパス（Build Path） | クラスパス（Classpath） |
|------|------------------------|------------------------|
| **目的** | コンパイルするソースコードの場所 | Javaがクラスファイル（.class）やJARファイルを検索する場所のリストです。 |
| **内容** | `.java`ファイル（ソースコード） | `.jar`ファイル、`.class`ファイル（バイトコード） |
| **使用タイミング** | コンパイル時のみ | コンパイル時と実行時 |
| **設定場所** | `pom.xml`の`<source>`要素、Build Helperプラグイン | `pom.xml`の`<dependency>`要素 |
| **例** | `src/main/java/`、`target/generated-sources/` | `~/.m2/repository/`、JAR内の`BOOT-INF/lib/` |

**実際の動作例**:

```java
// コンパイル時
import org.springframework.web.bind.annotation.RestController;  // ← クラスパスから読み込む
import com.example.backend.generated.api.ExpensesApi;         // ← ビルドパスから読み込む

@RestController
public class ExpenseController implements ExpensesApi {
    // ...
}
```

**処理の流れ**:
1. ビルドパスからソースコード（`.java`）を読み込み
2. クラスパスからライブラリ（`.jar`）を参照
3. コンパイルして`.class`ファイルを生成
4. 実行時はFat JAR内の`BOOT-INF/lib/`と`BOOT-INF/classes/`がクラスパスになる

---

## MySQL（データベース）

### 📋 目次

1. [MySQLとは](#mysqlとは)
2. [このプロジェクトでのMySQLの役割](#このプロジェクトでのmysqlの役割)
3. [データベース接続設定](#データベース接続設定)
4. [データベーススキーマ](#データベーススキーマ)
5. [MySQL用語の説明](#mysql用語の説明)
   - [文字セット（Character Set）と文字コード（Character Encoding）](#文字セットcharacter-setと文字コードcharacter-encoding)
   - [照合順序（Collation）](#照合順序collation)
   - [ストレージエンジン（Storage Engine）](#ストレージエンジンstorage-engine)
   - [VARCHAR(255)](#varchar255)
6. [MySQL設定ファイル](#mysql設定ファイル)

---

### MySQLとは

**MySQL**は、世界で最も広く使用されているオープンソースのリレーショナルデータベース管理システム（RDBMS）です。

**主な特徴**:
- **リレーショナルデータベース**: テーブル（表）形式でデータを管理
- **SQL**: 構造化照会言語（SQL）を使用してデータを操作
- **ACID特性**: トランザクションの信頼性を保証
- **高性能**: 大規模なデータ処理に対応

### このプロジェクトでのMySQLの役割

このプロジェクトでは、MySQLを使用して以下のデータを管理しています：

1. **ユーザー情報** (`users`テーブル)
2. **支出データ** (`expenses`テーブル)

### データベース接続設定

Spring BootアプリケーションがMySQLに接続するための設定は、`application.properties`で行います。

```3:16:backend/src/main/resources/application.properties
# MySQLの接続設定（ローカル開発用）
spring.datasource.url=${SPRING_DATASOURCE_URL_DEV}
spring.datasource.username=${MYSQL_ROOT_USER}
spring.datasource.password=${MYSQL_ROOT_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPAの設定
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.jdbc.time_zone=Asia/Tokyo
# Open-in-Viewを無効化（警告メッセージを抑制し、パフォーマンスも向上）
spring.jpa.open-in-view=false
```

**各設定項目の説明**:

| 設定項目 | 説明 |
|---------|------|
| `spring.datasource.url` | MySQLデータベースへの接続URL（例: `jdbc:mysql://localhost:3306/demo`） |
| `spring.datasource.username` | MySQLのユーザー名（通常は`root`） |
| `spring.datasource.password` | MySQLのパスワード |
| `spring.datasource.driver-class-name` | JDBCドライバーのクラス名（MySQL用） |
| `spring.jpa.hibernate.ddl-auto` | テーブルの自動生成設定（`update`: 既存テーブルを更新） |
| `spring.jpa.database-platform` | 使用するデータベースの方言（MySQL用） |
| `spring.jpa.properties.hibernate.jdbc.time_zone` | タイムゾーン設定（日本時間） |

#### `spring.jpa.hibernate.ddl-auto=update` とは

**`spring.jpa.hibernate.ddl-auto=update`**: Hibernateがアプリ起動時に、エンティティクラス（`@Entity`）の定義に基づいてデータベーススキーマを自動更新する設定です。

**動作の仕組み**:
- アプリケーション起動時に、Hibernateが`@Entity`クラスをスキャン
- 既存のデータベーステーブルと比較
- 差分があれば自動的にスキーマを更新（カラムの追加など）

**注意点**:
- 本番環境では整合性をチェックする`validate`や何もしない`none`を使うのが一般的です
- 開発環境でのみ`update`を使用することを推奨します

**セキュリティ上の注意**:
- パスワードなどの機密情報は、環境変数（`${MYSQL_ROOT_PASSWORD}`）で管理
- `.env`ファイルに実際の値を設定し、`.gitignore`で除外

### データベーススキーマ

このプロジェクトでは、`docker/mysql/init.sql`でデータベースの初期化を行います。

```1:34:docker/mysql/init.sql
-- データベースの作成（もしなければ）
CREATE DATABASE IF NOT EXISTS demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- データベースの選択
USE demo;

-- セッションの文字コードをUTF-8に設定
SET NAMES utf8mb4;

-- ユーザーテーブルの作成
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cognito_sub VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 家計簿テーブルの作成
CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    category VARCHAR(50) NOT NULL,
    amount INT NOT NULL,
    description VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_expenses_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**テーブル構造の説明**:

#### `users`テーブル

| カラム名 | 型 | 説明 |
|---------|---|------|
| `id` | `BIGINT` | 主キー（自動増分） |
| `cognito_sub` | `VARCHAR(255)` | AWS CognitoのユーザーID |
| `email` | `VARCHAR(255)` | メールアドレス |

#### `expenses`テーブル

| カラム名 | 型 | 説明 |
|---------|---|------|
| `id` | `BIGINT` | 主キー（自動増分） |
| `date` | `DATE` | 支出日付 |
| `category` | `VARCHAR(50)` | カテゴリー（食費、交通費など） |
| `amount` | `INT` | 支出金額 |
| `description` | `VARCHAR(255)` | 説明 |
| `user_id` | `BIGINT` | ユーザーID（外部キー） |

**外部キー制約**:
- `user_id`は`users`テーブルの`id`を参照
- `ON DELETE CASCADE`: ユーザーが削除されると、そのユーザーの支出も自動的に削除される
- `CONSTRAINT fk_expenses_user`: 外部キー制約の名前を指定

**文字コード設定**:
- `utf8mb4`: UTF-8の完全なサポート（絵文字なども保存可能）
- `utf8mb4_unicode_ci`: Unicode照合順序（大文字小文字を区別しない）

### MySQL用語の説明

データベーススキーマで使用されている重要な用語について説明します。

#### 文字セット（Character Set）と文字コード（Character Encoding）

**文字セット**と**文字コード**は、よく混同されがちですが、概念としては異なります。ただし、MySQLでは多くの場合、同じ名前（例: `utf8mb4`）で両方を指します。

| 項目 | 文字セット | 文字コード |
|------|-----------|-----------|
| **定義** | どの文字を扱うか（文字の集合） | 文字をどうバイト列に変換するか（変換方式） |
| **例** | Unicode、ASCII、JIS X 0208 | UTF-8、UTF-16、Shift_JIS |
| **関係** | 「文字の辞書」 | 「辞書の使い方（保存方法）」 |

**MySQLでの`utf8mb4`**:
- **文字セット**: Unicode（すべての文字を含む）
- **文字コード**: UTF-8（可変長、最大4バイト）
- **名前の由来**: `utf8`（UTF-8文字コード）+ `mb4`（Multi-Byte 4 = 最大4バイト）。MySQLの古い`utf8`は最大3バイトまでしか対応していなかったため、4バイト対応を明示するために`utf8mb4`という名前になった

**クライアント接続の文字コード**:

MySQLでは、以下の3つの場所で文字コードの設定が必要です：

1. **サーバー側**: データベースやテーブルに保存する際の文字コード
2. **クライアント側**: アプリケーションが送信するデータの文字コード
3. **接続**: クライアントとサーバーの間でデータを転送する際の文字コード

**`SET NAMES utf8mb4;`の役割**:

以下の3つの設定を一度に実行します：

```sql
SET character_set_client = 'utf8mb4';      -- クライアントが送信するデータの文字コード
SET character_set_connection = 'utf8mb4';  -- 接続で使用する文字コード
SET character_set_results = 'utf8mb4';     -- サーバーが返すデータの文字コード
```

**重要なポイント**: `SET NAMES utf8mb4;`だけでは不十分。**テーブル作成時に文字コードを明示的に指定する必要があります**。

- **`SET NAMES utf8mb4;`**: データの送受信時の文字コードを設定（接続レベル）
- **`DEFAULT CHARSET=utf8mb4`**: テーブルにデータを保存する際の文字コードを設定（テーブルレベル）

**推奨される設定方法**:

```sql
-- 1. データベース作成時に文字コードを指定
CREATE DATABASE demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 接続時の文字コードを設定（データの送受信用）
SET NAMES utf8mb4;

-- 3. テーブル作成時に明示的に文字コードを指定（重要！）
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 照合順序（Collation）

**照合順序**は、文字列を比較・ソートする際のルールです。同じ文字セットでも、異なる照合順序を使用できます。

**主な照合順序**:

| 照合順序 | 説明 | 例 |
|---------|------|-----|
| `utf8mb4_unicode_ci` | Unicode標準に基づく比較（大文字小文字を区別しない） | `'Apple'` = `'apple'` |
| `utf8mb4_bin` | バイナリ比較（大文字小文字を区別する） | `'Apple'` ≠ `'apple'` |
| `utf8mb4_general_ci` | 簡易的な比較（`unicode_ci`より高速だが、一部の言語で不正確） | `'Apple'` = `'apple'` |

**`ci`の意味**: Case Insensitive（大文字小文字を区別しない）

**使用例**:
```sql
-- 大文字小文字を区別しない検索
SELECT * FROM users WHERE email = 'TEST@EXAMPLE.COM';
-- utf8mb4_unicode_ciの場合、'test@example.com'もマッチする
```

#### ストレージエンジン（Storage Engine）

**ストレージエンジン**は、データをどのように保存・取得するかを決定するMySQLのコンポーネントです。テーブルごとに異なるストレージエンジンを選択できます。

**主なストレージエンジン**:

| ストレージエンジン | 説明 | 特徴 |
|------------------|------|------|
| **`InnoDB`** | トランザクション対応のデフォルトエンジン | ✅ トランザクション（ACID特性）<br>✅ 外部キー制約<br>✅ 行レベルロック<br>✅ クラッシュリカバリ |
| **`MyISAM`** | 古いデフォルトエンジン（非推奨） | ❌ トランザクションなし<br>❌ 外部キー制約なし<br>✅ 高速な読み取り |

**なぜ`InnoDB`を使うか**:
- **トランザクション**: 複数のSQL文を1つの単位として実行（すべて成功するか、すべて失敗するか）
- **外部キー制約**: データの整合性を保証（例: `expenses.user_id`は`users.id`に存在する必要がある）
- **データの安全性**: クラッシュしてもデータが失われにくい

**設定例**:
```sql
CREATE TABLE users (
    ...
) ENGINE=InnoDB;  -- InnoDBストレージエンジンを使用
```

#### VARCHAR(255)

**`VARCHAR(n)`**は、可変長の文字列型です。`n`は最大文字数を指定します。

**特徴**:
- **可変長**: 実際に保存された文字数分だけの容量を使用
  - `VARCHAR(255)`で`'Hello'`を保存 → 5文字分の容量のみ使用
  - `VARCHAR(255)`で`'あいうえお'`を保存 → 15バイト（UTF-8で日本語は1文字3バイト）
- **最大長**: `n`文字まで保存可能（`utf8mb4`の場合、最大`n × 4`バイト）

**なぜ255がよく使われるか**:
- MySQL 5.0.3以前では、`VARCHAR`の最大長が255バイトだった（歴史的な理由）
- 現在は最大65,535バイトまで可能だが、255は一般的な文字列の長さに適している
- メールアドレス、説明文など、多くの用途で十分な長さ

**他の文字列型との比較**:

| 型 | 説明 | 使用例 |
|---|------|--------|
| `VARCHAR(n)` | 可変長文字列（最大n文字） | メールアドレス、説明文 |
| `CHAR(n)` | 固定長文字列（常にn文字） | 郵便番号、固定コード |
| `TEXT` | 長い文字列（最大65,535バイト） | 長い説明文、記事本文 |

**使用例**:
```sql
-- メールアドレス用（通常255文字で十分）
email VARCHAR(255) NOT NULL

-- カテゴリー用（短い文字列なので50文字で十分）
category VARCHAR(50) NOT NULL

-- 説明文用（長い文章の場合はTEXT型も検討）
description VARCHAR(255) NOT NULL
```

### MySQL設定ファイル

MySQLの動作をカスタマイズするために、`docker/mysql/my.cnf`を使用しています。

```1:15:docker/mysql/my.cnf
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
character-set-client-handshake=FALSE
init_connect='SET NAMES utf8mb4'
skip-character-set-client-handshake

[client]
default-character-set=utf8mb4

[mysql]
default-character-set=utf8mb4

[mysqldump]
default-character-set=utf8mb4
```

**設定の説明**:

##### `[mysqld]`セクション（MySQLサーバー設定）

| 設定項目 | 説明 |
|---------|------|
| `character-set-server=utf8mb4` | サーバーのデフォルト文字セット（データベース・テーブル作成時のデフォルト） |
| `collation-server=utf8mb4_unicode_ci` | サーバーのデフォルト照合順序 |
| `character-set-client-handshake=FALSE` | クライアントが送信する文字コード情報を無視 |
| `init_connect='SET NAMES utf8mb4'` | 接続時に自動的に`SET NAMES utf8mb4;`を実行 |
| `skip-character-set-client-handshake` | クライアントハンドシェイクをスキップ（上記と同じ効果） |

**動作の仕組み**: クライアントの文字コード設定を無視し、サーバー側で強制的に`utf8mb4`を設定することで、すべての接続で一貫性を保証します。

**注意**: `init_connect`は、`SUPER`権限を持つユーザー（rootなど）には適用されません。

##### `[client]`、`[mysql]`、`[mysqldump]`セクション

| セクション | 対象 | 説明 |
|---------|------|------|
| `[client]` | JDBCドライバーなど | アプリケーション接続時のデフォルト文字コード |
| `[mysql]` | `mysql`コマンド | ターミナルから接続する際のデフォルト文字コード |
| `[mysqldump]` | `mysqldump`コマンド | データベースバックアップ時のデフォルト文字コード |

**設定の全体像**:

これらの設定により、MySQLの**すべての側面**（サーバー、クライアント、接続、バックアップ）で`utf8mb4`が使用されます。

**なぜUTF-8が重要か**:
- 日本語や絵文字などのマルチバイト文字を正しく保存・表示
- データベース、テーブル、接続すべてでUTF-8を統一することで、文字化けを防止
- どのような接続方法でも一貫性を保証

---

## Docker（コンテナ化技術）

### Dockerとは

**Docker**は、アプリケーションとその依存関係を「コンテナ」という軽量なパッケージにまとめる技術です。

**主な利点**:
- **環境の統一**: 開発環境と本番環境を同じにできる
- **簡単なデプロイ**: コンテナをどこでも実行可能
- **依存関係の分離**: 各アプリケーションが独立した環境で動作
- **スケーラビリティ**: 複数のコンテナを簡単に起動・停止

### Dockerの基本概念

#### 1. イメージ（Image）

**イメージ**は、アプリケーションとその実行環境をパッケージ化したものです。テンプレートのようなもので、これからコンテナを作成します。

**例**: `mysql:8.0`、`maven:3.9-eclipse-temurin-21`

#### 2. コンテナ（Container）

**コンテナ**は、イメージから作成された実行中のインスタンスです。実際にアプリケーションが動作する環境です。

#### 3. Dockerfile

**Dockerfile**は、イメージを作成するための手順書です。

### このプロジェクトでのDockerfile

このプロジェクトでは、`backend/Dockerfile`でSpring Bootアプリケーションのイメージを作成します。

```1:27:backend/Dockerfile
# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

#依存関係を事前にダウンロードするコマンド
#Dockerの機能でRUNコマンドまでのレイヤーに変更がない場合はRUNコマンドはスキップされキャッシュを利用する
#pomに変更がない場合はキャッシュが利用され、イメージのビルドが速くなる
COPY backend/pom.xml .
#依存関係を事前にダウンロードする
RUN mvn dependency:go-offline

COPY backend/src ./src
COPY openapi/openapi.yaml ./openapi/openapi.yaml

#Mavenの並列ビルドを有効化
RUN mvn clean package -T 1C -DskipTests -Pdocker

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar
#このコンテナ内のアプリケーションが8080ポートをリッスンすることを明示
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

**Dockerfileの構造**:

#### マルチステージビルド

このDockerfileは、**マルチステージビルド**を使用しています。これは、ビルド用のイメージと実行用のイメージを分離することで、最終的なイメージサイズを小さくする手法です。

**ステージ1: ビルドステージ（`builder`）**

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS builder
```

- **ベースイメージ**: `maven:3.9-eclipse-temurin-21`（MavenとJava 21が含まれる）
- **目的**: アプリケーションをコンパイルしてJARファイルを作成

**各コマンドの説明**:

1. **`WORKDIR /app`**: 作業ディレクトリを`/app`に設定
2. **`COPY backend/pom.xml .`**: `pom.xml`をコンテナにコピー
3. **`RUN mvn dependency:go-offline`**: 依存関係を事前にダウンロード（キャッシュ最適化）
4. **`COPY backend/src ./src`**: ソースコードをコピー
5. **`RUN mvn clean package`**: アプリケーションをビルドしてJARファイルを作成
   - `-T 1C`: 並列ビルドを有効化（CPUコア数に応じて並列実行）
   - `-DskipTests`: テストをスキップ（ビルド時間短縮）
   - `-Pdocker`: Docker用プロファイルを使用

**ステージ2: ランタイムステージ**

```dockerfile
FROM eclipse-temurin:21-jre
```

- **ベースイメージ**: `eclipse-temurin:21-jre`（Java 21のランタイムのみ、コンパイラは含まれない）
- **目的**: ビルド済みのJARファイルを実行

**各コマンドの説明**:

1. **`COPY --from=builder /app/target/*.jar app.jar`**: ビルドステージで作成したJARファイルをコピー
2. **`EXPOSE 8080`**: コンテナが8080ポートを使用することを宣言（実際にはポートを開くわけではない）
3. **`CMD ["java", "-jar", "app.jar"]`**: コンテナ起動時に実行するコマンド

**マルチステージビルドの利点**:
- **イメージサイズの削減**: ビルドツール（Maven）を含めないため、最終イメージが小さくなる
- **セキュリティ**: ビルドツールが本番環境に含まれないため、攻撃面が減る
- **パフォーマンス**: 軽量なイメージは起動が速い

### Docker Compose

**Docker Compose**は、複数のコンテナを定義・管理するためのツールです。`docker-compose.yaml`ファイルで、アプリケーション全体の構成を定義します。

### 本番環境用のDocker Compose

```1:45:docker-compose.yaml
services:
  backend:
    container_name: backend
    build:
      context: ./
      dockerfile: backend/Dockerfile
    ports:
      - "8080:8080"
      - "5005:5005" # デバッグ用ポート
    environment:
      SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL_PROD}
      SPRING_DATASOURCE_USERNAME: ${MYSQL_ROOT_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      SPRING_JPA_SHOW_SQL: "true"
      COGNITO_JWK_SET_URL: ${COGNITO_JWK_SET_URL}
    command: >
      sh -c "java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar app.jar"
    # ↑ デバッグオプション付きでSpring Bootを起動  
    depends_on:
      mysql:
        condition: service_healthy

  mysql:
    image: mysql:8.0
    container_name: mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
    volumes:
      - mysql_data:/var/lib/mysql
      - ./docker/mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
      - ./docker/mysql/my.cnf:/etc/mysql/conf.d/my.cnf
    healthcheck:
      test: [ "CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "${MYSQL_ROOT_USER}", "-p${MYSQL_ROOT_PASSWORD}" ]
      interval: 5s
      timeout: 5s
      retries: 20

volumes:
  mysql_data:
    name: smart_household_mysql_data
```

**構成要素の説明**:

#### 1. Backendサービス

```yaml
backend:
  container_name: backend
  build:
    context: ./
    dockerfile: backend/Dockerfile
```

- **`container_name`**: コンテナの名前
- **`build`**: イメージのビルド設定
  - **`context`**: ビルドコンテキスト（Dockerfileで参照できるファイルの範囲）
  - **`dockerfile`**: Dockerfileのパス

```yaml
  ports:
    - "8080:8080"
    - "5005:5005"
```

- **`ports`**: ポートマッピング（ホスト:コンテナ）
  - `8080:8080`: アプリケーションのHTTPポート
  - `5005:5005`: リモートデバッグ用ポート

```yaml
  environment:
    SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL_PROD}
    SPRING_DATASOURCE_USERNAME: ${MYSQL_ROOT_USER}
    SPRING_DATASOURCE_PASSWORD: ${MYSQL_ROOT_PASSWORD}
```

- **`environment`**: 環境変数の設定
  - `${変数名}`: `.env`ファイルから値を読み込む

```yaml
  depends_on:
    mysql:
      condition: service_healthy
```

- **`depends_on`**: 依存関係の定義
  - `mysql`サービスが起動してから`backend`サービスを起動
  - `condition: service_healthy`: MySQLのヘルスチェックが成功するまで待機

#### 2. MySQLサービス

```yaml
mysql:
  image: mysql:8.0
  container_name: mysql
```

- **`image`**: 使用するDockerイメージ（ビルド不要）
- **`container_name`**: コンテナの名前

```yaml
  volumes:
    - mysql_data:/var/lib/mysql
    - ./docker/mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
    - ./docker/mysql/my.cnf:/etc/mysql/conf.d/my.cnf
```

- **`volumes`**: データの永続化とファイルのマウント
  - **`mysql_data:/var/lib/mysql`**: データベースファイルを永続化（名前付きボリューム）
  - **`./docker/mysql/init.sql:/docker-entrypoint-initdb.d/init.sql`**: 初期化SQLをマウント（初回起動時に自動実行）
  - **`./docker/mysql/my.cnf:/etc/mysql/conf.d/my.cnf`**: MySQL設定ファイルをマウント

```yaml
  healthcheck:
    test: [ "CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "${MYSQL_ROOT_USER}", "-p${MYSQL_ROOT_PASSWORD}" ]
    interval: 5s
    timeout: 5s
    retries: 20
```

- **`healthcheck`**: コンテナの健康状態をチェック
  - **`test`**: ヘルスチェックコマンド（MySQLが応答するか確認）
  - **`interval`**: チェック間隔（5秒）
  - **`timeout`**: タイムアウト（5秒）
  - **`retries`**: リトライ回数（20回 = 最大100秒待機）

#### 3. ボリューム定義

```yaml
volumes:
  mysql_data:
    name: smart_household_mysql_data
```

- **`volumes`**: 名前付きボリュームの定義
  - データベースのデータを永続化（コンテナを削除してもデータが残る）

### 開発環境用のDocker Compose

```1:32:docker-compose.dev.yaml
# ========================================
# 開発環境用 Docker Compose 設定
# MySQLのみ起動（Spring Bootはローカルで実行）
# ========================================

services:
  mysql:
    image: mysql:8.0
    container_name: mysql-dev
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
    volumes:
      # データ永続化
      - mysql_dev_data:/var/lib/mysql
      # 初期化スクリプト
      - ./docker/mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
      # MySQL設定
      - ./docker/mysql/my.cnf:/etc/mysql/conf.d/my.cnf
    healthcheck:
      test: [ "CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}" ]
      interval: 5s
      timeout: 5s
      retries: 20

volumes:
  mysql_dev_data:
    name: smart_household_mysql_dev_data
```

**開発環境用の特徴**:
- **MySQLのみ**: Spring Bootはローカルで実行（IDEから起動）
- **別のボリューム**: `mysql_dev_data`を使用（本番環境とデータを分離）
- **別のコンテナ名**: `mysql-dev`（本番環境と区別）

### Docker Composeのコマンド

**基本的なコマンド**:

```bash
# コンテナを起動（バックグラウンド）
docker-compose up -d

# コンテナを停止
docker-compose down

# コンテナのログを確認
docker-compose logs -f

# コンテナの状態を確認
docker-compose ps

# 開発環境用の起動
docker-compose -f docker-compose.dev.yaml up -d
```

**ボリュームの管理**:

```bash
# ボリュームの一覧を表示
docker volume ls

# ボリュームを削除（データが消える）
docker volume rm smart_household_mysql_data
```

---

## プロジェクトでの統合使用方法

### 開発環境での使用方法

#### 1. MySQLをDockerで起動

```bash
# 開発環境用のMySQLを起動
docker-compose -f docker-compose.dev.yaml up -d

# 起動確認
docker-compose -f docker-compose.dev.yaml ps
```

#### 2. Spring Bootをローカルで起動

IDE（Cursor/VS Code）から起動するか、Mavenコマンドで起動：

```bash
cd backend
mvn spring-boot:run
```

#### 3. アプリケーションの動作確認

- バックエンド: http://localhost:8080
- MySQL: localhost:3306

### 本番環境での使用方法

#### 1. 環境変数の設定

`.env`ファイルを作成：

```env
MYSQL_ROOT_PASSWORD=your_password
MYSQL_DATABASE=demo
MYSQL_ROOT_USER=root
SPRING_DATASOURCE_URL_PROD=jdbc:mysql://mysql:3306/demo
SPRING_DATASOURCE_URL_DEV=jdbc:mysql://localhost:3306/demo
COGNITO_JWK_SET_URL=https://your-cognito-url/.well-known/jwks.json
```

#### 2. Docker Composeで起動

```bash
# イメージをビルドして起動
docker-compose up -d --build

# ログを確認
docker-compose logs -f backend
```

#### 3. 停止とクリーンアップ

```bash
# コンテナを停止
docker-compose down

# コンテナとボリュームを削除（データも消える）
docker-compose down -v
```

### トラブルシューティング

#### MySQLに接続できない

1. **コンテナが起動しているか確認**:
   ```bash
   docker-compose ps
   ```

2. **MySQLのログを確認**:
   ```bash
   docker-compose logs mysql
   ```

3. **ヘルスチェックの状態を確認**:
   ```bash
   docker inspect mysql | grep Health
   ```

#### ポートが既に使用されている

```bash
# ポート8080を使用しているプロセスを確認
lsof -i :8080

# ポート3306を使用しているプロセスを確認
lsof -i :3306
```

#### データベースのデータをリセット

```bash
# コンテナとボリュームを削除
docker-compose down -v

# 再起動（初期化SQLが実行される）
docker-compose up -d
```

---

## まとめ

このプロジェクトでは、以下の技術を統合して使用しています：

1. **Maven**: Javaプロジェクトのビルドと依存関係管理
2. **MySQL**: リレーショナルデータベースでデータを永続化
3. **Docker**: アプリケーションとデータベースをコンテナ化して環境を統一

これらの技術を組み合わせることで、開発環境と本番環境の一貫性を保ち、簡単にデプロイできるアプリケーションを構築しています。

**学習のポイント**:
- Mavenの依存関係管理とビルドプロセスを理解する
- MySQLのテーブル設計とSQLの基本を理解する
- Dockerのコンテナ化とDocker Composeによる複数コンテナ管理を理解する
- 環境変数による設定管理とセキュリティの重要性を理解する

