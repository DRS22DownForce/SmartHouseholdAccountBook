"use client"

/**
 * 月別支出取得用フック
 * 
 * このフックは、指定された月の支出データをバックエンドAPIから取得します。
 * ページネーション対応で、必要な月のデータのみを取得することで通信量を削減します。
 */

import { useState, useEffect, useCallback } from "react"
import { fetchMonthlyExpenses } from "@/api/expenseApi"
import type { Expense } from "@/lib/types"
import { showApiErrorMessage } from "@/lib/api-error-handler"

/**
 * 月別支出取得用フック
 * 
 * このフックは、指定された月の支出データをバックエンドAPIから取得します。
 * ページネーション対応で、必要な月のデータのみを取得することで通信量を削減します。
 * 
 * @param month 対象月（YYYY-MM形式）
 * @returns 支出リスト、読み込み状態、再取得関数
 */
export function useMonthlyExpenses(month: string) {
  const [expenses, setExpenses] = useState<Expense[]>([])
  const [isLoaded, setIsLoaded] = useState(false)

  // データ取得の共通ロジック（monthを引数として受け取ることで、関数の再作成を避ける）
  const fetchData = useCallback(async (targetMonth: string) => {
    if (!targetMonth) {
      setIsLoaded(true)
      return
    }

    try {
      const expenseList = await fetchMonthlyExpenses(targetMonth)
      setExpenses(expenseList)
      setIsLoaded(true)
    } catch (error) {
      showApiErrorMessage(error, "支出データの取得に失敗しました")
      setIsLoaded(true)
    }
  }, []) // 依存配列は空（関数は再作成されない）

  // monthが変更されたときにデータを取得
  useEffect(() => {
    fetchData(month)
  }, [month, fetchData])

  // 再取得用の関数（現在のmonthで呼び出す）
  const refetch = useCallback(() => {
    fetchData(month)
  }, [month, fetchData])

  return {
    expenses,
    isLoaded,
    fetchMonthlyExpenses: refetch,
  }
}

