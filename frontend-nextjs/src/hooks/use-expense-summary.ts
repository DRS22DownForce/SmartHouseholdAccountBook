"use client"

import { useMemo } from "react"
import { useMonthlySummary } from "@/hooks/use-monthly-summary"
import { getCurrentMonthString } from "@/lib/formatters"
import { getPreviousMonthString } from "@/lib/date-utils"

export interface ExpenseSummaryData {
  monthlyTotal: number
  transactionCount: number
  dailyAverage: number
  monthlyChange?: number
}

export interface ExpenseSummaryResult extends ExpenseSummaryData {
  /** 今月のサマリー取得が完了したか（表示用） */
  isLoaded: boolean
}

/**
 * 今月の支出サマリーと前月比を算出するフック。
 * 今月・前月は同一キャッシュキーで dedupe される。
 */
export function useExpenseSummary(): ExpenseSummaryResult {
  const currentMonth = getCurrentMonthString()
  const previousMonth = getPreviousMonthString()

  const {
    monthlySummary: currentSummary,
    isLoaded: isCurrentSummaryLoaded,
  } = useMonthlySummary(currentMonth)
  const { monthlySummary: previousSummary } = useMonthlySummary(previousMonth)

  const summaryData = useMemo(() => {
    const monthlyTotal = currentSummary?.total ?? 0
    const transactionCount = currentSummary?.count ?? 0

    const now = new Date()
    const daysInMonth = now.getDate()
    const dailyAverage =
      daysInMonth > 0 ? Math.round(monthlyTotal / daysInMonth) : 0

    let monthlyChange: number | undefined
    if (
      currentSummary?.total !== undefined &&
      previousSummary?.total !== undefined &&
      previousSummary.total > 0
    ) {
      const changePercent =
        ((currentSummary.total - previousSummary.total) / previousSummary.total) *
        100
      monthlyChange = Math.round(changePercent * 10) / 10
    }

    return {
      monthlyTotal,
      transactionCount,
      dailyAverage,
      monthlyChange,
    }
  }, [currentSummary, previousSummary])

  return {
    ...summaryData,
    isLoaded: isCurrentSummaryLoaded,
  }
}
