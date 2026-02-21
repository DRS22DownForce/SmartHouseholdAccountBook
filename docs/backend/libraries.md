# バックエンドライブラリ詳細資料

> このドキュメントは、Smart Household Account Bookプロジェクトのバックエンドで使用されている主要なライブラリを学習用にまとめたものです。

## 📋 目次

1. [OpenAPI Generator](#openapi-generator)
2. [Lombok](#lombok)
3. [Jakarta Validation](#jakarta-validation)

---

## OpenAPI Generator

**役割**: OpenAPI仕様書（`openapi.yaml`）からJavaのインターフェースとモデルクラスを自動生成するツール。

**このプロジェクトでの使用箇所**:
- `pom.xml`のMavenプラグイン設定
- 生成されたコード: `target/generated-sources/openapi/`配下

**主な機能**:
- **APIインターフェース生成**: `ExpensesApi`インターフェースを自動生成
- **モデルクラス生成**: `ExpenseDto`, `ExpenseRequestDto`などを自動生成
- **型安全性**: 仕様書とコードが常に同期

**コード生成方法**:
```bash
# backendディレクトリで実行
mvn generate-sources
# または、クリーンビルドと一緒に実行
mvn clean generate-sources
```
生成されたコードは `target/generated-sources/openapi/` 配下に出力されます。

**`clean`をつけないときとつけるときの違い**:
- **`mvn generate-sources`（cleanなし）**: 
  - 既存の`target`ディレクトリをそのまま残してコードを生成
  - 既に生成済みのファイルがあっても、新しいファイルを追加または上書き
  - **メリット**: 実行が速い（削除処理がないため）
  - **デメリット**: 削除されたAPIのファイルが残り続ける可能性がある
  
- **`mvn clean generate-sources`（cleanあり）**:
  - まず`target`ディレクトリ全体を削除してから、新しくコードを生成
  - 完全にクリーンな状態から生成される
  - **メリット**: 古いファイルが残らない（完全に同期される）
  - **デメリット**: 実行に少し時間がかかる（削除処理があるため）
  
**使い分けの目安**:
- 通常の開発時: `mvn generate-sources`（速いので）
- OpenAPI仕様書を大幅に変更した時: `mvn clean generate-sources`（古いファイルを確実に削除）

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

---

## Lombok

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
---

## Jakarta Validation

**役割**: 入力値の検証を行うフレームワーク。アノテーションでバリデーションルールを定義する。

---

### 主なアノテーション

| アノテーション     | 説明                                                                 | 使用例 |
| ------------------ | -------------------------------------------------------------------- | ------ |
| `@NotNull`        | null でないことを検証                                                | `@NotNull private String name;` |
| `@NotEmpty`       | null でなく、かつ空でないことを検証（文字列・コレクション用）       | `@NotEmpty private List<String> items;` |
| `@NotBlank`       | null・空文字・空白のみでないことを検証（文字列用）                   | `@NotBlank private String description;` |
| `@Min`, `@Max`    | 数値の範囲を検証                                                     | `@Min(1) @Max(100) private Integer amount;` |
| `@Size`           | 文字列やコレクションの長さ・要素数を検証                             | `@Size(min = 1, max = 100) private String text;` |
| `@Email`          | メールアドレスの形式を検証                                           | `@Email private String email;` |
| `@Valid`          | ネストしたオブジェクトのバリデーションを有効化（Bean Validation 標準） | `@Valid @RequestBody Dto dto;` |
| `@Validated`      | クラス単位でメソッド／コンストラクタの検証を有効化（Spring 拡張）     | `@Validated public class MyService {}` |

---

### 主要な使用ケース

| ケース | 実行タイミング | 必要なアノテーション | 発生する例外 |
| ------ | -------------- | -------------------- | ------------ |
| 1. コントローラーのリクエストボディ | HTTP リクエスト時 | `@Valid` + `@RequestBody` | `MethodArgumentNotValidException` |
| 2. コントローラーのクエリ・パス変数 | HTTP リクエスト時 | コントローラーに `@Validated`、引数に制約アノテーション | `ConstraintViolationException` |
| 3. Bean のインスタンス化時 | Bean 作成時（起動時・初回参照時） | クラスに `@Validated`、フィールド／コンストラクタ引数に制約 | `ConstraintViolationException` |
| 4. サービス等の Bean のメソッド呼び出し時 | メソッド実行時 | クラスに `@Validated`、メソッド引数に制約 | `ConstraintViolationException` |

**ポイント**

- 継承元のインターフェースやクラスで付けたバリデーションは具象クラスにも適用される。親の制約を弱めるような上書きはできない。
- コントローラー以外で `@Valid` を効かせるには、**クラスに `@Validated` を付ける必要がある**。
  - コントローラーでは Spring MVC が `@Valid` を解釈して検証する。
  - それ以外のクラスでは、Spring の AOP がメソッド／コンストラクタの引数を検証する必要があり、`@Validated` を付けたクラスだけがその対象になる。
- `new` で自前インスタンス化するだけのクラス（Spring が Bean として作らないクラス）では、この仕組みは動かない。

---

### リクエストボディの検証と @RequestBody

`@RequestBody` は、HTTP リクエストのボディ（本文）を Java オブジェクトに変換するための Spring MVC のアノテーション。

**流れ**

1. リクエストボディ（例: JSON）が `@RequestBody` の型にデシリアライズされる。
2. 引数に `@Valid` が付いていれば、変換後のオブジェクトに対してバリデーションが実行される。
3. 違反があると `MethodArgumentNotValidException` がスローされ、通常は 400 Bad Request が返る。コントローラーメソッド本体は実行されない。

---

### エラーハンドリング

| 対象         | 例外                             | 対応例 |
| ------------ | -------------------------------- | ------ |
| コントローラー（リクエストボディ） | `MethodArgumentNotValidException` | `@ControllerAdvice` でキャッチし、400 とエラー内容を返す |
| コントローラー（クエリ・パス）・サービス等 | `ConstraintViolationException`     | `@ControllerAdvice` やサービス層でキャッチし、適切に 400 等を返す |




