"use client"

/**
 * 最近の取引セクションコンポーネント
 * 
 * 直近の収入・支出の取引をタイムライン形式でリッチに表示します。
 * カテゴリアイコン、色分け、ホバーエフェクトを使用して
 * 視覚的に分かりやすいUIを提供します。
 * 
 * 【初心者向け解説】
 * - タイムライン形式: 時系列に沿って縦に並べる表示方法
 * - トランザクション: 収入や支出の1件1件の取引データ
 * - カテゴリ: 取引を分類するためのグループ（食費、交通費など）
 */

import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { 
  ArrowUpCircle, 
  ArrowDownCircle, 
  ChevronRight, 
  List,
  ArrowUpRight,
  Clock,
  Utensils,
  Train,
  Zap,
  Briefcase,
  DollarSign
} from "lucide-react"
import { formatCurrency } from "@/lib/formatters"
import { formatDate } from "@/lib/date-formatters"
import Link from "next/link"
import { cn } from "@/lib/utils"

/**
 * 取引データの型定義
 */
interface Transaction {
  id: number
  type: "income" | "expense"
  date: string
  category: string
  description: string
  amount: number
}

/**
 * カテゴリに応じたアイコンを取得
 * @param category カテゴリ名
 * @returns アイコンコンポーネント
 */
const getCategoryIconComponent = (category: string) => {
  const icons: Record<string, React.ElementType> = {
    "給与": Briefcase,
    "副業": DollarSign,
    "食費": Utensils,
    "交通費": Train,
    "光熱費": Zap,
  }
  return icons[category] || DollarSign
}

/**
 * カテゴリに応じた色を取得
 * @param category カテゴリ名
 * @param type 収入/支出
 * @returns 色クラス
 */
const getCategoryStyle = (category: string, type: "income" | "expense") => {
  // 収入は緑系、支出はカテゴリ別に色分け
  if (type === "income") {
    return {
      bg: "bg-emerald-500/10",
      text: "text-emerald-600 dark:text-emerald-400",
      iconBg: "bg-gradient-to-br from-emerald-400 to-green-500"
    }
  }

  const styles: Record<string, { bg: string; text: string; iconBg: string }> = {
    "食費": {
      bg: "bg-orange-500/10",
      text: "text-orange-600 dark:text-orange-400",
      iconBg: "bg-gradient-to-br from-orange-400 to-red-500"
    },
    "交通費": {
      bg: "bg-blue-500/10",
      text: "text-blue-600 dark:text-blue-400",
      iconBg: "bg-gradient-to-br from-blue-400 to-cyan-500"
    },
    "光熱費": {
      bg: "bg-yellow-500/10",
      text: "text-yellow-600 dark:text-yellow-400",
      iconBg: "bg-gradient-to-br from-yellow-400 to-amber-500"
    },
  }

  return styles[category] || {
    bg: "bg-rose-500/10",
    text: "text-rose-600 dark:text-rose-400",
    iconBg: "bg-gradient-to-br from-rose-400 to-pink-500"
  }
}

