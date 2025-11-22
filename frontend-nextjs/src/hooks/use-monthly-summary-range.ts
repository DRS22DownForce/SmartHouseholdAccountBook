"use client"

/**
 * 範囲指定で月別サマリー取得用フック
 * 
 * このフックは、指定された範囲の各月の支出サマリーをバックエンドAPIから取得します。
 * expense-trend-chartコンポーネントで使用します。
 */

import { useState, useEffect, useCallback } from "react"
import { fetchMonthlySummaryRange } from "@/api/expenseApi"
import type { MonthlySummary } from "@/api/expenseMappers"
import { showApiErrorMessage } from "@/lib/api-error-handler"

/**
 * 範囲指定で月別サマリー取得用フック
 * 
 * @param startMonth 開始月（YYYY-MM形式）
 * @param endMonth 終了月（YYYY-MM形式）
 * @returns 月別サマリーのリスト、読み込み状態、再取得関数
 */
export function useMonthlySummaryRange(startMonth: string, endMonth: string) {
  const [monthlySummaries, setMonthlySummaries] = useState<MonthlySummary[]>([])
  const [isLoaded, setIsLoaded] = useState(false)

  const fetchSummaries = useCallback(async () => {
    if (!startMonth || !endMonth) {
      setIsLoaded(true)
      return
    }

    try {
      const summaries = await fetchMonthlySummaryRange(startMonth, endMonth)
      setMonthlySummaries(summaries)
      setIsLoaded(true)
    } catch (error) {
      showApiErrorMessage(error, "月別サマリーの取得に失敗しました")
      setIsLoaded(true)
    }
  }, [startMonth, endMonth])

  useEffect(() => {
    fetchSummaries()
  }, [fetchSummaries])

  return {
    monthlySummaries,
    isLoaded,
    refetch: fetchSummaries,
  }
}

