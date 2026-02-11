"use client"

/**
 * 支出サマリーセクションコンポーネント
 * 
 * 今月の支出合計、取引件数、日平均支出をリッチなカードで表示します。
 * ホームページのBalanceSummarySectionと同様のデザインを採用しています。
 * 
 * 【初心者向け解説】
 * - Props: 親コンポーネントから受け取るデータ（summaryData）と
 *   データ再読み込みのトリガー（refreshTrigger）
 * - カード配列: 3つのサマリーカード（今月の支出、取引件数、日平均）の
 *   設定を配列で定義し、map関数で順番にレンダリング
 * - グラデーション背景: 各カードに異なる色のグラデーションを適用して
 *   視覚的に区別しやすくしています
 * - アニメーション: stagger-1, stagger-2, stagger-3クラスで
 *   カードが順番に表示される効果を実現
 * 
 * 【コンポーネントの利点】
 * - 再利用性: 他のページでも同じサマリー表示が可能
 * - 保守性: サマリー表示のロジックが一箇所に集約され、修正が容易
 * - 可読性: ページコンポーネントがシンプルになり、理解しやすくなる
 */

import { Card, CardContent } from "@/components/ui/card"
import {
  ArrowDownCircle,
  Receipt,
  CalendarDays,
  TrendingDown,
  TrendingUp,
} from "lucide-react"
import { formatCurrency } from "@/lib/formatters"
import { cn } from "@/lib/utils"

/**
 * コンポーネントのProps型定義
 * 
 * - interface: TypeScriptでオブジェクトの構造を定義する方法
 * - summaryData: 表示するサマリーデータ（必須）
 * - refreshTrigger: データ再読み込みのトリガー（オプショナル）
 */
interface ExpenseSummarySectionProps {
  summaryData: {
    monthlyTotal: number        // 今月の支出合計
    transactionCount: number    // 取引件数
    dailyAverage: number        // 日平均支出
    monthlyChange?: number      // 前月比（オプショナル、将来の実装用）
  }
}

/**
 * サマリーカードの設定型定義
 * 
 * - title: カードのタイトル（例: "今月の支出"）
 * - value: 表示する数値
 * - icon: カードに表示するアイコンコンポーネント
 * - isCount: trueの場合、数値の後に"件"を付ける
 * - trend: 前月比などのトレンド情報（オプショナル）
 * - gradient: 背景のグラデーション色
 * - iconBg: アイコンの背景色
 * - valueColor: 数値の文字色
 */
interface SummaryCard {
  title: string
  value: number
  icon: React.ElementType
  isCount?: boolean
  trend?: {
    value: number
    isPositive: boolean  // 支出は減少がポジティブ
    label: string
  }
  gradient: string
  iconBg: string
  valueColor: string
}

/**
 * 支出サマリーセクションコンポーネント
 * 
 * @param props - コンポーネントのProps
 * @returns 支出サマリーカードのJSX要素
 */
