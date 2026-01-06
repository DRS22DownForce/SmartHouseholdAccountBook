"use client"

/**
 * 収支バランスサマリーセクションコンポーネント
 * 
 * 今月の収入、支出、収支バランスをリッチなカードで表示します。
 * グラスモーフィズム、グラデーション、アニメーションを使用して
 * 視覚的にインパクトのある表示を実現します。
 * 
 * 【初心者向け解説】
 * - refreshTrigger: この値が変わると、親コンポーネントからデータの
 *   再読み込みが要求されたことを示します（将来の実装用）
 * - モックデータ: 本番では実際のAPIからデータを取得します
 */

import { Card, CardContent } from "@/components/ui/card"
import { 
  ArrowUpCircle, 
  ArrowDownCircle, 
  TrendingUp, 
  TrendingDown,
  Sparkles,
  Wallet,
  CircleDollarSign
} from "lucide-react"
import { formatCurrency } from "@/lib/formatters"
import { cn } from "@/lib/utils"

interface BalanceSummarySectionProps {
  refreshTrigger?: number  // データ再読み込みのトリガー（将来の実装用）
}

/**
 * 統計カードの型定義
 */
interface StatCard {
  title: string
  value: number
  icon: React.ElementType
  trend?: {
    value: number
    isPositive: boolean
    label: string
  }
  gradient: string
  iconBg: string
  valueColor: string
}

export function BalanceSummarySection({ refreshTrigger }: BalanceSummarySectionProps) {
  // モックデータ（実際のロジックは未実装）
  // 将来的にはAPIからデータを取得します
  const mockData = {
    income: 500000,
    expenses: 350000,
    balance: 150000,
    previousMonthChange: 4.2,
    expenseChange: -2.5,
    previousYearChange: -2.1,
  }

  const isPositive = mockData.balance >= 0

  // 統計カードの設定
  const statCards: StatCard[] = [
    {
      title: "今月の収入",
      value: mockData.income,
      icon: ArrowUpCircle,
      trend: {
        value: mockData.previousMonthChange,
        isPositive: true,
        label: "前月比"
      },
      gradient: "from-emerald-500/10 via-emerald-500/5 to-transparent",
      iconBg: "bg-gradient-to-br from-emerald-400 to-emerald-600",
      valueColor: "text-emerald-600 dark:text-emerald-400"
    },
    {
      title: "今月の支出",
      value: mockData.expenses,
      icon: ArrowDownCircle,
      trend: {
        value: Math.abs(mockData.expenseChange),
        isPositive: mockData.expenseChange < 0,  // 支出は減少が positive
        label: "前月比"
      },
      gradient: "from-rose-500/10 via-rose-500/5 to-transparent",
      iconBg: "bg-gradient-to-br from-rose-400 to-rose-600",
      valueColor: "text-rose-600 dark:text-rose-400"
    },
    {
      title: "収支バランス",
      value: mockData.balance,
      icon: Wallet,
      gradient: isPositive 
        ? "from-blue-500/10 via-blue-500/5 to-transparent"
        : "from-orange-500/10 via-orange-500/5 to-transparent",
      iconBg: isPositive 
        ? "bg-gradient-to-br from-blue-400 to-blue-600"
        : "bg-gradient-to-br from-orange-400 to-orange-600",
      valueColor: isPositive 
        ? "text-blue-600 dark:text-blue-400"
        : "text-orange-600 dark:text-orange-400"
    }
  ]

  return (
    <div className="space-y-4">
      {/* セクションヘッダー */}
      <h2 className="text-lg md:text-xl font-bold text-foreground flex items-center gap-2">
        <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-blue-500 to-cyan-500 text-white shadow-lg">
          <CircleDollarSign className="h-4 w-4" />
        </div>
        収支バランス
        <span className="text-xs font-normal text-muted-foreground ml-2">
          今月の概要
        </span>
      </h2>

      {/* 統計カードグリッド */}
      <div className="grid gap-4 md:grid-cols-3">
        {statCards.map((card, index) => {
          const Icon = card.icon
          return (
            <Card 
              key={card.title}
              className={cn(
                // ベーススタイル
                "relative overflow-hidden group",
                // ボーダーとシャドウ
                "border-border/40 shadow-rich",
                // ホバー効果
                "hover:shadow-xl transition-all duration-500",
                "hover:-translate-y-1",
                // アニメーション
                "animate-fade-in",
                index === 0 ? "stagger-1" : index === 1 ? "stagger-2" : "stagger-3"
              )}
            >
              {/* グラデーション背景 */}
              <div className={cn(
                "absolute inset-0 bg-gradient-to-br",
                card.gradient
              )} />
              
              {/* 装飾的な円形要素 */}
              <div className="absolute -top-10 -right-10 w-32 h-32 rounded-full bg-current opacity-[0.03] group-hover:opacity-[0.06] transition-opacity" />
              
              {/* シマー効果（ホバー時） */}
              <div className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500">
                <div className="absolute inset-0 animate-shimmer" />
              </div>

              <CardContent className="relative p-5 md:p-6">
                {/* ヘッダー行 */}
                <div className="flex items-start justify-between mb-4">
                  {/* アイコン */}
                  <div className={cn(
                    "flex h-12 w-12 items-center justify-center rounded-2xl text-white shadow-lg",
                    "transform transition-transform duration-300 group-hover:scale-110 group-hover:rotate-3",
                    card.iconBg
                  )}>
                    <Icon className="h-6 w-6" />
                  </div>

                  {/* トレンドバッジ */}
                  {card.trend && (
                    <div className={cn(
                      "flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold",
                      "transition-transform duration-300 group-hover:scale-105",
                      card.trend.isPositive 
                        ? "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
                        : "bg-rose-500/10 text-rose-600 dark:text-rose-400"
                    )}>
                      {card.trend.isPositive ? (
                        <TrendingUp className="h-3 w-3" />
                      ) : (
                        <TrendingDown className="h-3 w-3" />
                      )}
                      {card.trend.isPositive ? "+" : "-"}{card.trend.value}%
                    </div>
                  )}
                </div>

                {/* タイトル */}
                <p className="text-xs md:text-sm font-bold text-muted-foreground uppercase tracking-wider mb-2">
                  {card.title}
                </p>

                {/* 金額 */}
                <p className={cn(
                  "text-2xl md:text-3xl lg:text-4xl font-black tracking-tight tabular-nums",
                  "transition-transform duration-300 group-hover:scale-[1.02]",
                  card.valueColor
                )}>
                  {card.title === "収支バランス" && mockData.balance >= 0 && "+"}
                  {formatCurrency(card.value)}
                </p>

                {/* サブテキスト */}
                <div className="flex items-center gap-2 mt-3">
                  {card.trend ? (
                    <p className="text-xs text-muted-foreground">
                      {card.trend.label}: 
                      <span className={cn(
                        "ml-1 font-bold",
                        card.trend.isPositive ? "text-emerald-600 dark:text-emerald-400" : "text-rose-600 dark:text-rose-400"
                      )}>
                        {card.trend.isPositive ? "+" : "-"}{card.trend.value}%
                      </span>
                    </p>
                  ) : (
                    <div className="flex items-center gap-2">
                      <span className={cn(
                        "inline-flex items-center gap-1 text-xs font-bold px-2 py-0.5 rounded-full",
                        isPositive 
                          ? "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
                          : "bg-rose-500/10 text-rose-600 dark:text-rose-400"
                      )}>
                        <Sparkles className="h-3 w-3" />
                        {isPositive ? "黒字" : "赤字"}
                      </span>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>
    </div>
  )
}
