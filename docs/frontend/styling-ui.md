# スタイリング・UIライブラリ詳細資料

> このドキュメントは、Smart Household Account Bookプロジェクトで使用されているスタイリングとUIライブラリについて、学習用にまとめたものです。

## 📋 目次

1. [Tailwind CSS 4.1.9](#tailwind-css-419)
2. [shadcn/ui](#shadcnui)
3. [Radix UI](#radix-ui)
4. [class-variance-authority](#class-variance-authority)
5. [clsxとtailwind-merge](#clsxとtailwind-merge)
6. [next-themes（ダークモード）](#next-themesダークモード)

---

## Tailwind CSS 4.1.9

**役割**: ユーティリティファーストのCSSフレームワーク。クラス名でスタイルを適用します。

**このプロジェクトでの使用箇所**:
- 全コンポーネントのスタイリング
- `app/globals.css`: グローバルスタイルとテーマ設定

**主な特徴**:
- **ユーティリティクラス**: `px-4`, `py-2`, `bg-primary`などのクラスでスタイルを適用
- **レスポンシブ**: `md:`, `lg:`などのプレフィックスでレスポンシブデザイン
- **ダークモード**: `dark:`プレフィックスでダークモード対応
- **カスタマイズ可能**: `globals.css`でテーマをカスタマイズ

**実際のコード例（グローバルスタイル）**:

`frontend-nextjs/app/globals.css` (1-3行目):

```css
@import "tailwindcss";
@import "tw-animate-css";

@custom-variant dark (&:is(.dark *));
```

**解説**:
- `@import "tailwindcss"`: Tailwind CSSをインポート
- `@import "tw-animate-css"`: アニメーション用のユーティリティをインポート
- `@custom-variant dark`: ダークモード用のカスタムバリアントを定義

**実際のコード例（テーマ設定）**:

`frontend-nextjs/app/globals.css` (46-79行目):

```css
:root {
  --radius: 0.625rem;
  --background: oklch(1 0 0);
  --foreground: oklch(0.145 0 0);
  --card: oklch(1 0 0);
  --card-foreground: oklch(0.145 0 0);
  --primary: oklch(0.205 0 0);
  --primary-foreground: oklch(0.985 0 0);
  --secondary: oklch(0.97 0 0);
  --secondary-foreground: oklch(0.205 0 0);
  --muted: oklch(0.97 0 0);
  --muted-foreground: oklch(0.556 0 0);
  --accent: oklch(0.97 0 0);
  --accent-foreground: oklch(0.205 0 0);
  --destructive: oklch(0.577 0.245 27.325);
  --border: oklch(0.922 0 0);
  --input: oklch(0.922 0 0);
  --ring: oklch(0.708 0 0);
}
```

**解説**:
- **CSS変数**: `--background`, `--foreground`などでテーマカラーを定義
- **oklch**: 色空間（色相、彩度、明度）で色を定義
- **ダークモード**: `.dark`クラスでダークモード用の色を定義

**実際のコード例（コンポーネントでの使用）**:

```typescript
<div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/20">
  <main className="container mx-auto max-w-7xl px-6 md:px-8 lg:px-12 py-1 md:py-2">
    <div className="space-y-2 md:space-y-2.5">
      {/* コンテンツ */}
    </div>
  </main>
</div>
```

**解説**:
- `min-h-screen`: 最小高さを画面の高さに設定
- `bg-gradient-to-br`: 背景を右下へのグラデーションに設定
- `container mx-auto`: コンテナを中央揃え
- `max-w-7xl`: 最大幅を設定
- `px-6 md:px-8 lg:px-12`: レスポンシブなパディング（画面サイズに応じて変更）
- `space-y-2`: 縦方向のスペーシング

**主なユーティリティクラス**:

| カテゴリ | クラス例 | 説明 |
|---------|---------|------|
| **レイアウト** | `flex`, `grid`, `container` | レイアウトシステム |
| **スペーシング** | `px-4`, `py-2`, `m-4` | パディング・マージン |
| **タイポグラフィ** | `text-lg`, `font-bold`, `text-center` | テキストスタイル |
| **色** | `bg-primary`, `text-foreground` | 背景色・文字色 |
| **ボーダー** | `border`, `rounded-md` | ボーダー・角丸 |
| **レスポンシブ** | `md:px-4`, `lg:text-xl` | 画面サイズに応じたスタイル |
| **ダークモード** | `dark:bg-card` | ダークモード用のスタイル |

**学習ポイント**:
- **ユーティリティファースト**: クラス名で直接スタイルを適用
- **レスポンシブ**: プレフィックスで画面サイズに応じたスタイルを適用
- **カスタマイズ**: CSS変数でテーマをカスタマイズ可能

**参考資料**:
- [Tailwind CSS公式ドキュメント](https://tailwindcss.com/docs)

---

## shadcn/ui

**役割**: アクセシブルでカスタマイズ可能なUIコンポーネントライブラリ。Radix UIとTailwind CSSを基盤としています。

**このプロジェクトでの使用箇所**:
- `src/components/ui/`: shadcn/uiのコンポーネント（Button、Input、Dialogなど）

**主な特徴**:
- **コピー&ペースト**: コンポーネントをコピーしてプロジェクトに追加
- **カスタマイズ可能**: コンポーネントのコードを直接編集可能
- **アクセシブル**: Radix UIを基盤としているため、アクセシビリティが高い
- **型安全**: TypeScriptで型安全

**実際のコード例（Buttonコンポーネント）**:

`frontend-nextjs/src/components/ui/button.tsx` (7-37行目):

```typescript
const buttonVariants = cva(
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-medium transition-all disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg:not([class*='size-'])]:size-4 shrink-0 [&_svg]:shrink-0 outline-none focus-visible:border-ring focus-visible:ring-ring/50 focus-visible:ring-[3px] aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive: "bg-destructive text-white hover:bg-destructive/90 focus-visible:ring-destructive/20 dark:focus-visible:ring-destructive/40 dark:bg-destructive/60",
        outline: "border bg-background shadow-xs hover:bg-accent hover:text-accent-foreground dark:bg-input/30 dark:border-input dark:hover:bg-input/50",
        secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground dark:hover:bg-accent/50",
        link: "text-primary underline-offset-4 hover:underline",
      },
      size: {
        default: "h-9 px-4 py-2 has-[>svg]:px-3",
        sm: "h-8 rounded-md gap-1.5 px-3 has-[>svg]:px-2.5",
        lg: "h-10 rounded-md px-6 has-[>svg]:px-4",
        icon: "size-9",
        "icon-sm": "size-8",
        "icon-lg": "size-10",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)
```

**解説**:
- **`cva`**: class-variance-authorityでバリアントを定義
- **`variants`**: `variant`と`size`でバリアントを定義
- **`defaultVariants`**: デフォルトのバリアントを設定

**実際のコード例（Buttonコンポーネントの使用）**:

```typescript
import { Button } from "@/components/ui/button"

<Button variant="default" size="default">
  ボタン
</Button>

<Button variant="destructive" size="sm">
  削除
</Button>

<Button variant="outline" size="lg">
  キャンセル
</Button>
```

**主なコンポーネント**:

| コンポーネント | 説明 | 使用例 |
|--------------|------|--------|
| `Button` | ボタンコンポーネント | `<Button variant="default">` |
| `Input` | 入力フィールド | `<Input type="text" />` |
| `Dialog` | モーダルダイアログ | `<Dialog><DialogContent>...</DialogContent></Dialog>` |
| `Select` | セレクトボックス | `<Select><SelectTrigger>...</SelectTrigger></Select>` |
| `Card` | カードコンポーネント | `<Card><CardHeader>...</CardHeader></Card>` |
| `Label` | ラベルコンポーネント | `<Label htmlFor="input">ラベル</Label>` |
| `Alert` | アラートコンポーネント | `<Alert variant="destructive">...</Alert>` |

**学習ポイント**:
- **コンポーネントベース**: 再利用可能なコンポーネントを提供
- **バリアント**: `variant`と`size`でスタイルを変更
- **アクセシビリティ**: Radix UIを基盤としているため、アクセシビリティが高い

**参考資料**:
- [shadcn/ui公式ドキュメント](https://ui.shadcn.com/)

---

## Radix UI

**役割**: アクセシブルなプリミティブコンポーネントライブラリ。shadcn/uiの基盤として使用されています。

**このプロジェクトでの使用箇所**:
- shadcn/uiコンポーネントの基盤（内部的に使用）
- `@radix-ui/react-dialog`, `@radix-ui/react-select`など

**主な特徴**:
- **アクセシビリティ**: WAI-ARIA準拠
- **キーボード操作**: キーボード操作をサポート
- **フォーカス管理**: フォーカス管理を自動的に処理
- **アニメーション**: アニメーションをサポート

**実際のコード例（Dialogコンポーネント）**:

shadcn/uiのDialogコンポーネントは、内部的にRadix UIの`DialogPrimitive`を使用しています。

```typescript
import * as DialogPrimitive from "@radix-ui/react-dialog"

function DialogContent({ className, children, ...props }) {
  return (
    <DialogPrimitive.Portal>
      <DialogPrimitive.Overlay />
      <DialogPrimitive.Content className={className}>
        {children}
      </DialogPrimitive.Content>
    </DialogPrimitive.Portal>
  )
}
```

**主なプリミティブコンポーネント**:

| コンポーネント | 説明 | 使用例 |
|--------------|------|--------|
| `Dialog` | モーダルダイアログ | `<Dialog><DialogContent>...</DialogContent></Dialog>` |
| `Select` | セレクトボックス | `<Select><SelectTrigger>...</SelectTrigger></Select>` |
| `AlertDialog` | アラートダイアログ | `<AlertDialog><AlertDialogContent>...</AlertDialogContent></AlertDialog>` |
| `DropdownMenu` | ドロップダウンメニュー | `<DropdownMenu><DropdownMenuTrigger>...</DropdownMenuTrigger></DropdownMenu>` |
| `Label` | ラベルコンポーネント | `<Label htmlFor="input">ラベル</Label>` |
| `Slot` | スロットコンポーネント | `<Slot>...</Slot>` |

**学習ポイント**:
- **アクセシビリティ**: WAI-ARIA準拠で、スクリーンリーダーに対応
- **キーボード操作**: キーボード操作を自動的にサポート
- **フォーカス管理**: フォーカス管理を自動的に処理

**参考資料**:
- [Radix UI公式ドキュメント](https://www.radix-ui.com/)

---

## class-variance-authority

**役割**: コンポーネントのバリアント（バリエーション）を型安全に管理するライブラリ。

**このプロジェクトでの使用箇所**:
- `src/components/ui/button.tsx`: Buttonコンポーネントのバリアント定義
- `src/components/ui/alert.tsx`: Alertコンポーネントのバリアント定義

**主な機能**:
- **型安全**: TypeScriptで型安全なバリアント定義
- **バリアント管理**: `variant`と`size`などのバリアントを管理
- **デフォルト値**: デフォルトのバリアントを設定可能

**実際のコード例**:

`frontend-nextjs/src/components/ui/button.tsx` (7-37行目):

```typescript
import { cva, type VariantProps } from "class-variance-authority"

const buttonVariants = cva(
  // ベースクラス（すべてのバリアントに適用される）
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-medium transition-all",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive: "bg-destructive text-white hover:bg-destructive/90",
        outline: "border bg-background shadow-xs hover:bg-accent",
        secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline",
      },
      size: {
        default: "h-9 px-4 py-2",
        sm: "h-8 rounded-md gap-1.5 px-3",
        lg: "h-10 rounded-md px-6",
        icon: "size-9",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

// 型を推論
type ButtonVariants = VariantProps<typeof buttonVariants>
```

**使用例**:

```typescript
function Button({ variant, size, className, ...props }: ButtonVariants & React.ComponentProps<"button">) {
  return (
    <button
      className={cn(buttonVariants({ variant, size }), className)}
      {...props}
    />
  )
}
```

**学習ポイント**:
- **型安全**: TypeScriptで型安全なバリアント定義
- **バリアント管理**: `variant`と`size`などのバリアントを管理
- **デフォルト値**: `defaultVariants`でデフォルトのバリアントを設定

**参考資料**:
- [class-variance-authority公式ドキュメント](https://cva.style/docs)

---

## clsxとtailwind-merge

**役割**: クラス名をマージするユーティリティライブラリ。条件付きクラス名を生成し、Tailwind CSSのクラス名の競合を解決します。

**このプロジェクトでの使用箇所**:
- `src/lib/utils.ts`: `cn`関数（`clsx`と`tailwind-merge`を組み合わせた関数）

**主な機能**:
- **`clsx`**: 条件付きクラス名を生成
- **`tailwind-merge`**: Tailwind CSSのクラス名の競合を解決

**実際のコード例**:

`frontend-nextjs/src/lib/utils.ts` (1-6行目):

```typescript
import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
```

**使用例**:

```typescript
import { cn } from "@/lib/utils"

// 条件付きクラス名
<button className={cn("px-4 py-2", isActive && "bg-blue-500")}>
  ボタン
</button>

// クラス名の競合を解決（後ろのクラスが優先される）
<button className={cn("px-4", "px-8")}>
  ボタン
</button>
// 結果: "px-8"（px-4は上書きされる）

// 複数のクラス名をマージ
<div className={cn("flex", "items-center", "justify-center")}>
  コンテンツ
</div>
```

**学習ポイント**:
- **条件付きクラス名**: `clsx`で条件付きクラス名を生成
- **クラス名の競合解決**: `tailwind-merge`でTailwind CSSのクラス名の競合を解決
- **型安全**: TypeScriptで型安全

**参考資料**:
- [clsx公式ドキュメント](https://github.com/lukeed/clsx)
- [tailwind-merge公式ドキュメント](https://github.com/dcastil/tailwind-merge)

---

## next-themes（ダークモード）

**役割**: Next.jsアプリケーションでテーマ（ライトモード・ダークモード）を管理するライブラリ。

**このプロジェクトでの使用箇所**:
- ダークモードの切り替え（将来的に使用予定）

**主な機能**:
- **テーマ切り替え**: ライトモード・ダークモードを切り替え
- **システム設定**: システムの設定に従う
- **永続化**: ローカルストレージにテーマを保存

**基本的な使用例**:

```typescript
import { ThemeProvider } from "next-themes"

function RootLayout({ children }) {
  return (
    <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
      {children}
    </ThemeProvider>
  )
}
```

**使用例（テーマ切り替え）**:

```typescript
import { useTheme } from "next-themes"

function ThemeToggle() {
  const { theme, setTheme } = useTheme()

  return (
    <button onClick={() => setTheme(theme === "dark" ? "light" : "dark")}>
      {theme === "dark" ? "ライトモード" : "ダークモード"}
    </button>
  )
}
```

**学習ポイント**:
- **テーマ切り替え**: ライトモード・ダークモードを切り替え
- **システム設定**: システムの設定に従う
- **永続化**: ローカルストレージにテーマを保存

**参考資料**:
- [next-themes公式ドキュメント](https://github.com/pacocoursey/next-themes)

---

## まとめ

このプロジェクトのスタイリングとUIライブラリは、以下の技術で構成されています：

### スタイリング
- **Tailwind CSS 4.1.9**: ユーティリティファーストのCSSフレームワーク
- **CSS変数**: テーマカラーをCSS変数で管理

### UIコンポーネント
- **shadcn/ui**: アクセシブルでカスタマイズ可能なUIコンポーネントライブラリ
- **Radix UI**: アクセシブルなプリミティブコンポーネントライブラリ

### ユーティリティ
- **class-variance-authority**: 型安全なバリアント管理
- **clsx**: 条件付きクラス名の生成
- **tailwind-merge**: Tailwind CSSのクラス名の競合解決

### テーマ
- **next-themes**: ダークモードの管理（将来的に使用予定）

これらの技術を組み合わせることで、保守性と拡張性の高いUIを実現しています。

---

## 学習の進め方

1. **基礎から学ぶ**: Tailwind CSS、shadcn/uiの基礎を理解する
2. **実際のコードを読む**: このプロジェクトのコードを読んで理解を深める
3. **手を動かす**: 小さなコンポーネントを追加して実践する
4. **ドキュメントを読む**: 公式ドキュメントで詳細を確認する

---

## 参考資料

### 公式ドキュメント
- [Tailwind CSS公式ドキュメント](https://tailwindcss.com/docs)
- [shadcn/ui公式ドキュメント](https://ui.shadcn.com/)
- [Radix UI公式ドキュメント](https://www.radix-ui.com/)

### 書籍
- [Tailwind CSS実践ガイド](https://www.shoeisha.co.jp/book/detail/9784798172025)

---

**最終更新日**: 2024年

