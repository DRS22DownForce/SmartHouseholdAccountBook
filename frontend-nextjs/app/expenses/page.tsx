"use client"

/**
 * 支出一覧ページコンポーネント
 * 
 * 支出更新・削除後に画面を自動的に再取得します。
 */

import { useMemo, useState, useCallback } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { useExpenses } from "@/hooks/use-expenses"
import { Header } from "@/components/dashboard/Header"
import { ExpenseList } from "@/components/expense-list"
import type { ExpenseFormData } from "@/lib/types"

function LoadingSpinner() {
  return (
    <div className="min-h-screen flex items-center justify-center">
      <p className="text-muted-foreground">読み込み中...</p>
    </div>
  )
}

function getUserDisplayName(user: ReturnType<typeof useAuthenticator>["user"]): string {
  return user.signInDetails?.loginId || user.username
}

export default function ExpensesPage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const { expenseItems, addExpenseItem, addExpenseItems, updateExpenseItem, deleteExpenseItem, isLoaded } =
    useExpenses()
  const username = useMemo(() => getUserDisplayName(user), [user])


  // 支出更新・削除後に画面を再取得するためのトリガー
  const [refreshTrigger, setRefreshTrigger] = useState(0)

  // 支出追加後にrefetchを呼び出すラッパー関数
  const handleAddExpense = useCallback(async (data: ExpenseFormData) => {
    await addExpenseItem(data)
    setRefreshTrigger(prev => prev + 1)
  }, [addExpenseItem])

  const handleAddExpenses = useCallback(async (dataArray: ExpenseFormData[]) => {
    await addExpenseItems(dataArray)
    setRefreshTrigger(prev => prev + 1)
  }, [addExpenseItems])

  const handleUpdateExpense = useCallback(async (id: string, data: ExpenseFormData) => {
    await updateExpenseItem(id, data)
    setRefreshTrigger(prev => prev + 1)
  }, [updateExpenseItem])

  const handleDeleteExpense = useCallback(async (id: string) => {
    await deleteExpenseItem(id)
    setRefreshTrigger(prev => prev + 1)
  }, [deleteExpenseItem])

  if (!isLoaded) {
    return <LoadingSpinner />
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/20">
      <Header
        expenses={expenseItems}
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

