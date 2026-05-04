# Appendix — 今後書く予定のトピック

> この章は、Spring 中級エンジニアとしてさらに深めたい内容の**インデックス**です。本編（[01](./01-spring-core.md)〜[06](./06-testing.md)）を一通り読み終えてから、興味のあるトピックを深掘りしていきましょう。詳細の執筆は今後行います。

---

## ビルド・パッケージング

### Maven の詳細

- **ビルドライフサイクル**: `validate → compile → test → package → verify → install → deploy`
- **フェーズとゴール**: プラグインゴールがどのフェーズに紐付くか
- **依存スコープ**: `compile` / `provided` / `runtime` / `test` / `system` の使い分け
- **プロファイル（`<profile>`）**: 環境ごとのビルド設定切替
- **BOM（Bill of Materials）**: 推移的依存のバージョン統一
- **マルチモジュール構成**: 複数の子モジュールを 1 つの親 POM で管理

### Gradle との比較

- Maven（XML、規約重視）vs Gradle（Kotlin/Groovy DSL、柔軟）
- Spring Boot プロジェクトで Gradle を選ぶときの指針

---

## データベース詳細

### MySQL の深掘り

- **文字コード**: `utf8mb4` と `utf8` の違い、絵文字対応
- **インデックス設計**: B-tree / 複合インデックス / カバリングインデックス
- **実行計画**: `EXPLAIN` の読み方
- **ロック**: 行ロック / ギャップロック / 意図共有ロック、デッドロックの発生と回避
- **REPEATABLE_READ と MVCC**: InnoDB のトランザクション分離レベル実装

### PostgreSQL との比較

- 型システム / JSON サポート / 拡張機能の違い

### Testcontainers

- H2 ではなく**本番と同じ MySQL でテスト**する方法
- `@Testcontainers` + `MySQLContainer` で Docker を起動
- 本番で動くが H2 で動かない SQL のトラブルを回避

---

## コンテナ・デプロイ

### Docker の詳細

- **マルチステージビルド**: ビルド用コンテナと実行用コンテナを分ける
- **JVM のメモリ設定**: `-XX:+UseContainerSupport`、`-XX:MaxRAMPercentage`
- **イメージサイズ削減**: 軽量ベースイメージ（`eclipse-temurin:21-jre-alpine` 等）
- **ヘルスチェック設定**: Dockerfile の `HEALTHCHECK`、Compose の `healthcheck`

### docker-compose

- サービス間通信 / ネットワーク / volumes
- 本プロジェクトの `docker/compose/docker-compose.single-host.yaml` の構成

### Kubernetes / ECS へのデプロイ

- liveness / readiness プローブの設定
- 環境変数・Secret の渡し方
- ローリングデプロイとブルーグリーン

→ 本プロジェクトの具体的な AWS デプロイ手順は [infrastructure/deployment-guide.md](../infrastructure/deployment-guide.md) を参照。

---

## コード自動生成・ボイラープレート削減

### OpenAPI Generator の詳細

- テンプレートカスタマイズ
- `configOptions` の主要項目
- `additionalProperties` での拡張
- 生成されたコードをリポジトリに含める方針 vs 毎回生成する方針

### Lombok の詳細

- `@Data` / `@Value` / `@Builder` / `@SneakyThrows`
- `@EqualsAndHashCode` の注意点（`callSuper`、`onlyExplicitlyIncluded`）
- Lombok の落とし穴: リフレクション系ライブラリとの相性
- IDE 設定（Annotation Processor 有効化）

### MapStruct

- 手書き Mapper の自動生成
- `@Mapper(componentModel = "spring")` による DI 対応
- 複雑なマッピングの扱い

---

## 設計

### ドメイン駆動設計（DDD）の詳細

- **エンティティ / 値オブジェクト / 集約**: Eric Evans のオリジナル概念
- **集約ルートとトランザクション境界**
- **リポジトリの抽象化**: ドメイン層のインターフェース vs インフラ層の実装
- **ドメインサービス vs アプリケーションサービス**
- **ユビキタス言語**: 設計と実装とビジネスで同じ言葉を使う
- 本プロジェクトは DDD の「軽量版」を採用している（値オブジェクト、レイヤー分離）

### ヘキサゴナルアーキテクチャ / クリーンアーキテクチャ

