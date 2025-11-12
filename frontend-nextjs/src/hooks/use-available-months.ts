"use client"

/**
 * 利用可能な月のリスト取得用フック
 * 
 * このフックは、支出データが存在する月のリストをバックエンドAPIから取得します。
 * 月別サマリーや月別支出の表示で使用する月の選択肢を提供します。
 */

import { useState, useEffect, useCallback, useMemo } from "react"
import { getApiClient, withAuthHeader } from "@/api/expenseApi"
import { showApiErrorMessage } from "@/lib/api-error-handler"

/**
 * 利用可能な月のリスト取得用フック
 * 
 * @returns 利用可能な月のリスト、読み込み状態、再取得関数
 */
export function useAvailableMonths() {
  const [availableMonths, setAvailableMonths] = useState<string[]>([])
  const [isLoaded, setIsLoaded] = useState(false)

  const api = useMemo(() => getApiClient(), [])

  const fetchAvailableMonths = useCallback(async () => {
    try {
      const options = await withAuthHeader()
      const response = await api.apiExpensesMonthsGet(options)
      setAvailableMonths(response.data ?? [])
      setIsLoaded(true)
    } catch (error) {
      showApiErrorMessage(error, "利用可能な月の取得に失敗しました")
      setIsLoaded(true)
    }
  }, [api])

  useEffect(() => {
    fetchAvailableMonths()
  }, [fetchAvailableMonths])

  return {
    availableMonths,
    isLoaded,
    fetchAvailableMonths
  }
}

