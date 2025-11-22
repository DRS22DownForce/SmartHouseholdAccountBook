"use client"

/**
 * 月別サマリー取得用フック
 * 
 * このフックは、指定された月の支出サマリーをバックエンドAPIから取得します。
 * フロントエンドでの計算を削減し、通信量を最適化するために使用します。
 */

import { useState, useEffect, useCallback } from "react"
import { fetchMonthlySummary } from "@/api/expenseApi"
import type { MonthlySummary } from "@/api/expenseMappers"
import { showApiErrorMessage } from "@/lib/api-error-handler"

/**
 * 月別サマリー取得用フック
 * 
 * @param month 対象月（YYYY-MM形式）
 * @returns 月別サマリー、読み込み状態、再取得関数
 */
export function useMonthlySummary(month: string) {
  const [monthlySummary, setMonthlySummary] = useState<MonthlySummary | null>(null)
  const [isLoaded, setIsLoaded] = useState(false)

  const fetchSummary = useCallback(async () => {
    if (!month) {
      setIsLoaded(true)
      return
    }

    try {
      const summary = await fetchMonthlySummary(month)
      setMonthlySummary(summary)
      setIsLoaded(true)
    } catch (error) {
      showApiErrorMessage(error, "月別サマリーの取得に失敗しました")
      setIsLoaded(true)
    }
  }, [month])

  useEffect(() => {
    fetchSummary()
  }, [fetchSummary])

  return {
    monthlySummary,
    isLoaded,
    fetchMonthlySummary: fetchSummary
  }
}

