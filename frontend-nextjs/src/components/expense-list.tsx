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
import { getCategoryColor, getCategoryColorWithAlpha, getCategoryGradient } from "@/lib/category-colors"
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
        <div className="p-16 md:p-24 text-center border-2 border-dashed border-muted-foreground/10 rounded-3xl bg-muted/5 flex flex-col items-center gap-6 animate-in fade-in zoom-in-95 duration-500">
          <div className="p-6 bg-background rounded-full shadow-lg shadow-primary/5">
            <div className="p-4 bg-primary/10 rounded-full text-primary">
              <Tag className="w-10 h-10" />
            </div>
          </div>
          <div className="space-y-2 max-w-sm">
            <h3 className="text-xl font-bold text-foreground">この月の支出はまだありません</h3>
            <p className="text-muted-foreground leading-relaxed">
              新しい支出を追加して、家計簿の記録を始めましょう。
              右上の追加ボタンから簡単に登録できます。
            </p>
          </div>
        </div>
      ) : filteredExpenses.length === 0 ? (
        <div className="p-16 text-center border border-border/40 rounded-3xl bg-background/50 flex flex-col items-center gap-4 animate-in fade-in zoom-in-95 duration-300">
          <div className="p-4 bg-muted/30 rounded-full">
            <Search className="w-8 h-8 text-muted-foreground/40" />
          </div>
          <div className="space-y-1">
            <p className="text-lg font-medium text-foreground">検索結果が見つかりません</p>
            <p className="text-muted-foreground">条件を変更して再度お試しください</p>
          </div>
          <Button variant="outline" onClick={() => setSearchQuery("")} className="mt-2 gap-2">
            検索条件をクリア
          </Button>
        </div>
      ) : (
        <>
          <div className="space-y-8">
            {groupedExpenses.map((group) => (
              <div key={group.date} className="space-y-4">
                <div className="flex items-center gap-4 sticky top-0 z-10 py-3 bg-background/95 backdrop-blur-md border-b border-border/40 md:border-none md:bg-transparent md:backdrop-blur-none">
                  <div className="flex items-baseline gap-2">
                    <span className="text-2xl font-bold tracking-tight text-foreground">
                      {new Date(group.date).getDate()}
                    </span>
                    <span className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
                      {new Date(group.date).toLocaleDateString('en-US', { weekday: 'short' })}
                    </span>
                  </div>
                  <div className="h-px flex-1 bg-gradient-to-r from-border/60 to-transparent" />
                  <span className="text-xs font-medium text-muted-foreground bg-muted/30 px-3 py-1 rounded-full border border-border/30">
                    {formatDate(group.date)}
                  </span>
                </div>

                <div className="grid gap-3 sm:grid-cols-1">
                  {group.items.map((expense) => (
                    <div
                      key={expense.id}
                      className="group relative flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-2xl bg-card/60 border border-border/40 hover:border-border/80 hover:bg-card hover:shadow-lg transition-all duration-300 hover:-translate-y-0.5"
                    >
                      <div className="flex items-start gap-4 sm:gap-5">
                        <div className="shrink-0 pt-1 sm:pt-0">
                          {/* グラデーションアイコン（ホームページと統一感のあるデザイン） */}
                          {(() => {
                            const Icon = getCategoryIcon(expense.category);
                            const { gradient, shadow } = getCategoryGradient(expense.category);
                            return (
                              <div
                                className={cn(
                                  "w-12 h-12 rounded-2xl flex items-center justify-center",
                                  "bg-gradient-to-br text-white shadow-lg",
                                  "group-hover:scale-110 group-hover:rotate-3",
                                  "transition-all duration-300",
                                  gradient,
                                  shadow
                                )}
                              >
                                <Icon className="w-6 h-6" />
                              </div>
                            );
                          })()}
                        </div>
                        <div className="space-y-1.5 flex-1 min-w-0">
                          <div className="flex flex-wrap items-center gap-2">
                            {/* カテゴリーバッジ（グラデーション対応） */}
                            {(() => {
                              const { gradient, shadow } = getCategoryGradient(expense.category);
                              return (
                                <span
                                  className={cn(
                                    "inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg",
                                    "text-[10px] font-bold uppercase tracking-wider",
                                    "bg-gradient-to-r text-white shadow-md",
                                    "transition-all hover:scale-105 hover:-translate-y-0.5 select-none",
                                    gradient,
                                    shadow
                                  )}
                                >
                                  {expense.category}
                                </span>
                              );
                            })()}
                          </div>
                          <p className="font-semibold text-foreground text-base sm:text-lg line-clamp-1 group-hover:text-primary transition-colors">
                            {expense.description}
                          </p>
                        </div>
                      </div>

                      <div className="flex items-center justify-between sm:justify-end gap-2 sm:gap-6 pl-[4.25rem] sm:pl-0">
                        <span className="text-xl sm:text-2xl font-bold font-mono tracking-tight text-foreground">
                          {formatCurrency(expense.amount)}
                        </span>

                        <div className="flex items-center gap-1 opacity-100 sm:opacity-0 sm:group-hover:opacity-100 transition-all duration-200 sm:translate-x-2 sm:group-hover:translate-x-0">
                          <ExpenseForm {...createEditFormProps(expense)} />
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <Button
                                variant="ghost"
                                size="icon"
                                className="h-9 w-9 rounded-full text-muted-foreground hover:bg-destructive/10 hover:text-destructive transition-colors"
                              >
                                <Trash2 className="h-4 w-4" />
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
