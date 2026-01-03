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
 * このフックは、指定された範囲の各月の支出サマリーをバックエンドAPIから取得します。
 * expense-trend-chartコンポーネントで使用します。
 * 
 * @param startMonth 開始月（YYYY-MM形式）
 * @param endMonth 終了月（YYYY-MM形式）
 * @returns 月別サマリーのリスト、読み込み状態、再取得関数
 */
export function useMonthlySummaryRange(startMonth: string, endMonth: string) {
  const [monthlySummaries, setMonthlySummaries] = useState<MonthlySummary[]>([])
  const [isLoaded, setIsLoaded] = useState(false)

  // データ取得の共通ロジック（startMonthとendMonthを引数として受け取ることで、関数の再作成を避ける）
  const fetchData = useCallback(async (targetStartMonth: string, targetEndMonth: string) => {
    if (!targetStartMonth || !targetEndMonth) {
      setIsLoaded(true)
      return
    }

    try {
      const summaries = await fetchMonthlySummaryRange(targetStartMonth, targetEndMonth)
      setMonthlySummaries(summaries)
      setIsLoaded(true)
    } catch (error) {
      showApiErrorMessage(error, "月別サマリーの取得に失敗しました")
      setIsLoaded(true)
    }
  }, []) // 依存配列は空（関数は再作成されない）

  // startMonthまたはendMonthが変更されたときにデータを取得
  useEffect(() => {
    fetchData(startMonth, endMonth)
  }, [startMonth, endMonth, fetchData])

  // 再取得用の関数（現在のstartMonthとendMonthで呼び出す）
  const refetch = useCallback(() => {
    fetchData(startMonth, endMonth)
  }, [startMonth, endMonth, fetchData])

  return {
    monthlySummaries,
    isLoaded,
    refetch,
  }
}

