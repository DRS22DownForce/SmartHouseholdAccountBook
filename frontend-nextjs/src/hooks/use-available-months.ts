"use client"

import { fetchAvailableMonths } from "@/api/expenseApi"
import { expenseKeys } from "@/lib/query-keys"
import { useExpenseQuery } from "@/hooks/use-expense-query"

/**
 * 支出データが存在する月のリストを取得するフック。
 */
export function useAvailableMonths() {
  const { data, isLoaded } = useExpenseQuery({
    queryKey: expenseKeys.availableMonths(),
    queryFn: fetchAvailableMonths,
    errorMessage: "利用可能な月の取得に失敗しました",
  })

  return {
    availableMonths: data ?? [],
    isLoaded,
  }
}
