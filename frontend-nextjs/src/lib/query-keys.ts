/**
 * TanStack Query のキャッシュキー定義。
 * 同じキーを使うコンポーネント間で API レスポンスが自動的に共有（dedupe）される。
 */
export const expenseKeys = {
  all: ["expenses"] as const,
  monthlySummary: (month: string) =>
    [...expenseKeys.all, "monthlySummary", month] as const,
  monthlyExpenses: (month: string, page: number, size: number) =>
    [...expenseKeys.all, "monthlyExpenses", month, page, size] as const,
  monthlySummaryRange: (startMonth: string, endMonth: string) =>
    [...expenseKeys.all, "monthlySummaryRange", startMonth, endMonth] as const,
  availableMonths: () => [...expenseKeys.all, "availableMonths"] as const,
}

/** 月次 AI レポート用のキャッシュキー */
export const reportKeys = {
  all: ["reports"] as const,
  cached: (month: string) => [...reportKeys.all, "cached", month] as const,
}
