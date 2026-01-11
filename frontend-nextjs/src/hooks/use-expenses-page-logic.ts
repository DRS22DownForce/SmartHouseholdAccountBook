"use client"

/**
 * 支出一覧ページのロジックを管理するカスタムフック
 * 
 * 支出一覧ページで使用するビジネスロジック（支出追加・更新・削除処理、リフレッシュトリガー管理）を
 * UIコンポーネントから分離して管理します。
 */

import { useState, useCallback } from "react"
import { useExpenses } from "@/hooks/use-expenses"
import type { ExpenseFormData } from "@/lib/types"

/**
 * 支出一覧ページのロジックを提供するカスタムフック
 * 
 * 支出の追加・更新・削除処理と、それに伴うデータ再取得のトリガー管理を行います。
 * 
 * refreshTriggerは他のコンポーネント（ExpenseTrendChart, MonthlySummarySection等）
 * の再取得用に使用されます。
 * 
 * @returns 操作ハンドラーとリフレッシュトリガー
 */
export function useExpensesPageLogic() {
    const [refreshTrigger, setRefreshTrigger] = useState(0)
    const { 
        addExpenseItem, 
        addExpenseItems, 
        updateExpenseItem, 
        deleteExpenseItem 
    } = useExpenses()

    /**
     * 支出を追加するハンドラー関数
     * 
     * 支出を追加した後、他のコンポーネント（グラフ、サマリー等）を再取得するために
     * リフレッシュトリガーを更新します。
     * 
     * @param data - 追加する支出データ
     */
    const handleAddExpense = useCallback(async (data: ExpenseFormData) => {
        await addExpenseItem(data)
        setRefreshTrigger((prev) => prev + 1)
    }, [addExpenseItem])

    /**
     * 支出を一括追加するハンドラー関数
     * 
     * 複数の支出を追加した後、他のコンポーネントを再取得するために
     * リフレッシュトリガーを更新します。
     * 
     * @param dataArray - 追加する支出データの配列
     */
    const handleAddExpenses = useCallback(async (dataArray: ExpenseFormData[]) => {
        await addExpenseItems(dataArray)
        setRefreshTrigger((prev) => prev + 1)
    }, [addExpenseItems])

    /**
     * 支出を更新するハンドラー関数
     * 
     * 支出を更新した後、他のコンポーネントを再取得するために
     * リフレッシュトリガーを更新します。
     * 
     * @param id - 更新する支出のID
     * @param data - 更新する支出データ
     */
    const handleUpdateExpense = useCallback(async (id: string, data: ExpenseFormData) => {
        await updateExpenseItem(id, data)
        setRefreshTrigger((prev) => prev + 1)
    }, [updateExpenseItem])

    /**
     * 支出を削除するハンドラー関数
     * 
     * 支出を削除した後、他のコンポーネントを再取得するために
     * リフレッシュトリガーを更新します。
     * 
     * @param id - 削除する支出のID
     */
    const handleDeleteExpense = useCallback(async (id: string) => {
        await deleteExpenseItem(id)
        setRefreshTrigger((prev) => prev + 1)
    }, [deleteExpenseItem])

    return {
        refreshTrigger,
        handleAddExpense,
        handleAddExpenses,
        handleUpdateExpense,
        handleDeleteExpense,
    }
}

