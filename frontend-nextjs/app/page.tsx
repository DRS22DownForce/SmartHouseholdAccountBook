"use client"

import { useState, useMemo, useEffect } from "react"
import { useRouter } from "next/navigation"
import { useExpenses } from "@/hooks/use-expenses"
import { useUser } from "@/hooks/use-user"
import { ExpenseForm } from "@/components/expense-form"
import { ExpenseList } from "@/components/expense-list"
import { MonthlySummary } from "@/components/monthly-summary"
import { ExpenseTrendChart } from "@/components/expense-trend-chart"
import { CsvUploadDialog } from "@/components/csv-upload-dialog"
import { UserMenu } from "@/components/user-menu"
import { Wallet } from "lucide-react"
import { AiChatDialog } from "@/components/ai-chat-dialog"

export default function HomePage() {
  const router = useRouter()
  const { expenses, addExpense, addExpenses, updateExpense, deleteExpense, isLoaded } = useExpenses()
  const { username, saveUsername, logout, isLoaded: isUserLoaded } = useUser()

  const currentMonth = useMemo(() => {
    return new Date().toISOString().substring(0, 7)
  }, [])

  const [selectedMonth, setSelectedMonth] = useState(currentMonth)

  useEffect(() => {
    if (isUserLoaded && !username) {
      router.push("/login")
    }
  }, [isUserLoaded, username, router])

  if (!isLoaded || !isUserLoaded) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-muted-foreground">読み込み中...</p>
      </div>
    )
  }

  if (!username) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-muted-foreground">リダイレクト中...</p>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-50 border-b border-border/40 bg-card/95 backdrop-blur supports-[backdrop-filter]:bg-card/60">
        <div className="container mx-auto px-4 py-3 md:py-4">
          <div className="flex items-center justify-between gap-2">
            <div className="flex items-center gap-2 md:gap-3 flex-shrink-0">
              <div className="flex h-9 w-9 md:h-11 md:w-11 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-primary/80 shadow-lg shadow-primary/25 flex-shrink-0">
                <Wallet className="h-5 w-5 md:h-6 md:w-6 text-primary-foreground" />
              </div>
              <div>
                <h1 className="text-lg md:text-2xl font-bold text-foreground tracking-tight whitespace-nowrap">
                  家計簿
                </h1>
                <p className="text-[10px] md:text-xs text-muted-foreground hidden sm:block">支出管理アプリ</p>
              </div>
            </div>
            <div className="flex items-center gap-1 md:gap-2 flex-shrink-0">
              <AiChatDialog expenses={expenses} />
              <CsvUploadDialog onUpload={addExpenses} />
              <ExpenseForm onSubmit={addExpense} />
              <UserMenu username={username} onLogout={logout} />
            </div>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-10">
        <div className="mb-10">
          <ExpenseTrendChart expenses={expenses} />
        </div>

        <div className="grid gap-10 lg:grid-cols-3">
          <div className="lg:col-span-2 space-y-8">
            <div>
              <h2 className="text-3xl font-bold text-foreground mb-6 tracking-tight">支出一覧</h2>
              <ExpenseList expenses={expenses} onUpdate={updateExpense} onDelete={deleteExpense} />
            </div>
          </div>

          <div className="lg:col-span-1">
            <MonthlySummary expenses={expenses} selectedMonth={selectedMonth} onMonthChange={setSelectedMonth} />
          </div>
        </div>
      </main>
    </div>
  )
}
