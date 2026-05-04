# 03. MySQL：このプロジェクトのデータ保存基盤

> この章で学ぶこと: **このプロジェクトで MySQL が担当する範囲**、**Compose / Spring Boot / Flyway との接続点**、**初期スキーマの読み方**、**文字コード・タイムゾーン・永続化設定**、**開発中によく使う確認コマンド**。

## 目次

1. [この資料の位置づけ](#この資料の位置づけ)
2. [このプロジェクトでの MySQL の役割](#このプロジェクトでの-mysql-の役割)
3. [MySQL 関連ファイル](#mysql-関連ファイル)
4. [接続設定の読み方](#接続設定の読み方)
5. [MySQL コンテナ設定](#mysql-コンテナ設定)
6. [初期スキーマの読み方](#初期スキーマの読み方)
7. [文字コードとタイムゾーン](#文字コードとタイムゾーン)
8. [Flyway と Hibernate validate の境界](#flyway-と-hibernate-validate-の境界)
9. [よく使う確認コマンド](#よく使う確認コマンド)
10. [トラブルシュート](#トラブルシュート)
11. [セキュリティとパフォーマンスの注意点](#セキュリティとパフォーマンスの注意点)
12. [まず覚えるポイント](#まず覚えるポイント)

---

## この資料の位置づけ

この資料では、MySQL そのものの超基本ではなく、**このプロジェクトで MySQL をどう使っているか**に絞って説明します。

次の内容は別資料で詳しく説明済みなので、ここでは深掘りしません。

| 内容 | 参照先 |
|------|--------|
| JPA / Hibernate / Repository / トランザクション | [03. データ層](../backend/03-data.md) |
| Flyway の基本、命名規則、適用順 | [03. データ層](../backend/03-data.md#flywaydb-マイグレーション) |
| JDBC ドライバ、DataSource、HikariCP | [03. データ層](../backend/03-data.md#jdbc-ドライバと-datasource) |
| Docker Compose、ネットワーク、ボリューム、healthcheck | [02. Docker](./02-docker.md) |
| Maven の MySQL ドライバ依存関係 | [01. Maven](./01-maven.md) |

---

## このプロジェクトでの MySQL の役割

このプロジェクトは、支出・ユーザー・月次レポートを MySQL 8 に保存します。

```mermaid
flowchart LR
    Frontend["Next.js"]
    Backend["Spring Boot"]
    Jpa["Spring Data JPA / Hibernate"]
    MySQL[("MySQL 8")]

    Frontend --> Backend
    Backend --> Jpa
    Jpa --> MySQL
```

MySQL 側で扱う主なテーブルは次の 3 つです。

| テーブル | 役割 |
|----------|------|
| `users` | Cognito のユーザーとアプリ内ユーザーを対応させる |
| `expenses` | 支出データを保存する |
| `monthly_reports` | AI が生成した月次レポートを保存する |

この資料で重要なのは、Java の Entity そのものではなく、**最終的に MySQL 上にどのようなテーブル・制約・インデックスが作られるか**です。

---

## MySQL 関連ファイル

MySQL に直接関係するファイルは次の通りです。

```text
.
├── docker/compose/docker-compose.dev.yaml
├── docker/compose/docker-compose.single-host.yaml
├── docker/compose/docker-compose.single-host.local.yaml
├── docker/compose/docker-compose.single-host.prod.yaml
├── docker/mysql/my.cnf
├── backend/src/main/resources/application.properties
└── backend/src/main/resources/db/migration/V1__initial_schema.sql
```

| ファイル | 見るポイント |
|----------|--------------|
| `docker/compose/docker-compose.dev.yaml` | MySQL だけをローカル開発用に起動する設定 |
| `docker/compose/docker-compose.single-host.yaml` | MySQL とバックエンドを同じ Docker ネットワークで起動する設定 |
| `docker/compose/docker-compose.single-host.local.yaml` | ローカル用のポート公開と SQL ログ設定 |
| `docker/compose/docker-compose.single-host.prod.yaml` | 本番寄せの SQL ログ抑制設定 |
| `docker/mysql/my.cnf` | MySQL サーバーの文字コード・タイムゾーン設定 |
| `application.properties` | Spring Boot が読む DB 接続設定 |
| `V1__initial_schema.sql` | Flyway が作成する初期テーブル定義 |

初心者向けに言うと、`docker/compose/docker-compose*.yaml` は「MySQL をどう起動するか」、`application.properties` は「Spring Boot が MySQL へどう接続するか」、`V1__initial_schema.sql` は「MySQL の中にどんな表を作るか」です。

---

## 接続設定の読み方

このプロジェクトでは、実行方法によって MySQL の接続先ホスト名が変わります。

| 実行方法 | Spring Boot の場所 | MySQL の見え方 | JDBC URL のホスト |
|----------|-------------------|----------------|-------------------|
| `docker/compose/docker-compose.dev.yaml` | ローカルPC / IDE | `localhost:3306` | `localhost` |
| `docker/compose/docker-compose.single-host.yaml` | Docker コンテナ内 | Compose サービス名 | `mysql` |

### ローカル開発時

`application.properties` では、ローカル開発用の URL を環境変数から読みます。

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL_DEV}
spring.datasource.username=${MYSQL_ROOT_USER}
spring.datasource.password=${MYSQL_ROOT_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

`.env` 側では、たとえば次のように設定します。

```env
MYSQL_ROOT_USER=root
MYSQL_ROOT_PASSWORD=your-password
MYSQL_DATABASE=demo
SPRING_DATASOURCE_URL_DEV=jdbc:mysql://localhost:3306/demo?serverTimezone=UTC
```

`localhost` になる理由は、Spring Boot を Docker ではなくローカルPC側で動かすためです。

### 単一ホスト構成時

`docker/compose/docker-compose.single-host.yaml` では、バックエンドコンテナに次の環境変数を渡します。

```yaml
SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=UTC
```

ここでの `mysql` は、MySQL コンテナのサービス名です。
Docker Compose の同じネットワーク内では、サービス名がホスト名として使えます。

`localhost` は「自分自身」という意味なので、Docker 内のバックエンドから MySQL コンテナへつなぐ場合は `localhost` ではなく `mysql` を使います。

---

## MySQL コンテナ設定

`docker/compose/docker-compose.dev.yaml` では MySQL だけを起動します。

```yaml
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
      - mysql_dev_data:/var/lib/mysql
      - ./docker/mysql/my.cnf:/etc/mysql/conf.d/my.cnf
```

見るべき点は 4 つです。

| 設定 | 意味 |
|------|------|
| `image: mysql:8.0` | MySQL 8 系の公式イメージを使う |
| `MYSQL_DATABASE` | 初回起動時に作成するデータベース名 |
| `/var/lib/mysql` | MySQL の実データ保存場所 |
| `./docker/mysql/my.cnf` | プロジェクト側の MySQL 設定をコンテナへ渡す |

MySQL のデータは `/var/lib/mysql` に保存されます。

```yaml
volumes:
  - mysql_dev_data:/var/lib/mysql
```

ここを名前付きボリュームにしているため、コンテナを消しても DB データは残ります。

単一ホスト構成では、MySQL が接続可能になってからバックエンドを起動するために `healthcheck` を使います。

```yaml
healthcheck:
  test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
  interval: 10s
  timeout: 5s
  retries: 10
```

Docker のネットワークやボリュームの基本は [02. Docker](./02-docker.md#ネットワークとボリューム) を参照してください。

---

## 初期スキーマの読み方

初期スキーマは `backend/src/main/resources/db/migration/V1__initial_schema.sql` にあります。

```sql
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cognito_sub VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_cognito_sub (cognito_sub)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

この `CREATE TABLE` から、次のことが読み取れます。

| 部分 | 意味 |
|------|------|
| `BIGINT NOT NULL AUTO_INCREMENT` | MySQL が自動採番する ID |
| `PRIMARY KEY (id)` | `id` を主キーにする |
| `UNIQUE KEY uk_users_cognito_sub` | Cognito ユーザーIDの重複を防ぐ |
| `ENGINE=InnoDB` | トランザクションと外部キーに対応したストレージエンジンを使う |
| `DEFAULT CHARSET=utf8mb4` | 日本語や絵文字を扱いやすい文字コードを使う |
| `COLLATE=utf8mb4_unicode_ci` | 文字列を比較・並び替えするときのルールを指定する |

### テーブル同士の関係

`expenses` と `monthly_reports` は `users` を参照します。

```sql
CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users (id)
```

```sql
CONSTRAINT fk_monthly_reports_user FOREIGN KEY (user_id) REFERENCES users (id)
```

外部キーはアプリ側だけではなく、DB 側でもデータの整合性を守るための仕組みです。

```mermaid
erDiagram
    users ||--o{ expenses : has
    users ||--o{ monthly_reports : has
```

### このプロジェクトのインデックス

`expenses` には、ユーザー別・日付別検索用のインデックスがあります。

```sql
KEY idx_expenses_user_id_and_date (user_id, date)
```

家計簿では「あるユーザーの、ある月の支出」を取得する処理が多くなります。
そのため、`user_id` と `date` の組み合わせにインデックスを付けています。

`monthly_reports` には、同じユーザー・同じ月のレポートが重複しないように UNIQUE 制約があります。

```sql
UNIQUE KEY idx_monthly_reports_user_id_month (user_id, month)
```

これは検索高速化だけでなく、「同じ月のレポートを 2 件作らない」という業務ルールも表しています。

`KEY` は検索を速くするための普通のインデックスで、同じ値が複数あっても問題ありません。
`UNIQUE KEY` は検索を速くするだけでなく、同じ値の重複登録も防ぎます。

---

## 文字コードとタイムゾーン

MySQL で特に意識したい設定は、文字コードとタイムゾーンです。

### `utf8mb4`

このプロジェクトでは `utf8mb4` を使います。

```ini
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
init_connect='SET NAMES utf8mb4'
skip-character-set-client-handshake

[client]
default-character-set=utf8mb4

[mysql]
default-character-set=utf8mb4

[mysqldump]
default-character-set=utf8mb4
```

`utf8mb4` は、日本語、英数字、記号に加えて、絵文字のような 4 バイト文字も保存しやすい MySQL の文字コードです。

`docker/mysql/my.cnf` では、接続経路ごとに文字コードの方針をそろえています。

| 設定 | 意味 |
|------|------|
| `character-set-server=utf8mb4` | MySQL サーバー側の標準文字コードを `utf8mb4` にする |
| `collation-server=utf8mb4_unicode_ci` | MySQL サーバー側の標準の比較・並び替えルールを指定する |
| `skip-character-set-client-handshake` | クライアントから別の文字コード指定が来ても、サーバー側の `utf8mb4` 方針にそろえる |
| `init_connect='SET NAMES utf8mb4'` | 通常ユーザーで接続した直後に、接続の文字コードを `utf8mb4` にする |
| `[client]` / `[mysql]` / `[mysqldump]` の `default-character-set=utf8mb4` | CLI やダンプ取得でも文字化けしにくくする |

`V1__initial_schema.sql` でも各テーブルに同じ方針を指定しています。

```sql
DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
```

`DEFAULT CHARSET=utf8mb4` は、このテーブルで保存する文字の形式を `utf8mb4` にする指定です。
`COLLATE=utf8mb4_unicode_ci` は、文字列を比較したり並び替えたりするときのルールです。
`ci` は case-insensitive の略で、大文字・小文字を区別しにくい比較ルールを意味します。
たとえば検索や UNIQUE 判定では、保存する文字コードだけでなく「文字同士をどう比べるか」も重要になるため、`CHARSET` と `COLLATE` をセットで明示しています。

### UTC

MySQL サーバーのタイムゾーンは UTC にそろえています。

```ini
[mysqld]
default-time-zone='+00:00'
```

Spring Boot 側も UTC にそろえています。

```properties
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
spring.jackson.time-zone=UTC
```

日時は、保存時は UTC にそろえ、画面表示で必要に応じて日本時間へ変換する方が安全です。
サーバーの場所や Docker 実行環境が変わっても、日時の解釈がぶれにくくなります。

---

## Flyway と Hibernate validate の境界

このプロジェクトでは、DB スキーマの作成・変更は Flyway が担当します。

```text
backend/src/main/resources/db/migration/V1__initial_schema.sql
```

Hibernate はテーブルを自動生成せず、Entity と DB の形が合っているかだけ確認します。

```properties
spring.jpa.hibernate.ddl-auto=validate
```

役割を分けると次のようになります。

| 役割 | 担当 |
|------|------|
| テーブルを作る | Flyway |
| カラムやインデックスを変更する | 新しい Flyway マイグレーション |
| Entity とテーブルのズレを検出する | Hibernate `validate` |
| Repository から SQL を実行する | Spring Data JPA / Hibernate |

重要なのは、**一度適用済みの `V1__initial_schema.sql` を後から編集しない**ことです。

すでに適用された DB を変更したい場合は、新しいファイルを追加します。

```text
V2__add_xxx_column.sql
V3__create_xxx_index.sql
```

Flyway の詳しいルールは [03. データ層](../backend/03-data.md#flywaydb-マイグレーション) を参照してください。

---

## よく使う確認コマンド

### MySQL だけ起動する

```bash
docker compose --project-directory "$(pwd)" --env-file .env -f docker/compose/docker-compose.dev.yaml up -d
```

### MySQL とバックエンドをまとめて起動する

```bash
docker compose --project-directory "$(pwd)" --env-file .env \
  -f docker/compose/docker-compose.single-host.yaml \
  -f docker/compose/docker-compose.single-host.local.yaml \
  up -d --build
```

### MySQL コンテナに入る

```bash
docker exec -it mysql-dev mysql -u root -p
```

単一ホスト構成の場合です。

```bash
docker exec -it smart_household_mysql_single mysql -u root -p
```

### データベースとテーブルを確認する

```sql
SHOW DATABASES;
USE demo;
SHOW TABLES;
```

`demo` の部分は `.env` の `MYSQL_DATABASE` に合わせます。

### テーブル定義を見る

```sql
SHOW CREATE TABLE expenses\G
```

`\G` は MySQL クライアントの縦表示です。
外部キーやインデックスが長いときに読みやすくなります。

### Flyway の適用履歴を見る

```sql
SELECT installed_rank, version, description, success, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;
```

### 支出データを軽く確認する

```sql
SELECT id, description, amount, date, category, user_id
FROM expenses
ORDER BY date DESC
LIMIT 10;
```

---

## トラブルシュート

### Spring Boot が MySQL に接続できない

まず JDBC URL のホスト名を確認します。

| Spring Boot の起動場所 | 正しいホスト名 |
|------------------------|----------------|
| ローカルPC / IDE | `localhost` |
| Docker コンテナ内 | `mysql` |

Docker 内のバックエンドから `localhost` を指定すると、MySQL ではなくバックエンドコンテナ自身を見に行きます。

### `Access denied for user` が出る

`.env` のユーザー名・パスワードを確認します。

```env
MYSQL_ROOT_USER=root
MYSQL_ROOT_PASSWORD=your-password
MYSQL_DATABASE=demo
```

MySQL の初期化後に `.env` のパスワードを変えても、既存ボリューム内のユーザー設定は自動では変わりません。
学習用に作り直す場合は、データ削除を理解したうえで `down -v` を使います。

### `Table ... doesn't exist` が出る

Flyway が初期スキーマを作れていない可能性があります。

確認するものは次の通りです。

- `backend/src/main/resources/db/migration/V1__initial_schema.sql`
- バックエンド起動ログの Flyway エラー
- `flyway_schema_history` の `success`

### 文字化けする

`docker/mysql/my.cnf` とテーブル定義の `utf8mb4` 設定を確認します。

```sql
SHOW CREATE TABLE users\G
```

`DEFAULT CHARSET=utf8mb4` が入っていれば、少なくともテーブル側の文字コード方針は合っています。

### 日時がずれる

MySQL、Hibernate、Jackson が UTC にそろっているか確認します。

```properties
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
spring.jackson.time-zone=UTC
```

---

## セキュリティとパフォーマンスの注意点

### セキュリティ

- `.env` には DB パスワードや API キーが入るため、Git にコミットしません。
- 本番では MySQL ポートをインターネットへ公開しません。
- 本番では SQL ログを出しっぱなしにしません。支出内容やメールアドレスがログに混ざる可能性があります。
- 本番では root ユーザーではなく、必要な権限だけを持つアプリ専用ユーザーを使う方が安全です。
- SQL インジェクション対策は、文字列結合ではなく Repository / JPQL のパラメータバインディングに寄せます。詳しくは [03. データ層](../backend/03-data.md#sql-インジェクション対策の仕組み) を参照してください。

### パフォーマンス

- `expenses(user_id, date)` のように、よく使う検索条件へインデックスを張ります。
- インデックスは増やしすぎると書き込みが重くなるため、検索パターンに合わせて追加します。
- DB 接続を長く握る処理は避けます。コネクションプール枯渇の原因になります。
- N+1 問題は MySQL の問題というより JPA の使い方の問題です。詳しくは [03. データ層](../backend/03-data.md#n1-問題と対策) を参照してください。

---

## まず覚えるポイント

- このプロジェクトの MySQL は、`users`、`expenses`、`monthly_reports` を保存します。
- ローカル Spring Boot からは `localhost:3306`、Docker 内バックエンドからは `mysql:3306` に接続します。
- MySQL の実データは `/var/lib/mysql` にあり、Docker ボリュームで永続化しています。
- 初期テーブルは Flyway の `V1__initial_schema.sql` が作ります。
- Hibernate は `ddl-auto=validate` で、テーブルを作るのではなく Entity とのズレを検出します。
- `utf8mb4` と UTC を明示して、文字化けや日時ずれを減らしています。
