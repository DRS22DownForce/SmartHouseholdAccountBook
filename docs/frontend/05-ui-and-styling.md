# 05. UI とスタイル — 見た目と部品ライブラリ

> この章で学ぶこと: **HTML/CSS の最小限**、**Tailwind CSS**、**shadcn-ui / Radix UI**、**コンポーネント構成**、**アイコンとグラフ**、**アクセシビリティの基礎**。

## 目次

1. [HTML と CSS の超要点](#html-と-css-の超要点)
2. [Tailwind CSS とは](#tailwind-css-とは)
3. [よく使うユーティリティクラス](#よく使うユーティリティクラス)
4. [cn ヘルパー](#cn-ヘルパー)
5. [shadcn-ui とは](#shadcn-ui-とは)
6. [Radix UI の役割](#radix-ui-の役割)
7. [このプロジェクトの UI 部品](#このプロジェクトの-ui-部品)
8. [レイアウトコンポーネント](#レイアウトコンポーネント)
9. [アイコンとグラフ](#アイコンとグラフ)
10. [プロジェクトでの実装](#プロジェクトでの実装)

---

## HTML と CSS の超要点

バックエンドエンジニアが画面を読むときの最小セットです。

| 要素 | 用途 |
|------|------|
| `div` | 汎用コンテナ（レイアウトの箱） |
| `button` | クリック操作 |
| `input` / `select` | フォーム入力 |
| `label` | 入力欄の説明（アクセシビリティ） |
| `form` | 入力のまとまり（`onSubmit` で送信） |

CSS では **Flexbox** と **Grid** がよく使われます。Tailwind では `flex`, `grid`, `gap-4` などのクラスで指定します。

---

## Tailwind CSS とは

**ユーティリティファースト**の CSS フレームワークです。あらかじめ小さなクラスが用意され、HTML（JSX）に直接書きます。

```tsx
<div className="flex items-center gap-4 p-6 rounded-lg bg-card">
  <h1 className="text-2xl font-bold">支出一覧</h1>
</div>
```

| 従来の CSS | Tailwind |
|------------|----------|
| `.card { padding: 1.5rem; }` | `className="p-6"` |
| メディアクエリを別ファイルに | `md:flex-row` のようにプレフィックス |

**メリット**: コンポーネントとスタイルが同じファイルにあり、デザインの一貫性を保ちやすい。  
**注意**: クラス名が長くなる → `cn()` で条件付き結合（後述）。

本プロジェクトは **Tailwind CSS v4**（`@tailwindcss/postcss`）を使用しています。エントリは [`app/globals.css`](../../frontend-nextjs/app/globals.css) です。

---

## よく使うユーティリティクラス

| クラス | 意味 |
|--------|------|
| `flex` | Flex コンテナ |
| `flex-col` | 縦並び |
| `items-center` | 交差軸方向の中央 |
| `justify-between` | 主軸方向に両端配置 |
| `gap-4` | 子要素間の隙間 |
| `p-4` / `px-6` / `py-2` | padding |
| `m-4` | margin |
| `w-full` / `h-screen` | 幅・高さ |
| `text-sm` / `text-2xl` | 文字サイズ |
| `font-bold` | 太字 |
| `rounded-lg` | 角丸 |
| `bg-background` / `text-foreground` | テーマ色（CSS 変数連動） |
| `hidden` / `md:block` | 表示・レスポンシブ |

[`AppLayout.tsx`](../../frontend-nextjs/src/components/layout/AppLayout.tsx) の `min-h-screen bg-gradient-to-br ...` は、画面全体の背景グラデーションです。

---

## cn ヘルパー

[`utils.ts`](../../frontend-nextjs/src/lib/utils.ts):

```typescript
import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
```

- `clsx`: 条件に応じてクラスを結合
- `tailwind-merge`: 競合する Tailwind クラスを後勝ちで整理

```tsx
<Button className={cn("w-full", isLoading && "opacity-50")} />
```

---

## shadcn-ui とは

**コピーして自分のリポジトリに置く UI 部品集**です。npm に「shadcn パッケージ」があるのではなく、`src/components/ui/` にソースが入ります。

| 特徴 | 説明 |
|------|------|
| Radix UI ベース | アクセシブルなプリミティブ |
| Tailwind でスタイル | 見た目をプロジェクトで調整可能 |
| 所有権 | コードが手元にあるので改修しやすい |

[`button.tsx`](../../frontend-nextjs/src/components/ui/button.tsx) などは、プロジェクトのデザイントークンに合わせて編集してよい部品です。

---

## Radix UI の役割

ダイアログ・ドロップダウン・セレクトなどは、キーボード操作やフォーカス管理が難しいです。**Radix UI** がその振る舞いを提供し、shadcn が Tailwind で見た目を載せます。

例:

| ファイル | 用途 |
|----------|------|
| `dialog.tsx` | モーダル（支出フォーム） |
| `select.tsx` | カテゴリ選択 |
| `dropdown-menu.tsx` | ユーザーメニュー |
| `alert-dialog.tsx` | 削除確認 |

---

## このプロジェクトの UI 部品

`src/components/ui/` に集約:

- `button`, `input`, `label`, `card`
- `dialog`, `alert-dialog`, `select`, `dropdown-menu`
- `scroll-area`, `pagination`, `progress`, `loading-spinner`

**使い方の型**:

```tsx
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogTrigger } from "@/components/ui/dialog"

<Dialog>
  <DialogTrigger asChild>
    <Button>支出を追加</Button>
  </DialogTrigger>
  <DialogContent>...</DialogContent>
</Dialog>
```

ドメイン固有の UI（支出フォーム・一覧）は `src/components/` 直下に置き、`ui/` は汎用部品だけにします。

---

## レイアウトコンポーネント

| コンポーネント | 役割 |
|----------------|------|
| [`AppLayout.tsx`](../../frontend-nextjs/src/components/layout/AppLayout.tsx) | サイドバー + メイン領域 |
| [`Sidebar.tsx`](../../frontend-nextjs/src/components/layout/Sidebar.tsx) | ナビ・折りたたみ |
| [`Header.tsx`](../../frontend-nextjs/src/components/dashboard/Header.tsx) | ページ見出し |
| [`expense-list.tsx`](../../frontend-nextjs/src/components/expense-list.tsx) | 一覧テーブル |
| [`expense-form.tsx`](../../frontend-nextjs/src/components/expense-form.tsx) | 追加・編集ダイアログ |

`AppLayout` は props で `onAddExpense` などを受け取り、サイドバーの「追加」ボタンと連携します（[第 2 章](./02-react.md) の props パターン）。

---

## アイコンとグラフ

### Lucide React

[`lucide-react`](https://lucide.dev/) から SVG アイコンを import します。

```tsx
import { Sparkles, List } from "lucide-react"
<Sparkles className="h-4 w-4" />
```

### Recharts

[`expense-trend-chart.tsx`](../../frontend-nextjs/src/components/expense-trend-chart.tsx) で月次推移を描画。データ整形は [`chart-data-transformers.ts`](../../frontend-nextjs/src/lib/chart-data-transformers.ts) に分離しています。

グラフライブラリの API を深く覚えるより、**props で渡すデータの形**を追うと理解しやすいです。

### カテゴリ色・アイコン

- [`category-colors.ts`](../../frontend-nextjs/src/lib/category-colors.ts)
- [`category-icons.tsx`](../../frontend-nextjs/src/lib/category-icons.tsx)

---

## プロジェクトでの実装

### フォーム UI

[`expense-form.tsx`](../../frontend-nextjs/src/components/expense-form.tsx):

- `useState` でフォーム値を管理（react-hook-form は package にあるが、このフォームは controlled な state 方式）
- `Select` でカテゴリ
- AI カテゴリ予測ボタン → `predictCategory` API

### トースト（Sonner）

操作結果のフィードバックは hooks 側の `toast.success` / `toast.error` が担当（[第 4 章](./04-api-integration.md)）。

### テーマ

`next-themes` が依存に含まれます。ダークモード対応を広げる場合の拡張ポイントです（現状はライト中心のスタイル）。

---

## この章のまとめ

- スタイルは主に **Tailwind の className**
- ボタンやダイアログは **shadcn-ui（Radix + Tailwind）**
- 汎用は `ui/`、業務は `components/` に分ける
- 見た目より **props とデータの流れ**を先に理解する

次章では **AWS Cognito / Amplify** による認証を解説します。

→ [06. 認証](./06-auth.md)
