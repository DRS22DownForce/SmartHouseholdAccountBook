"use client"

/**
 * 支出一覧ページコンポーネント
 * 
 * 支出更新・削除後に画面を自動的に再取得します。
 * ロジックはuse-expenses-page-logicカスタムフックに分離されています。
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
import { Sparkles, List } from "lucide-react"

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
    <AppLayout
      username={username}
      onLogout={signOut}
      onAddExpense={handleAddExpense}
      onAddExpenses={handleAddExpenses}
    >
      <div className="space-y-4 md:space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-foreground tracking-tight mb-1 flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-orange-500/10 text-orange-500">
                <List className="h-5 w-5" />
              </div>
              支出一覧
            </h1>
            <p className="text-sm text-muted-foreground">
              全ての支出を確認し、編集・削除できます
            </p>
          </div>
          <Button variant="outline" className="gap-2">
            <Sparkles className="h-4 w-4" />
            AI自動分類
          </Button>
        </div>

        {/* 支出の推移グラフ */}
        <ExpenseTrendChart refreshTrigger={refreshTrigger} />

        {/* 月別サマリー */}
        <MonthlySummarySection refreshTrigger={refreshTrigger} />

        {/* 支出一覧 */}
        <ExpenseList
          onUpdate={handleUpdateExpense}
          onDelete={handleDeleteExpense}
          refreshTrigger={refreshTrigger}
        />
      </div>
    </AppLayout>
  )
}

