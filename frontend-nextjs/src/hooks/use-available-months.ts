"use client"

/**
 * 利用可能な月のリスト取得用フック
 * 
 * このフックは、支出データが存在する月のリストをバックエンドAPIから取得します。
 * 月別サマリーや月別支出の表示で使用する月の選択肢を提供します。
 */

import { useState, useEffect, useCallback } from "react"
import { fetchAvailableMonths } from "@/api/expenseApi"
import { showApiErrorMessage } from "@/lib/api-error-handler"

/**
 * 利用可能な月のリスト取得用フック
 * 
 * このフックは、支出データが存在する月のリストをバックエンドAPIから取得します。
 * 月別サマリーや月別支出の表示で使用する月の選択肢を提供します。
 * 
 * @returns 利用可能な月のリスト、読み込み状態、再取得関数
 */
export function useAvailableMonths() {
  const [availableMonths, setAvailableMonths] = useState<string[]>([])
  const [isLoaded, setIsLoaded] = useState(false)

  // 初回マウント時にデータを取得（依存配列が空なので1回のみ実行）
  useEffect(() => {
    const fetchMonths = async () => {
      try {
        const months = await fetchAvailableMonths()
        setAvailableMonths(months)
        setIsLoaded(true)
      } catch (error) {
        showApiErrorMessage(error, "利用可能な月の取得に失敗しました")
        setIsLoaded(true)
      }
    }

    fetchMonths()
  }, [])

  // 再取得用の関数（外部から呼び出し可能）
  const refetch = useCallback(async () => {
    try {
      const months = await fetchAvailableMonths()
      setAvailableMonths(months)
      setIsLoaded(true)
    } catch (error) {
      showApiErrorMessage(error, "利用可能な月の取得に失敗しました")
      setIsLoaded(true)
    }
  }, [])

  return {
    availableMonths,
    isLoaded,
    fetchAvailableMonths: refetch
  }
}

