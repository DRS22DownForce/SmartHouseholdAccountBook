"use client"

import { useMemo } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { useExpenses } from "@/hooks/use-expenses"
import { Header } from "@/components/dashboard/Header"
import { ExpenseListSection } from "@/components/dashboard/ExpenseListSection"

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
  const { expenses, addExpense, addExpenses, updateExpense, deleteExpense, isLoaded } =
    useExpenses()
  const username = useMemo(() => getUserDisplayName(user), [user])

  if (!isLoaded) {
    return <LoadingSpinner />
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/20">
      <Header
        expenses={expenses}
        username={username}
        onLogout={signOut}
        onAddExpense={addExpense}
        onAddExpenses={addExpenses}
      />

      <main className="container mx-auto max-w-7xl px-6 md:px-8 lg:px-12 py-8 md:py-12">
        <div className="mb-8">
          <h1 className="text-3xl md:text-4xl font-bold text-foreground tracking-tight mb-2">
            支出一覧
          </h1>
          <p className="text-muted-foreground">全ての支出を確認し、編集・削除できます</p>
        </div>

        <ExpenseListSection expenses={expenses} onUpdate={updateExpense} onDelete={deleteExpense} />
      </main>
    </div>
  )
}

