"use client"

import { fetchMonthlyExpenses } from "@/api/expenseApi"
import { expenseKeys } from "@/lib/query-keys"
import { useExpenseQuery } from "@/hooks/use-expense-query"
import type { Expense } from "@/lib/types"

export interface MonthlyExpensesResult {
  expenses: Expense[]
  totalElements: number
  totalPages: number
  currentPage: number
  isLoaded: boolean
  isFetching: boolean
}

/**
 * 月別支出を取得するフック（サーバー側ページネーション + TanStack Query）。
 */
export function useMonthlyExpenses(
  month: string,
  page: number,
  size: number
): MonthlyExpensesResult {
  const { data, isLoaded, isFetching } = useExpenseQuery({
    queryKey: expenseKeys.monthlyExpenses(month, page, size),
    queryFn: () => fetchMonthlyExpenses(month, page, size),
    errorMessage: "支出データの取得に失敗しました",
    enabled: Boolean(month),
  })

  return {
    expenses: data?.content ?? [],
    totalElements: data?.totalElements ?? 0,
    totalPages: data?.totalPages ?? 0,
    currentPage: page,
    isLoaded,
    isFetching,
  }
}
