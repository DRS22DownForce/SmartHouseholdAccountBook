"use client"

import { useCallback } from "react"
import { useMutation, useQueryClient } from "@tanstack/react-query"
import { toast } from "sonner"
import {
  createExpense,
  createExpenses,
  updateExpense,
  deleteExpense,
} from "@/api/expenseApi"
import type { ExpenseFormData } from "@/lib/types"
import { showApiErrorMessage } from "@/lib/api-error-handler"
import { invalidateExpenseQueries } from "@/lib/invalidate-expense-queries"

/**
 * 支出 CRUD を提供するフック。
 * 成功時のみキャッシュを無効化し、失敗時は false を返す。
 */
export function useExpenses() {
  const queryClient = useQueryClient()

  const invalidateOnSuccess = useCallback(() => {
    invalidateExpenseQueries(queryClient)
  }, [queryClient])

  const addMutation = useMutation({
    mutationFn: createExpense,
    onSuccess: () => {
      toast.success("支出を追加しました")
      invalidateOnSuccess()
    },
    onError: (error) => showApiErrorMessage(error, "支出の追加に失敗しました"),
  })

  const addBulkMutation = useMutation({
    mutationFn: createExpenses,
    onSuccess: (_data, variables) => {
      toast.success(`${variables.length}件の支出を追加しました`)
      invalidateOnSuccess()
    },
    onError: (error) =>
      showApiErrorMessage(error, "支出の一括追加に失敗しました"),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: ExpenseFormData }) =>
      updateExpense(id, data),
    onSuccess: () => {
      toast.success("支出を更新しました")
      invalidateOnSuccess()
    },
    onError: (error) => showApiErrorMessage(error, "支出の更新に失敗しました"),
  })

  const deleteMutation = useMutation({
    mutationFn: deleteExpense,
    onSuccess: () => {
      toast.success("支出を削除しました")
      invalidateOnSuccess()
    },
    onError: (error) => showApiErrorMessage(error, "支出の削除に失敗しました"),
  })

  const addExpenseItem = useCallback(
    async (data: ExpenseFormData): Promise<boolean> => {
      try {
        await addMutation.mutateAsync(data)
        return true
      } catch {
        return false
      }
    },
    [addMutation]
  )

  const addExpenseItems = useCallback(
    async (dataArray: ExpenseFormData[]): Promise<boolean> => {
      try {
        await addBulkMutation.mutateAsync(dataArray)
        return true
      } catch {
        return false
      }
    },
    [addBulkMutation]
  )

  const updateExpenseItem = useCallback(
    async (id: string, data: ExpenseFormData): Promise<boolean> => {
      try {
        await updateMutation.mutateAsync({ id, data })
        return true
      } catch {
        return false
      }
    },
    [updateMutation]
  )

  const deleteExpenseItem = useCallback(
    async (id: string): Promise<boolean> => {
      try {
        await deleteMutation.mutateAsync(id)
        return true
      } catch {
        return false
      }
    },
    [deleteMutation]
  )

  return {
    addExpenseItem,
    addExpenseItems,
    updateExpenseItem,
    deleteExpenseItem,
    /** CSV アップロード完了後など、支出関連キャッシュを一括無効化する */
    invalidateExpenseCaches: invalidateOnSuccess,
  }
}
