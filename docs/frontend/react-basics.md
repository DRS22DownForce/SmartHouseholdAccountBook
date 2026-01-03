# React基礎文法資料

> このドキュメントは、Smart Household Account Bookプロジェクトで使用されているReactの要点を、学習用にまとめたものです。

## 📋 目次

1. [Reactとは](#reactとは)
2. [コンポーネントとJSX](#コンポーネントとjsx)
3. [Props（プロパティ）](#propsプロパティ)
4. [State（状態）とHooks](#state状態とhooks)
5. [イベントハンドリング](#イベントハンドリング)
6. [レンダリング技法](#レンダリング技法)
7. [カスタムフック](#カスタムフック)
8. [実際のプロジェクトでの使用例](#実際のプロジェクトでの使用例)
9. [まとめと学習の進め方](#まとめと学習の進め方)

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

## コンポーネントとJSX

### コンポーネントとは

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

### JSXとは

**JSX**は、JavaScriptの中にHTMLライクな構文を書くための記法です。

#### 基本的なJSX

```typescript
// JSXの例
const element = <h1>こんにちは、React！</h1>

// 変数を埋め込む
const name = "山田太郎"
const element = <h1>こんにちは、{name}さん</h1>

// 式を埋め込む
const element = <h1>1 + 1 = {1 + 1}</h1>  // "1 + 1 = 2"
```

#### JSXのルール

1. **1つのルート要素**: JSXは1つのルート要素で囲む必要がある
2. **閉じタグ**: すべてのタグを閉じる必要がある
3. **className**: `class`ではなく`className`を使用
4. **camelCase**: 属性名はcamelCase（`onClick`, `onChange`など）

##### classNameについて

JSXでは、HTMLの`class`属性の代わりに`className`を使用します。これは、JavaScriptの予約語である`class`と衝突を避けるためです。

```typescript
// ❌ エラー: classは使用できない
<div class="container">コンテンツ</div>

// ✅ 正しい: classNameを使用
<div className="container">コンテンツ</div>
```

**理由**: JavaScriptでは`class`はクラス定義のための予約語のため、JSXでは`className`を使用します。ブラウザでは最終的に`class`属性として変換されます。

##### 複数の要素を返す場合

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

### スプレッド構文（`{...formData}`）

スプレッド構文（`...`）は、オブジェクトや配列を展開するための構文です。Reactでは、Stateを更新する際に既存の値を保持しながら一部だけを変更するために使用します。

```typescript
const formData = { name: "太郎", age: 25, city: "東京" }

// スプレッド構文で既存の値を保持しながら、ageだけを更新
const updated = { ...formData, age: 26 }
// 結果: { name: "太郎", age: 26, city: "東京" }

// 複数のプロパティを更新することも可能
const updated2 = { ...formData, age: 26, city: "大阪" }
// 結果: { name: "太郎", age: 26, city: "大阪" }
```

**重要なポイント**:
- `{...formData}`: `formData`オブジェクトのすべてのプロパティを展開
- 新しいオブジェクトを作成するため、元のオブジェクトは変更されない（不変性）
- Stateを更新する際は、必ず新しいオブジェクトを作成する必要がある

### 実際のプロジェクトでの使用例

`frontend-nextjs/src/components/expense-form.tsx`:

```typescript
export function ExpenseForm({ expense, onSubmit, reactNode }: ExpenseFormProps) {
  const [open, setOpen] = useState(false)
  const [formData, setFormData] = useState<ExpenseFormData>(getInitialFormData())
  
  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        {reactNode || (
          <Button className="gap-2">
            <Plus className="h-4 w-4" />
            支出を追加
          </Button>
        )}
      </DialogTrigger>
      <DialogContent>
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
          />
        </form>
      </DialogContent>
    </Dialog>
  )
}
```

**学習ポイント**:
- **コンポーネント名**: 大文字で始める（`ExpenseForm`など）
- **再利用性**: 同じコンポーネントを複数回使用できる
- **分割**: 大きなコンポーネントを小さなコンポーネントに分割
- **className**: CSSクラスを指定（`class`ではない）
- **スプレッド構文**: 既存のオブジェクトを保持しつつ、一部のプロパティだけを更新

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

### 分割代入について

**分割代入**（Destructuring）は、オブジェクトや配列から値を取り出して、個別の変数に代入する構文です。

```typescript
// オブジェクトの分割代入
const props = { name: "山田太郎", age: 25 }
const { name, age } = props
// これで name = "山田太郎", age = 25 として使用できる

// 関数の引数でも使用可能（Reactでよく使われる）
function Welcome({ name, age }: WelcomeProps) {
  // name と age を直接使用できる
  return <h1>こんにちは、{name}さん</h1>
}

// 配列の分割代入（useStateで使用）
const [count, setCount] = useState(0)
// count: 現在の値、setCount: 更新関数として使用できる
```

**メリット**:
- コードが簡潔になる
- 必要な値だけを取り出せる
- Propsを受け取る際によく使用される

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

### 実際のプロジェクトでの使用例

`frontend-nextjs/src/components/expense-form.tsx` (15-37行目):

```typescript
/**
 * ExpenseFormコンポーネントのプロップス型定義
 */
export interface ExpenseFormProps {
  expense?: Expense
  onSubmit: (data: ExpenseFormData) => void
  reactNode?: React.ReactNode
}

export function ExpenseForm({ expense, onSubmit, reactNode }: ExpenseFormProps) {
  // コンポーネントの実装
}
```

**解説**:
- `export interface ExpenseFormProps`: 型をexportすることで、親コンポーネントでも型を参照できる
- `expense?: Expense`: オプショナルなProps（`?`で省略可能）
- `onSubmit: (data: ExpenseFormData) => void`: 関数型のProps（コールバック関数）
- `reactNode?: React.ReactNode`: ダイアログを開くためのトリガー要素（ボタンなど）。指定されない場合はデフォルトボタンが表示される

**親コンポーネントでの型の使用例**:

```typescript
// Header.tsx
import type { ExpenseFormProps } from "@/components/expense-form"

const addExpenseFormProps: ExpenseFormProps = {
  onSubmit: onAddExpense,
}

<ExpenseForm {...addExpenseFormProps} />
```

**解説**:
- 親コンポーネントで`ExpenseFormProps`型をインポートして使用することで、型安全性が向上する
- 型を明示的に使用することで、コードの可読性と保守性が向上する

**学習ポイント**:
- **Props**: 親から子へデータを渡す
- **型定義**: TypeScriptでPropsの型を定義
- **オプショナル**: `?`で省略可能なPropsを定義
- **分割代入**: Propsを受け取る際によく使用される

---

## State（状態）とHooks

### Stateとは

**State**は、コンポーネント内で管理される**変更可能なデータ**です。Stateが変更されると、コンポーネントが再レンダリングされます。

### Hooksとは

**Hooks**は、関数コンポーネントで状態管理やライフサイクルを扱うための機能です。Hooksは`use`で始まる関数です。

### 主なHooks一覧

| Hook | 説明 | 使用例 |
|------|------|--------|
| `useState` | Stateを管理 | `const [count, setCount] = useState(0)` |
| `useEffect` | 副作用を処理（API呼び出しなど） | `useEffect(() => { ... }, [])` |
| `useCallback` | 関数をメモ化 | `const fn = useCallback(() => { ... }, [])` |
| `useMemo` | 値をメモ化 | `const value = useMemo(() => { ... }, [])` |

### useStateフック

`useState`は、Stateを管理するための**フック**です。

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

#### Stateの更新方法

```typescript
// 直接値を設定
setCount(10)

// 前の値を使用して更新（関数を渡す）
setCount((prev) => prev + 1)

// オブジェクトのStateを更新
const [user, setUser] = useState({ name: "山田", age: 25 })

// ❌ 間違い: 直接変更
user.name = "佐藤"  // 再レンダリングされない

// ✅ 正しい: 新しいオブジェクトを作成
setUser({ ...user, name: "佐藤" })
```

**State更新関数について**:
- State更新関数（`setCount`など）には、**値と関数の両方**を渡せる
- 値を直接渡す: `setCount(10)` → 直接値を設定
- 関数を渡す: `setCount((prev) => prev + 1)` → 前の値を使って計算
- 関数を渡すと、Reactが前の値を引数として関数を実行し、戻り値を新しいStateにする
- 前の値に基づいて更新する場合や、連続更新を正しく処理する場合は関数形式が推奨される

#### 実際のプロジェクトでの使用例

`frontend-nextjs/src/components/expense-form.tsx` (29-30行目):

```typescript
const [open, setOpen] = useState(false)
const [formData, setFormData] = useState<ExpenseFormData>(getInitialFormData())
```

**解説**:
- `useState(false)`: ダイアログの開閉状態を管理
- `useState<ExpenseFormData>(...)`: 型を指定してStateを管理

### useEffectフック

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

#### useEffectの実行タイミング

`useEffect`の実行タイミングは、**依存配列**の内容によって決まります。

| 依存配列 | 実行タイミング | 説明 |
|---------|--------------|------|
| `[]`（空） | マウント時のみ | コンポーネントが初めて表示された時に1回だけ実行。再描画時は実行されない |
| `[count]` | マウント時 + `count`が変更された時 | 初回マウント時と、`count`が変わるたびに実行される |
| なし | 毎回のレンダリング | コンポーネントが再描画されるたびに実行される（パフォーマンスの問題を引き起こす可能性があるため非推奨） |

**重要なポイント**:
- **マウント時**: 依存配列の内容に関係なく、初回マウント時には必ず実行される
- **再描画時**: 依存配列が空`[]`の場合は実行されない。依存配列に値がある場合は、その値が変更された時のみ実行される

#### 実際のプロジェクトでの使用例

`frontend-nextjs/src/hooks/use-expenses.ts` (30-32行目):

```typescript
useEffect(() => {
  fetchExpensesList()
}, [fetchExpensesList])
```

**解説**:
- `fetchExpensesList`が変更された時に実行される
- 初回マウント時にも実行される

### useCallbackフック

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

#### 実際のプロジェクトでの使用例

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

### useMemoフック

`useMemo`は、**値をメモ化**するフックです。計算コストが高い処理の結果をキャッシュして、パフォーマンスを向上させます。

```typescript
import { useState, useMemo } from "react"

function ExpensiveComponent({ items }: { items: number[] }) {
  // useMemoで計算結果をメモ化
  // itemsが変わらない限り、再計算されない
  const sum = useMemo(() => {
    console.log("計算を実行中...")
    return items.reduce((acc, item) => acc + item, 0)
  }, [items])  // itemsが変わった時だけ再計算

  return <p>合計: {sum}</p>
}
```

**解説**:
- `useMemo`: 計算結果をメモ化して、不要な再計算を防ぐ
- 依存配列: 依存する値が変更された時のみ、再計算される

### 再レンダリングのトリガーと最適化

Reactコンポーネントは、以下の場合に再レンダリングされます：

| トリガー | 説明 |
|---------|------|
| コンポーネント自身のState変更 | `useState`で管理しているStateが変更された時 |
| 親から渡されるProps変更 | 親コンポーネントから渡されるPropsが変更された時 |
| 親コンポーネントの再レンダリング | 親が再レンダリングされると、子も再レンダリングされる（Propsが変わっていなくても） |

**重要なポイント**:
- 親コンポーネントが再レンダリングされると、子コンポーネントも再レンダリングされる
- 不要な再レンダリングを防ぐために、`useCallback`や`useMemo`を使用する

### useCallbackとuseMemoによる最適化

`useCallback`と`useMemo`を使用することで、不要な再レンダリングや再計算を防ぐことができます。

```typescript
function Parent() {
  const [count, setCount] = useState(0)
  const [name, setName] = useState("太郎")

  // useCallback: 関数をメモ化
  // 依存配列が空なので、関数は再作成されない
  const handleClick = useCallback(() => {
    console.log("クリックされました")
  }, [])

  // useMemo: 値をメモ化
  // nameが変わらない限り、再計算されない
  const greeting = useMemo(() => {
    return `こんにちは、${name}さん`
  }, [name])

  return (
    <div>
      <p>カウント: {count}</p>
      <p>{greeting}</p>
      <button onClick={() => setCount(count + 1)}>カウント増やす</button>
      <ChildComponent onClick={handleClick} />
    </div>
  )
}

// React.memoでラップすると、Propsが変わっていない場合は再レンダリングされない
const ChildComponent = React.memo(function ChildComponent({ 
  onClick 
}: { 
  onClick: () => void 
}) {
  console.log("ChildComponentが再レンダリングされました")
  return <button onClick={onClick}>子コンポーネントのボタン</button>
})
```

**最適化の効果**:
- `useCallback`: 関数をメモ化することで、子コンポーネントに同じ関数参照を渡せる
- `useMemo`: 計算結果をメモ化することで、不要な再計算を防ぐ
- `React.memo`: Propsが変わっていない場合は、子コンポーネントの再レンダリングを防ぐ。（親コンポーネントが再レンダリングされても再レンダリングされない）

**学習ポイント**:
- **useState**: Stateを管理するフック
- **再レンダリング**: Stateが変更されると、コンポーネントが再レンダリングされる
- **不変性**: Stateは直接変更せず、新しい値で更新する
- **useEffect**: 副作用を処理（API呼び出しなど）
- **useCallback**: 関数をメモ化して再レンダリングを防ぐ
- **useMemo**: 値をメモ化して不要な再計算を防ぐ

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

### フォームのイベントハンドリング

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

`frontend-nextjs/src/components/expense-form.tsx`:

```typescript
// フォーム送信のハンドリング（45-52行目）
const handleSubmit = (e: React.FormEvent) => {
  e.preventDefault()
  onSubmit(formData)
  setOpen(false)
  if (!expense) {
    setFormData(getInitialFormData())
  }
}

// 入力フィールドのハンドリング（78行目）
onChange={(e) => setFormData({ ...formData, amount: Number(e.target.value) })}
```

**詳細解説**:
- `onChange`: 入力欄の値が変わった時に実行されるイベントハンドラー
- `(e) => ...`: アロー関数で、`e`はイベントオブジェクト（入力欄の変更情報を含む）
- `e.target.value`: 入力欄に入力された値（文字列として取得される）
- `Number(e.target.value)`: 文字列を数値に変換（例: `"1000"` → `1000`）
- `{ ...formData, amount: ... }`: スプレッド構文で既存の`formData`をコピーし、`amount`だけを新しい値で上書き
- `setFormData(...)`: ReactのState更新関数で、フォームの状態を更新

**動作の流れ**:
1. ユーザーが入力欄に「1000」と入力
2. `onChange`イベントが発火
3. `e.target.value`で「"1000"」（文字列）を取得
4. `Number()`で`1000`（数値）に変換
5. `{ ...formData, amount: 1000 }`で新しいオブジェクトを作成（既存のデータを保持しつつ、`amount`だけ更新）
6. `setFormData`でStateを更新し、コンポーネントが再レンダリングされる

**なぜスプレッド構文を使うのか？**:
- Stateは直接変更せず、新しいオブジェクトを作成して更新する必要がある（不変性の原則）
- `{ ...formData, amount: ... }`により、`category`、`description`、`date`などの他のフィールドを保持しつつ、`amount`だけを更新できる

**学習ポイント**:
- **イベントハンドラー**: ユーザーの操作に応答する関数
- **型定義**: TypeScriptでイベントの型を指定
- **制御されたコンポーネント**: `value`と`onChange`で入力値を制御
- **型変換**: 入力値は文字列なので、数値として扱う場合は`Number()`で変換
- **スプレッド構文**: 既存のオブジェクトを保持しつつ、一部のプロパティだけを更新

---

## レンダリング技法

### 条件付きレンダリング

**条件付きレンダリング**は、条件に応じて異なるUIを表示する機能です。

#### if文による条件付きレンダリング

```typescript
function Welcome({ isLoggedIn }: { isLoggedIn: boolean }) {
  if (isLoggedIn) {
    return <h1>ようこそ！</h1>
  } else {
    return <h1>ログインしてください</h1>
  }
}
```

#### 三項演算子による条件付きレンダリング

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

#### &&演算子による条件付きレンダリング

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

#### 実際のプロジェクトでの使用例

`frontend-nextjs/app/page.tsx`:

```typescript
if (!isLoaded) {
  return <LoadingSpinner />
}
```

**解説**:
- データが読み込まれていない時は、ローディングスピナーを表示

### リストレンダリング

**リストレンダリング**は、配列のデータをリストとして表示する機能です。

#### mapメソッドによるリストレンダリング

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

#### keyプロパティの重要性

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

#### 実際のプロジェクトでの使用例

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

**学習ポイント**:
- **条件付きレンダリング**: 条件に応じて異なるUIを表示
- **三項演算子**: `条件 ? 真の場合 : 偽の場合`
- **&&演算子**: 条件が`true`の時のみ表示
- **mapメソッド**: 配列をリストに変換
- **keyプロパティ**: 各要素に一意のキーを指定（必須）

---

## カスタムフック

**カスタムフック**は、**Stateや副作用を含むロジックを再利用するための仕組み**です。カスタムフックは`use`で始まる関数です。

### カスタムフックの目的

カスタムフックの主な目的は、**UIから処理を切り離してコンポーネントを読みやすくする**ことです。

**具体的なメリット**:
- **ロジックの再利用**: 複数のコンポーネントで同じロジック（State管理、API呼び出しなど）を共有できる
- **関心の分離**: UIの表示ロジックとデータ処理ロジックを分離し、コンポーネントをシンプルに保つ
- **可読性の向上**: コンポーネントがUIの表示に集中でき、ビジネスロジックが別の場所に整理される

**典型的なパターン**: 状態（State）と更新ロジックをセットで提供する

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
        setExpenses((prev) => [newExpense, ...prev]) // 新しい要素を先頭に置き、その後に既存要素を並べた新しい配列を作成
        toast.success("支出を追加しました")
      } catch (error) {
        showApiErrorMessage(error, "支出の追加に失敗しました")
      }
    },
    []
  )

  const addExpenseItems = useCallback(
    async (dataArray: ExpenseFormData[]) => {
      try {
        const newExpenses = await createExpenses(dataArray)
        setExpenses((prev) => [...newExpenses, ...prev]) // 複数の新しい要素を先頭に追加
        toast.success(`${newExpenses.length}件の支出を追加しました`)
      } catch (error) {
        showApiErrorMessage(error, "支出の一括追加に失敗しました")
      }
    },
    []
  )

  const updateExpenseItem = useCallback(
    async (id: string, data: ExpenseFormData) => {
      try {
        const updatedExpense = await updateExpense(id, data)
        setExpenses((prev) =>
          prev.map((expense) => (expense.id === id ? updatedExpense : expense))
        )
        toast.success("支出を更新しました")
      } catch (error) {
        showApiErrorMessage(error, "支出の更新に失敗しました")
      }
    },
    []
  )

  const deleteExpenseItem = useCallback(
    async (id: string) => {
      try {
        await deleteExpense(id)
        setExpenses((prev) => prev.filter((expense) => expense.id !== id))
        toast.success("支出を削除しました")
      } catch (error) {
        showApiErrorMessage(error, "支出の削除に失敗しました")
      }
    },
    []
  )

  return {
    expenseItems,
    addExpenseItem,
    addExpenseItems,
    updateExpenseItem,
    deleteExpenseItem,
    isLoaded,
  }
}
```

**解説**:
- `useExpenses`: 支出データを管理するカスタムフック
- `expenseItems`: 支出のリスト
- `addExpenseItem`: 支出を1件追加する関数
- `addExpenseItems`: 支出を複数件一括追加する関数（CSVインポートなどで使用）
- `updateExpenseItem`: 支出を更新する関数
- `deleteExpenseItem`: 支出を削除する関数
- `isLoaded`: データが読み込まれたかどうか

**再レンダリング時の動作**:
- `useExpenses()`がコンポーネントで呼ばれると、コンポーネントが再レンダリングされるたびに実行される
- `useState`: 初回のみ初期化され、以降は既存の状態を保持（再初期化されない）
- `useCallback`でラップしていない関数: 再レンダリングのたびに新しい関数が生成される
- `useEffect`: 依存配列の値が変わったときのみ実行される（`fetchExpensesList`が`useCallback`でメモ化されているため、初回のみ実行）

### ページ固有のロジックを管理するカスタムフック

複雑なページでは、ページ固有のロジック（支出操作処理、リフレッシュトリガー管理など）を別のカスタムフックに分離することで、コンポーネントをより読みやすく保つことができます。

#### use-home-page-logic（ホームページ用）

`frontend-nextjs/src/hooks/use-home-page-logic.ts`:

```typescript
export function useHomePageLogic() {
    const { addExpenseItem, addExpenseItems } = useExpenses()
    const [refreshTrigger, setRefreshTrigger] = useState(0)

    const handleAddExpense = useCallback(async (data: ExpenseFormData) => {
        await addExpenseItem(data)
        // 月別サマリーと支出の推移を再取得するためにトリガーを更新
        setRefreshTrigger((prev) => prev + 1)
    }, [addExpenseItem])

    const handleAddExpenses = useCallback(async (dataArray: ExpenseFormData[]) => {
        await addExpenseItems(dataArray)
        // 月別サマリーと支出の推移を再取得するためにトリガーを更新
        setRefreshTrigger((prev) => prev + 1)
    }, [addExpenseItems])

    return {
        refreshTrigger,
        handleAddExpense,
        handleAddExpenses,
    }
}
```

**解説**:
- **目的**: ホームページで使用するビジネスロジック（支出追加処理、リフレッシュトリガー管理）をUIコンポーネントから分離
- **`refreshTrigger`**: 月別サマリーや支出の推移チャートを再取得するためのトリガー（数値が増えると再取得が実行される）
- **`handleAddExpense`**: 支出を追加した後、自動的にチャートを更新するためのハンドラー
- **`handleAddExpenses`**: CSVインポートなどで複数の支出を追加した後、自動的にチャートを更新するためのハンドラー

#### use-expenses-page-logic（支出一覧ページ用）

`frontend-nextjs/src/hooks/use-expenses-page-logic.ts`:

```typescript
export function useExpensesPageLogic() {
    const { addExpenseItem, addExpenseItems, updateExpenseItem, deleteExpenseItem } = useExpenses()
    const [refreshTrigger, setRefreshTrigger] = useState(0)

    const handleAddExpense = useCallback(async (data: ExpenseFormData) => {
        await addExpenseItem(data)
        setRefreshTrigger((prev) => prev + 1)
    }, [addExpenseItem])

    const handleAddExpenses = useCallback(async (dataArray: ExpenseFormData[]) => {
        await addExpenseItems(dataArray)
        setRefreshTrigger((prev) => prev + 1)
    }, [addExpenseItems])

    const handleUpdateExpense = useCallback(async (id: string, data: ExpenseFormData) => {
        await updateExpenseItem(id, data)
        setRefreshTrigger((prev) => prev + 1)
    }, [updateExpenseItem])

    const handleDeleteExpense = useCallback(async (id: string) => {
        await deleteExpenseItem(id)
        setRefreshTrigger((prev) => prev + 1)
    }, [deleteExpenseItem])

    return {
        refreshTrigger,
        handleAddExpense,
        handleAddExpenses,
        handleUpdateExpense,
        handleDeleteExpense,
    }
}
```

**解説**:
- **目的**: 支出一覧ページで使用するビジネスロジック（支出追加・更新・削除処理、リフレッシュトリガー管理）をUIコンポーネントから分離
- **`handleUpdateExpense`**: 支出を更新した後、画面を再取得するためのハンドラー
- **`handleDeleteExpense`**: 支出を削除した後、画面を再取得するためのハンドラー
- ホームページ用と同様に、`refreshTrigger`で画面の再取得を管理

**ページ固有のロジックフックのメリット**:
- **関心の分離**: UIコンポーネントからビジネスロジックを分離し、コンポーネントを読みやすくする
- **再利用性**: 同じページで複数回使用されるロジックを1つのフックにまとめる
- **テスト容易性**: ページ固有のロジックを独立してテストできる

### カスタムフックのメリット

1. **再利用性**: 複数のコンポーネントで同じロジック（State管理、API呼び出し、副作用処理など）を共有できる
2. **関心の分離**: UIロジック（表示）とデータ処理ロジック（State管理、API呼び出しなど）を分離し、コンポーネントを読みやすくする
3. **可読性の向上**: コンポーネントがUIの表示に集中でき、ビジネスロジックが別の場所に整理される
4. **テスト容易性**: フック単体でテストできる
5. **ページ固有のロジックの管理**: 複雑なページでは、ページ固有のロジック（支出操作処理、リフレッシュトリガー管理など）を別のカスタムフックに分離することで、コンポーネントをより読みやすく保つことができる

**学習ポイント**:
- **カスタムフック**: `use`で始まる関数で、Stateや副作用を含むロジックを抽出
- **目的**: UIから処理を切り離してコンポーネントを読みやすくする
- **再利用性**: 複数のコンポーネントで同じロジックを共有できる
- **関心の分離**: UIロジック（表示）とデータ処理ロジック（State管理、API呼び出しなど）を分離
- **ページ固有のロジック**: 複雑なページでは、ページ固有のロジックを別のカスタムフック（例: `use-home-page-logic`、`use-expenses-page-logic`）に分離することで、コンポーネントをより読みやすく保つ

---

## 実際のプロジェクトでの使用例

### 1. コンポーネントの実装

`frontend-nextjs/src/components/expense-form.tsx`:

```typescript
export function ExpenseForm({ expense, onSubmit, reactNode }: ExpenseFormProps) {
  const [open, setOpen] = useState(false)
  const [formData, setFormData] = useState<ExpenseFormData>(getInitialFormData())

  useEffect(() => {
    if (expense) {
      // 編集モード: 既存の支出データをフォームに設定
      setFormData(expenseToFormData(expense))
    } else if (open) {
      // 新規追加モード: ダイアログが開いた時にフォームをリセット
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
      <DialogTrigger asChild>
        {reactNode || (
          <Button className="gap-2">
            <Plus className="h-4 w-4" />
            支出を追加
          </Button>
        )}
      </DialogTrigger>
      <DialogContent>
        {/* フォームの内容 */}
      </DialogContent>
    </Dialog>
  )
}
```

**解説**:
- **Props**: `expense`, `onSubmit`, `reactNode`を受け取る
- **State**: `open`, `formData`を管理
- **useEffect**: `expense`や`open`が変更された時に実行
- **イベントハンドリング**: `handleSubmit`でフォーム送信を処理
- **型のexport**: `ExpenseFormProps`をexportすることで、親コンポーネントでも型を参照できる

### 2. カスタムフックの使用

`frontend-nextjs/app/page.tsx`:

```typescript
export default function HomePage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const { isLoaded } = useExpenses()
  const username = useMemo(() => getUserDisplayName(user), [user])

  // ホームページのロジック（支出追加処理、リフレッシュトリガー管理）をカスタムフックから取得
  const { refreshTrigger, handleAddExpense, handleAddExpenses } = useHomePageLogic()

  if (!isLoaded) {
    return <LoadingSpinner />
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/20">
      <Header
        username={username}
        onLogout={signOut}
        onAddExpense={handleAddExpense}
        onAddExpenses={handleAddExpenses}
      />

      <main className="container mx-auto max-w-7xl px-6 md:px-8 lg:px-12 py-1 md:py-2">
        <div className="space-y-2 md:space-y-2.5">
          <ExpenseTrendChart refreshTrigger={refreshTrigger} />
          <MonthlySummarySection refreshTrigger={refreshTrigger} />
        </div>
      </main>
    </div>
  )
}
```

**解説**:
- **カスタムフック**: `useExpenses()`で支出データの読み込み状態を取得
- **ページ固有のロジック**: `useHomePageLogic()`でホームページ固有のロジック（支出追加処理、リフレッシュトリガー管理）を取得
- **`refreshTrigger`**: 月別サマリーや支出の推移チャートに渡し、データ更新時に自動的に再取得される
- **`handleAddExpense`と`handleAddExpenses`**: Headerコンポーネントに渡し、支出追加時に自動的にチャートが更新される
- **条件付きレンダリング**: `isLoaded`が`false`の時はローディングを表示

### 3. 支出一覧ページでの使用例

`frontend-nextjs/app/expenses/page.tsx`:

```typescript
export default function ExpensesPage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const { expenseItems, isLoaded } = useExpenses()
  const username = useMemo(() => getUserDisplayName(user), [user])

  // 支出一覧ページのロジック（支出操作処理、リフレッシュトリガー管理）をカスタムフックから取得
  const {
    refreshTrigger,
    handleAddExpense,
    handleAddExpenses,
    handleUpdateExpense,
    handleDeleteExpense,
  } = useExpensesPageLogic()

  if (!isLoaded) {
    return <LoadingSpinner />
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/20">
      <Header
        username={username}
        onLogout={signOut}
        onAddExpense={handleAddExpense}
        onAddExpenses={handleAddExpenses}
      />

      <main className="container mx-auto max-w-7xl px-6 md:px-8 lg:px-12 py-8 md:py-12">
        <ExpenseList
          onUpdate={handleUpdateExpense}
          onDelete={handleDeleteExpense}
          refreshTrigger={refreshTrigger}
        />
      </main>
    </div>
  )
}
```

**解説**:
- **ページ固有のロジック**: `useExpensesPageLogic()`で支出一覧ページ固有のロジック（支出追加・更新・削除処理、リフレッシュトリガー管理）を取得
- **`handleUpdateExpense`と`handleDeleteExpense`**: ExpenseListコンポーネントに渡し、支出更新・削除時に自動的に画面が再取得される
- **`refreshTrigger`**: ExpenseListコンポーネントに渡し、データ更新時に自動的に再取得される

---

## まとめと学習の進め方

### まとめ

このプロジェクトで使用されているReactの要点：

#### コンポーネント
- **関数コンポーネント**: 関数として定義（推奨）
- **JSX**: HTMLライクな構文でUIを記述
- **Props**: 親から子へデータを渡す

#### StateとHooks
- **useState**: Stateを管理
- **useEffect**: 副作用を処理（API呼び出しなど）
- **useCallback**: 関数をメモ化
- **useMemo**: 値をメモ化

#### レンダリング
- **条件付きレンダリング**: 条件に応じて異なるUIを表示
- **リストレンダリング**: 配列をリストに変換

#### カスタムフック
- **再利用性**: ロジックを再利用可能なフックに抽出
- **関心の分離**: UIロジックとデータ取得ロジックを分離

これらの機能を組み合わせることで、**保守性**と**拡張性**の高いアプリケーションを構築できます。

### 学習の進め方

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
