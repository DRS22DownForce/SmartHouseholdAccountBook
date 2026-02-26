"use client"

/**
 * 月別支出取得用フック（サーバー側ページネーション対応）
 *
 * 指定した月の支出を、表示するページに応じてAPIから取得します。
 * page と size を渡すことで、そのページ分だけを取得し通信量を抑えます。
 */

import { useState, useEffect, useCallback } from "react"
import { fetchMonthlyExpenses } from "@/api/expenseApi"
import type { Expense } from "@/lib/types"
import { showApiErrorMessage } from "@/lib/api-error-handler"

export interface MonthlyExpensesResult {
  expenses: Expense[]
  totalElements: number
  totalPages: number
  currentPage: number
  isLoaded: boolean
  fetchMonthlyExpenses: () => void
}

/**
 * 月別支出を取得（サーバー側ページネーション）
 *
 * @param month 対象月（YYYY-MM）
 * @param page ページ番号（0始まり）
 * @param size 1ページあたりの件数
 */
export function useMonthlyExpenses(
  month: string,
  page: number,
  size: number
): MonthlyExpensesResult {
  const [expenses, setExpenses] = useState<Expense[]>([])
  const [totalElements, setTotalElements] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [isLoaded, setIsLoaded] = useState(false)

  const fetchData = useCallback(async (targetMonth: string, pageIndex: number, pageSize: number) => {
    if (!targetMonth) {
      setIsLoaded(true)
      return
    }

    try {
      const result = await fetchMonthlyExpenses(targetMonth, pageIndex, pageSize)
      setExpenses(result.content)
      setTotalElements(result.totalElements)
      setTotalPages(result.totalPages)
    } catch (error) {
      showApiErrorMessage(error, "支出データの取得に失敗しました")
      setExpenses([])
      setTotalElements(0)
      setTotalPages(0)
    } finally {
      setIsLoaded(true)
    }
  }, [])

  useEffect(() => {
    setIsLoaded(false)
    fetchData(month, page, size)
  }, [month, page, size, fetchData])

  const refetch = useCallback(() => {
    fetchData(month, page, size)
  }, [month, page, size, fetchData])

  return {
    expenses,
    totalElements,
    totalPages,
    currentPage: page,
    isLoaded,
    fetchMonthlyExpenses: refetch,
  }
}
