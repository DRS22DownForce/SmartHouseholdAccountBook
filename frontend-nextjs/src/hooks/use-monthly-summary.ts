"use client"

/**
 * 月別サマリー取得用フック
 * 
 * このフックは、指定された月の支出サマリーをバックエンドAPIから取得します。
 * フロントエンドでの計算を削減し、通信量を最適化するために使用します。
 */

import { useState, useEffect, useCallback, useMemo } from "react"
import { getApiClient, withAuthHeader } from "@/api/expenseApi"
import type { MonthlySummaryDto } from "@/api/generated/api"
import { showApiErrorMessage } from "@/lib/api-error-handler"

/**
 * 月別サマリーの型定義
 * バックエンドAPIから取得したDTOをフロントエンドで使用しやすい形式に変換
 */
export interface MonthlySummary {
  total: number
  count: number
  byCategory: Array<{
    category: string
    amount: number
  }>
}

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
 * 月別サマリー取得用フック
 * 
 * @param month 対象月（YYYY-MM形式）
 * @returns 月別サマリー、読み込み状態、再取得関数
 */
export function useMonthlySummary(month: string) {
  const [monthlySummary, setMonthlySummary] = useState<MonthlySummary | null>(null)
  const [isLoaded, setIsLoaded] = useState(false)

  const api = useMemo(() => getApiClient(), [])

  const fetchMonthlySummary = useCallback(async () => {
    if (!month) {
      setIsLoaded(true)
      return
    }

    try {
      const options = await withAuthHeader()
      const response = await api.apiExpensesSummaryGet(month, options)
      const summary = toMonthlySummary(response.data)
      setMonthlySummary(summary)
      setIsLoaded(true)
    } catch (error) {
      showApiErrorMessage(error, "月別サマリーの取得に失敗しました")
      setIsLoaded(true)
    }
  }, [api, month])

  useEffect(() => {
    fetchMonthlySummary()
  }, [fetchMonthlySummary])

  return {
    monthlySummary,
    isLoaded,
    fetchMonthlySummary
  }
}

