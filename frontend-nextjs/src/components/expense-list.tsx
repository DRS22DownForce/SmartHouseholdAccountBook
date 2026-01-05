"use client"

/**
 * 支出リストコンポーネント
 * 
 * このコンポーネントは、バックエンドAPIから月別支出を取得して表示します。
 * フロントエンドでのフィルタリングを削減し、通信量を最適化するために使用します。
 */

import { useMemo, useEffect, useCallback, useState } from "react"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
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
import { Pagination } from "@/components/ui/pagination"
import { ExpenseForm } from "./expense-form"
import type { ExpenseFormProps } from "./expense-form"
import { Pencil, Trash2, ChevronLeft, ChevronRight, Search, Calendar, Tag, AlertCircle, MoreHorizontal } from "lucide-react"
import type { Expense, ExpenseFormData } from "@/lib/types"
import { getCategoryColor, getCategoryColorWithAlpha } from "@/lib/category-colors"
import { getCategoryIcon } from "@/lib/category-icons"
import { formatCurrency } from "@/lib/formatters"
import { formatDate, formatMonthYear } from "@/lib/date-formatters"
import { useMonthlyExpenses } from "@/hooks/use-monthly-expenses"
import { useDateNavigation } from "@/hooks/use-date-navigation"
import { cn } from "@/lib/utils"

interface ExpenseListProps {
  onUpdate: (id: string, data: ExpenseFormData) => void
  onDelete: (id: string) => void
  refreshTrigger?: number // 支出更新・削除後に再取得するためのトリガー
}

// 日付ごとのグループ化用ヘルパー
function groupExpensesByDate(expenses: Expense[]) {
  const groups: { date: string; items: Expense[] }[] = []

  // 日付で降順ソート
  const sortedExpenses = [...expenses].sort((a, b) =>
    new Date(b.date).getTime() - new Date(a.date).getTime()
  )

  sortedExpenses.forEach(expense => {
    const lastGroup = groups[groups.length - 1]
    if (lastGroup && lastGroup.date === expense.date) {
      lastGroup.items.push(expense)
    } else {
      groups.push({ date: expense.date, items: [expense] })
    }
  })

  return groups
}

