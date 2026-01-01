# TypeScript基礎文法資料

> このドキュメントは、Smart Household Account Bookプロジェクトで使用されているTypeScriptの文法の要点を、学習用にまとめたものです。

## 📋 目次

1. [TypeScriptとは](#typescriptとは)
2. [基本的な型](#基本的な型)
3. [型注釈と型推論](#型注釈と型推論)
4. [オブジェクトとインターフェース](#オブジェクトとインターフェース)
5. [配列とタプル](#配列とタプル)
6. [関数の型定義](#関数の型定義)
7. [Union型とLiteral型](#union型とliteral型)
8. [型エイリアスと型ユーティリティ](#型エイリアスと型ユーティリティ)
9. [ジェネリクス](#ジェネリクス)
10. [exportとモジュール](#exportとモジュール)
11. [オプショナルチェーン](#オプショナルチェーン)
12. [Promise、async、await](#promiseasyncawait)
13. [実際のプロジェクトでの使用例](#実際のプロジェクトでの使用例)

---

## TypeScriptとは

**TypeScript**は、JavaScriptに**型システム**を追加したプログラミング言語です。

### JavaScriptとの違い

| 特徴 | JavaScript | TypeScript |
|------|-----------|------------|
| **型チェック** | 実行時のみ | コンパイル時（開発時） |
| **エラー発見** | 実行して初めて分かる | コードを書いている時に分かる |
| **自動補完** | 限定的 | 充実（IDEが型情報を理解） |
| **リファクタリング** | 危険（型情報がない） | 安全（型情報がある） |

### なぜTypeScriptを使うのか？

1. **バグの早期発見**: コンパイル時にエラーを検出できるため、実行前に問題を発見できる
2. **コードの可読性向上**: 型情報があることで、コードの意図が明確になる
3. **開発効率の向上**: IDE（Cursor/VS Code）で自動補完やエラー表示が充実
4. **大規模開発に適している**: チーム開発で、型情報があることでコードの理解が容易

### 実際のコード例

```typescript
// JavaScript（型がない）
function add(a, b) {
  return a + b
}
add(1, 2)        // 3（正常）
add("1", "2")    // "12"（文字列結合になってしまう！）

// TypeScript（型がある）
function add(a: number, b: number): number {
  return a + b
}
add(1, 2)        // 3（正常）
add("1", "2")    // エラー！型が合わない
```

**解説**: `a: number`はパラメータ`a`が`number`型であることを指定。`: number`は戻り値が`number`型であることを指定。型が合わない場合、コンパイル時にエラーが表示される。

---

## 基本的な型

### プリミティブ型

| 型 | 説明 | 例 |
|---|------|-----|
| `string` | 文字列 | `"Hello"`, `'World'` |
| `number` | 数値（整数・小数・負数すべて） | `1`, `3.14`, `-5` |
| `boolean` | 真偽値 | `true`, `false` |
| `null` | null値 | `null` |
| `undefined` | undefined値 | `undefined` |

### コード例

```typescript
// 文字列型
const name: string = "山田太郎"
const message: string = `こんにちは、${name}さん`  // テンプレートリテラル

// 数値型
const age: number = 25
const price: number = 1000.5

// 真偽値型
const isActive: boolean = true
```

**学習ポイント**: `変数名: 型`の形式で型を指定。テンプレートリテラル（バッククォート）で`${変数}`で変数を埋め込める。

---

## 型注釈と型推論

TypeScriptは、**型推論**により、明示的に型を書かなくても型を推測してくれます。

### 型注釈と型推論の違い

```typescript
// 型注釈（明示的に型を指定）
const name: string = "山田太郎"
const age: number = 25

// 型推論（TypeScriptが自動で型を推測）
const name = "山田太郎"        // string型と推論される
const age = 25                 // number型と推論される
```

### いつ型注釈を使うべきか？

1. **関数のパラメータ**: 型推論できないため、必ず型注釈が必要
2. **関数の戻り値**: 明示的に指定することで、意図を明確にできる
3. **変数の初期値がない場合**: `let`で宣言する場合など

```typescript
// 関数のパラメータには型注釈が必要
function greet(name: string): string {
  return `こんにちは、${name}さん`
}

// 変数の初期値がない場合
let count: number  // 型注釈が必要
count = 10

// 初期値がある場合は型推論でOK
const count = 10  // number型と推論される
```

**学習ポイント**: 明らかな場合は型推論を活用。関数のパラメータや、型推論できない場合のみ型注釈を使用。

---

## オブジェクトとインターフェース

### オブジェクト型とインターフェース

```typescript
// オブジェクト型の定義（直接指定）
const user: {
  name: string
  age: number
  email: string
} = {
  name: "山田太郎",
  age: 25,
  email: "yamada@example.com"
}

// インターフェース（推奨：再利用可能で保守性が高い）
interface User {
  name: string
  age: number
  email: string
}

const user: User = {
  name: "山田太郎",
  age: 25,
  email: "yamada@example.com"
}
```

### 実際のプロジェクトでの使用例

`frontend-nextjs/src/lib/types.ts`:

```typescript
// UI用の支出データ型
export interface Expense {
  id: string  // UIではstring型で管理（APIはnumber型）
  amount: number
  category: string
  description: string
  date: string
  createdAt: string  // UI用のメタデータ（APIには存在しない）
}
```

### オプショナルプロパティと読み取り専用プロパティ

```typescript
interface User {
  readonly id: string  // 読み取り専用（変更不可）
  name: string
  age: number
  email?: string  // オプショナル（省略可能）
}

const user1: User = {
  id: "123",
  name: "山田太郎",
  age: 25,
  email: "yamada@example.com"  // emailを指定
}

const user2: User = {
  id: "456",
  name: "佐藤花子",
  age: 30
  // emailを省略してもOK
}

user1.id = "789"  // エラー！readonlyプロパティは変更不可
user1.name = "鈴木一郎"  // OK（readonlyではない）
```

**学習ポイント**: `?`でオプショナルプロパティ、`readonly`で読み取り専用プロパティを定義できる。

---

## 配列とタプル

### 配列型

```typescript
// 配列型の定義（2つの書き方）
const numbers: number[] = [1, 2, 3, 4, 5]
const names: Array<string> = ["山田", "佐藤", "鈴木"]

// 型推論も可能
const numbers = [1, 2, 3, 4, 5]  // number[]型と推論される

// 実際のプロジェクトでの使用例
const expenses: Expense[] = [
  { id: "1", amount: 1000, category: "食費", description: "ランチ代", date: "2024-01-01", createdAt: "2024-01-01T10:00:00Z" }
]
```

### タプル型

タプルは、**固定長**で、**各要素の型が異なる**配列です。

```typescript
// タプル型の定義
const user: [string, number] = ["山田太郎", 25]
const data: [string, number, boolean] = ["山田太郎", 25, true]

// 実際の使用例（ReactのuseState）
const [name, setName] = useState<string>("")  // [string, (value: string) => void]型
```

**学習ポイント**: 配列は同じ型の要素を複数持つ（`number[]`）。タプルは固定長で各要素の型が異なる（`[string, number]`）。

---

## 関数の型定義

### 関数宣言とアロー関数

```typescript
// 関数の型定義
function add(a: number, b: number): number {
  return a + b
}

// アロー関数の型定義
const multiply = (a: number, b: number): number => {
  return a * b
}
```

### 関数型（関数を変数に代入する場合）

```typescript
// 方法1: 型エイリアスを使用（推奨：再利用性・可読性・保守性が高い）
type AddFunction = (a: number, b: number) => number
const add: AddFunction = (a, b) => {
  return a + b
}

// 方法2: 直接型注釈を使用（単発使用に適している）
const add: (a: number, b: number) => number = (a, b) => {
  return a + b
}
```

**推奨**: 複数箇所で使用する場合は型エイリアス、1回だけ使用する場合は直接型注釈を使用。

### オプショナルパラメータとデフォルトパラメータ

```typescript
// オプショナルパラメータ（省略可能）
function greet(name: string, title?: string): string {
  return title ? `${title} ${name}さん` : `${name}さん`
}
greet("山田太郎")              // "山田太郎さん"
greet("山田太郎", "社長")      // "社長 山田太郎さん"

// デフォルトパラメータ
function greet2(name: string, title: string = "さん"): string {
  return `${name}${title}`
}
greet2("山田太郎")              // "山田太郎さん"
greet2("山田太郎", "様")        // "山田太郎様"
```

### 実際のプロジェクトでの使用例

```typescript
// 支出を追加する関数の型定義
function addExpenseItem(data: ExpenseFormData): Promise<void> {
  return createExpense(data)  // 非同期処理（Promiseを返す）
}

// コールバック関数の型定義
type OnSubmitCallback = (data: ExpenseFormData) => void

interface ExpenseFormProps {
  onSubmit: OnSubmitCallback  // コールバック関数の型
}
```

**学習ポイント**: `(パラメータ: 型) => 戻り値の型`の形式。`?`でオプショナルパラメータ、`= 値`でデフォルト値を指定。

---

## Union型とLiteral型

### Union型（`|`）

Union型は、**複数の型のいずれか**を表す型です。

```typescript
// Union型の定義
type StringOrNumber = string | number

const value1: StringOrNumber = "Hello"  // OK
const value2: StringOrNumber = 123     // OK
const value3: StringOrNumber = true     // エラー！boolean型は含まれていない
```

### Literal型

Literal型は、**特定の値のみ**を許可する型です。

```typescript
// Literal型の定義
type Status = "pending" | "completed" | "failed"

const status1: Status = "pending"     // OK
const status2: Status = "completed"   // OK
const status3: Status = "failed"      // OK
const status4: Status = "error"       // エラー！"error"は許可されていない

// 実際のプロジェクトでの使用例
type ExpenseCategory = "食費" | "交通費" | "娯楽" | "その他"
const category: ExpenseCategory = "食費"  // OK
const category2: ExpenseCategory = "雑費"  // エラー！許可されていない
```

**学習ポイント**: Union型は`型1 | 型2`で複数の型のいずれかを許可。Literal型は特定の値のみを許可（`"値1" | "値2"`など）。

---

## 型エイリアスと型ユーティリティ

### 型エイリアス（`type`）

型エイリアスは、**型に別名を付ける**機能です。

```typescript
// 型エイリアスの定義
type UserId = string
type UserName = string
type UserAge = number

// 型エイリアスを使用
const id: UserId = "123"
const name: UserName = "山田太郎"
const age: UserAge = 25
```

### typeとinterfaceの違い

| 項目 | `type`（型エイリアス） | `interface`（インターフェース） |
|------|---------------------|---------------------------|
| **用途** | あらゆる型に別名を付ける | オブジェクトの構造を定義 |
| **拡張** | `&`（交差型）で結合 | `extends`で継承 |
| **マージ** | ❌ 不可 | ✅ 可能（宣言のマージ） |
| **Union型** | ✅ 可能 | ❌ 不可 |
| **計算型** | ✅ 可能（型ユーティリティなど） | ❌ 不可 |

#### 使用例と使い分け

```typescript
// ✅ interface: オブジェクトの構造を定義（推奨）
interface User {
  name: string
  age: number
}

// ✅ type: Union型や計算型
type UserId = string
type Status = "pending" | "completed" | "failed"  // Union型も可能
type UserWithId = User & { id: UserId }  // 交差型で結合
type ExpenseCategory = "食費" | "交通費" | "娯楽"
```

**使い分け**: オブジェクトの構造を定義する場合は`interface`を推奨。Union型や計算型を使う場合は`type`を使用。

### 型ユーティリティ

TypeScriptには、型を操作するための**型ユーティリティ**が用意されています。

| 型ユーティリティ | 説明 | 使用例 |
|----------------|------|--------|
| `Omit<T, K>` | 指定したプロパティを除いた型 | `Omit<User, "id">` |
| `Pick<T, K>` | 指定したプロパティのみを含む型 | `Pick<User, "name" | "age">` |
| `Partial<T>` | すべてのプロパティをオプショナルにする | `Partial<User>` |
| `Required<T>` | すべてのプロパティを必須にする | `Required<Partial<User>>` |
| `Readonly<T>` | すべてのプロパティを読み取り専用にする | `Readonly<User>` |

#### 実際の使用例

```typescript
interface User {
  id: string
  name: string
  age: number
  email?: string
}

// Omit: idを除いた型
type UserWithoutId = Omit<User, "id">
// { name: string; age: number; email?: string }

// Pick: nameとageのみを含む型
type UserBasic = Pick<User, "name" | "age">
// { name: string; age: number }

// Partial: すべてのプロパティをオプショナルにする
type PartialUser = Partial<User>
// { id?: string; name?: string; age?: number; email?: string }

// 実際のプロジェクトでの使用例
export type ExpenseFormData = Omit<Expense, "id" | "createdAt">
```

**学習ポイント**: `type`で型に別名を付ける。型ユーティリティ（`Omit`, `Pick`, `Partial`など）で既存の型から新しい型を作成できる。

---

## ジェネリクス

ジェネリクスは、**型をパラメータ化**する機能です。同じコードで複数の型に対応できます。

### 基本的なジェネリクス

```typescript
// ジェネリクスを使用した関数
function identity<T>(value: T): T {
  return value
}

// 使用例
const number = identity<number>(123)        // number型
const string = identity<string>("Hello")   // string型
const boolean = identity<boolean>(true)     // boolean型

// 実際のプロジェクトでの使用例（ReactのuseState）
const [expenses, setExpenses] = useState<Expense[]>([])
const numbers: Array<number> = [1, 2, 3]
```

### ジェネリクス制約

ジェネリクスに**制約**を付けることができます。

```typescript
// ジェネリクス制約（Tは必ずnameプロパティを持つオブジェクト型）
function getProperty<T extends { name: string }>(obj: T): string {
  return obj.name
}

const user = { name: "山田太郎", age: 25 }
const name = getProperty(user)  // OK（nameプロパティがある）

const data = { id: "123" }
const name2 = getProperty(data)  // エラー！nameプロパティがない
```

**学習ポイント**: `<T>`で型をパラメータ化。`extends`で制約を付ける。

---

## exportとモジュール

**export**は、他のファイルから使用できるようにするための機能です。TypeScriptでは、**モジュールシステム**を使用して、コードを複数のファイルに分割して管理します。

### exportの基本

```typescript
// types.ts - 名前付きエクスポート（複数の値をエクスポート可能、推奨）
export interface User {
  name: string
  age: number
}
export type UserId = string
export function getUserName(user: User): string {
  return user.name
}

// user.ts - デフォルトエクスポート（1つの値のみ）
export default class User {
  name: string
  age: number
  constructor(name: string, age: number) {
    this.name = name
    this.age = age
  }
}
```

### importの基本

```typescript
// 名前付きインポート
import { User, UserId, getUserName } from "./types"

// デフォルトインポート
import User from "./user"

// 名前の変更（as）
import { User as UserType } from "./types"

// 型のみインポート（実行時のコードに含まれない）
import type { Expense, ExpenseFormData } from "@/lib/types"
```

### 実際のプロジェクトでの使用例

`frontend-nextjs/src/lib/types.ts`:

```typescript
export interface Expense {
  id: string
  amount: number
  category: string
  description: string
  date: string
  createdAt: string
}
export type ExpenseFormData = Omit<Expense, "id" | "createdAt">
```

`frontend-nextjs/src/components/expense-form.tsx`:

```typescript
import type { Expense, ExpenseFormData } from "@/lib/types"

interface ExpenseFormProps {
  expense?: Expense
  onSubmit: (data: ExpenseFormData) => void
}
```

### exportの種類

| 種類 | 構文 | 使用例 |
|------|------|--------|
| **名前付きエクスポート** | `export` | `export interface User { ... }` |
| **デフォルトエクスポート** | `export default` | `export default class User { ... }` |
| **再エクスポート** | `export { ... } from` | `export { User } from "./types"` |
| **型のみエクスポート** | `export type` | `export type { User } from "./types"` |

**学習ポイント**: 名前付きエクスポートは複数の値をエクスポート可能（推奨）。`import type`で型のみをインポート（実行時のコードに含まれない）。

---

## オプショナルチェーン

**オプショナルチェーン**（`?.`）は、オブジェクトのプロパティに安全にアクセスするための機能です。`null`や`undefined`の場合でもエラーを発生させずに処理を続行できます。

### 基本的な使い方

```typescript
// 通常のアクセス（エラーが発生する可能性がある）
const name = user.name  // userがnullやundefinedの場合、エラー！

// オプショナルチェーン（安全にアクセス）
const name = user?.name  // userがnullやundefinedの場合、undefinedを返す
```

### 実際のコード例

```typescript
interface User {
  name: string
  address?: {
    city: string
    zipCode: string
  }
}

const user: User | null = null

// ❌ エラー: userがnullの場合、エラーが発生
const city = user.address.city

// ✅ 安全: オプショナルチェーンを使用
const city = user?.address?.city  // undefined（エラーが発生しない）

// デフォルト値を設定（null合体演算子と組み合わせ）
const city = user?.address?.city ?? "不明"  // "不明"
```

### オプショナルチェーンの種類

| 構文 | 説明 | 使用例 |
|------|------|--------|
| `?.` | プロパティアクセス | `user?.name` |
| `?.[]` | 配列アクセス | `array?.[0]` |
| `?.()` | 関数呼び出し | `func?.()` |

### 実際のプロジェクトでの使用例

```typescript
// APIレスポンスの処理
const response = await fetchUser()
const userName = response?.data?.user?.name ?? "ゲスト"

// 配列アクセス
const firstExpense = expenses?.[0]

// 関数呼び出し
const result = callback?.()
```

### null合体演算子（`??`）との組み合わせ

#### 基本的な違い

- **`??`（null合体演算子）**: 左側が`null`または`undefined`の場合のみ、右側の値を返す
- **`||`（論理OR演算子）**: 左側が「falsy」な値の場合、右側の値を返す

#### 「falsy」とは？

JavaScriptでは、以下の値が「falsy」として扱われます：

- `false`
- `0`（数値のゼロ）
- `""`（空文字列）
- `null`
- `undefined`
- `NaN`

#### 実際のコード例

```typescript
// null合体演算子: nullやundefinedの場合にデフォルト値を設定
const name = user?.name ?? "ゲスト"

// ||演算子との違い
const value1 = null ?? "デフォルト"     // "デフォルト"（null合体演算子）
const value2 = null || "デフォルト"     // "デフォルト"（論理OR演算子）
const value3 = 0 ?? "デフォルト"        // 0（null合体演算子: 0は有効な値）
const value4 = 0 || "デフォルト"        // "デフォルト"（論理OR演算子: 0はfalsy）
```

**学習ポイント**: `?.`で安全にプロパティにアクセス。`??`でデフォルト値を設定。`null`や`undefined`の場合でもエラーを発生させない。`??`は`null`/`undefined`のみをチェックし、`||`はすべてのfalsy値をチェックする。

---

## Promise、async、await

**Promise**、**async**、**await**は、非同期処理（時間のかかる処理を「待たずに」次の処理へ進める仕組み）を扱うための機能です。

### Promiseとは

**Promise**は、非同期処理の結果を表すオブジェクトです。処理が完了した時（成功または失敗）に結果を返します。

```typescript
// Promiseの基本構造
const promise = new Promise<number>((resolve, reject) => {
  setTimeout(() => {
    resolve(100)  // 成功時に値を返す
    // または reject(new Error("エラー"))  // 失敗時にエラーを返す
  }, 1000)
})
```

### Promiseの状態

| 状態 | 説明 | 例 |
|------|------|-----|
| **pending** | 処理中 | 処理が完了していない |
| **fulfilled** | 成功 | `resolve(value)`が呼ばれた |
| **rejected** | 失敗 | `reject(error)`が呼ばれた |

### async/awaitの基本

**async**と**await**は、Promiseをより簡単に扱うための構文です。

```typescript
// async関数の定義
async function fetchData(): Promise<string> {
  // await: Promiseの結果を待つ
  const response = await fetch("https://api.example.com/data")
  const data = await response.json()
  return data
}
```

### async/awaitの制御の流れ
**前提**: TypeScriptはシングルスレッドで動作する

**重要なポイント**: `async`関数の呼び出し自体は**同期的**に実行されますが、`await`で待機している間は**制御が呼び出し元に戻り**、他の処理が実行できます。

```typescript
async function fetchData() {
  console.log("1. await前")
  
  const response = await fetch("/api/data")
  // ↑ ここで待機中: 制御が呼び出し元に戻る
  //   非同期処理（ネットワーク通信）が完了するまで待つ
  
  console.log("2. await後")
  // ↑ await完了後: 制御がここに戻り、次の行が実行される
  
  return response.json()
}

console.log("A. 関数呼び出し前")
fetchData()  // 関数は同期的に開始される
console.log("B. 関数呼び出し後")
// ↑ await待機中でも、この行が実行される

// 実行順序:
// A. 関数呼び出し前
// 1. await前
// B. 関数呼び出し後  ← await待機中に実行
// (ネットワーク通信完了後)
// 2. await後  ← 制御が戻って実行
```

**重要な理解**: `await`で呼び出した`async`関数の戻り値のPromiseが**pending**（未解決）の場合、`await`は待機し、**制御が呼び出し元に戻ります**。Promiseが**fulfilled**（解決）または**rejected**（拒否）になったら、制御が`await`の次の行に戻ります。

```typescript
async function main() {
  console.log("1. await前")
  
  const result = await fetchData()
  // ↑ fetchData()はPromiseを返す
  //   そのPromiseがpendingの場合 → awaitは待機し、制御が呼び出し元に戻る
  //   そのPromiseがfulfilledの場合 → 待機せずに次の行に進む
  
  console.log("2. await後")
  // ↑ Promiseがfulfilledになったら、制御がここに戻る
}
```

**制御の流れのまとめ**:
1. `async`関数の呼び出し → 同期的に開始
2. `await`に到達 → 非同期処理を開始し、制御が呼び出し元に戻る
3. 他の処理が実行される（待機中）
4. `await`完了 → 制御が`async`関数内に戻る
5. `await`の次の行が実行される

### Promiseチェーンとasync/awaitの比較

```typescript
// Promiseチェーン（従来の方法）
function fetchExpenses(): Promise<Expense[]> {
  return fetch("/api/expenses")
    .then((response) => response.json())
    .then((data) => data.expenses)
    .catch((error) => {
      console.error("エラー:", error)
      throw error
    })
}

// async/await（推奨：より読みやすい）
async function fetchExpenses(): Promise<Expense[]> {
  try {
    const response = await fetch("/api/expenses")
    const data = await response.json()
    return data.expenses
  } catch (error) {
    console.error("エラー:", error)
    throw error
  }
}
```

### 実際のプロジェクトでの使用例

`frontend-nextjs/src/hooks/use-expenses.ts`:

```typescript
const fetchExpensesList = useCallback(async () => {
  try {
    const expenseList = await fetchExpenses()  // Promiseの結果を待つ
    setExpenses(expenseList)
    setIsLoaded(true)
  } catch (error) {
    showApiErrorMessage(error, "支出データの取得に失敗しました")
    setIsLoaded(true)
  }
}, [])
```

### Promiseの型定義とエラーハンドリング

```typescript
// Promiseの型定義
async function fetchUser(id: string): Promise<User> {
  const response = await fetch(`/api/users/${id}`)
  return response.json()
}

// エラーハンドリング
async function fetchUserSafely(id: string): Promise<User | null> {
  try {
    return await fetchUser(id)
  } catch (error) {
    console.error("エラー:", error)
    return null
  }
}

// 複数のPromiseを並列実行
async function fetchAllData() {
  const [users, expenses, categories] = await Promise.all([
    fetchUsers(),
    fetchExpenses(),
    fetchCategories()
  ])
  return { users, expenses, categories }
}
```

**学習ポイント**: `async`で非同期関数を定義。`await`でPromiseの結果を待つ。`Promise<型>`でPromiseの型を指定。`try-catch`でエラーを処理。

---

## 実際のプロジェクトでの使用例

### 型定義ファイル（`types.ts`）

`frontend-nextjs/src/lib/types.ts`:

```typescript
// インターフェースの定義
export interface Expense {
  id: string
  amount: number
  category: string
  description: string
  date: string
  createdAt: string
}

// 型エイリアスと型ユーティリティ
export type ExpenseFormData = Omit<Expense, "id" | "createdAt">
```

### 関数とReact Hooksでの型定義

```typescript
// 関数の型定義
async function fetchExpenses(): Promise<Expense[]> {
  const response = await api.getExpenses()
  return response.data
}

// コールバック関数の型定義
type OnSubmitCallback = (data: ExpenseFormData) => void

interface ExpenseFormProps {
  onSubmit: OnSubmitCallback
}

// React Hooksでの型定義
const [expenses, setExpenses] = useState<Expense[]>([])

const fetchExpensesList = useCallback(async (): Promise<void> => {
  const expenseList = await fetchExpenses()
  setExpenses(expenseList)
}, [])
```

**解説**: `interface`でデータ構造を定義、`type`で型エイリアスや型ユーティリティを使用。`Promise<型>`で非同期処理の型を指定。React Hooksでは`useState<型>`で状態の型を指定。

---

## まとめ

このプロジェクトで使用されているTypeScriptの要点：

### 基本的な型
- **プリミティブ型**: `string`, `number`, `boolean`
- **オブジェクト型**: `interface`で定義
- **配列型**: `型[]`または`Array<型>`

### 型定義の方法
- **型注釈**: `変数名: 型`で明示的に型を指定
- **型推論**: TypeScriptが自動で型を推測
- **型エイリアス**: `type`で型に別名を付ける

### 高度な型
- **Union型**: `型1 | 型2`で複数の型のいずれかを許可
- **Literal型**: 特定の値のみを許可
- **型ユーティリティ**: `Omit`, `Pick`, `Partial`など
- **ジェネリクス**: `<T>`で型をパラメータ化

### 実際のプロジェクトでの活用
- **型定義ファイル**: `types.ts`で型を一元管理
- **関数の型定義**: パラメータと戻り値の型を明示
- **React Hooks**: `useState<型>`, `useCallback`などで型を指定

これらの機能を組み合わせることで、**型安全**で**保守性の高い**コードを書くことができます。

---

## 学習の進め方

1. **基礎から学ぶ**: 基本的な型（`string`, `number`, `boolean`）を理解する
2. **インターフェース**: オブジェクトの構造を定義する方法を学ぶ
3. **関数の型定義**: 関数のパラメータと戻り値の型を指定する方法を学ぶ
4. **実際のコードを読む**: このプロジェクトのコードを読んで理解を深める
5. **手を動かす**: 小さな機能を追加して実践する

---

## 参考資料

### 公式ドキュメント
- [TypeScript公式ドキュメント](https://www.typescriptlang.org/docs/)
- [TypeScriptハンドブック](https://www.typescriptlang.org/docs/handbook/intro.html)

### 書籍
- [プログラミングTypeScript](https://www.oreilly.co.jp/books/9784873119045/)

### オンラインコース
- [TypeScript公式チュートリアル](https://www.typescriptlang.org/docs/handbook/typescript-in-5-minutes.html)

---

**最終更新日**: 2024年

