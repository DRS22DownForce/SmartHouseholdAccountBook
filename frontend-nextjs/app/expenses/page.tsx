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
import { Header } from "@/components/dashboard/Header"
import { ExpenseList } from "@/components/expense-list"
import { LoadingSpinner } from "@/components/ui/loading-spinner"
import { getUserDisplayName } from "@/lib/user-utils"
import { useExpensesPageLogic } from "@/hooks/use-expenses-page-logic"

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
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/20">
      <Header
        username={username}
        onLogout={signOut}
        onAddExpense={handleAddExpense}
        onAddExpenses={handleAddExpenses}
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

