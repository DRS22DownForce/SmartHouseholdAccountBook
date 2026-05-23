"use client"

import { useMemo } from "react"
import { useSelectedMonth } from "@/contexts/selected-month-context"
import { useMonthlySummary } from "@/hooks/use-monthly-summary"
import { useAvailableMonths } from "@/hooks/use-available-months"
import { useMonthlyReport } from "@/hooks/use-monthly-report"
import { transformMonthlySummaryToChartData } from "@/lib/chart-data-transformers"

/**
 * 月別サマリーセクションの状態とデータ取得をまとめるフック。
 */
export function useMonthlySummarySection() {
  const { selectedMonth, setSelectedMonth } = useSelectedMonth()

  const { monthlySummary, isLoaded: isSummaryLoaded } =
    useMonthlySummary(selectedMonth)
  const { availableMonths, isLoaded: isMonthsLoaded } = useAvailableMonths()
  const {
    report,
    isLoading: isReportLoading,
    error: reportError,
    fetchReport,
    regenerateReport,
  } = useMonthlyReport(selectedMonth)

  const chartData = useMemo(() => {
    if (!monthlySummary?.byCategory) return []
    return transformMonthlySummaryToChartData(monthlySummary)
  }, [monthlySummary])

  const totalAmount = monthlySummary?.total ?? 0
  const totalCount = monthlySummary?.count ?? 0
  const isLoaded = isSummaryLoaded && isMonthsLoaded

  return {
    selectedMonth,
    setSelectedMonth,
    monthlySummary,
    availableMonths,
    chartData,
    totalAmount,
    totalCount,
    isLoaded,
    report,
    isReportLoading,
    reportError,
    fetchReport,
    regenerateReport,
  }
}
