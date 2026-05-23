"use client"

import { fetchMonthlySummaryRange } from "@/api/expenseApi"
import { expenseKeys } from "@/lib/query-keys"
import { useExpenseQuery } from "@/hooks/use-expense-query"

/**
 * 期間指定で月別サマリー一覧を取得するフック。
 */
export function useMonthlySummaryRange(startMonth: string, endMonth: string) {
  const { data, isLoaded } = useExpenseQuery({
    queryKey: expenseKeys.monthlySummaryRange(startMonth, endMonth),
    queryFn: () => fetchMonthlySummaryRange(startMonth, endMonth),
    errorMessage: "月別サマリーの取得に失敗しました",
    enabled: Boolean(startMonth && endMonth),
  })

  return {
    monthlySummaries: data ?? [],
    isLoaded,
  }
}
