"use client"

/**
 * ホームページのロジックを管理するカスタムフック
 * 
 * ホームページで使用するビジネスロジック（支出追加処理、リフレッシュトリガー管理）を
 * UIコンポーネントから分離して管理します。
 */

import { useState, useCallback } from "react"
import { useExpenses } from "@/hooks/use-expenses"
import type { ExpenseFormData } from "@/lib/types"

/**
 * ホームページのロジックを提供するカスタムフック
 * 
 * 支出追加処理と、それに伴うデータ再取得のトリガー管理を行います。
 * 
 * @returns 支出追加ハンドラーとリフレッシュトリガー
 */
export function useHomePageLogic() {
    const { addExpenseItem, addExpenseItems } = useExpenses()
    const [refreshTrigger, setRefreshTrigger] = useState(0)

    /**
     * 支出を追加するハンドラー関数
     * 
     * 支出を追加した後、月別サマリーと支出の推移を再取得するために
     * リフレッシュトリガーを更新します。
     * 
     * @param data - 追加する支出データ
     */
    const handleAddExpense = useCallback(async (data: ExpenseFormData) => {
        await addExpenseItem(data)
        // 月別サマリーと支出の推移を再取得するためにトリガーを更新
        setRefreshTrigger((prev) => prev + 1)
    }, [addExpenseItem])

    /**
     * 支出を一括追加するハンドラー関数
     * 
     * 複数の支出を追加した後、月別サマリーと支出の推移を再取得するために
     * リフレッシュトリガーを更新します。
     * 
     * @param dataArray - 追加する支出データの配列
     */
    const handleAddExpenses = useCallback(async (dataArray: ExpenseFormData[]) => {
        await addExpenseItems(dataArray)
        // 月別サマリーと支出の推移を再取得するためにトリガーを更新
        setRefreshTrigger((prev) => prev + 1)
    }, [addExpenseItems])

    /**
     * CSVアップロード後のデータ再取得ハンドラー関数
     * 
     * CSVアップロードが完了した後、月別サマリーと支出の推移を再取得するために
     * リフレッシュトリガーを更新します。
     */
    const handleCsvUploadComplete = useCallback(() => {
        // 月別サマリーと支出の推移を再取得するためにトリガーを更新
        setRefreshTrigger((prev) => prev + 1)
    }, [])

    return {
        refreshTrigger,
        handleAddExpense,
        handleAddExpenses,
        handleCsvUploadComplete,
    }
}

