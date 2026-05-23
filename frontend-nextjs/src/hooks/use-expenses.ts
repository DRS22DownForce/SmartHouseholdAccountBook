"use client"

import { useCallback } from "react"
import { useMutation, useQueryClient } from "@tanstack/react-query"
import { toast } from "sonner"
import {
  createExpense,
  updateExpense,
  deleteExpense,
} from "@/api/expenseApi"
import type { ExpenseFormData } from "@/lib/types"
import { showApiErrorMessage } from "@/lib/api-error-handler"
import { expenseKeys } from "@/lib/query-keys"

/**
 * 支出 CRUD を提供するフック。
 * 成功時のみキャッシュを無効化し、失敗時は false を返す。
 */
export function useExpenses() {
  const queryClient = useQueryClient()

  //支出関連のキャッシュを無効化する関数コンポーネント
  const invalidateExpenseCaches = useCallback(() => {
    void queryClient.invalidateQueries({ queryKey: expenseKeys.all })
  }, [queryClient])

  const addMutation = useMutation({
    mutationFn: createExpense,
    onSuccess: () => {
      toast.success("支出を追加しました")
      invalidateExpenseCaches()
    },
    onError: (error) => showApiErrorMessage(error, "支出の追加に失敗しました"),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: ExpenseFormData }) =>
      updateExpense(id, data),
    onSuccess: () => {
      toast.success("支出を更新しました")
      invalidateExpenseCaches()
    },
    onError: (error) => showApiErrorMessage(error, "支出の更新に失敗しました"),
  })

  const deleteMutation = useMutation({
    mutationFn: deleteExpense,
    onSuccess: () => {
      toast.success("支出を削除しました")
      invalidateExpenseCaches()
    },
    onError: (error) => showApiErrorMessage(error, "支出の削除に失敗しました"),
  })

  //支出を追加する関数コンポーネント。例外処理でラップ
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
    updateExpenseItem,
    deleteExpenseItem,
    /** CSV アップロード完了後など、支出関連キャッシュを一括無効化する */
    invalidateExpenseCaches
  }
}
