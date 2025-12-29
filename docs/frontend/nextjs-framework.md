# Next.jsフレームワーク詳細資料

> このドキュメントは、Smart Household Account Bookプロジェクトで使用されているNext.jsフレームワーク関連の技術について、学習用にまとめたものです。

## 📋 目次

1. [Next.js 15.5.4](#nextjs-1554)
2. [App Router](#app-router)
3. [Server ComponentsとClient Components](#server-componentsとclient-components)
4. [ルーティング](#ルーティング)
5. [メタデータ](#メタデータ)
6. [フォント最適化](#フォント最適化)

---

## Next.js 15.5.4

**役割**: Reactアプリケーション開発のためのフルスタックフレームワーク。サーバーサイドレンダリング（SSR）、静的サイト生成（SSG）、APIルートなどの機能を提供します。

**主な機能**:
- **App Router**: ファイルベースのルーティングシステム（Next.js 13以降）
- **Server Components**: サーバー側でレンダリングされるコンポーネント（デフォルト）
- **Client Components**: ブラウザ側でレンダリングされるコンポーネント（`"use client"`が必要）
- **API Routes**: サーバーサイドのAPIエンドポイントを作成可能
- **画像最適化**: 自動的な画像最適化
- **フォント最適化**: Google Fontsなどのフォントを最適化

**実際のコード例**:

`frontend-nextjs/app/layout.tsx` (1-33行目):

```typescript
import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/contexts/auth-provider";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "スマート家計簿アプリ",
  description: "AWS Cognitoで保護された家計簿管理アプリケーション",
};

interface RootLayoutProps {
  children: React.ReactNode
}

export default function RootLayout({ children }: Readonly<RootLayoutProps>) {
  return (
    <html lang="ja">
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  )
}
```

**解説**:
- `export const metadata`: Next.jsのメタデータ（SEO用）
- `Geist`, `Geist_Mono`: Google Fontsからフォントを読み込む
- `AuthProvider`: 認証状態を提供するプロバイダーコンポーネント
- **Server Component**: `"use client"`がないため、サーバー側でレンダリングされる

---

## App Router

**役割**: Next.js 13以降で導入された新しいルーティングシステム。ファイルベースのルーティングを提供します。

**主な特徴**:
- **ファイルベースルーティング**: `app/`ディレクトリ内のファイル構造がルートになる
- **レイアウト**: `layout.tsx`でページ全体のレイアウトを定義
- **ページ**: `page.tsx`でページコンポーネントを定義
- **ローディング**: `loading.tsx`でローディング状態を定義
- **エラー**: `error.tsx`でエラー状態を定義

**ディレクトリ構造**:

```
app/
├── layout.tsx          # ルートレイアウト（全ページ共通）
├── page.tsx            # ホームページ（/）
├── expenses/
│   └── page.tsx        # 支出一覧ページ（/expenses）
└── login/
    └── page.tsx        # ログインページ（/login）
```

**実際のコード例（ページコンポーネント）**:

`frontend-nextjs/app/page.tsx` (1-76行目):

```typescript
"use client"

/**
 * ホームページコンポーネント
 * 
 * 支出追加後に月別サマリーと支出の推移を自動的に再取得します。
 */

import { useState, useMemo, memo, useCallback } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { useExpenses } from "@/hooks/use-expenses"
import { Header } from "@/components/dashboard/Header"
import { ExpenseTrendChart } from "@/components/expense-trend-chart"
import { MonthlySummarySection } from "@/components/dashboard/MonthlySummarySection"
import type { ExpenseFormData } from "@/lib/types"

export default function HomePage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const { expenseItems, addExpenseItem, addExpenses, isLoaded } = useExpenses()
  const username = useMemo(() => getUserDisplayName(user), [user])

  // 支出追加後に月別サマリーと支出の推移を再取得するためのトリガー
  const [refreshTrigger, setRefreshTrigger] = useState(0)

  // 支出追加後にrefetchを呼び出すラッパー関数
  const handleAddExpense = useCallback(async (data: ExpenseFormData) => {
    await addExpenseItem(data)
    // 月別サマリーと支出の推移を再取得するためにトリガーを更新
    setRefreshTrigger(prev => prev + 1)
  }, [addExpenseItem])

  if (!isLoaded) {
    return <LoadingSpinner />
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/20">
      <MemoizedHeader
        expenses={expenseItems}
        username={username}
        onLogout={signOut}
        onAddExpense={handleAddExpense}
      />

      <main className="container mx-auto max-w-7xl px-6 md:px-8 lg:px-12 py-1 md:py-2">
        <div className="space-y-2 md:space-y-2.5">
          <ExpenseTrendChart refreshTrigger={refreshTrigger} key={`trend-${refreshTrigger}`} />
          <MonthlySummarySection
            refreshTrigger={refreshTrigger} key={`summary-${refreshTrigger}`} />
        </div>
      </main>
    </div>
  )
}
```

**解説**:
- `"use client"`: このコンポーネントはClient Component（ブラウザで実行）
- `useState`, `useCallback`: React Hooksを使用（Client Componentでのみ使用可能）
- `useAuthenticator`: AWS Amplifyの認証フック（Client Componentでのみ使用可能）

---

## Server ComponentsとClient Components

**役割**: Next.js 13以降では、コンポーネントはデフォルトでServer Componentです。インタラクティブな機能が必要な場合のみ、Client Componentとして明示的に指定します。

### Server Components（デフォルト）

**特徴**:
- **サーバー側でレンダリング**: サーバー側でHTMLを生成してクライアントに送信
- **パフォーマンス**: クライアント側のJavaScriptバンドルサイズを削減
- **データベースアクセス**: サーバー側で直接データベースにアクセス可能
- **制限**: React Hooks（`useState`, `useEffect`など）は使用不可
- **制限**: イベントハンドラー（`onClick`など）は使用不可

**使用例**:

```typescript
// Server Component（"use client"がない）
import { Metadata } from "next"

export const metadata: Metadata = {
  title: "ページタイトル",
}

export default function ServerComponent() {
  // サーバー側で実行される
  const data = await fetchData()
  
  return <div>{data}</div>
}
```

### Client Components

**特徴**:
- **ブラウザ側でレンダリング**: ブラウザ側でJavaScriptを実行
- **インタラクティブ**: イベントハンドラーやReact Hooksを使用可能
- **状態管理**: `useState`, `useEffect`などで状態を管理可能
- **制限**: サーバー側のリソース（データベースなど）に直接アクセス不可

**使用例**:

```typescript
"use client"  // ← このディレクティブが必要

import { useState } from "react"

export default function ClientComponent() {
  // ブラウザ側で実行される
  const [count, setCount] = useState(0)
  
  return (
    <button onClick={() => setCount(count + 1)}>
      クリック: {count}
    </button>
  )
}
```

### 使い分けの判断基準

| 機能 | Server Component | Client Component |
|------|----------------|-----------------|
| **データ取得** | ✅ 推奨（サーバー側で直接取得） | ❌ クライアント側でfetch |
| **状態管理** | ❌ 不可 | ✅ 可能（`useState`など） |
| **イベントハンドラー** | ❌ 不可 | ✅ 可能（`onClick`など） |
| **React Hooks** | ❌ 不可 | ✅ 可能 |
| **ブラウザAPI** | ❌ 不可（`window`, `localStorage`など） | ✅ 可能 |
| **パフォーマンス** | ✅ 優れている（バンドルサイズが小さい） | ⚠️ バンドルサイズが大きくなる |

**実際のコード例（Server Component）**:

`frontend-nextjs/app/layout.tsx` (25-33行目):

```typescript
export default function RootLayout({ children }: Readonly<RootLayoutProps>) {
  return (
    <html lang="ja">
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  )
}
```

**解説**:
- `"use client"`がないため、Server Component
- メタデータやフォント設定など、静的な設定に適している

**実際のコード例（Client Component）**:

`frontend-nextjs/app/page.tsx` (32-76行目):

```typescript
"use client"  // ← Client Componentとして明示

export default function HomePage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const { expenseItems, addExpenseItem, isLoaded } = useExpenses()
  
  const [refreshTrigger, setRefreshTrigger] = useState(0)
  
  const handleAddExpense = useCallback(async (data: ExpenseFormData) => {
    await addExpenseItem(data)
    setRefreshTrigger(prev => prev + 1)
  }, [addExpenseItem])

  return (
    // ...
  )
}
```

**解説**:
- `"use client"`があるため、Client Component
- `useState`, `useCallback`, `useAuthenticator`などのHooksを使用しているため、Client Componentが必要

---

## ルーティング

**役割**: App Routerでは、`app/`ディレクトリ内のファイル構造がルートになります。

**ルーティング規則**:

| ファイルパス | URL | 説明 |
|------------|-----|------|
| `app/page.tsx` | `/` | ホームページ |
| `app/expenses/page.tsx` | `/expenses` | 支出一覧ページ |
| `app/login/page.tsx` | `/login` | ログインページ |
| `app/layout.tsx` | - | ルートレイアウト（全ページ共通） |

**実際のコード例（ルーティング）**:

`frontend-nextjs/app/expenses/page.tsx` (28-89行目):

```typescript
"use client"

export default function ExpensesPage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const { expenseItems, addExpenseItem, updateExpenseItem, deleteExpenseItem, isLoaded } =
    useExpenses()

  // ...

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/20">
      <Header
        expenses={expenseItems}
        username={username}
        onLogout={signOut}
        onAddExpense={handleAddExpense}
      />

      <main className="container mx-auto max-w-7xl px-6 md:px-8 lg:px-12 py-8 md:py-12">
        <div className="mb-8">
          <h1 className="text-3xl md:text-4xl font-bold text-foreground tracking-tight mb-2">
            支出一覧
          </h1>
          <p className="text-muted-foreground">全ての支出を確認し、編集・削除できます</p>
        </div>

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
- `app/expenses/page.tsx`が`/expenses`ルートに対応
- Client Componentとして実装（`"use client"`が必要）

---

## メタデータ

**役割**: SEOやソーシャルメディア共有のために、ページのメタデータを定義します。

**実際のコード例**:

`frontend-nextjs/app/layout.tsx` (16-19行目):

```typescript
export const metadata: Metadata = {
  title: "スマート家計簿アプリ",
  description: "AWS Cognitoで保護された家計簿管理アプリケーション",
};
```

**主なメタデータプロパティ**:

| プロパティ | 説明 | 使用例 |
|----------|------|--------|
| `title` | ページタイトル | `title: "ページタイトル"` |
| `description` | ページの説明 | `description: "ページの説明"` |
| `keywords` | キーワード | `keywords: ["キーワード1", "キーワード2"]` |
| `openGraph` | Open Graphメタデータ（SNS共有用） | `openGraph: { title: "...", description: "..." }` |
| `twitter` | Twitterカードメタデータ | `twitter: { card: "summary" }` |

**学習ポイント**:
- **SEO**: 検索エンジン最適化のためにメタデータを設定
- **ソーシャルメディア**: SNS共有時の表示を制御
- **Server Component**: メタデータはServer Componentでのみ使用可能

---

## フォント最適化

**役割**: Next.jsはGoogle Fontsなどのフォントを自動的に最適化します。

**実際のコード例**:

`frontend-nextjs/app/layout.tsx` (6-14行目):

```typescript
const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});
```

**解説**:
- `Geist`, `Geist_Mono`: Google Fontsからフォントを読み込む
- `variable`: CSS変数名を指定（`--font-geist-sans`など）
- `subsets`: 読み込む文字セットを指定（`"latin"`はラテン文字）

**使用例**:

```typescript
<body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
```

**CSS変数での使用**:

```css
.font-geist-sans {
  font-family: var(--font-geist-sans);
}

.font-geist-mono {
  font-family: var(--font-geist-mono);
}
```

**学習ポイント**:
- **パフォーマンス**: フォントを自動的に最適化して読み込み速度を向上
- **CSS変数**: フォントをCSS変数として使用可能
- **サブセット**: 必要な文字セットのみを読み込んでサイズを削減

---

## まとめ

このプロジェクトのNext.jsフレームワークは、以下の機能で構成されています：

### コア機能
- **App Router**: ファイルベースのルーティングシステム
- **Server Components**: サーバー側でレンダリングされるコンポーネント（デフォルト）
- **Client Components**: ブラウザ側でレンダリングされるコンポーネント（`"use client"`が必要）

### 最適化機能
- **フォント最適化**: Google Fontsなどのフォントを自動的に最適化
- **画像最適化**: 自動的な画像最適化（将来的に使用予定）

### 開発体験
- **型安全性**: TypeScriptで型安全な開発
- **ホットリロード**: 開発時の自動リロード

これらの機能を組み合わせることで、パフォーマンスと開発体験の両立を実現しています。

---

## 学習の進め方

1. **基礎から学ぶ**: App Router、Server Components、Client Componentsの基礎を理解する
2. **実際のコードを読む**: このプロジェクトのコードを読んで理解を深める
3. **手を動かす**: 小さな機能を追加して実践する
4. **ドキュメントを読む**: 公式ドキュメントで詳細を確認する

---

## 参考資料

### 公式ドキュメント
- [Next.js公式ドキュメント](https://nextjs.org/docs)
- [App Router公式ドキュメント](https://nextjs.org/docs/app)
- [Server Components公式ドキュメント](https://nextjs.org/docs/app/building-your-application/rendering/server-components)

### 書籍
- [Next.js実践ガイド](https://www.shoeisha.co.jp/book/detail/9784798172025)

### オンラインコース
- [Next.js公式チュートリアル](https://nextjs.org/learn)

---

**最終更新日**: 2024年

