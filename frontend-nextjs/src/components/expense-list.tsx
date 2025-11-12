"use client"

/**
 * 支出リストコンポーネント
 * 
 * このコンポーネントは、バックエンドAPIから月別支出を取得して表示します。
 * フロントエンドでのフィルタリングを削減し、通信量を最適化するために使用します。
 */

import { useState, useMemo, useEffect } from "react"
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
import type { ExpenseFormData } from "@/lib/types"
import { getCategoryColor } from "@/lib/category-colors"
import { formatCurrency } from "@/lib/formatters"
import { useMonthlyExpenses } from "@/hooks/use-monthly-expenses"

interface ExpenseListProps {
  onUpdate: (id: string, data: ExpenseFormData) => void
  onDelete: (id: string) => void
  refreshTrigger?: number // 支出更新・削除後に再取得するためのトリガー
}

export function ExpenseList({ onUpdate, onDelete, refreshTrigger }: ExpenseListProps) {
  const [selectedDate, setSelectedDate] = useState(new Date())

  // 選択された月をYYYY-MM形式に変換
  const selectedMonth = useMemo(() => {
    const year = selectedDate.getFullYear()
    const month = String(selectedDate.getMonth() + 1).padStart(2, "0")
    return `${year}-${month}`
  }, [selectedDate])
  

  // バックエンドAPIから月別支出を取得
  const { expenses, isLoaded, fetchMonthlyExpenses } = useMonthlyExpenses(selectedMonth)

  // refreshTriggerが変更されたときに再取得
  useEffect(() => {
    if (refreshTrigger !== undefined && refreshTrigger > 0) {
      fetchMonthlyExpenses()
    }
  }, [refreshTrigger, fetchMonthlyExpenses])

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return new Intl.DateTimeFormat("ja-JP", {
      year: "numeric",
      month: "long",
      day: "numeric",
    }).format(date)
  }

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

  const monthTotal = useMemo(() => {
    return expenses.reduce((sum, expense) => sum + expense.amount, 0)
  }, [expenses])

  // 読み込み中の表示
  if (!isLoaded) {
    return (
      <div className="space-y-6">
        <Card className="border-border/50 shadow-sm bg-gradient-to-r from-card to-card/95">
          <div className="flex items-center justify-center p-8">
            <p className="text-muted-foreground">読み込み中...</p>
          </div>
        </Card>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <Card className="border-border/50 shadow-sm bg-gradient-to-r from-card to-card/95">
        <div className="flex items-center justify-between gap-4 p-4 md:p-5">
          <Button
            variant="outline"
            size="icon"
            onClick={goToPreviousMonth}
            className="rounded-lg hover:bg-muted/80 transition-colors"
          >
            <ChevronLeft className="h-4 w-4" />
            <span className="sr-only">前月</span>
          </Button>
          <div className="flex-1 text-center">
            <h3 className="text-lg md:text-xl font-semibold text-foreground tracking-tight">
              {formatMonthYear(selectedDate)}
            </h3>
            <p className="text-sm md:text-base text-muted-foreground mt-2 font-medium">
              合計: <span className="text-foreground font-semibold">{formatCurrency(monthTotal)}</span>
            </p>
          </div>
          <Button
            variant="outline"
            size="icon"
            onClick={goToNextMonth}
            className="rounded-lg hover:bg-muted/80 transition-colors"
          >
            <ChevronRight className="h-4 w-4" />
            <span className="sr-only">次月</span>
          </Button>
        </div>
      </Card>

      {!isCurrentMonth() && (
        <div className="text-center">
          <Button
            variant="ghost"
            size="sm"
            onClick={goToCurrentMonth}
            className="rounded-lg hover:bg-primary/10 hover:text-primary transition-colors"
          >
            今月に戻る
          </Button>
        </div>
      )}

      {expenses.length === 0 ? (
        <Card className="p-16 md:p-20 text-center border-2 border-dashed border-muted-foreground/20 shadow-sm bg-muted/30">
          <p className="text-muted-foreground text-lg md:text-xl font-medium">この月の支出はありません</p>
        </Card>
      ) : (
        <div className="max-h-[600px] overflow-y-auto pr-2 space-y-4 scrollbar-thin scrollbar-thumb-border scrollbar-track-transparent">
          {expenses.map((expense) => (
            <Card
              key={expense.id}
              className="group p-5 md:p-6 hover:shadow-lg hover:shadow-primary/5 transition-all duration-300 border-border/60 bg-gradient-to-br from-card to-card/95 hover:border-primary/20"
            >
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-3 mb-4">
                    <span
                      className="inline-flex items-center rounded-lg px-3 py-1.5 text-xs md:text-sm font-semibold text-white shadow-md transition-transform group-hover:scale-105"
                      style={{
                        backgroundColor: getCategoryColor(expense.category),
                        boxShadow: `0 4px 12px ${getCategoryColor(expense.category)}40`,
                      }}
                    >
                      {expense.category}
                    </span>
                    <span className="text-xs md:text-sm text-muted-foreground font-medium">
                      {formatDate(expense.date)}
                    </span>
                  </div>
                  <p className="font-semibold text-foreground mb-3 text-base md:text-lg">{expense.description}</p>
                  <p className="text-lg md:text-xl font-bold text-foreground tracking-tight">
                    {formatCurrency(expense.amount)}
                  </p>
                </div>
                <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                  <ExpenseForm
                    expense={expense}
                    onSubmit={(data) => onUpdate(expense.id, data)}
                    trigger={
                      <Button
                        variant="ghost"
                        size="icon"
                        className="rounded-lg hover:bg-primary/10 hover:text-primary transition-colors"
                      >
                        <Pencil className="h-4 w-4" />
                        <span className="sr-only">編集</span>
                      </Button>
                    }
                  />
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="rounded-lg hover:bg-destructive/10 hover:text-destructive transition-colors"
                      >
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
