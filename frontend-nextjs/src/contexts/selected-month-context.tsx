"use client"

import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from "react"
import { formatYearMonth } from "@/lib/date-utils"

/** "YYYY-MM" 文字列をその月の1日の Date に変換 */
function monthStringToDate(month: string): Date {
  const [year, monthNum] = month.split("-").map(Number)
  return new Date(year, monthNum - 1, 1)
}

interface SelectedMonthContextValue {
  /** API 用の月キー（YYYY-MM） */
  selectedMonth: string
  /** 一覧ヘッダー表示用（formatMonthYear 向け） */
  selectedDate: Date
  setSelectedMonth: (month: string) => void
  isCurrentMonth: boolean
  goToPreviousMonth: () => void
  goToNextMonth: () => void
  goToCurrentMonth: () => void
}

const SelectedMonthContext = createContext<SelectedMonthContextValue | null>(
  null
)

/**
 * 支出一覧・月別サマリーで共有する「選択中の月」。
 * 兄弟コンポーネント間で月ナビと Select が同期する。
 */
export function SelectedMonthProvider({ children }: { children: ReactNode }) {
  const [selectedDate, setSelectedDate] = useState(() => new Date())

  const selectedMonth = useMemo(
    () => formatYearMonth(selectedDate),
    [selectedDate]
  )

  const setSelectedMonth = useCallback((month: string) => {
    setSelectedDate(monthStringToDate(month))
  }, [])

  const goToPreviousMonth = useCallback(() => {
    setSelectedDate((prev) => {
      const next = new Date(prev)
      next.setMonth(next.getMonth() - 1)
      return next
    })
  }, [])

  const goToNextMonth = useCallback(() => {
    setSelectedDate((prev) => {
      const next = new Date(prev)
      next.setMonth(next.getMonth() + 1)
      return next
    })
  }, [])

  const goToCurrentMonth = useCallback(() => {
    setSelectedDate(new Date())
  }, [])

  const isCurrentMonth = useMemo(() => {
    const now = new Date()
    return (
      selectedDate.getFullYear() === now.getFullYear() &&
      selectedDate.getMonth() === now.getMonth()
    )
  }, [selectedDate])

  const value = useMemo(
    () => ({
      selectedMonth,
      selectedDate,
      setSelectedMonth,
      isCurrentMonth,
      goToPreviousMonth,
      goToNextMonth,
      goToCurrentMonth,
    }),
    [
      selectedMonth,
      selectedDate,
      setSelectedMonth,
      isCurrentMonth,
      goToPreviousMonth,
      goToNextMonth,
      goToCurrentMonth,
    ]
  )

  return (
    <SelectedMonthContext.Provider value={value}>
      {children}
    </SelectedMonthContext.Provider>
  )
}

export function useSelectedMonth(): SelectedMonthContextValue {
  const ctx = useContext(SelectedMonthContext)
  if (!ctx) {
    throw new Error(
      "useSelectedMonth は SelectedMonthProvider の内側で使ってください"
    )
  }
  return ctx
}
