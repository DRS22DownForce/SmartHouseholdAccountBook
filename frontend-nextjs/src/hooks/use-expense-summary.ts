"use client"

/**
 * 支出サマリー取得用フック
 * 
 * 今月の支出サマリーと前月比を計算します。
 * MonthlySummary APIを使用してデータを取得し、前月比を自動計算します。
 * 
 * refreshTrigger: データを再取得するためのトリガー（支出追加・更新・削除時に使用）
 */

import { useMemo } from "react"
import { useMonthlySummary } from "@/hooks/use-monthly-summary"
import { useRefreshTrigger } from "@/hooks/use-refresh-trigger"
import { getCurrentMonthString } from "@/lib/formatters"
import { getPreviousMonthString } from "@/lib/date-utils"

/**
 * 支出サマリーデータの型定義
 * 
 * - monthlyTotal: 今月の支出合計額
 * - transactionCount: 今月の取引件数
 * - dailyAverage: 今月の日平均支出額
 * - monthlyChange: 前月比（パーセンテージ、オプショナル）
 */
export interface ExpenseSummaryData {
  monthlyTotal: number
  transactionCount: number
  dailyAverage: number
  monthlyChange?: number  // 前月比（パーセンテージ）
}

/**
 * 支出サマリーを取得するフック
 * 
 * 今月と前月のMonthlySummary APIを呼び出して、
 * 今月の支出サマリーと前月比を計算します。
 * 
 * 【動作の流れ】
 * 1. 今月と前月の文字列を取得（例: "2024-01", "2023-12"）
 * 2. useMonthlySummaryフックで今月と前月のサマリーを取得
 * 3. refreshTriggerが変化したときにデータを再取得
 * 4. 今月のデータから合計額、件数、日平均を計算
 * 5. 前月のデータがある場合、前月比をパーセンテージで計算
 * 
 * @param refreshTrigger - データ再取得のトリガー（オプショナル）
 * @returns 支出サマリーデータ（前月比含む）
 */
export function useExpenseSummary(refreshTrigger?: number): ExpenseSummaryData {
  // 今月と前月の文字列を取得
  const currentMonth = getCurrentMonthString()
  const previousMonth = getPreviousMonthString()
  
  // 今月と前月のサマリーを取得
  // useMonthlySummaryは内部でAPIを呼び出してデータを取得します
  const { monthlySummary: currentSummary, fetchMonthlySummary: fetchCurrentSummary } = useMonthlySummary(currentMonth)
  const { monthlySummary: previousSummary, fetchMonthlySummary: fetchPreviousSummary } = useMonthlySummary(previousMonth)

  // refreshTriggerが変化したときに今月と前月のデータを再取得
  // 支出追加・更新・削除時に自動的にデータが更新されます
  useRefreshTrigger(refreshTrigger, fetchCurrentSummary, fetchPreviousSummary)

  return useMemo(() => {
    const monthlyTotal = currentSummary?.total ?? 0
    const transactionCount = currentSummary?.count ?? 0
    
    // 日平均を計算
    const now = new Date()
    const daysInMonth = now.getDate()
    const dailyAverage = daysInMonth > 0 ? Math.round(monthlyTotal / daysInMonth) : 0

    // 前月比を計算
    let monthlyChange: number | undefined = undefined
    if (currentSummary?.total !== undefined && previousSummary?.total !== undefined && previousSummary.total > 0) {
      const changePercent = ((currentSummary.total - previousSummary.total) / previousSummary.total) * 100
      monthlyChange = Math.round(changePercent * 10) / 10
    }

    return {
      monthlyTotal,
      transactionCount,
      dailyAverage,
      monthlyChange,
    }
  }, [currentSummary, previousSummary])
}
