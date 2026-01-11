"use client"

/**
 * 支出一覧ページコンポーネント
 * 
 * 支出更新・削除後に画面を自動的に再取得します。
 */

import { useAuthenticator } from "@aws-amplify/ui-react"
import { AppLayout } from "@/components/layout/AppLayout"
import { ExpenseList } from "@/components/expense-list"
import { ExpenseTrendChart } from "@/components/expense-trend-chart"
import { MonthlySummarySection } from "@/components/dashboard/MonthlySummarySection"
import { ExpenseSummarySection } from "@/components/dashboard/ExpenseSummarySection"
import { LoadingSpinner } from "@/components/ui/loading-spinner"
import { getUserDisplayName } from "@/lib/user-utils"
import { useExpensesPageLogic } from "@/hooks/use-expenses-page-logic"
import { useExpenseSummary } from "@/hooks/use-expense-summary"
import { Button } from "@/components/ui/button"
import {
  Sparkles,
  List,
  Filter,
  Download,
} from "lucide-react"
import { cn } from "@/lib/utils"
import { useMemo } from "react"

export default function ExpensesPage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const username = useMemo(() => getUserDisplayName(user), [user])

  const {
    expenseItems,
    isLoaded,
    refreshTrigger,
    handleAddExpense,
    handleAddExpenses,
    handleUpdateExpense,
    handleDeleteExpense,
  } = useExpensesPageLogic()

  // 今月の支出サマリーを取得（前月比含む）
  // refreshTriggerを渡すことで、支出追加・更新・削除時に自動的にデータが更新されます
  const summaryData = useExpenseSummary(refreshTrigger)

  if (!isLoaded) {
    return <LoadingSpinner />
  }

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
        {/* サマリーカード*/}
        {/* ========================= */}
        <ExpenseSummarySection 
          summaryData={summaryData}
        />

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
