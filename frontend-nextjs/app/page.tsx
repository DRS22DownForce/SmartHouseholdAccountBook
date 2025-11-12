"use client"

/**
 * ホームページコンポーネント
 * 
 * 支出追加後に月別サマリーと支出の推移を自動的に再取得します。
 */

import { useState, useMemo, memo, useCallback } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { useExpenses } from "@/hooks/use-expenses"
import { Header } from "@/components/dashboard/Header"
import { ExpenseTrendChart } from "@/components/expense-trend-chart"
import { MonthlySummarySection } from "@/components/dashboard/MonthlySummarySection"
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

const MemoizedHeader = memo(Header)
// ExpenseTrendChartはrefreshTriggerプロップを受け取るため、memo化しない

export default function HomePage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const { expenses, addExpense, addExpenses, isLoaded } = useExpenses()
  const username = useMemo(() => getUserDisplayName(user), [user])
  
  // 支出追加後に月別サマリーと支出の推移を再取得するためのトリガー
  const [refreshTrigger, setRefreshTrigger] = useState(0)

  // 支出追加後にrefetchを呼び出すラッパー関数
  const handleAddExpense = useCallback(async (data: ExpenseFormData) => {
    await addExpense(data)
    // 月別サマリーと支出の推移を再取得するためにトリガーを更新
    setRefreshTrigger(prev => prev + 1)
  }, [addExpense])

  const handleAddExpenses = useCallback(async (dataArray: ExpenseFormData[]) => {
    await addExpenses(dataArray)
    // 月別サマリーと支出の推移を再取得するためにトリガーを更新
    setRefreshTrigger(prev => prev + 1)
  }, [addExpenses])

  if (!isLoaded) {
    return <LoadingSpinner />
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/20">
      <MemoizedHeader
        expenses={expenses}
        username={username}
        onLogout={signOut}
        onAddExpense={handleAddExpense}
        onAddExpenses={handleAddExpenses}
      />

      <main className="container mx-auto max-w-7xl px-6 md:px-8 lg:px-12 py-1 md:py-2">
        <div className="space-y-2 md:space-y-2.5">
          <ExpenseTrendChart refreshTrigger={refreshTrigger} key={`trend-${refreshTrigger}`} />
          <MonthlySummarySection
            refreshTrigger={refreshTrigger} key={`summary-${refreshTrigger}`} />
        </div>
      </main>
    </div>
  )
}
