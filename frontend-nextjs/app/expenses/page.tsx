"use client"

/**
 * 支出一覧ページコンポーネント
 * 
 * 支出更新・削除後に画面を自動的に再取得します。
 * ホームページと統一感のあるリッチなデザインを採用しています。
 * 
 * 【初心者向け解説】
 * - useExpenses: 支出データを管理するカスタムフック
 * - refreshTrigger: 値が変わるとデータを再読み込みする仕組み
 * - スタガードアニメーション: 要素が順番に表示される効果
 * 
 * 【レイアウト構成】
 * 1. ヘッダー（タイトル + アクションボタン）
 * 2. サマリーカード（3カラム：今月の支出、取引件数、日平均）
 * 3. 支出の推移グラフ
 * 4. 月別サマリー
 * 5. 支出一覧
 */

import { useMemo } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { useExpenses } from "@/hooks/use-expenses"
import { AppLayout } from "@/components/layout/AppLayout"
import { ExpenseList } from "@/components/expense-list"
import { ExpenseTrendChart } from "@/components/expense-trend-chart"
import { MonthlySummarySection } from "@/components/dashboard/MonthlySummarySection"
import { LoadingSpinner } from "@/components/ui/loading-spinner"
import { getUserDisplayName } from "@/lib/user-utils"
import { useExpensesPageLogic } from "@/hooks/use-expenses-page-logic"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { 
  Sparkles, 
  List, 
  TrendingDown, 
  TrendingUp,
  Filter,
  Download,
  Receipt,
  CalendarDays,
  Calculator,
  ArrowDownCircle
} from "lucide-react"
import { cn } from "@/lib/utils"
import { formatCurrency } from "@/lib/formatters"

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

  // モックデータ（実際のロジックでは expenseItems から計算）
  const mockSummary = {
    monthlyTotal: 350000,
    transactionCount: 48,
    dailyAverage: 11290,
    monthlyChange: -5.2,  // 前月比
  }

  if (!isLoaded) {
    return <LoadingSpinner />
  }

  // サマリーカードの設定（ホームページと同じスタイル）
  const summaryCards = [
    {
      title: "今月の支出",
      value: mockSummary.monthlyTotal,
      icon: ArrowDownCircle,
      trend: {
        value: Math.abs(mockSummary.monthlyChange),
        isPositive: mockSummary.monthlyChange < 0,  // 支出は減少がポジティブ
        label: "前月比"
      },
      gradient: "from-rose-500/10 via-rose-500/5 to-transparent",
      iconBg: "bg-gradient-to-br from-rose-400 to-pink-500",
      valueColor: "text-rose-600 dark:text-rose-400"
    },
    {
      title: "取引件数",
      value: mockSummary.transactionCount,
      isCount: true,
      icon: Receipt,
      gradient: "from-blue-500/10 via-blue-500/5 to-transparent",
      iconBg: "bg-gradient-to-br from-blue-400 to-cyan-500",
      valueColor: "text-blue-600 dark:text-blue-400"
    },
    {
      title: "日平均支出",
      value: mockSummary.dailyAverage,
      icon: CalendarDays,
      gradient: "from-amber-500/10 via-amber-500/5 to-transparent",
      iconBg: "bg-gradient-to-br from-amber-400 to-orange-500",
      valueColor: "text-amber-600 dark:text-amber-400"
    }
  ]

  return (
    <AppLayout
      username={username}
      onLogout={signOut}
      onAddExpense={handleAddExpense}
      onAddExpenses={handleAddExpenses}
    >
      <div className="space-y-6 md:space-y-8">
        {/* ==================== */}
        {/* ヘッダーセクション */}
        {/* ==================== */}
        <div className="animate-fade-in">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              {/* グローイングアイコン */}
              <div className="relative">
                <div className="absolute inset-0 bg-orange-500/20 rounded-xl blur-md" />
                <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-orange-400 to-rose-500 text-white shadow-lg">
                  <List className="h-5 w-5" />
                </div>
              </div>
              <div>
                <h1 className="text-2xl md:text-3xl font-black text-foreground tracking-tight">
                  支出一覧
                </h1>
                <p className="text-sm text-muted-foreground flex items-center gap-1">
                  <Sparkles className="w-3 h-3" />
                  全ての支出履歴の確認・編集・分析
                </p>
              </div>
            </div>

            {/* アクションボタン群 */}
            <div className="flex items-center gap-2">
              <Button 
                variant="outline" 
                size="sm" 
                className={cn(
                  "gap-2 shadow-sm hover:shadow-md transition-all",
                  "border-border/50 rounded-xl"
                )}
              >
                <Filter className="h-4 w-4" />
                <span className="hidden sm:inline">フィルター</span>
              </Button>

              <Button 
                variant="outline" 
                size="sm" 
                className={cn(
                  "gap-2 shadow-sm hover:shadow-md transition-all",
                  "border-border/50 rounded-xl"
                )}
              >
                <Download className="h-4 w-4" />
                <span className="hidden sm:inline">エクスポート</span>
              </Button>

              <Button 
                className={cn(
                  "gap-2 shadow-lg rounded-xl",
                  "bg-gradient-to-r from-orange-400 to-rose-500 hover:from-orange-500 hover:to-rose-600",
                  "transition-all duration-300 hover:shadow-xl hover:-translate-y-0.5"
                )}
              >
                <Sparkles className="h-4 w-4" />
                AI自動分類
              </Button>
            </div>
          </div>
        </div>

        {/* ========================= */}
        {/* サマリーカード（ホームページと同じスタイル） */}
        {/* ========================= */}
        <div className="grid gap-4 md:grid-cols-3">
          {summaryCards.map((card, index) => {
            const Icon = card.icon
            return (
              <Card 
                key={card.title}
                className={cn(
                  "relative overflow-hidden group",
                  "border-border/40 shadow-rich",
                  "hover:shadow-xl transition-all duration-500",
                  "hover:-translate-y-1",
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
                          <TrendingDown className="h-3 w-3" />
                        ) : (
                          <TrendingUp className="h-3 w-3" />
                        )}
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
                    "transition-transform duration-300 group-hover:scale-[1.02]",
                    card.valueColor
                  )}>
                    {card.isCount ? `${card.value}件` : formatCurrency(card.value)}
                  </p>

                  {/* サブテキスト */}
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

        {/* ==================== */}
        {/* 支出の推移グラフ */}
        {/* ==================== */}
        <ExpenseTrendChart refreshTrigger={refreshTrigger} />

        {/* ==================== */}
        {/* 月別サマリー */}
        {/* ==================== */}
        <MonthlySummarySection refreshTrigger={refreshTrigger} />

        {/* ==================== */}
        {/* 支出一覧 */}
        {/* ==================== */}
        <ExpenseList
          onUpdate={handleUpdateExpense}
          onDelete={handleDeleteExpense}
          refreshTrigger={refreshTrigger}
        />
      </div>
    </AppLayout>
  )
}
