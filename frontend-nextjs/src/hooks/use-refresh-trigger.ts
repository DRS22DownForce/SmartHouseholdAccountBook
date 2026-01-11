"use client"

/**
 * refreshTriggerに基づいてデータを再取得するカスタムフック
 * 
 * このフックは、refreshTriggerが変化したときに指定された再取得関数を実行します。
 * 複数のコンポーネントで同じパターンを使用する場合に便利です。
 * 
 * @param refreshTrigger - 再取得をトリガーする数値（undefinedの場合は無視）
 * @param refetchFunctions - 実行する再取得関数の配列（可変長引数）
 * 
 * @example
 * // 単一の関数を実行する場合
 * useRefreshTrigger(refreshTrigger, fetchMonthlySummary)
 * 
 * // 複数の関数を実行する場合
 * useRefreshTrigger(refreshTrigger, fetchMonthlySummary, fetchAvailableMonths)
 */

import { useEffect } from "react"

export function useRefreshTrigger(
  refreshTrigger: number | undefined,
  ...refetchFunctions: Array<(() => void) | (() => Promise<void>)>
) {
  useEffect(() => {
    // refreshTriggerが定義されていて、0より大きい場合のみ実行
    // これにより、初回マウント時（refreshTriggerが0またはundefined）には実行されません
    if (refreshTrigger !== undefined && refreshTrigger > 0) {
      refetchFunctions.forEach(refetch => {
        refetch()
      })
    }
  }, [refreshTrigger, ...refetchFunctions])
}