export function ExpenseSummarySection({ 
  summaryData
}: ExpenseSummarySectionProps) {
  // サマリーカードの設定配列
  const summaryCards: SummaryCard[] = [
    {
      title: "今月の支出",
      value: summaryData.monthlyTotal,
      icon: ArrowDownCircle,
      // 前月比がある場合のみトレンド情報を表示
      trend: summaryData.monthlyChange !== 0 ? {
        value: Math.abs(summaryData.monthlyChange ?? 0),
        // 支出は減少がポジティブ（良いこと）なので、負の値がポジティブ
        isPositive: (summaryData.monthlyChange ?? 0) < 0,
        label: "前月比"
      } : undefined,
      gradient: "from-rose-500/10 via-rose-500/5 to-transparent",
      iconBg: "bg-gradient-to-br from-rose-400 to-pink-500",
      valueColor: "text-rose-600 dark:text-rose-400"
    },
    {
      title: "取引件数",
      value: summaryData.transactionCount,
      isCount: true,  // 数値の後に"件"を付ける
      icon: Receipt,
      gradient: "from-blue-500/10 via-blue-500/5 to-transparent",
      iconBg: "bg-gradient-to-br from-blue-400 to-cyan-500",
      valueColor: "text-blue-600 dark:text-blue-400"
    },
    {
      title: "日平均支出",
      value: summaryData.dailyAverage,
      icon: CalendarDays,
      gradient: "from-amber-500/10 via-amber-500/5 to-transparent",
      iconBg: "bg-gradient-to-br from-amber-400 to-orange-500",
      valueColor: "text-amber-600 dark:text-amber-400"
    }
  ]

  return (
    <div className="grid gap-4 md:grid-cols-3">
      {summaryCards.map((card, index) => {
        // アイコンコンポーネントを取得（Reactではコンポーネントは大文字で始まる必要がある）
        const Icon = card.icon
        
        return (
          <Card
            key={card.title}
            className={cn(
              // ベーススタイル
              "relative overflow-hidden group",
              // ボーダーとシャドウ
              "border-border/40 shadow-rich",
              // ホバー効果: マウスを乗せた時にカードが浮き上がる
              "hover:shadow-xl transition-all duration-500",
              "hover:-translate-y-1",
              // アニメーション: カードが順番に表示される
              "animate-fade-in",
              // stagger-1, stagger-2, stagger-3で表示タイミングをずらす
              index === 0 ? "stagger-1" : index === 1 ? "stagger-2" : "stagger-3"
            )}
          >
            {/* グラデーション背景 */}
            {/* 
              【初心者向け解説】
              absolute inset-0: 親要素全体を覆う絶対配置
              bg-gradient-to-br: 左上から右下へのグラデーション
            */}
            <div className={cn(
              "absolute inset-0 bg-gradient-to-br",
              card.gradient
            )} />

            {/* 装飾的な円形要素 */}
            {/* 
              【初心者向け解説】
              カードの右上に半透明の円を配置して、視覚的なアクセントにしています
              group-hover: 親要素（group）にホバーした時に透明度が変わる
            */}
            <div className="absolute -top-10 -right-10 w-32 h-32 rounded-full bg-current opacity-[0.03] group-hover:opacity-[0.06] transition-opacity" />

            {/* シマー効果（ホバー時） */}
            {/* 
              【初心者向け解説】
              ホバー時に光が流れるようなアニメーション効果を追加
              animate-shimmer: カスタムアニメーション（tailwind.configで定義）
            */}
            <div className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500">
              <div className="absolute inset-0 animate-shimmer" />
            </div>

            <CardContent className="relative p-5 md:p-6">
              {/* ヘッダー行: アイコンとトレンドバッジ */}
              <div className="flex items-start justify-between mb-4">
                {/* アイコン */}
                <div className={cn(
                  "flex h-12 w-12 items-center justify-center rounded-2xl text-white shadow-lg",
                  // ホバー時にアイコンが拡大・回転する
                  "transform transition-transform duration-300 group-hover:scale-110 group-hover:rotate-3",
                  card.iconBg
                )}>
                  <Icon className="h-6 w-6" />
                </div>

                {/* トレンドバッジ: 前月比がある場合のみ表示 */}
                {card.trend && (
                  <div className={cn(
                    "flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold",
                    "transition-transform duration-300 group-hover:scale-105",
                    // ポジティブ（支出減少）は緑、ネガティブ（支出増加）は赤
                    card.trend.isPositive
                      ? "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
                      : "bg-rose-500/10 text-rose-600 dark:text-rose-400"
                  )}>
                    {/* トレンドの方向に応じてアイコンを切り替え */}
                    {card.trend.isPositive ? (
                      <TrendingDown className="h-3 w-3" />
                    ) : (
                      <TrendingUp className="h-3 w-3" />
                    )}
                    {/* パーセンテージを表示（+/-記号付き） */}
                    {card.trend.isPositive ? "-" : "+"}{card.trend.value}%
                  </div>
                )}
              </div>

              {/* タイトル */}
              <p className="text-xs md:text-sm font-bold text-muted-foreground uppercase tracking-wider mb-2">
                {card.title}
              </p>

              {/* 金額/数値 */}
              <p className={cn(
                "text-2xl md:text-3xl lg:text-4xl font-black tracking-tight tabular-nums",
                // ホバー時に数値が少し拡大
                "transition-transform duration-300 group-hover:scale-[1.02]",
                card.valueColor
              )}>
                {/* isCountがtrueの場合は"件"を付ける、そうでなければ通貨フォーマット */}
                {card.isCount ? `${card.value}件` : formatCurrency(card.value)}
              </p>

              {/* サブテキスト: トレンド情報がある場合 */}
              {card.trend && (
                <div className="flex items-center gap-2 mt-3">
                  <p className="text-xs text-muted-foreground">
                    {card.trend.label}:
                    <span className={cn(
                      "ml-1 font-bold",
                      card.trend.isPositive ? "text-emerald-600 dark:text-emerald-400" : "text-rose-600 dark:text-rose-400"
                    )}>
                      {card.trend.isPositive ? "-" : "+"}{card.trend.value}%
                    </span>
                  </p>
                </div>
              )}

              {/* 取引件数の場合のサブテキスト */}
              {card.isCount && (
                <p className="text-xs text-muted-foreground mt-3">
                  今月の取引数
                </p>
              )}

              {/* 日平均の場合のサブテキスト */}
              {card.title === "日平均支出" && (
                <p className="text-xs text-muted-foreground mt-3">
                  1日あたりの支出額
                </p>
              )}
            </CardContent>
          </Card>
        )
      })}
    </div>
  )
}
