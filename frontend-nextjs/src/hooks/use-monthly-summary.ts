"use client"

import { fetchMonthlySummary } from "@/api/expenseApi"
import { expenseKeys } from "@/lib/query-keys"
import { useExpenseQuery } from "@/hooks/use-expense-query"

/**
 * 指定月の支出サマリーを取得するフック（TanStack Query）。
 * 同じ month を参照するコンポーネント間でキャッシュが共有される。
 */
export function useMonthlySummary(month: string) {
  const { data, isLoaded } = useExpenseQuery({
    queryKey: expenseKeys.monthlySummary(month),
    queryFn: () => fetchMonthlySummary(month), //awaitしていないので、結果を待たず今ある状態をreturnする。API完了後の再レンダリング時に結果が反映される。
    errorMessage: "月別サマリーの取得に失敗しました",
    enabled: Boolean(month),
  })

  return {
    monthlySummary: data ?? null,
    isLoaded,
  }
}