export function RecentTransactionsSection() {
  // モックデータ（実際のロジックは未実装）
  const mockTransactions: Transaction[] = [
    { id: 1, type: "income", date: "2024-01-20", category: "給与", description: "基本給", amount: 250000 },
    { id: 2, type: "expense", date: "2024-01-19", category: "食費", description: "スーパーマーケット", amount: 3500 },
    { id: 3, type: "expense", date: "2024-01-18", category: "交通費", description: "電車代", amount: 2000 },
    { id: 4, type: "expense", date: "2024-01-17", category: "光熱費", description: "電気代", amount: 5000 },
    { id: 5, type: "income", date: "2024-01-15", category: "副業", description: "フリーランス", amount: 50000 },
  ]

  return (
    <div className="space-y-4">
      {/* セクションヘッダー */}
      <div className="flex items-center justify-between">
        <h2 className="text-lg md:text-xl font-bold text-foreground flex items-center gap-2">
          <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-indigo-400 to-purple-500 text-white shadow-lg">
            <Clock className="h-4 w-4" />
          </div>
          最近の取引
        </h2>
        <Link href="/expenses">
          <Button 
            variant="ghost" 
            size="sm" 
            className="gap-1 text-xs font-bold text-primary hover:text-primary/80 group"
          >
            すべて見る
            <ArrowUpRight className="h-3.5 w-3.5 transition-transform group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
          </Button>
        </Link>
      </div>

      {/* メインカード */}
      <Card className={cn(
        "relative overflow-hidden",
        "border-border/40 shadow-rich shadow-rich-hover",
        "bg-gradient-to-br from-card via-card to-indigo-500/5",
        "transition-all duration-500 animate-fade-in"
      )}>
        {/* 背景装飾 */}
        <div className="absolute inset-0 pattern-dots pointer-events-none opacity-50" />
        <div className="absolute bottom-0 right-0 w-48 h-48 bg-gradient-to-tl from-purple-500/5 to-transparent rounded-full translate-x-1/4 translate-y-1/4" />

        <CardContent className="relative p-0">
          {mockTransactions.length === 0 ? (
            <div className="p-8 text-center">
              <div className="w-16 h-16 mx-auto rounded-full bg-muted/20 flex items-center justify-center mb-4">
                <List className="w-8 h-8 text-muted-foreground/30" />
              </div>
              <p className="text-muted-foreground">
                取引データがありません
              </p>
            </div>
          ) : (
            <div className="divide-y divide-border/40">
              {mockTransactions.map((transaction, index) => {
                const CategoryIcon = getCategoryIconComponent(transaction.category)
                const style = getCategoryStyle(transaction.category, transaction.type)

                return (
                  <div
                    key={transaction.id}
                    className={cn(
                      "group relative flex items-center gap-4 p-4",
                      "hover:bg-muted/5 transition-all duration-300",
                      "cursor-pointer",
                      // スタガードアニメーション
                      "animate-fade-in",
                      `stagger-${Math.min(index + 1, 5)}`
                    )}
                  >
                    {/* 左側のタイムラインドット */}
                    <div className="hidden md:flex absolute left-0 top-1/2 -translate-y-1/2 w-px h-full">
                      <div className={cn(
                        "absolute top-1/2 -translate-y-1/2 -left-px w-2.5 h-2.5 rounded-full border-2 border-background",
                        transaction.type === "income" ? "bg-emerald-500" : "bg-rose-500"
                      )} />
                      {index !== mockTransactions.length - 1 && (
                        <div className="absolute top-1/2 left-0 w-px h-full bg-border/40" />
                      )}
                    </div>

                    {/* アイコン */}
                    <div className={cn(
                      "flex-shrink-0 w-12 h-12 rounded-2xl flex items-center justify-center text-white shadow-lg",
                      "transform transition-all duration-300 group-hover:scale-110 group-hover:rotate-6",
                      style.iconBg
                    )}>
                      <CategoryIcon className="h-5 w-5" />
                    </div>

                    {/* 取引情報 */}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <p className="font-bold text-foreground truncate">
                          {transaction.description}
                        </p>
                        {/* 収入/支出バッジ */}
                        <span className={cn(
                          "flex-shrink-0 text-[10px] font-bold px-2 py-0.5 rounded-full",
                          transaction.type === "income" 
                            ? "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
                            : "bg-rose-500/10 text-rose-600 dark:text-rose-400"
                        )}>
                          {transaction.type === "income" ? "収入" : "支出"}
                        </span>
                      </div>
                      <div className="flex items-center gap-3 text-xs text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <Clock className="w-3 h-3" />
                          {formatDate(transaction.date)}
                        </span>
                        <span>•</span>
                        <span className={cn(
                          "px-2 py-0.5 rounded-full",
                          style.bg,
                          style.text,
                          "font-medium"
                        )}>
                          {transaction.category}
                        </span>
                      </div>
                    </div>

                    {/* 金額 */}
                    <div className="flex-shrink-0 text-right">
                      <p className={cn(
                        "text-lg md:text-xl font-black tabular-nums",
                        "transition-transform duration-300 group-hover:scale-105",
                        transaction.type === "income"
                          ? "text-emerald-600 dark:text-emerald-400"
                          : "text-rose-600 dark:text-rose-400"
                      )}>
                        {transaction.type === "income" ? "+" : "-"}
                        {formatCurrency(transaction.amount)}
                      </p>
                    </div>

                    {/* ホバー時の矢印 */}
                    <ChevronRight className="w-5 h-5 text-muted-foreground/30 opacity-0 group-hover:opacity-100 transition-all group-hover:translate-x-0.5" />
                  </div>
                )
              })}
            </div>
          )}
        </CardContent>

        {/* フッター: もっと見るボタン */}
        <div className="px-4 py-3 bg-muted/5 border-t border-border/40">
          <Link href="/expenses" className="block">
            <Button 
              variant="ghost" 
              className="w-full justify-center gap-2 text-sm font-bold text-muted-foreground hover:text-foreground"
            >
              すべての取引を表示
              <ArrowUpRight className="w-4 h-4" />
            </Button>
          </Link>
        </div>
      </Card>
    </div>
  )
}
