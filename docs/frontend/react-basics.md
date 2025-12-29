# React基礎文法資料

> このドキュメントは、Smart Household Account Bookプロジェクトで使用されているReactの要点を、学習用にまとめたものです。

## 📋 目次

1. [Reactとは](#reactとは)
2. [コンポーネント](#コンポーネント)
3. [JSX](#jsx)
4. [Props（プロパティ）](#propsプロパティ)
5. [State（状態）](#state状態)
6. [Hooks（フック）](#hooksフック)
7. [イベントハンドリング](#イベントハンドリング)
8. [条件付きレンダリング](#条件付きレンダリング)
9. [リストレンダリング](#リストレンダリング)
10. [カスタムフック](#カスタムフック)
11. [実際のプロジェクトでの使用例](#実際のプロジェクトでの使用例)

---

## Reactとは

**React**は、ユーザーインターフェース（UI）を構築するための**JavaScriptライブラリ**です。

### Reactの特徴

1. **コンポーネント指向**: UIを再利用可能な部品（コンポーネント）に分割
2. **仮想DOM**: 効率的なDOM更新を実現
3. **宣言的UI**: 「どのように」ではなく「何を」表示するかを記述
4. **単一方向データフロー**: データは親から子へ流れる

### DOMと仮想DOMの理解

#### HTML・DOM・仮想DOMの関係

```
HTML（テキストファイル）
  ↓ ブラウザが読み込む
DOM（ブラウザが作成する構造）
  ↓ ブラウザが描画
画面表示
```

- **HTML**: テキストファイル（設計図）。Webページの構造を記述したファイル
- **DOM**: ブラウザがHTMLを読み込んで作成する構造。JavaScriptで操作可能なオブジェクトの階層構造。ブラウザはDOMを基に画面を描画する
- **DOM操作**: JavaScriptでDOMを操作すると、画面が変わる。要素の追加・削除・変更が可能

#### Reactの仮想DOM

Reactは直接DOMを操作せず、**仮想DOM**（JavaScriptオブジェクト）で変更を管理し、差分検出により必要な部分だけを効率的に更新します。

```
状態が変更
  ↓
仮想DOMを更新
  ↓
前回の仮想DOMと比較（差分検出）
  ↓
変更が必要な部分だけを実際のDOMに反映
  ↓
画面が更新される
```

**メリット**:
- **パフォーマンス向上**: 必要な部分だけを更新するため、高速
- **開発効率向上**: DOM操作を手動で行う必要がない

---

## コンポーネント

**コンポーネント**は、UIの再利用可能な部品です。コンポーネントを組み合わせて、アプリケーション全体を構築します。

### 関数コンポーネント（推奨）

関数コンポーネントは、**関数として定義**するコンポーネントです。現在のReactでは、関数コンポーネントが推奨されています。

```typescript
// 関数コンポーネントの定義
function Welcome() {
  return <h1>こんにちは、React！</h1>
}

// アロー関数でも定義可能
const Welcome = () => {
  return <h1>こんにちは、React！</h1>
}
```

### コンポーネントの使用

```typescript
// コンポーネントを使用
function App() {
  return (
    <div>
      <Welcome />
      <Welcome />
      <Welcome />
    </div>
  )
}
```

**解説**:
- `<Welcome />`: コンポーネントを呼び出す（JSXの構文）
- 同じコンポーネントを複数回使用できる（再利用性）

### 実際のプロジェクトでの使用例

`frontend-nextjs/src/components/expense-form.tsx` (28-133行目):

```typescript
export function ExpenseForm({ expense, onSubmit, trigger }: ExpenseFormProps) {
  // コンポーネントの実装
  return (
    <Dialog open={open} onOpenChange={setOpen}>
      {/* JSXの内容 */}
    </Dialog>
  )
}
```

**学習ポイント**:
- **コンポーネント名**: 大文字で始める（`ExpenseForm`など）
- **再利用性**: 同じコンポーネントを複数回使用できる
- **分割**: 大きなコンポーネントを小さなコンポーネントに分割

---

## JSX

**JSX**は、JavaScriptの中にHTMLライクな構文を書くための記法です。

### 基本的なJSX

```typescript
// JSXの例
const element = <h1>こんにちは、React！</h1>

// 変数を埋め込む
const name = "山田太郎"
const element = <h1>こんにちは、{name}さん</h1>

// 式を埋め込む
const element = <h1>1 + 1 = {1 + 1}</h1>  // "1 + 1 = 2"
```

### JSXのルール

1. **1つのルート要素**: JSXは1つのルート要素で囲む必要がある
2. **閉じタグ**: すべてのタグを閉じる必要がある
3. **className**: `class`ではなく`className`を使用
4. **camelCase**: 属性名はcamelCase（`onClick`, `onChange`など）

```typescript
// ❌ エラー: 複数のルート要素
function Component() {
  return (
    <h1>タイトル</h1>
    <p>本文</p>
  )
}

// ✅ 正しい: 1つのルート要素で囲む
function Component() {
  return (
    <div>
      <h1>タイトル</h1>
      <p>本文</p>
    </div>
  )
}

// ✅ または、Fragmentを使用
function Component() {
  return (
    <>
      <h1>タイトル</h1>
      <p>本文</p>
    </>
  )
}
```

### 実際のプロジェクトでの使用例

```typescript
return (
  <div className="space-y-4">
    <h1 className="text-2xl font-bold">支出フォーム</h1>
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        value={formData.description}
        onChange={(e) => setFormData({ ...formData, description: e.target.value })}
      />
    </form>
  </div>
)
```

**解説**:
- `className`: CSSクラスを指定（`class`ではない）
- `onChange`: イベントハンドラー（後述）
- `{...formData}`: スプレッド構文（オブジェクトの展開）

---

## Props（プロパティ）

**Props**は、親コンポーネントから子コンポーネントにデータを渡すための仕組みです。

### Propsの基本

```typescript
// 子コンポーネント（Propsを受け取る）
interface WelcomeProps {
  name: string
  age: number
}

function Welcome({ name, age }: WelcomeProps) {
  return (
    <div>
      <h1>こんにちは、{name}さん</h1>
      <p>年齢: {age}歳</p>
    </div>
  )
}

// 親コンポーネント（Propsを渡す）
function App() {
  return (
    <div>
      <Welcome name="山田太郎" age={25} />
      <Welcome name="佐藤花子" age={30} />
    </div>
  )
}
```

**解説**:
- `interface WelcomeProps`: Propsの型定義（TypeScript）
- `{ name, age }`: 分割代入でPropsを受け取る
- `<Welcome name="山田太郎" age={25} />`: Propsを渡す

### 実際のプロジェクトでの使用例

`frontend-nextjs/src/components/expense-form.tsx` (15-19行目):

```typescript
interface ExpenseFormProps {
  expense?: Expense
  onSubmit: (data: ExpenseFormData) => void
  trigger?: React.ReactNode
}

export function ExpenseForm({ expense, onSubmit, trigger }: ExpenseFormProps) {
  // コンポーネントの実装
}
```

**解説**:
- `expense?: Expense`: オプショナルなProps（`?`で省略可能）
- `onSubmit: (data: ExpenseFormData) => void`: 関数型のProps（コールバック関数）
- `trigger?: React.ReactNode`: React要素をPropsとして渡す

### デフォルトProps

```typescript
// デフォルト値を設定
function Welcome({ name = "ゲスト", age = 0 }: WelcomeProps) {
  return (
    <div>
      <h1>こんにちは、{name}さん</h1>
      <p>年齢: {age}歳</p>
    </div>
  )
}

// Propsを省略するとデフォルト値が使用される
<Welcome />  // name="ゲスト", age=0
```

**学習ポイント**:
- **Props**: 親から子へデータを渡す
- **型定義**: TypeScriptでPropsの型を定義
- **オプショナル**: `?`で省略可能なPropsを定義

---

## State（状態）

**State**は、コンポーネント内で管理される**変更可能なデータ**です。Stateが変更されると、コンポーネントが再レンダリングされます。

### useStateフック

`useState`は、Stateを管理するための**フック**（後述）です。

```typescript
import { useState } from "react"

function Counter() {
  // useStateの使用
  const [count, setCount] = useState(0)

  return (
    <div>
      <p>カウント: {count}</p>
      <button onClick={() => setCount(count + 1)}>増やす</button>
      <button onClick={() => setCount(count - 1)}>減らす</button>
    </div>
  )
}
```

**解説**:
- `useState(0)`: 初期値を`0`に設定
- `[count, setCount]`: 分割代入で、現在の値と更新関数を取得
- `setCount(count + 1)`: Stateを更新（再レンダリングが発生）

### 実際のプロジェクトでの使用例

`frontend-nextjs/src/components/expense-form.tsx` (29-30行目):

```typescript
const [open, setOpen] = useState(false)
const [formData, setFormData] = useState<ExpenseFormData>(getInitialFormData())
```

**解説**:
- `useState(false)`: ダイアログの開閉状態を管理
- `useState<ExpenseFormData>(...)`: 型を指定してStateを管理

### Stateの更新方法

```typescript
// 直接値を設定
setCount(10)

// 前の値を使用して更新
setCount((prev) => prev + 1)

// オブジェクトのStateを更新
const [user, setUser] = useState({ name: "山田", age: 25 })

// ❌ 間違い: 直接変更
user.name = "佐藤"  // 再レンダリングされない

// ✅ 正しい: 新しいオブジェクトを作成
setUser({ ...user, name: "佐藤" })
```

**学習ポイント**:
- **useState**: Stateを管理するフック
- **再レンダリング**: Stateが変更されると、コンポーネントが再レンダリングされる
- **不変性**: Stateは直接変更せず、新しい値で更新する

---

## Hooks（フック）

**Hooks**は、関数コンポーネントで状態管理やライフサイクルを扱うための機能です。Hooksは`use`で始まる関数です。

### 主なHooks

| Hook | 説明 | 使用例 |
|------|------|--------|
| `useState` | Stateを管理 | `const [count, setCount] = useState(0)` |
| `useEffect` | 副作用を処理（API呼び出しなど） | `useEffect(() => { ... }, [])` |
| `useCallback` | 関数をメモ化 | `const fn = useCallback(() => { ... }, [])` |
| `useMemo` | 値をメモ化 | `const value = useMemo(() => { ... }, [])` |

### useState（既に説明済み）

Stateを管理するフック。

### useEffect

`useEffect`は、**副作用**（API呼び出し、DOM操作など）を処理するためのフックです。

```typescript
import { useState, useEffect } from "react"

function UserList() {
  const [users, setUsers] = useState([])

  // useEffectの使用
  useEffect(() => {
    // コンポーネントがマウントされた時に実行される
    fetchUsers().then((data) => {
      setUsers(data)
    })
  }, [])  // 依存配列が空なので、初回のみ実行

  return (
    <div>
      {users.map((user) => (
        <div key={user.id}>{user.name}</div>
      ))}
    </div>
  )
}
```

**解説**:
- `useEffect(() => { ... }, [])`: 初回のみ実行される
- `useEffect(() => { ... }, [count])`: `count`が変更された時に実行される
- `useEffect(() => { ... })`: 毎回実行される（非推奨）

### 実際のプロジェクトでの使用例

`frontend-nextjs/src/hooks/use-expenses.ts` (30-32行目):

```typescript
useEffect(() => {
  fetchExpensesList()
}, [fetchExpensesList])
```

**解説**:
- `fetchExpensesList`が変更された時に実行される
- 初回マウント時にも実行される

### useCallback

`useCallback`は、**関数をメモ化**するフックです。再レンダリングを防ぐために使用します。

```typescript
import { useState, useCallback } from "react"

function Counter() {
  const [count, setCount] = useState(0)

  // useCallbackで関数をメモ化
  const increment = useCallback(() => {
    setCount((prev) => prev + 1)
  }, [])  // 依存配列が空なので、関数は再作成されない

  return (
    <div>
      <p>カウント: {count}</p>
      <button onClick={increment}>増やす</button>
    </div>
  )
}
```

**解説**:
- `useCallback`: 関数をメモ化して、再レンダリングを防ぐ
- 依存配列: 依存する値が変更された時のみ、関数を再作成

### 実際のプロジェクトでの使用例

`frontend-nextjs/src/hooks/use-expenses.ts` (34-45行目):

```typescript
const addExpenseItem = useCallback(
  async (data: ExpenseFormData) => {
    try {
      const newExpense = await createExpense(data)
      setExpenses((prev) => [newExpense, ...prev])
      toast.success("支出を追加しました")
    } catch (error) {
      showApiErrorMessage(error, "支出の追加に失敗しました")
    }
  },
  []  // 依存配列が空なので、関数は再作成されない
)
```

**学習ポイント**:
- **useState**: Stateを管理
- **useEffect**: 副作用を処理（API呼び出しなど）
- **useCallback**: 関数をメモ化して再レンダリングを防ぐ

---

## イベントハンドリング

**イベントハンドリング**は、ユーザーの操作（クリック、入力など）に応答する処理です。

### 基本的なイベントハンドリング

```typescript
function Button() {
  const handleClick = () => {
    alert("ボタンがクリックされました")
  }

  return <button onClick={handleClick}>クリック</button>
}
```

### 実際のプロジェクトでの使用例

`frontend-nextjs/src/components/expense-form.tsx` (45-52行目):

```typescript
const handleSubmit = (e: React.FormEvent) => {
  e.preventDefault()  // フォームのデフォルト動作を防ぐ
  onSubmit(formData)
  setOpen(false)
  if (!expense) {
    setFormData(getInitialFormData())
  }
}
```

**解説**:
- `e: React.FormEvent`: イベントオブジェクトの型
- `e.preventDefault()`: フォームのデフォルト動作（ページリロード）を防ぐ
- `onSubmit(formData)`: 親コンポーネントにデータを渡す

### 入力フィールドのイベントハンドリング

```typescript
function InputField() {
  const [value, setValue] = useState("")

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setValue(e.target.value)
  }

  return (
    <input
      type="text"
      value={value}
      onChange={handleChange}
    />
  )
}
```

**解説**:
- `e: React.ChangeEvent<HTMLInputElement>`: 入力イベントの型
- `e.target.value`: 入力された値
- `value={value}`: 制御されたコンポーネント（Controlled Component）

### 実際のプロジェクトでの使用例

`frontend-nextjs/src/components/expense-form.tsx` (78行目):

```typescript
onChange={(e) => setFormData({ ...formData, amount: Number(e.target.value) })}
```

**解説**:
- インラインでイベントハンドラーを定義
- `{ ...formData, amount: ... }`: スプレッド構文でオブジェクトを更新

**学習ポイント**:
- **イベントハンドラー**: ユーザーの操作に応答する関数
- **型定義**: TypeScriptでイベントの型を指定
- **制御されたコンポーネント**: `value`と`onChange`で入力値を制御

---

## 条件付きレンダリング

**条件付きレンダリング**は、条件に応じて異なるUIを表示する機能です。

### if文による条件付きレンダリング

```typescript
function Welcome({ isLoggedIn }: { isLoggedIn: boolean }) {
  if (isLoggedIn) {
    return <h1>ようこそ！</h1>
  } else {
    return <h1>ログインしてください</h1>
  }
}
```

### 三項演算子による条件付きレンダリング

```typescript
function Welcome({ isLoggedIn }: { isLoggedIn: boolean }) {
  return (
    <div>
      {isLoggedIn ? (
        <h1>ようこそ！</h1>
      ) : (
        <h1>ログインしてください</h1>
      )}
    </div>
  )
}
```

### &&演算子による条件付きレンダリング

```typescript
function Welcome({ count }: { count: number }) {
  return (
    <div>
      {count > 0 && <p>カウント: {count}</p>}
    </div>
  )
}
```

**解説**:
- `count > 0 && <p>...</p>`: `count > 0`が`true`の時のみ`<p>`を表示
- `false`の時は何も表示されない

### 実際のプロジェクトでの使用例

`frontend-nextjs/app/page.tsx` (135-137行目):

```typescript
if (!isLoaded) {
  return <LoadingSpinner />
}
```

**解説**:
- データが読み込まれていない時は、ローディングスピナーを表示

**学習ポイント**:
- **条件付きレンダリング**: 条件に応じて異なるUIを表示
- **三項演算子**: `条件 ? 真の場合 : 偽の場合`
- **&&演算子**: 条件が`true`の時のみ表示

---

## リストレンダリング

**リストレンダリング**は、配列のデータをリストとして表示する機能です。

### mapメソッドによるリストレンダリング

```typescript
function UserList() {
  const users = [
    { id: 1, name: "山田太郎" },
    { id: 2, name: "佐藤花子" },
    { id: 3, name: "鈴木一郎" }
  ]

  return (
    <ul>
      {users.map((user) => (
        <li key={user.id}>{user.name}</li>
      ))}
    </ul>
  )
}
```

**解説**:
- `users.map(...)`: 配列の各要素を変換
- `key={user.id}`: 各要素に一意のキーを指定（必須）

### 実際のプロジェクトでの使用例

`frontend-nextjs/src/components/expense-form.tsx` (94-98行目):

```typescript
{EXPENSE_CATEGORIES.map((category) => (
  <SelectItem key={category} value={category}>
    {category}
  </SelectItem>
))}
```

**解説**:
- `EXPENSE_CATEGORIES`配列を`map`で変換
- `key={category}`: カテゴリー名をキーとして使用

### keyプロパティの重要性

`key`プロパティは、Reactが要素を識別するために使用されます。**一意の値**を指定する必要があります。

```typescript
// ❌ 間違い: インデックスをキーとして使用（非推奨）
{users.map((user, index) => (
  <li key={index}>{user.name}</li>
))}

// ✅ 正しい: 一意のIDをキーとして使用
{users.map((user) => (
  <li key={user.id}>{user.name}</li>
))}
```

**学習ポイント**:
- **mapメソッド**: 配列をリストに変換
- **keyプロパティ**: 各要素に一意のキーを指定（必須）

---

## カスタムフック

**カスタムフック**は、ロジックを再利用可能なフックに抽出する機能です。カスタムフックは`use`で始まる関数です。

### カスタムフックの定義

```typescript
// カスタムフックの定義
function useCounter(initialValue: number = 0) {
  const [count, setCount] = useState(initialValue)

  const increment = () => setCount((prev) => prev + 1)
  const decrement = () => setCount((prev) => prev - 1)
  const reset = () => setCount(initialValue)

  return { count, increment, decrement, reset }
}

// カスタムフックの使用
function Counter() {
  const { count, increment, decrement, reset } = useCounter(0)

  return (
    <div>
      <p>カウント: {count}</p>
      <button onClick={increment}>増やす</button>
      <button onClick={decrement}>減らす</button>
      <button onClick={reset}>リセット</button>
    </div>
  )
}
```

### 実際のプロジェクトでの使用例

`frontend-nextjs/src/hooks/use-expenses.ts` (15-96行目):

```typescript
export function useExpenses() {
  const [expenseItems, setExpenses] = useState<Expense[]>([])
  const [isLoaded, setIsLoaded] = useState(false)

  const fetchExpensesList = useCallback(async () => {
    try {
      const expenseList = await fetchExpenses()
      setExpenses(expenseList)
      setIsLoaded(true)
    } catch (error) {
      showApiErrorMessage(error, "支出データの取得に失敗しました")
      setIsLoaded(true)
    }
  }, [])

  useEffect(() => {
    fetchExpensesList()
  }, [fetchExpensesList])

  const addExpenseItem = useCallback(
    async (data: ExpenseFormData) => {
      try {
        const newExpense = await createExpense(data)
        setExpenses((prev) => [newExpense, ...prev])
        toast.success("支出を追加しました")
      } catch (error) {
        showApiErrorMessage(error, "支出の追加に失敗しました")
      }
    },
    []
  )

  return {
    expenseItems,
    addExpenseItem,
    updateExpenseItem,
    deleteExpenseItem,
    isLoaded,
  }
}
```

**解説**:
- `useExpenses`: 支出データを管理するカスタムフック
- `expenseItems`: 支出のリスト
- `addExpenseItem`: 支出を追加する関数
- `isLoaded`: データが読み込まれたかどうか

### カスタムフックのメリット

1. **再利用性**: 複数のコンポーネントで同じロジックを共有できる
2. **関心の分離**: UIロジックとデータ取得ロジックを分離
3. **テスト容易性**: フック単体でテストできる

**学習ポイント**:
- **カスタムフック**: `use`で始まる関数でロジックを抽出
- **再利用性**: 複数のコンポーネントで同じロジックを共有
- **関心の分離**: UIロジックとデータ取得ロジックを分離

---

## 実際のプロジェクトでの使用例

### 1. コンポーネントの実装

`frontend-nextjs/src/components/expense-form.tsx`:

```typescript
export function ExpenseForm({ expense, onSubmit, trigger }: ExpenseFormProps) {
  const [open, setOpen] = useState(false)
  const [formData, setFormData] = useState<ExpenseFormData>(getInitialFormData())

  useEffect(() => {
    if (expense) {
      setFormData({
        amount: expense.amount,
        category: expense.category,
        description: expense.description,
        date: expense.date,
      })
    } else if (!open) {
      setFormData(getInitialFormData())
    }
  }, [expense, open])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSubmit(formData)
    setOpen(false)
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      {/* JSXの内容 */}
    </Dialog>
  )
}
```

**解説**:
- **Props**: `expense`, `onSubmit`, `trigger`を受け取る
- **State**: `open`, `formData`を管理
- **useEffect**: `expense`や`open`が変更された時に実行
- **イベントハンドリング**: `handleSubmit`でフォーム送信を処理

### 2. カスタムフックの使用

`frontend-nextjs/app/page.tsx`:

```typescript
export default function HomePage() {
  const { expenseItems, addExpenseItem, isLoaded } = useExpenses()
  const [refreshTrigger, setRefreshTrigger] = useState(0)

  const handleAddExpense = useCallback(async (data: ExpenseFormData) => {
    await addExpenseItem(data)
    setRefreshTrigger(prev => prev + 1)
  }, [addExpenseItem])

  if (!isLoaded) {
    return <LoadingSpinner />
  }

  return (
    <div>
      <Header onAddExpense={handleAddExpense} />
      {/* 他のコンポーネント */}
    </div>
  )
}
```

**解説**:
- **カスタムフック**: `useExpenses()`で支出データを取得
- **useState**: `refreshTrigger`で再取得をトリガー
- **useCallback**: `handleAddExpense`をメモ化
- **条件付きレンダリング**: `isLoaded`が`false`の時はローディングを表示

---

## まとめ

このプロジェクトで使用されているReactの要点：

### コンポーネント
- **関数コンポーネント**: 関数として定義（推奨）
- **JSX**: HTMLライクな構文でUIを記述
- **Props**: 親から子へデータを渡す

### StateとHooks
- **useState**: Stateを管理
- **useEffect**: 副作用を処理（API呼び出しなど）
- **useCallback**: 関数をメモ化

### レンダリング
- **条件付きレンダリング**: 条件に応じて異なるUIを表示
- **リストレンダリング**: 配列をリストに変換

### カスタムフック
- **再利用性**: ロジックを再利用可能なフックに抽出
- **関心の分離**: UIロジックとデータ取得ロジックを分離

これらの機能を組み合わせることで、**保守性**と**拡張性**の高いアプリケーションを構築できます。

---

## 学習の進め方

1. **基礎から学ぶ**: コンポーネント、Props、Stateの基礎を理解する
2. **Hooks**: useState、useEffectなどのHooksを学ぶ
3. **実際のコードを読む**: このプロジェクトのコードを読んで理解を深める
4. **手を動かす**: 小さな機能を追加して実践する
5. **カスタムフック**: ロジックを再利用可能なフックに抽出する方法を学ぶ

---

## 参考資料

### 公式ドキュメント
- [React公式ドキュメント](https://react.dev/)
- [React Hooks公式ドキュメント](https://react.dev/reference/react)

### 書籍
- [React実践の教科書](https://www.shoeisha.co.jp/book/detail/9784798162439)

### オンラインコース
- [React公式チュートリアル](https://react.dev/learn)

---

**最終更新日**: 2024年

