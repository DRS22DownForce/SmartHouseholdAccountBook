"use client"

/**
 * ホームページコンポーネント
 * 
 * 支出追加後に月別サマリーと支出の推移を自動的に再取得します。
 * ロジックはuse-home-page-logicカスタムフックに分離されています。
 */

import { useMemo } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { useExpenses } from "@/hooks/use-expenses"
import { Header } from "@/components/dashboard/Header"
import { ExpenseTrendChart } from "@/components/expense-trend-chart"
import { MonthlySummarySection } from "@/components/dashboard/MonthlySummarySection"
import { LoadingSpinner } from "@/components/ui/loading-spinner"
import { getUserDisplayName } from "@/lib/user-utils"
import { useHomePageLogic } from "@/hooks/use-home-page-logic"

export default function HomePage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const { isLoaded } = useExpenses()
  const username = useMemo(() => getUserDisplayName(user), [user])

  // ホームページのロジック（支出追加処理、リフレッシュトリガー管理）をカスタムフックから取得
  const { refreshTrigger, handleAddExpense, handleAddExpenses } = useHomePageLogic()

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

      <main className="container mx-auto max-w-7xl px-6 md:px-8 lg:px-12 py-1 md:py-2">
        <div className="space-y-2 md:space-y-2.5">
          <ExpenseTrendChart refreshTrigger={refreshTrigger} />
          <MonthlySummarySection refreshTrigger={refreshTrigger} />
        </div>
      </main>
    </div>
  )
}
