"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog"
import { ExpenseForm } from "./expense-form"
import { Pencil, Trash2, ChevronLeft, ChevronRight } from "lucide-react"
import type { Expense, ExpenseFormData } from "@/lib/types"
import { getCategoryColor } from "@/lib/category-colors"

interface ExpenseListProps {
  expenses: Expense[]
  onUpdate: (id: string, data: ExpenseFormData) => void
  onDelete: (id: string) => void
}

export function ExpenseList({ expenses, onUpdate, onDelete }: ExpenseListProps) {
  const [selectedDate, setSelectedDate] = useState(new Date())

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return new Intl.DateTimeFormat("ja-JP", {
      year: "numeric",
      month: "long",
      day: "numeric",
    }).format(date)
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("ja-JP", {
      style: "currency",
      currency: "JPY",
    }).format(amount)
  }

  const filteredExpenses = expenses.filter((expense) => {
    const expenseDate = new Date(expense.date)
    return (
      expenseDate.getFullYear() === selectedDate.getFullYear() && expenseDate.getMonth() === selectedDate.getMonth()
    )
  })

  const goToPreviousMonth = () => {
    setSelectedDate((prev) => {
      const newDate = new Date(prev)
      newDate.setMonth(newDate.getMonth() - 1)
      return newDate
    })
  }

  const goToNextMonth = () => {
    setSelectedDate((prev) => {
      const newDate = new Date(prev)
      newDate.setMonth(newDate.getMonth() + 1)
      return newDate
    })
  }

  const goToCurrentMonth = () => {
    setSelectedDate(new Date())
  }

  const formatMonthYear = (date: Date) => {
    return new Intl.DateTimeFormat("ja-JP", {
      year: "numeric",
      month: "long",
    }).format(date)
  }

  const isCurrentMonth = () => {
    const now = new Date()
    return selectedDate.getFullYear() === now.getFullYear() && selectedDate.getMonth() === now.getMonth()
  }

  const monthTotal = filteredExpenses.reduce((sum, expense) => sum + expense.amount, 0)

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-4 p-4">
        <Button variant="outline" size="icon" onClick={goToPreviousMonth} className="rounded-lg bg-transparent">
          <ChevronLeft className="h-4 w-4" />
          <span className="sr-only">前月</span>
        </Button>
        <div className="flex-1 text-center">
          <h3 className="text-xl font-bold text-foreground">{formatMonthYear(selectedDate)}</h3>
          <p className="text-sm text-muted-foreground mt-1">合計: {formatCurrency(monthTotal)}</p>
        </div>
        <Button variant="outline" size="icon" onClick={goToNextMonth} className="rounded-lg bg-transparent">
          <ChevronRight className="h-4 w-4" />
          <span className="sr-only">次月</span>
        </Button>
      </div>

      {!isCurrentMonth() && (
        <div className="text-center">
          <Button variant="ghost" size="sm" onClick={goToCurrentMonth} className="rounded-lg">
            今月に戻る
          </Button>
        </div>
      )}

      {filteredExpenses.length === 0 ? (
        <Card className="p-16 text-center border-dashed shadow-sm">
          <p className="text-muted-foreground text-lg">この月の支出はありません</p>
        </Card>
      ) : (
        <div className="max-h-[600px] overflow-y-auto pr-2 space-y-4 scrollbar-thin scrollbar-thumb-border scrollbar-track-transparent">
          {filteredExpenses.map((expense) => (
            <Card key={expense.id} className="p-5 hover:shadow-md transition-shadow border-border/50">
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-3 mb-3">
                    <span
                      className="inline-flex items-center rounded-lg px-3 py-1 text-sm font-semibold text-white shadow-sm"
                      style={{ backgroundColor: getCategoryColor(expense.category) }}
                    >
                      {expense.category}
                    </span>
                    <span className="text-sm text-muted-foreground font-medium">{formatDate(expense.date)}</span>
                  </div>
                  <p className="font-semibold text-foreground mb-2 text-lg">{expense.description}</p>
                  <p className="text-3xl font-bold text-foreground tracking-tight">{formatCurrency(expense.amount)}</p>
                </div>
                <div className="flex items-center gap-2">
                  <ExpenseForm
                    expense={expense}
                    onSubmit={(data) => onUpdate(expense.id, data)}
                    trigger={
                      <Button variant="ghost" size="icon" className="rounded-lg">
                        <Pencil className="h-4 w-4" />
                        <span className="sr-only">編集</span>
                      </Button>
                    }
                  />
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button variant="ghost" size="icon" className="rounded-lg">
                        <Trash2 className="h-4 w-4" />
                        <span className="sr-only">削除</span>
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>支出を削除しますか？</AlertDialogTitle>
                        <AlertDialogDescription>
                          この操作は取り消せません。この支出データは完全に削除されます。
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>キャンセル</AlertDialogCancel>
                        <AlertDialogAction onClick={() => onDelete(expense.id)}>削除</AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
