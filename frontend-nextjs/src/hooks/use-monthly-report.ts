"use client"

import { useCallback, useState } from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { getMonthlyReport } from "@/api/reportApi"
import { reportKeys } from "@/lib/query-keys"
import { showApiErrorMessage } from "@/lib/api-error-handler"
import type { MonthlyReportResponse } from "@/api/generated/api"

/**
 * 月次 AI レポートを取得するフック（TanStack Query）。
 * 月切替時はキャッシュのみ取得し、明示操作時に生成する。
 */
export function useMonthlyReport(month: string) {
  const queryClient = useQueryClient()
  const [reportError, setReportError] = useState<string | null>(null)

  const cachedQuery = useQuery({
    queryKey: reportKeys.cached(month),
    queryFn: () => getMonthlyReport(month, false),
    enabled: Boolean(month),
    retry: false,
  })

  const generateMutation = useMutation({
    mutationFn: (targetMonth: string) => getMonthlyReport(targetMonth, true),
    onSuccess: (data, targetMonth) => {
      queryClient.setQueryData(reportKeys.cached(targetMonth), data)
      setReportError(null)
    },
    onError: (error) => {
      setReportError(
        "レポートの取得に失敗しました。しばらく経ってから再度お試しください。"
      )
      showApiErrorMessage(error, "レポートの取得に失敗しました")
    },
  })

  const regenerateMutation = useMutation({
    mutationFn: (targetMonth: string) => getMonthlyReport(targetMonth, true),
    onSuccess: (data, targetMonth) => {
      queryClient.setQueryData(reportKeys.cached(targetMonth), data)
      setReportError(null)
    },
    onError: (error) => {
      setReportError(
        "レポートの再生成に失敗しました。しばらく経ってから再度お試しください。"
      )
      showApiErrorMessage(error, "レポートの再生成に失敗しました")
    },
  })

  const fetchReport = useCallback(
    async (targetMonth: string) => {
      setReportError(null)
      try {
        const cached = await queryClient.fetchQuery({
          queryKey: reportKeys.cached(targetMonth),
          queryFn: () => getMonthlyReport(targetMonth, false),
        })
        if (cached != null) {
          queryClient.setQueryData(reportKeys.cached(targetMonth), cached)
          return
        }
        await generateMutation.mutateAsync(targetMonth)
      } catch (error) {
        if (!generateMutation.isError) {
          setReportError(
            "レポートの取得に失敗しました。しばらく経ってから再度お試しください。"
          )
          showApiErrorMessage(error, "レポートの取得に失敗しました")
        }
      }
    },
    [queryClient, generateMutation]
  )

  const regenerateReport = useCallback(
    async (targetMonth: string) => {
      setReportError(null)
      await regenerateMutation.mutateAsync(targetMonth)
    },
    [regenerateMutation]
  )

  const report: MonthlyReportResponse | null = cachedQuery.data ?? null
  const isReportLoading =
    generateMutation.isPending || regenerateMutation.isPending

  return {
    report,
    isLoading: isReportLoading,
    isReportLoading,
    error: reportError,
    reportError,
    fetchReport,
    regenerateReport,
  }
}
