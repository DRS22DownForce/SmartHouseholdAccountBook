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
 * の再取得用に使用されます
 * 
 * @returns 支出データ、操作ハンドラー、リフレッシュトリガー
 */
export function useExpensesPageLogic() {
    const [refreshTrigger, setRefreshTrigger] = useState(0)
    const { 
        expenseItems, 
        isLoaded,
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
     * useExpenses内のデータは既にローカル状態で更新されているため、
     * 追加のAPI呼び出しは不要です。
     * 
     * @param data - 追加する支出データ
     */
    const handleAddExpense = useCallback(async (data: ExpenseFormData) => {
        await addExpenseItem(data)  // 既にローカル状態を更新
        setRefreshTrigger((prev) => prev + 1)  // 他のコンポーネント用
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
        await addExpenseItems(dataArray)  // 既にローカル状態を更新
        setRefreshTrigger((prev) => prev + 1)  // 他のコンポーネント用
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
        await updateExpenseItem(id, data)  // 既にローカル状態を更新
        setRefreshTrigger((prev) => prev + 1)  // 他のコンポーネント用
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
        await deleteExpenseItem(id)  // 既にローカル状態を更新
        setRefreshTrigger((prev) => prev + 1)  // 他のコンポーネント用
    }, [deleteExpenseItem])

    return {
        expenseItems,      // 支出データ
        isLoaded,          // 読み込み状態
        refreshTrigger,    // 他のコンポーネントの再取得用
        handleAddExpense,
        handleAddExpenses,
        handleUpdateExpense,
        handleDeleteExpense,
    }
}

