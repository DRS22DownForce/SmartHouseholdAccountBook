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

**役割**: 入力値の検証を行うフレームワーク。アノテーションでバリデーションルールを定義します。

### 主なアノテーション

| アノテーション | 説明 | 使用例 |
|--------------|------|--------|
| `@NotNull` | nullでないことを検証 | `@NotNull private String name;` |
| `@NotEmpty` | nullでなく、かつ空でないことを検証（文字列やコレクション用） | `@NotEmpty private List<String> items;` |
| `@NotBlank` | null、空文字、空白のみでないことを検証（文字列用） | `@NotBlank private String description;` |
| `@Min`, `@Max` | 数値の範囲を検証 | `@Min(1) @Max(100) private Integer amount;` |
| `@Size` | 文字列やコレクションのサイズを検証 | `@Size(min = 1, max = 100) private String text;` |
| `@Email` | メールアドレスの形式を検証 | `@Email private String email;` |
| `@Valid` | オブジェクトのネストされたバリデーションを有効化 | `@Valid @RequestBody Dto dto;` |
| `@Validated` | クラスレベルでバリデーションを有効化（Spring用） | `@Validated public class Service {}` |

### 3つの主要な使用ケース

| ケース | 実行タイミング | 必要なアノテーション | 発生する例外 |
|--------|--------------|-------------------|------------|
| **1. 設定プロパティ** | アプリケーション起動時 | `@ConfigurationProperties` + `@Validated` | `BindException` |
| **2. コントローラー** | HTTPリクエスト時 | `@Valid` + `@RequestBody` | `MethodArgumentNotValidException` |
| **3. サービス** | メソッド呼び出し時 | `@Validated`（クラス）+ `@Valid`（パラメータ） | `ConstraintViolationException` |

---

### ケース1: 設定プロパティクラス

**使用目的**: `application.properties`の設定値が正しいことを起動時に検証

**コード例**:
```java
@ConfigurationProperties(prefix = "cors")
@Validated  // ← クラスレベルに必須
public class CorsProperties {
    @NotEmpty(message = "許可するオリジンは必須です")
    private List<String> allowedOrigins;
    
    @NotNull
    private List<String> allowedMethods;
}
```

**設定ファイル**（`application.properties`）:
```properties
cors.allowed-origins[0]=http://localhost:3000
cors.allowed-origins[1]=http://localhost:5173
```

**動作**:
- 起動時に`application.properties`から値を読み込み
- `@Validated`により自動的にバリデーション実行
- エラーがある場合、アプリケーションは起動しない

**`@ConfigurationProperties(prefix = "cors")`について**:
- `prefix = "cors"`は、`application.properties`で`cors.`で始まる設定を読み込むことを意味
- プロパティ名（ケバブケース）とJavaフィールド名（キャメルケース）が自動マッピング
- 例: `cors.allowed-origins` → `allowedOrigins`フィールド

### ケース2: コントローラーのリクエストボディ

**使用目的**: HTTPリクエストのデータが正しいことを検証

**`@RequestBody`について**: `@RequestBody`は、HTTPリクエストのボディ（本文）をJavaオブジェクトに変換するためのSpring MVCのアノテーションです。

**コード例**:
```java
@PostMapping("/api/expenses")
public ResponseEntity<ExpenseDto> createExpense(
    @Valid @RequestBody ExpenseRequestDto requestDto  // ← @Validでバリデーション実行
) {
    // バリデーションが通った場合のみ実行される
    return ResponseEntity.ok(expenseService.create(requestDto));
}
```

**DTOクラス**:
```java
public class ExpenseRequestDto {
    @NotNull(message = "金額は必須です")
    @Min(value = 1, message = "金額は1以上である必要があります")
    private Integer amount;
    
    @NotBlank(message = "説明は必須です")
    @Size(max = 100, message = "説明は100文字以内である必要があります")
    private String description;
}
```

