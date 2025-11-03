"use client"

import { useState, useEffect, useCallback, useMemo } from "react"
import { toast } from "sonner"
import { getApiClient, withAuthHeader } from "@/api/expenseApi"
import type { ExpenseDto, ExpenseRequestDto } from "@/api/generated/api"
import type { Expense, ExpenseFormData } from "@/lib/types"

function toExpense(dto: ExpenseDto): Expense {
  return {
    id: String(dto.id),
    amount: dto.amount,
    category: dto.category,
    description: dto.description,
    date: dto.date,
    createdAt: new Date().toISOString(),
  }
}

function toRequestDto(data: ExpenseFormData): ExpenseRequestDto {
  return {
    date: data.date,
    category: data.category,
    amount: data.amount,
    description: data.description,
  }
}

function showApiErrorMessage(error: unknown, defaultMessage: string): void {
  if (error && typeof error === "object" && "response" in error) {
    const apiError = error as { response?: { status?: number } }
    if (apiError.response?.status === 401) {
      toast.error("認証エラー: 再ログインしてください")
      return
    }
    if (apiError.response?.status === 404) {
      toast.error("データが見つかりませんでした")
      return
    }
  }
  toast.error(defaultMessage)
}

export function useExpenses() {
  const [expenses, setExpenses] = useState<Expense[]>([])
  const [isLoaded, setIsLoaded] = useState(false)

  const api = useMemo(() => getApiClient(), [])

  const fetchExpenses = useCallback(async () => {
    try {
      const options = await withAuthHeader()
      const response = await api.apiExpensesGet(options)
      const expenseList = response.data.map(toExpense)
      setExpenses(expenseList)
      setIsLoaded(true)
    } catch (error) {
      showApiErrorMessage(error, "支出データの取得に失敗しました")
      setIsLoaded(true)
    }
  }, [api])

  useEffect(() => {
    fetchExpenses()
  }, [fetchExpenses])

  const addExpense = useCallback(
    async (data: ExpenseFormData) => {
      try {
        const requestDto = toRequestDto(data)
        const options = await withAuthHeader()
        const response = await api.apiExpensesPost(requestDto, options)
        const newExpense = toExpense(response.data)
        setExpenses((prev) => [newExpense, ...prev])
        toast.success("支出を追加しました")
      } catch (error) {
        showApiErrorMessage(error, "支出の追加に失敗しました")
      }
    },
    [api]
  )

  const addExpenses = useCallback(
    async (dataArray: ExpenseFormData[]) => {
      try {
        const options = await withAuthHeader()
        const responses = await Promise.all(
          dataArray.map((data) => api.apiExpensesPost(toRequestDto(data), options))
        )
        const newExpenses = responses.map((response) => toExpense(response.data))
        setExpenses((prev) => [...newExpenses, ...prev])
        toast.success(`${newExpenses.length}件の支出を追加しました`)
      } catch (error) {
        showApiErrorMessage(error, "支出の一括追加に失敗しました")
      }
    },
    [api]
  )

  const updateExpense = useCallback(
    async (id: string, data: ExpenseFormData) => {
      try {
        const requestDto = toRequestDto(data)
        const options = await withAuthHeader()
        const response = await api.apiExpensesIdPut(Number(id), requestDto, options)
        const updatedExpense = toExpense(response.data)
        setExpenses((prev) =>
          prev.map((expense) => (expense.id === id ? updatedExpense : expense))
        )
        toast.success("支出を更新しました")
      } catch (error) {
        showApiErrorMessage(error, "支出の更新に失敗しました")
      }
    },
    [api]
  )

  const deleteExpense = useCallback(
    async (id: string) => {
      try {
        const options = await withAuthHeader()
        await api.apiExpensesIdDelete(Number(id), options)
        setExpenses((prev) => prev.filter((expense) => expense.id !== id))
        toast.success("支出を削除しました")
      } catch (error) {
        showApiErrorMessage(error, "支出の削除に失敗しました")
      }
    },
    [api]
  )

  return {
    expenses,
    addExpense,
    addExpenses,
    updateExpense,
    deleteExpense,
    isLoaded,
  }
}
