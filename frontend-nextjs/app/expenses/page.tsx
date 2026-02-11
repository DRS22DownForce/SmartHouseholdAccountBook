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
import { getUserDisplayName } from "@/lib/user-utils"
import { useExpensesPageLogic } from "@/hooks/use-expenses-page-logic"
import { useExpenseSummary } from "@/hooks/use-expense-summary"
import {
  Sparkles,
  List,
} from "lucide-react"
import { useMemo } from "react"

export default function ExpensesPage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const username = useMemo(() => getUserDisplayName(user), [user])

  const {
    refreshTrigger,
    handleAddExpense,
    handleAddExpenses,
    handleUpdateExpense,
    handleDeleteExpense,
    handleCsvUploadComplete,
  } = useExpensesPageLogic()

  // 今月の支出サマリーを取得（前月比含む）
  // refreshTriggerを渡すことで、支出追加・更新・削除時に自動的にデータが更新されます
  const summaryData = useExpenseSummary(refreshTrigger)

  return (
    <AppLayout
      username={username}
      onLogout={signOut}
      onAddExpense={handleAddExpense}
      onAddExpenses={handleAddExpenses}
      onCsvUploadComplete={handleCsvUploadComplete}
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
