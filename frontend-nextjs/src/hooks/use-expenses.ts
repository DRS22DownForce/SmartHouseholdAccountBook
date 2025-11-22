"use client"

import { useState, useEffect, useCallback } from "react"
import { toast } from "sonner"
import {
  fetchExpenses,
  createExpense,
  createExpenses,
  updateExpense,
  deleteExpense,
} from "@/api/expenseApi"
import type { Expense, ExpenseFormData } from "@/lib/types"
import { showApiErrorMessage } from "@/lib/api-error-handler"

export function useExpenses() {
  const [expenseItems, setExpenses] = useState<Expense[]>([])
  const [isLoaded, setIsLoaded] = useState(false)

  const fetchExpensesList = useCallback(async () => {
    try {
      const expenseList = await fetchExpenses()
      setExpenses(expenseList)
      setIsLoaded(true)
    } catch (error) {
      showApiErrorMessage(error, "支出データの取得に失敗しました")
      setIsLoaded(true)
    }
  }, [])

  useEffect(() => {
    fetchExpensesList()
  }, [fetchExpensesList])

  const addExpenseItem = useCallback(
    async (data: ExpenseFormData) => {
      try {
        const newExpense = await createExpense(data)
        setExpenses((prev) => [newExpense, ...prev])
        toast.success("支出を追加しました")
      } catch (error) {
        showApiErrorMessage(error, "支出の追加に失敗しました")
      }
    },
    []
  )

  const addExpenseItems = useCallback(
    async (dataArray: ExpenseFormData[]) => {
      try {
        const newExpenses = await createExpenses(dataArray)
        setExpenses((prev) => [...newExpenses, ...prev])
        toast.success(`${newExpenses.length}件の支出を追加しました`)
      } catch (error) {
        showApiErrorMessage(error, "支出の一括追加に失敗しました")
      }
    },
    []
  )

  const updateExpenseItem = useCallback(
    async (id: string, data: ExpenseFormData) => {
      try {
        const updatedExpense = await updateExpense(id, data)
        setExpenses((prev) =>
          prev.map((expense) => (expense.id === id ? updatedExpense : expense))
        )
        toast.success("支出を更新しました")
      } catch (error) {
        showApiErrorMessage(error, "支出の更新に失敗しました")
      }
    },
    []
  )

  const deleteExpenseItem = useCallback(
    async (id: string) => {
      try {
        await deleteExpense(id)
        setExpenses((prev) => prev.filter((expense) => expense.id !== id))
        toast.success("支出を削除しました")
      } catch (error) {
        showApiErrorMessage(error, "支出の削除に失敗しました")
      }
    },
    []
  )

  return {
    expenseItems,
    addExpenseItem,
    addExpenseItems,
    updateExpenseItem,
    deleteExpenseItem,
    isLoaded,
  }
}