**動作**:
- リクエストボディをJSONからJavaオブジェクトに変換後、`@Valid`によりバリデーション実行
- エラーがある場合、`MethodArgumentNotValidException`が発生し、400 Bad Requestを返す
- コントローラーメソッドは実行されない

---

### ケース3: サービスクラスや通常のメソッド

**使用目的**: ビジネスロジック層でもバリデーションを実行

**重要なポイント**: コントローラー以外で`@Valid`を使うには、**クラスレベルに`@Validated`が必要**

**コード例**:
```java
@Service
@Validated  // ← クラスレベルに必須（これがないと@Validが動作しない）
public class ExpenseApplicationService {
    
    public ExpenseDto addExpense(@Valid ExpenseRequestDto requestDto) {
        // バリデーションが通った場合のみ実行される
        // ...
    }
    
    // パラメータに直接アノテーションも可能
    public void updateExpense(
        @NotNull Long id,
        @Valid ExpenseRequestDto requestDto
    ) {
        // ...
    }
}
```

**動作**:
- SpringのAOP（アスペクト指向プログラミング）が介入
- メソッド呼び出し時に`@Valid`が付いたパラメータをバリデーション
- エラーがある場合、`ConstraintViolationException`が発生し、メソッド本体は実行されない

**なぜ`@Validated`が必要なのか**:
- コントローラーではSpring MVCが自動的に`@Valid`を処理
- 通常のメソッドでは、SpringのAOPによる介入が必要
- `@Validated`をクラスレベルに付けることで、AOPが介入してバリデーションが実行される

---

### `@Validated`と`@Valid`の違い

| アノテーション | 使用場所 | 実行タイミング | 必要な条件 |
|--------------|---------|--------------|----------|
| `@Validated` | クラスレベル | 起動時（設定プロパティ）<br>メソッド呼び出し時（サービス） | なし（単独で使用可能） |
| `@Valid` | メソッドパラメータ | リクエスト時（コントローラー）<br>メソッド呼び出し時（サービス + `@Validated`） | コントローラー: 不要<br>サービス: クラスに`@Validated`が必要 |

**使い分け**:
- **`@Validated`**: Springの機能拡張版。グループバリデーションなど高度な機能が使える
- **`@Valid`**: Jakarta Validationの標準アノテーション。シンプルで標準的

---

### よくある質問・注意点

**Q: バリデーションが実行されない場合は？**

以下の場合、バリデーションは**実行されません**:
1. パラメータに`@Valid`がない
2. DTOクラスのフィールドにバリデーションアノテーション（`@NotNull`など）がない
3. サービスクラスで`@Validated`がクラスレベルにない

**Q: エラーハンドリングはどうする？**

- **コントローラー**: `MethodArgumentNotValidException`を`@ControllerAdvice`でキャッチ
- **サービス**: `ConstraintViolationException`を適切に処理

**Q: 値オブジェクトでは使わないの？**

このプロジェクトでは、値オブジェクト（`ExpenseAmount`、`Category`など）ではJakarta Validationのアノテーションではなく、手動でバリデーションを行っています。これは、値オブジェクトの不変性とビジネスルールを明確にするためです。

例: `ExpenseAmount`クラス:
```38:56:backend/src/main/java/com/example/backend/domain/valueobject/ExpenseAmount.java
    public ExpenseAmount(Integer value) {
        validate(value);
        this.value = value;
    }

    private static void validate(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("金額はnullであってはなりません。");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("金額は1以上でなければなりません。");
        }
    }
```

---

### Jakarta Validationを使う時と使うべきでない時

Jakarta Validationは便利ですが、すべての場所で使うべきではありません。以下のガイドラインを参考にしてください。

#### ✅ Jakarta Validationを使うべき時

