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
import { AppLayout } from "@/components/layout/AppLayout"
import { BalanceSummarySection } from "@/components/dashboard/BalanceSummarySection"
import { BudgetStatusSection } from "@/components/dashboard/BudgetStatusSection"
import { GoalsStatusSection } from "@/components/dashboard/GoalsStatusSection"
import { RecentTransactionsSection } from "@/components/dashboard/RecentTransactionsSection"
import { AlertsSection } from "@/components/dashboard/AlertsSection"
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
    <AppLayout
      username={username}
      onLogout={signOut}
      onAddExpense={handleAddExpense}
      onAddExpenses={handleAddExpenses}
    >
      <div className="space-y-4 md:space-y-6">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold text-foreground tracking-tight mb-1">
            ダッシュボード
          </h1>
          <p className="text-sm text-muted-foreground">
            収支の概要と分析を確認できます
          </p>
        </div>

        {/* 収支バランスサマリー */}
        <BalanceSummarySection refreshTrigger={refreshTrigger} />

        {/* アラート・通知 */}
        <AlertsSection />

        {/* 予算達成状況 */}
        <BudgetStatusSection />

        {/* 目標達成状況 */}
        <GoalsStatusSection />

        {/* 最近の取引 */}
        <RecentTransactionsSection />
      </div>
    </AppLayout>
  )
}