export function ExpenseList({ onUpdate, onDelete, refreshTrigger }: ExpenseListProps) {
  // ページネーションとフィルター状態
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [searchQuery, setSearchQuery] = useState("")

  // 日付ナビゲーションロジックをカスタムフックから取得
  const {
    selectedDate,
    selectedMonth,
    isCurrentMonth,
    goToPreviousMonth,
    goToNextMonth,
    goToCurrentMonth,
  } = useDateNavigation()

  // バックエンドAPIから月別支出を取得
  const { expenses, isLoaded, fetchMonthlyExpenses } = useMonthlyExpenses(selectedMonth)

  // refreshTriggerが変更されたときに再取得
  useEffect(() => {
    if (refreshTrigger !== undefined && refreshTrigger > 0) {
      fetchMonthlyExpenses()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [refreshTrigger])

  // 月が変わったらページと検索をリセット
  useEffect(() => {
    setCurrentPage(1)
    setSearchQuery("")
  }, [selectedMonth])

  // クライアントサイドでのフィルタリング
  const filteredExpenses = useMemo(() => {
    if (!searchQuery.trim()) return expenses

    const query = searchQuery.toLowerCase()
    return expenses.filter(expense =>
      expense.description.toLowerCase().includes(query) ||
      expense.category.toLowerCase().includes(query) ||
      expense.amount.toString().includes(query)
    )
  }, [expenses, searchQuery])

  // クライアントサイドでのページネーション
  const totalItems = filteredExpenses.length
  const totalPages = Math.ceil(totalItems / pageSize)
  const paginatedExpenses = useMemo(() => {
    const start = (currentPage - 1) * pageSize
    return filteredExpenses.slice(start, start + pageSize)
  }, [filteredExpenses, currentPage, pageSize])

  // 日付ごとにグループ化
  const groupedExpenses = useMemo(() =>
    groupExpensesByDate(paginatedExpenses),
    [paginatedExpenses])

  const monthTotal = useMemo(() => {
    return filteredExpenses.reduce((sum, expense) => sum + expense.amount, 0)
  }, [filteredExpenses])

  /**
   * 編集用のExpenseFormのプロップスを生成するヘルパー関数
   */
  const createEditFormProps = useCallback(
    (expense: Expense): ExpenseFormProps => ({
      expense: expense,
      onSubmit: (data) => onUpdate(expense.id, data),
      reactNode: (
        <Button
          variant="ghost"
          size="icon"
          className="h-8 w-8 rounded-full hover:bg-primary/10 hover:text-primary transition-colors"
        >
          <Pencil className="h-3.5 w-3.5" />
          <span className="sr-only">編集</span>
        </Button>
      ),
    }),
    [onUpdate]
  )

  // 読み込み中の表示
  if (!isLoaded) {
    return (
      <div className="space-y-6">
        <Card className="border-border/40 shadow-sm bg-gradient-to-r from-card to-muted/10">
          <div className="flex items-center justify-center p-8 flex-col gap-3">
            <div className="w-6 h-6 rounded-full border-2 border-primary/30 border-t-primary animate-spin" />
            <p className="text-muted-foreground text-sm">支出リストを読み込み中...</p>
          </div>
        </Card>
      </div>
    )
  }

  return (
    <div className="space-y-4 md:space-y-6 animate-in slide-in-from-bottom-4 duration-500 fade-in">
      <Card className="border-border/40 shadow-sm bg-gradient-to-r from-card to-muted/10 overflow-hidden">
        <div className="flex flex-col md:flex-row items-center justify-between gap-4 p-4 md:p-5">
          <div className="flex items-center gap-2 w-full md:w-auto justify-between md:justify-start">
            <Button
              variant="outline"
              size="icon"
              onClick={goToPreviousMonth}
              className="h-9 w-9 rounded-xl border-border/60 hover:bg-background/80 hover:scale-105 transition-all"
            >
              <ChevronLeft className="h-4 w-4" />
              <span className="sr-only">前月</span>
            </Button>

            <div className="text-center px-4">
              <h3 className="text-lg md:text-xl font-bold text-foreground tracking-tight flex items-center justify-center gap-2">
                <Calendar className="w-5 h-5 text-primary" />
                {formatMonthYear(selectedDate)}
              </h3>
              <p className="text-xs md:text-sm text-muted-foreground mt-0.5 font-mono">
                Total: <span className="text-foreground font-bold">{formatCurrency(monthTotal)}</span>
              </p>
            </div>

            <Button
              variant="outline"
              size="icon"
              onClick={goToNextMonth}
              className="h-9 w-9 rounded-xl border-border/60 hover:bg-background/80 hover:scale-105 transition-all"
            >
              <ChevronRight className="h-4 w-4" />
              <span className="sr-only">次月</span>
            </Button>
          </div>

          <div className="w-full md:w-[300px] relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="支出を検索 (内容、カテゴリ、金額)..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-9 bg-background/50 border-border/60 focus:ring-primary/20 rounded-xl"
            />
          </div>
        </div>
      </Card>

      {!isCurrentMonth && (
        <div className="text-center">
          <Button
            variant="ghost"
            size="sm"
            onClick={goToCurrentMonth}
            className="rounded-full hover:bg-primary/10 hover:text-primary transition-all text-xs font-medium"
          >
            現在の月 ({formatMonthYear(new Date())}) に戻る
          </Button>
        </div>
      )}

      {expenses.length === 0 ? (
        <div className="p-12 md:p-16 text-center border-2 border-dashed border-muted-foreground/10 rounded-2xl bg-muted/5 flex flex-col items-center gap-4">
          <div className="p-4 bg-muted/20 rounded-full">
            <Tag className="w-8 h-8 text-muted-foreground/40" />
          </div>
          <div className="space-y-1">
            <p className="text-muted-foreground text-lg font-medium">この月の支出はありません</p>
            <p className="text-sm text-muted-foreground/60">新しい支出を追加して記録を始めましょう</p>
          </div>
        </div>
      ) : filteredExpenses.length === 0 ? (
        <div className="p-12 text-center border border-border/40 rounded-2xl bg-background/50 flex flex-col items-center gap-3">
          <Search className="w-8 h-8 text-muted-foreground/30" />
          <p className="text-muted-foreground">検索条件に一致する支出が見つかりませんでした</p>
          <Button variant="link" onClick={() => setSearchQuery("")} className="text-primary">
            検索条件をクリア
          </Button>
        </div>
      ) : (
        <>
          <div className="space-y-6">
            {groupedExpenses.map((group) => (
              <div key={group.date} className="space-y-3">
                <div className="flex items-center gap-3 sticky top-0 md:static z-10 py-2 bg-background/95 md:bg-transparent backdrop-blur-sm">
                  <div className="h-px flex-1 bg-border/40" />
                  <span className="px-3 py-1 rounded-full bg-muted/50 border border-border/40 text-xs font-semibold text-muted-foreground">
                    {formatDate(group.date)}
                  </span>
                  <div className="h-px flex-1 bg-border/40" />
                </div>

                <div className="grid gap-3">
                  {group.items.map((expense) => (
                    <div
                      key={expense.id}
                      className="group relative flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-xl bg-card border border-border/40 hover:border-primary/30 hover:shadow-md transition-all duration-300"
                    >
                      <div className="flex items-start gap-4">
                        <div className="hidden sm:flex shrink-0">
                          <div
                            className="w-10 h-10 rounded-xl flex items-center justify-center shadow-sm"
                            style={{
                              backgroundColor: getCategoryColorWithAlpha(expense.category, 0.15),
                              color: getCategoryColor(expense.category)
                            }}
                          >
                            {(() => {
                              const Icon = getCategoryIcon(expense.category);
                              return <Icon className="w-5 h-5" />;
                            })()}
                          </div>
                        </div>
                        <div className="space-y-1">
                          <div className="flex items-center gap-2">
                            <span
                              className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wider shadow-sm transition-transform hover:scale-105"
                              style={{
                                backgroundColor: getCategoryColor(expense.category),
                                color: "#fff",
                                boxShadow: `0 2px 6px ${getCategoryColor(expense.category)}40`
                              }}
                            >
                              {(() => {
                                const Icon = getCategoryIcon(expense.category);
                                return <Icon className="w-3 h-3" />;
                              })()}
                              {expense.category}
                            </span>
                            <span className="sm:hidden text-xs text-muted-foreground">
                              {formatDate(expense.date)}
                            </span>
                          </div>
                          <p className="font-medium text-foreground text-sm md:text-base line-clamp-1">
                            {expense.description}
                          </p>
                        </div>
                      </div>

                      <div className="flex items-center justify-between sm:justify-end gap-3 sm:gap-6 pl-14 sm:pl-0">
                        <span className="text-lg font-bold font-mono tracking-tight">
                          {formatCurrency(expense.amount)}
                        </span>

                        <div className="flex items-center gap-1 opacity-100 sm:opacity-0 sm:group-hover:opacity-100 transition-opacity">
                          <ExpenseForm {...createEditFormProps(expense)} />
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8 rounded-full hover:bg-destructive/10 hover:text-destructive transition-colors"
                              >
                                <Trash2 className="h-3.5 w-3.5" />
                                <span className="sr-only">削除</span>
                              </Button>
                            </AlertDialogTrigger>
                            <AlertDialogContent>
                              <AlertDialogHeader>
                                <AlertDialogTitle className="flex items-center gap-2 text-destructive">
                                  <AlertCircle className="w-5 h-5" />
                                  支出を削除しますか？
                                </AlertDialogTitle>
                                <AlertDialogDescription>
                                  「{expense.description}」({formatCurrency(expense.amount)}) を削除しようとしています。<br />
                                  この操作は取り消せません。
                                </AlertDialogDescription>
                              </AlertDialogHeader>
                              <AlertDialogFooter>
                                <AlertDialogCancel>キャンセル</AlertDialogCancel>
                                <AlertDialogAction
                                  onClick={() => onDelete(expense.id)}
                                  className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                                >
                                  削除する
                                </AlertDialogAction>
                              </AlertDialogFooter>
                            </AlertDialogContent>
                          </AlertDialog>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>

          {/* ページネーション */}
          {totalItems > 0 && totalPages > 1 && (
            <div className="mt-8 flex justify-center">
              <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={setCurrentPage}
                pageSize={pageSize}
                onPageSizeChange={setPageSize}
              />
            </div>
          )}
        </>
      )}
    </div>
  )
}
