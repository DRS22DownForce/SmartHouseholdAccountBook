"use client"

import { useState, useCallback } from "react"
import { getMonthlyReport } from "@/api/reportApi"
import type { MonthlyReportResponse } from "@/api/generated/api"

interface UseMonthlyReportReturn {
    report: MonthlyReportResponse | null
    isLoading: boolean
    error: string | null
    fetchReport: (month: string) => Promise<void>
    regenerateReport: (month: string) => Promise<void>
    loadCachedReport: (month: string) => Promise<void>
    clearReport: () => void
}

/**
 * 月次AIレポートを取得するカスタムフック
 */
export function useMonthlyReport(): UseMonthlyReportReturn {
    const [report, setReport] = useState<MonthlyReportResponse | null>(null)
    const [isLoading, setIsLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    /**
     * レポートを表示する。キャッシュがあればそれを返し、なければ新規生成する。
     * generate=false → キャッシュのみ取得（204ならなし）。null なら generate=true で生成。
     */
    const fetchReport = useCallback(async (month: string) => {
        setIsLoading(true)
        setError(null)
        try {
            const cached = await getMonthlyReport(month, false)
            if (cached != null) {
                setReport(cached)
                return
            }
            const data = await getMonthlyReport(month, true)
            setReport(data ?? null)
        } catch (err) {
            console.error("Failed to fetch monthly report:", err)
            setError("レポートの取得に失敗しました。しばらく経ってから再度お試しください。")
        } finally {
            setIsLoading(false)
        }
    }, [])

    const regenerateReport = useCallback(async (month: string) => {
        setIsLoading(true)
        setError(null)
        try {
            const data = await getMonthlyReport(month, true)
            setReport(data ?? null)
        } catch (err) {
            console.error("Failed to regenerate monthly report:", err)
            setError("レポートの再生成に失敗しました。しばらく経ってから再度お試しください。")
        } finally {
            setIsLoading(false)
        }
    }, [])

    /**
     * キャッシュが存在する場合のみレポートを表示する。
     * キャッシュがなければ report を null にリセットする（OpenAI非呼び出し）。
     * 月切り替え時の自動ロードに使用する。
     */
    const loadCachedReport = useCallback(async (month: string) => {
        setError(null)
        try {
            const data = await getMonthlyReport(month, false)
            setReport(data ?? null)
        } catch (err) {
            console.error("Failed to load cached report:", err)
            setReport(null)
        }
    }, [])

    const clearReport = useCallback(() => {
        setReport(null)
        setError(null)
    }, [])

    return { report, isLoading, error, fetchReport, regenerateReport, loadCachedReport, clearReport }
}