| 使用箇所 | 理由 | 例 |
|---------|------|-----|
| **DTOクラス** | API層での入力検証に適している。Spring MVCと統合されている | `ExpenseRequestDto`のフィールドに`@NotNull`、`@Min`など |
| **設定プロパティクラス** | 起動時に設定値の妥当性を検証できる | `CorsProperties`で`@NotEmpty`、`@NotNull`など |
| **コントローラーのリクエストボディ** | HTTPリクエストのデータ検証に最適。自動的にバリデーションが実行される | `@Valid @RequestBody ExpenseRequestDto` |
| **サービスクラスのメソッドパラメータ** | ビジネスロジック層でもバリデーションを実行できる（`@Validated`と組み合わせ） | `@Validated`クラス + `@Valid`パラメータ |

**メリット**:
- アノテーションでルールを宣言的に定義できる
- Spring Bootが自動的にバリデーションを実行してくれる
- エラーメッセージをカスタマイズできる
- コードが簡潔で読みやすい

#### ❌ Jakarta Validationを使うべきでない時

| 使用箇所 | 理由 | 例 |
|---------|------|-----|
| **値オブジェクト（ドメイン層）** | フレームワークに依存させたくない。コンストラクタで確実にバリデーションを実行したい | `ExpenseAmount`、`Category`など |
| **複雑なビジネスルール** | 単純なnullチェックや範囲チェックを超える複雑なロジック | カテゴリのリストチェック、複数フィールドの組み合わせチェックなど |
| **フレームワーク非依存のコード** | 再利用性やテストのしやすさを重視する場合 | ドメイン層のコア部分 |

**理由**:
- **値オブジェクトの不変性**: コンストラクタで一度だけバリデーションを行い、その後は変更できない。Jakarta ValidationはSpringの仕組みに依存するため、コンストラクタを直接呼び出す場合（`new ExpenseAmount()`）にはバリデーションが実行されない
- **ビジネスルールの明確化**: 複雑なビジネスルール（例: 有効なカテゴリのリストチェック）は、手動バリデーションの方が明確に表現できる
- **フレームワークへの依存を避ける**: ドメイン層のコア部分は、フレームワークに依存させないことで、再利用性とテストのしやすさが向上する

#### 使い分けの判断基準

**Jakarta Validationを使う場合**:
- ✅ シンプルなバリデーションルール（nullチェック、数値範囲、文字列長など）
- ✅ Springの仕組みを活用できる場所（DTO、設定プロパティなど）
- ✅ フレームワークの自動実行機能を活用したい場合

**手動バリデーションを使う場合**:
- ✅ 値オブジェクト（ドメイン層のコア）
- ✅ 複雑なビジネスルール（複数フィールドの組み合わせ、リストチェックなど）
- ✅ コンストラクタで確実にバリデーションを実行したい場合
- ✅ フレームワークに依存させたくない場合

#### このプロジェクトでの使い分け例

**Jakarta Validationを使用**:
```java
// DTOクラス（API層）
public class ExpenseRequestDto {
    @NotNull(message = "金額は必須です")
    @Min(value = 1, message = "金額は1以上である必要があります")
    private Integer amount;
}

// 設定プロパティクラス
@ConfigurationProperties(prefix = "cors")
@Validated
public class CorsProperties {
    @NotEmpty(message = "許可するオリジンは必須です")
    private List<String> allowedOrigins;
}
```

**手動バリデーションを使用**:
```java
// 値オブジェクト（ドメイン層）
public class ExpenseAmount {
    public ExpenseAmount(Integer value) {
        validate(value);  // ← 手動でバリデーション
        this.value = value;
    }
    
    private static void validate(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("金額はnullであってはなりません。");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("金額は1以上でなければなりません。");
        }
    }
}
```

---

### 学習ポイント

- **宣言的バリデーション**: アノテーションでルールを定義することで、コードが読みやすくなる
- **自動検証**: Spring Bootが自動的にバリデーションを実行してくれるため、手動でチェックする必要がない
- **エラーメッセージのカスタマイズ**: `message`属性で、ユーザーに分かりやすいエラーメッセージを設定できる
- **設定の安全性**: `@ConfigurationProperties`と組み合わせることで、設定ファイルの値が正しいことを保証できる


