"use client"

import { useAuthenticator } from "@aws-amplify/ui-react"
import { AppLayout } from "@/components/layout/AppLayout"
import { ExpenseList } from "@/components/expense-list"
import { ExpenseTrendChart } from "@/components/expense-trend-chart"
import { MonthlySummarySection } from "@/components/dashboard/monthly-summary-section"
import { ExpenseSummarySection } from "@/components/dashboard/expense-summary-section"
import { PageHeader } from "@/components/page-header"
import { SelectedMonthProvider } from "@/contexts/selected-month-context"
import { getUserDisplayName } from "@/lib/user-utils"
import { useExpenses } from "@/hooks/use-expenses"
import { useExpenseSummary } from "@/hooks/use-expense-summary"
import { List } from "lucide-react"
import { useMemo } from "react"

export default function ExpensesPage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const username = useMemo(() => getUserDisplayName(user), [user])

  const {
    addExpenseItem,
    addExpenseItems,
    updateExpenseItem,
    deleteExpenseItem,
    invalidateExpenseCaches,
  } = useExpenses()

  const summaryData = useExpenseSummary()

  return (
    <AppLayout
      username={username}
      onLogout={signOut}
      onAddExpense={addExpenseItem}
      onAddExpenses={addExpenseItems}
      onCsvUploadComplete={invalidateExpenseCaches}
    >
      <SelectedMonthProvider>
        <div className="space-y-6 md:space-y-8">
          <PageHeader
            icon={List}
            title="支出一覧"
            description="全ての支出履歴の確認・編集・分析"
          />

          <ExpenseSummarySection summaryData={summaryData} />
          <ExpenseTrendChart />
          <MonthlySummarySection />
          <ExpenseList
            onUpdate={updateExpenseItem}
            onDelete={deleteExpenseItem}
          />
        </div>
      </SelectedMonthProvider>
    </AppLayout>
  )
}