- 依存方向の逆転
- ポート＆アダプター
- Spring との相性

### CQRS（Command Query Responsibility Segregation）

- 読み込みと書き込みのモデル分離
- イベントソーシングとの組み合わせ

---

## イベント駆動・非同期

### イベント駆動アーキテクチャの深掘り

- `ApplicationEventPublisher` の同期/非同期
- `@TransactionalEventListener` の `Phase`（BEFORE_COMMIT / AFTER_COMMIT / AFTER_ROLLBACK）
- ドメインイベントの実装パターン
- 外部メッセージング（Kafka / SQS / RabbitMQ）との統合

### 非同期処理の深掘り

- `TaskExecutor` の設定（スレッドプール設計）
- `@Async` の戻り値: `CompletableFuture<T>` / `ListenableFuture<T>`
- スレッドローカルの扱い（SecurityContext / MDC の伝播）
- **Java 21 の Virtual Threads**: `spring.threads.virtual.enabled=true`

---

## 可観測性（Observability）

### Micrometer の深掘り

- カスタムメトリクス（Counter / Timer / Gauge）
- Prometheus へのエクスポート（`micrometer-registry-prometheus`）
- CloudWatch へのエクスポート
- Grafana でのダッシュボード構築

### 分散トレーシング

- **Micrometer Tracing + OpenTelemetry**
- トレース ID / スパン ID の概念
- Jaeger / Zipkin / AWS X-Ray

### 構造化ログ

- `logback-spring.xml` のカスタマイズ
- `LogstashEncoder` で JSON ログ
- CloudWatch Logs Insights / Datadog での検索

---

## パフォーマンス・JVM

### JVM / GC チューニング

- ヒープ設計（`-Xms` / `-Xmx`、コンテナでは `-XX:MaxRAMPercentage`）
- GC アルゴリズム（G1GC / ZGC / Shenandoah）の選択
- GC ログの読み方
- メモリリーク調査（ヒープダンプ / `jmap` / `jcmd`）

### プロファイリング

- **JFR (Java Flight Recorder)**
- **async-profiler**
- スレッドダンプ / `jstack`

### 負荷試験

- k6 / Gatling / Apache JMeter
- 継続的な性能回帰テスト

---

## Java モダン構文

Java 21 を使っているプロジェクトだが、本編では深く触れていないモダン機能。

### Record クラス

- 不変データホルダー
- Lombok `@Value` との比較

### Sealed クラス

- 継承可能なクラスを限定
- パターンマッチングとの相性

### switch パターンマッチング

- Java 21 で正式化
- 従来の switch-case との違い

### Virtual Threads (Java 21)

- 仮想スレッドの仕組み
- Spring Boot での有効化
- 同期 I/O を「軽量スレッドで並列」する新パラダイム

---

## CI/CD

### GitHub Actions

- `mvn test` / `mvn verify` / Docker イメージビルド
- PR トリガー / main へのマージ時のデプロイ
- シークレット管理

### デプロイ戦略

- Blue/Green
- Canary
- Rolling update

---

## その他の Spring 周辺技術

### Spring Batch

- 大量データ処理のバッチフレームワーク
- Job / Step / Chunk

### Spring Integration / Spring Cloud Stream

- エンタープライズ統合パターン
- メッセージング基盤の抽象化

### Spring WebFlux

- リアクティブプログラミング
- Spring MVC との違い
- いつ使うべきか / 使うべきでないか

---

## 次のステップ

本編 + 本 appendix のテーマをざっと理解できたら、以下の書籍・ドキュメントで学習を深めていくと良いです。

### 公式ドキュメント
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Framework Reference](https://docs.spring.io/spring-framework/reference/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/)

### 書籍
- **『Spring 徹底入門』（翔泳社）** — Spring Framework の基礎から実践
- **『エリック・エヴァンスのドメイン駆動設計』** — DDD のオリジナル
- **『エンタープライズアプリケーションアーキテクチャパターン』（Martin Fowler）** — リポジトリ等のパターンの出典
- **『リーダブルコード』** — コード品質の基本

### オンライン
- [Baeldung](https://www.baeldung.com/) — Spring の実用例が豊富
- [Reflectoring](https://reflectoring.io/) — Spring Boot 深掘り記事
- Spring 公式 YouTube チャンネル

---

[README に戻る](./README.md)
