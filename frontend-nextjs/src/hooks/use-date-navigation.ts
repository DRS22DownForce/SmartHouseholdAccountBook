"use client"

/**
 * 日付ナビゲーション用のカスタムフック
 * 
 * 日付の選択とナビゲーション（前月・次月への移動、現在月への戻り）を管理します。
 * 支出一覧ページなどで月を選択して表示する際に使用します。
 */

import { useState, useCallback, useMemo } from "react"

/**
 * 日付ナビゲーション用のカスタムフック
 * 
 * 選択された日付の状態管理と、月の移動操作を提供します。
 * 
 * @param initialDate - 初期日付（指定しない場合は現在の日付）
 * @returns 選択された日付、選択された月、現在月かどうかの判定、ナビゲーション関数
 * 
 * @example
 * ```typescript
 * const {
 *   selectedDate,
 *   selectedMonth,
 *   isCurrentMonth,
 *   goToPreviousMonth,
 *   goToNextMonth,
 *   goToCurrentMonth,
 * } = useDateNavigation()
 * ```
 */
export function useDateNavigation(initialDate?: Date) {
    const [selectedDate, setSelectedDate] = useState(initialDate || new Date())

    /**
     * 前月に移動する関数
     * 
     * 選択された日付を1ヶ月前に更新します。
     */
    const goToPreviousMonth = useCallback(() => {
        setSelectedDate((prev) => {
            const newDate = new Date(prev)
            newDate.setMonth(newDate.getMonth() - 1)
            return newDate
        })
    }, [])

    /**
     * 次月に移動する関数
     * 
     * 選択された日付を1ヶ月後に更新します。
     */
    const goToNextMonth = useCallback(() => {
        setSelectedDate((prev) => {
            const newDate = new Date(prev)
            newDate.setMonth(newDate.getMonth() + 1)
            return newDate
        })
    }, [])

    /**
     * 現在月に戻る関数
     * 
     * 選択された日付を現在の日付にリセットします。
     */
    const goToCurrentMonth = useCallback(() => {
        setSelectedDate(new Date())
    }, [])

    /**
     * 選択された日付から月をYYYY-MM形式で取得
     * 
     * 選択された日付から、APIで使用する月のキー（YYYY-MM形式）を生成します。
     */
    const selectedMonth = useMemo(() => {
        const year = selectedDate.getFullYear()
        const month = String(selectedDate.getMonth() + 1).padStart(2, "0")
        return `${year}-${month}`
    }, [selectedDate])

    /**
     * 選択された月が現在月かどうかを判定
     * 
     * 選択された日付が現在の年月と一致するかどうかを判定します。
     * "今月に戻る"ボタンの表示制御などに使用します。
     */
    const isCurrentMonth = useMemo(() => {
        const now = new Date()
        return selectedDate.getFullYear() === now.getFullYear() && selectedDate.getMonth() === now.getMonth()
    }, [selectedDate])

    return {
        selectedDate,
        selectedMonth,
        isCurrentMonth,
        goToPreviousMonth,
        goToNextMonth,
        goToCurrentMonth,
    }
}

