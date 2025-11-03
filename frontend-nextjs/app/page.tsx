"use client"

import { useState, useMemo, memo } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { useExpenses } from "@/hooks/use-expenses"
import { Header } from "@/components/dashboard/Header"
import { ExpenseTrendChart } from "@/components/expense-trend-chart"
import { SummarySection } from "@/components/dashboard/SummarySection"
import { getCurrentMonthString } from "@/lib/formatters"

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
const MemoizedExpenseTrendChart = memo(ExpenseTrendChart)

export default function HomePage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const { expenses, addExpense, addExpenses, isLoaded } = useExpenses()
  const currentMonth = useMemo(() => getCurrentMonthString(), [])
  const [selectedMonth, setSelectedMonth] = useState(currentMonth)
  const username = useMemo(() => getUserDisplayName(user), [user])

  if (!isLoaded) {
    return <LoadingSpinner />
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/20">
      <MemoizedHeader
        expenses={expenses}
        username={username}
        onLogout={signOut}
        onAddExpense={addExpense}
        onAddExpenses={addExpenses}
      />

      <main className="container mx-auto max-w-7xl px-6 md:px-8 lg:px-12 py-1 md:py-2">
        <div className="space-y-2 md:space-y-2.5">
          <MemoizedExpenseTrendChart expenses={expenses} />
          <SummarySection
            expenses={expenses}
            selectedMonth={selectedMonth}
            onMonthChange={setSelectedMonth}
          />
        </div>
      </main>
    </div>
  )
}
