"use client"

/**
 * 月別支出取得用フック
 * 
 * このフックは、指定された月の支出データをバックエンドAPIから取得します。
 * ページネーション対応で、必要な月のデータのみを取得することで通信量を削減します。
 */

import { useState, useEffect, useCallback, useMemo } from "react"
import { getApiClient, withAuthHeader } from "@/api/expenseApi"
import type { ExpenseDto } from "@/api/generated/api"
import type { Expense } from "@/lib/types"
import { showApiErrorMessage } from "@/lib/api-error-handler"

/**
 * DTOからフロントエンド用の型に変換
 * 
 * @param dto バックエンドAPIから取得したDTO
 * @returns フロントエンド用の支出データ
 */
function toExpense(dto: ExpenseDto): Expense {
  return {
    id: String(dto.id),
    amount: dto.amount ?? 0,
    category: dto.category ?? "",
    description: dto.description ?? "",
    date: dto.date ?? "",
    createdAt: new Date().toISOString(),
  }
}

/**
 * 月別支出取得用フック
 * 
 * @param month 対象月（YYYY-MM形式）
 * @returns 支出リスト、読み込み状態、再取得関数
 */
export function useMonthlyExpenses(month: string) {
  const [expenses, setExpenses] = useState<Expense[]>([])
  const [isLoaded, setIsLoaded] = useState(false)

  const api = useMemo(() => getApiClient(), [])

  const fetchMonthlyExpenses = useCallback(async () => {
    if (!month) {
      setIsLoaded(true)
      return
    }

    try {
      const options = await withAuthHeader()
      // 月別フィルタリングで支出を取得（ページネーションなし、全件取得）
      // monthパラメータを直接渡す（生成されたAPIクライアントが対応済み）
      const response = await api.apiExpensesGet(month, options)
      const expenseList = response.data.map(toExpense)
      setExpenses(expenseList)
      setIsLoaded(true)
    } catch (error) {
      showApiErrorMessage(error, "支出データの取得に失敗しました")
      setIsLoaded(true)
    }
  }, [api, month])

  useEffect(() => {
    fetchMonthlyExpenses()
  }, [fetchMonthlyExpenses])

  return {
    expenses,
    isLoaded,
    fetchMonthlyExpenses,
  }
}

