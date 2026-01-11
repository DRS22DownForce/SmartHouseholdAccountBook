"use client"

import { useCallback } from "react"
import { toast } from "sonner"
import {
  createExpense,
  createExpenses,
  updateExpense,
  deleteExpense,
} from "@/api/expenseApi"
import type { ExpenseFormData } from "@/lib/types"
import { showApiErrorMessage } from "@/lib/api-error-handler"

/**
 * 支出データのCRUD操作を提供するカスタムフック
 */
export function useExpenses() {
  /**
   * 支出を追加する関数
   * 
   * APIを呼び出して支出を追加します。
   * 
   * @param data - 追加する支出データ
   */
  const addExpenseItem = useCallback(
    async (data: ExpenseFormData) => {
      try {
        await createExpense(data)
        toast.success("支出を追加しました")
      } catch (error) {
        showApiErrorMessage(error, "支出の追加に失敗しました")
      }
    },
    []
  )

  /**
   * 支出を一括追加する関数
   * 
   * 複数の支出を一度に追加します。
   * 
   * @param dataArray - 追加する支出データの配列
   */
  const addExpenseItems = useCallback(
    async (dataArray: ExpenseFormData[]) => {
      try {
        await createExpenses(dataArray)
        toast.success(`${dataArray.length}件の支出を追加しました`)
      } catch (error) {
        showApiErrorMessage(error, "支出の一括追加に失敗しました")
      }
    },
    []
  )

  /**
   * 支出を更新する関数
   * 
   * 既存の支出データを更新します。
   * 
   * @param id - 更新する支出のID
   * @param data - 更新する支出データ
   */
  const updateExpenseItem = useCallback(
    async (id: string, data: ExpenseFormData) => {
      try {
        await updateExpense(id, data)
        toast.success("支出を更新しました")
      } catch (error) {
        showApiErrorMessage(error, "支出の更新に失敗しました")
      }
    },
    []
  )

  /**
   * 支出を削除する関数
   * 
   * 指定されたIDの支出を削除します。
   * 
   * @param id - 削除する支出のID
   */
  const deleteExpenseItem = useCallback(
    async (id: string) => {
      try {
        await deleteExpense(id)
        toast.success("支出を削除しました")
      } catch (error) {
        showApiErrorMessage(error, "支出の削除に失敗しました")
      }
    },
    []
  )

  return {
    addExpenseItem,
    addExpenseItems,
    updateExpenseItem,
    deleteExpenseItem,
  }
}
