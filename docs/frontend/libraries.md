# フロントエンドライブラリ詳細資料

> このドキュメントは、Smart Household Account Bookプロジェクトのフロントエンドで使用されている主要なライブラリを学習用にまとめたものです。

## 📋 目次

1. [OpenAPI Generator](#openapi-generator)
2. [AWS Amplify](#aws-amplify)
3. [React Hook Form](#react-hook-form)
4. [Zod](#zod)
5. [Axios](#axios)
6. [Sonner（トースト通知）](#sonnerトースト通知)
7. [date-fns](#date-fns)
8. [Recharts](#recharts)

---

## OpenAPI Generator

**役割**: OpenAPI仕様書（`openapi.yaml`）からTypeScriptのAPIクライアントコードを自動生成するツール。

**このプロジェクトでの使用箇所**:
- `package.json`のスクリプト設定
- 生成されたコード: `src/api/generated/`配下

**主な機能**:
- **APIクライアント生成**: `ExpensesApi`、`ChatApi`などのクラスを自動生成
- **型定義生成**: リクエスト・レスポンスの型を自動生成
- **型安全性**: 仕様書とコードが常に同期され、型が保証される

**コード生成方法**:
```bash
# frontend-nextjsディレクトリで実行
npm run generate:api
# または直接実行
npx openapi-generator-cli generate -i ../openapi/openapi.yaml -g typescript-axios -o ./src/api/generated
```
生成されたコードは `src/api/generated/` 配下に出力されます。

**`package.json`の設定**:

`frontend-nextjs/package.json` (10行目):

```json
"generate:api": "npx openapi-generator-cli generate -i ../openapi/openapi.yaml -g typescript-axios -o ./src/api/generated"
```

**実際のコード例（生成されたコードの使用）**:

`frontend-nextjs/src/api/apiClient.ts` (18-22行目):

```typescript
/**
 * Expense APIクライアントのインスタンスを作成
 */
export function getExpenseApiClient(): ExpensesApi {
    return new ExpensesApi(new Configuration({
        basePath: getBasePath()
    }));
}
```

**学習ポイント**:
- **コード生成**: 仕様書からコードを自動生成することで、手動のミスを防ぐ
- **型安全性**: 生成されたコードにより、APIの型が保証される
- **同期**: OpenAPI仕様書を更新して再生成することで、常に最新の状態を保つ

**参考資料**:
- [OpenAPI Generator公式ドキュメント](https://openapi-generator.tech/)

---

## AWS Amplify

**役割**: AWS Cognitoと連携して、ユーザー認証・認可を管理するライブラリ。

**このプロジェクトでの使用箇所**:
- `src/contexts/auth-provider.tsx`: 認証プロバイダーの設定
- `src/config/aws-exports.ts`: AWS Cognitoの設定
- `src/api/authUtils.ts`: JWTトークンの取得

**主な機能**:
- **Authenticatorコンポーネント**: ログイン・サインアップUIを自動提供
- **セッション管理**: 認証状態を自動的に管理
- **JWTトークン取得**: 認証済みユーザーのJWTトークンを取得

**実際のコード例（設定）**:

`frontend-nextjs/src/config/aws-exports.ts` (16-60行目):

```typescript
const awsConfig = {
    // AWSプロジェクトの基本設定
    aws_project_region: "ap-northeast-1",

    // Cognito Identity Pool（フェデレーテッドIDを管理）
    aws_cognito_identity_pool_id: "your-identity-pool-id",

    // Cognito User Poolの設定（ユーザー認証を管理）
    aws_cognito_region: "ap-northeast-1",
    aws_user_pools_id: "your-user-pool-id",
    aws_user_pools_web_client_id: "your-client-id",

    // サインアップ時にメールアドレスをユーザー名として使用
    aws_cognito_username_attributes: [
        "EMAIL"
    ],

    // パスワードポリシーの設定
    aws_cognito_password_protection_settings: {
        passwordPolicyMinLength: 8,  // 最小8文字
        passwordPolicyCharacters: [] // 文字種の制限なし
    },

    // アカウント検証方法（メールで確認コードを送信）
    aws_cognito_verification_mechanisms: [
        "EMAIL"
    ]
};
```

**実際のコード例（認証プロバイダー）**:

`frontend-nextjs/src/contexts/auth-provider.tsx` (52-54行目):

```typescript
export function AuthProvider({ children }: AuthProviderProps) {
  return <Authenticator>{children}</Authenticator>
}
```

**実際のコード例（JWTトークン取得）**:

`frontend-nextjs/src/api/authUtils.ts` (10-30行目):

```typescript
/**
 * CognitoからJWTトークン（IDトークン）を取得
 */
async function getJwtToken(): Promise<string> {
    const session = await fetchAuthSession();
    const token = session.tokens?.idToken?.toString();

    if (!token) {
        throw new Error('認証トークンの取得に失敗しました');
    }
    return token;
}

/**
 * Authorizationヘッダーを付与したオプションオブジェクトを作成
 */
export async function withAuthHeader(): Promise<{ headers: { Authorization: string } }> {
    const token = await getJwtToken();
    return {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    };
}
```

**学習ポイント**:
- **Authenticatorコンポーネント**: ログイン・サインアップUIを自動提供するため、手動で実装する必要がない
- **セッション管理**: 認証状態を自動的に管理し、リロード時も状態を保持
- **JWTトークン**: バックエンドAPIへのリクエスト時に、JWTトークンをAuthorizationヘッダーに付与

**参考資料**:
- [AWS Amplify公式ドキュメント](https://docs.amplify.aws/)

---

## React Hook Form

**役割**: フォームの状態管理とバリデーションを行うライブラリ。パフォーマンスが高く、再レンダリングを最小限に抑えます。

**このプロジェクトでの使用箇所**:
- フォームコンポーネント（将来的に使用予定）
- `@hookform/resolvers`: Zodなどのバリデーションライブラリと統合

**主な機能**:
- **パフォーマンス**: 制御されていないコンポーネント（uncontrolled components）を使用し、再レンダリングを最小限に
- **バリデーション**: Zodなどのスキーマバリデーションライブラリと統合可能
- **エラーハンドリング**: フィールドごとのエラーを自動的に管理

**基本的な使用例**:

```typescript
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"

// Zodスキーマを定義
const expenseSchema = z.object({
  amount: z.number().min(1, "金額は1以上である必要があります"),
  description: z.string().min(1, "説明は必須です"),
  category: z.string().min(1, "カテゴリーは必須です"),
  date: z.string().min(1, "日付は必須です"),
})

type ExpenseFormData = z.infer<typeof expenseSchema>

function ExpenseForm() {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ExpenseFormData>({
    resolver: zodResolver(expenseSchema),
  })

  const onSubmit = (data: ExpenseFormData) => {
    console.log(data)
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input {...register("amount")} type="number" />
      {errors.amount && <p>{errors.amount.message}</p>}
      
      <input {...register("description")} />
      {errors.description && <p>{errors.description.message}</p>}
      
      <button type="submit">送信</button>
    </form>
  )
}
```

**学習ポイント**:
- **`register`**: フォームフィールドをReact Hook Formに登録
- **`handleSubmit`**: フォーム送信時のバリデーションとコールバック実行
- **`formState.errors`**: バリデーションエラーの情報
- **`zodResolver`**: ZodスキーマをReact Hook Formのリゾルバーとして使用

**参考資料**:
- [React Hook Form公式ドキュメント](https://react-hook-form.com/)

---

## Zod

**役割**: TypeScriptファーストのスキーマバリデーションライブラリ。型安全なバリデーションを提供します。

**このプロジェクトでの使用箇所**:
- フォームバリデーション（将来的に使用予定）
- `@hookform/resolvers`と組み合わせてReact Hook Formと統合

**主な機能**:
- **型推論**: スキーマからTypeScriptの型を自動生成
- **バリデーション**: 実行時にデータの妥当性を検証
- **エラーメッセージ**: カスタマイズ可能なエラーメッセージ

**基本的な使用例**:

```typescript
import { z } from "zod"

// スキーマを定義
const expenseSchema = z.object({
  amount: z.number().min(1, "金額は1以上である必要があります"),
  description: z.string().min(1, "説明は必須です").max(100, "説明は100文字以内である必要があります"),
  category: z.enum(["食費", "交通費", "娯楽", "その他"], {
    errorMap: () => ({ message: "有効なカテゴリーを選択してください" }),
  }),
  date: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "日付はYYYY-MM-DD形式である必要があります"),
})

// 型を推論
type ExpenseFormData = z.infer<typeof expenseSchema>

// バリデーション実行
const result = expenseSchema.safeParse({
  amount: 1000,
  description: "ランチ代",
  category: "食費",
  date: "2024-01-01",
})

if (result.success) {
  console.log(result.data) // 型安全なデータ
} else {
  console.error(result.error.errors) // エラー情報
}
```

**主なバリデーション関数**:

| 関数 | 説明 | 使用例 |
|------|------|--------|
| `z.string()` | 文字列を検証 | `z.string().min(1).max(100)` |
| `z.number()` | 数値を検証 | `z.number().min(1).max(1000000)` |
| `z.enum()` | 列挙型を検証 | `z.enum(["食費", "交通費"])` |
| `z.date()` | 日付を検証 | `z.date()` |
| `z.boolean()` | 真偽値を検証 | `z.boolean()` |
| `z.array()` | 配列を検証 | `z.array(z.string())` |
| `z.object()` | オブジェクトを検証 | `z.object({ name: z.string() })` |

**学習ポイント**:
- **型推論**: `z.infer<typeof schema>`でスキーマから型を生成
- **`safeParse`**: エラーを例外として投げず、結果オブジェクトを返す（安全）
- **`parse`**: エラーを例外として投げる（簡潔だが、エラーハンドリングが必要）

**参考資料**:
- [Zod公式ドキュメント](https://zod.dev/)

---

## Axios

**役割**: HTTPクライアントライブラリ。PromiseベースのAPIを提供し、リクエスト・レスポンスのインターセプターを設定できます。

**このプロジェクトでの使用箇所**:
- OpenAPI Generatorで生成されたAPIクライアントが内部的にAxiosを使用
- `src/api/generated/`配下のコードで使用

**主な機能**:
- **Promiseベース**: `async/await`で非同期処理を記述可能
- **インターセプター**: リクエスト・レスポンスをインターセプトして処理を追加可能
- **自動JSON変換**: リクエスト・レスポンスを自動的にJSONに変換

**OpenAPI Generatorで生成されたコードでの使用**:

生成されたAPIクライアントは内部的にAxiosを使用しています。例えば、`ExpensesApi`クラスはAxiosのインスタンスを使用してHTTPリクエストを送信します。

**実際のコード例（生成されたコードの使用）**:

`frontend-nextjs/src/api/expenseApi.ts` (19-24行目):

```typescript
/**
 * 全支出を取得
 */
export async function fetchExpenses(): Promise<Expense[]> {
    const api = getExpenseApiClient();
    const options = await withAuthHeader();
    const response = await api.apiExpensesGet(undefined, options);
    return response.data.map(toExpense);
}
```

**学習ポイント**:
- **型安全性**: 生成されたコードにより、APIの型が保証される
- **エラーハンドリング**: Axiosのエラーは`catch`ブロックで処理
- **認証ヘッダー**: `withAuthHeader()`でJWTトークンを付与

**参考資料**:
- [Axios公式ドキュメント](https://axios-http.com/)

---

## Sonner（トースト通知）

**役割**: トースト通知（一時的なメッセージ表示）を表示するライブラリ。シンプルで軽量です。

**このプロジェクトでの使用箇所**:
- `src/hooks/use-expenses.ts`: 成功・エラーメッセージの表示
- `src/lib/api-error-handler.ts`: APIエラーメッセージの表示

**主な機能**:
- **シンプルなAPI**: `toast.success()`, `toast.error()`などで簡単に使用可能
- **カスタマイズ可能**: スタイルや位置をカスタマイズ可能
- **アクセシブル**: スクリーンリーダーに対応

**実際のコード例**:

`frontend-nextjs/src/hooks/use-expenses.ts` (39行目):

```typescript
toast.success("支出を追加しました")
```

`frontend-nextjs/src/lib/api-error-handler.ts` (9-24行目):

```typescript
export function showApiErrorMessage(error: unknown, defaultMessage: string): void {
  if (error && typeof error === "object" && "response" in error) {
    const apiError = error as { response?: { status?: number } }
    
    if (apiError.response?.status === 401) {
      toast.error("認証エラー: 再ログインしてください")
      return 
    }
    
    if (apiError.response?.status === 404) {
      toast.error("データが見つかりませんでした")
      return 
    }
  }
  toast.error(defaultMessage)
}
```

**主な関数**:

| 関数 | 説明 | 使用例 |
|------|------|--------|
| `toast.success()` | 成功メッセージを表示 | `toast.success("保存しました")` |
| `toast.error()` | エラーメッセージを表示 | `toast.error("エラーが発生しました")` |
| `toast.info()` | 情報メッセージを表示 | `toast.info("処理中です")` |
| `toast.warning()` | 警告メッセージを表示 | `toast.warning("注意が必要です")` |

**学習ポイント**:
- **ユーザーフィードバック**: 操作の結果をユーザーに即座に伝える
- **エラーハンドリング**: APIエラーをユーザーに分かりやすく表示
- **UX向上**: 成功・エラーの状態を視覚的に伝える

**参考資料**:
- [Sonner公式ドキュメント](https://sonner.emilkowal.ski/)

---

## date-fns

**役割**: 日付を操作・フォーマットするライブラリ。軽量で関数型のAPIを提供します。

**このプロジェクトでの使用箇所**:
- 日付のフォーマット（将来的に使用予定）

**主な機能**:
- **日付フォーマット**: `format()`で日付を文字列に変換
- **日付操作**: `addDays()`, `subMonths()`などで日付を操作
- **ロケール対応**: 日本語ロケールに対応

**基本的な使用例**:

```typescript
import { format, addDays, subMonths } from "date-fns"
import { ja } from "date-fns/locale"

// 日付をフォーマット
const formatted = format(new Date(), "yyyy-MM-dd", { locale: ja })
console.log(formatted) // "2024-01-01"

// 日付を操作
const tomorrow = addDays(new Date(), 1)
const lastMonth = subMonths(new Date(), 1)
```

**主な関数**:

| 関数 | 説明 | 使用例 |
|------|------|--------|
| `format()` | 日付をフォーマット | `format(date, "yyyy-MM-dd")` |
| `parse()` | 文字列を日付に変換 | `parse("2024-01-01", "yyyy-MM-dd", new Date())` |
| `addDays()` | 日を追加 | `addDays(date, 1)` |
| `subDays()` | 日を減算 | `subDays(date, 1)` |
| `addMonths()` | 月を追加 | `addMonths(date, 1)` |
| `subMonths()` | 月を減算 | `subMonths(date, 1)` |

**学習ポイント**:
- **関数型API**: 不変性を保ち、副作用がない
- **ロケール対応**: 日本語などのロケールに対応
- **軽量**: Moment.jsよりも軽量

**参考資料**:
- [date-fns公式ドキュメント](https://date-fns.org/)

---

## Recharts

**役割**: React用のチャートライブラリ。レスポンシブでカスタマイズ可能なチャートを提供します。

**このプロジェクトでの使用箇所**:
- `src/components/expense-trend-chart.tsx`: 支出の推移をグラフで表示

**主な機能**:
- **レスポンシブ**: 画面サイズに応じて自動的にリサイズ
- **カスタマイズ可能**: スタイルや色をカスタマイズ可能
- **アクセシブル**: スクリーンリーダーに対応

**基本的な使用例**:

```typescript
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from "recharts"

const data = [
  { name: "1月", amount: 10000 },
  { name: "2月", amount: 15000 },
  { name: "3月", amount: 12000 },
]

function ExpenseChart() {
  return (
    <LineChart width={600} height={300} data={data}>
      <CartesianGrid strokeDasharray="3 3" />
      <XAxis dataKey="name" />
      <YAxis />
      <Tooltip />
      <Legend />
      <Line type="monotone" dataKey="amount" stroke="#8884d8" />
    </LineChart>
  )
}
```

**主なコンポーネント**:

| コンポーネント | 説明 | 使用例 |
|--------------|------|--------|
| `LineChart` | 折れ線グラフ | `<LineChart data={data}>` |
| `BarChart` | 棒グラフ | `<BarChart data={data}>` |
| `PieChart` | 円グラフ | `<PieChart data={data}>` |
| `XAxis` | X軸 | `<XAxis dataKey="name" />` |
| `YAxis` | Y軸 | `<YAxis />` |
| `Tooltip` | ツールチップ | `<Tooltip />` |
| `Legend` | 凡例 | `<Legend />` |

**学習ポイント**:
- **データ駆動**: データを渡すだけでチャートを生成
- **カスタマイズ**: スタイルや色を自由にカスタマイズ可能
- **レスポンシブ**: 画面サイズに応じて自動的にリサイズ

**参考資料**:
- [Recharts公式ドキュメント](https://recharts.org/)

---

## まとめ

このプロジェクトのフロントエンドでは、以下のライブラリを使用しています：

### API・認証
- **OpenAPI Generator**: APIクライアントの自動生成
- **AWS Amplify**: AWS Cognitoとの連携
- **Axios**: HTTPクライアント（生成されたコードで使用）

### フォーム・バリデーション
- **React Hook Form**: フォーム状態管理
- **Zod**: スキーマバリデーション

### UI・UX
- **Sonner**: トースト通知
- **Recharts**: チャート表示

### ユーティリティ
- **date-fns**: 日付操作・フォーマット

これらのライブラリを組み合わせることで、保守性と拡張性の高いアプリケーションを実現しています。

---

**最終更新日**: 2024年

