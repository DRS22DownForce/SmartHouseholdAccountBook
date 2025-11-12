"use client"

/**
 * 範囲指定で月別サマリー取得用フック
 * 
 * このフックは、指定された範囲の各月の支出サマリーをバックエンドAPIから取得します。
 * expense-trend-chartコンポーネントで使用します。
 */

import { useState, useEffect, useCallback, useMemo } from "react"
import { getApiClient, withAuthHeader } from "@/api/expenseApi"
import type { MonthlySummaryDto } from "@/api/generated/api"
import type { MonthlySummary } from "./use-monthly-summary"
import { showApiErrorMessage } from "@/lib/api-error-handler"

/**
 * DTOからフロントエンド用の型に変換
 * 
 * @param dto バックエンドAPIから取得したDTO
 * @returns フロントエンド用の月別サマリー
 */
function toMonthlySummary(dto: MonthlySummaryDto): MonthlySummary {
  return {
    total: dto.total ?? 0,
    count: dto.count ?? 0,
    byCategory: (dto.byCategory ?? []).map((item) => ({
      category: item.category ?? "",
      amount: item.amount ?? 0,
    })),
  }
}

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

  const api = useMemo(() => getApiClient(), [])

  const fetchMonthlySummaryRange = useCallback(async () => {
    if (!startMonth || !endMonth) {
      setIsLoaded(true)
      return
    }

    try {
      const options = await withAuthHeader()
      const response = await api.apiExpensesSummaryRangeGet(startMonth, endMonth, options)
      const summaries = response.data.map(toMonthlySummary)
      setMonthlySummaries(summaries)
      setIsLoaded(true)
    } catch (error) {
      showApiErrorMessage(error, "月別サマリーの取得に失敗しました")
      setIsLoaded(true)
    }
  }, [api, startMonth, endMonth])

  useEffect(() => {
    fetchMonthlySummaryRange()
  }, [fetchMonthlySummaryRange])

  return {
    monthlySummaries,
    isLoaded,
    refetch: fetchMonthlySummaryRange,
  }
}

