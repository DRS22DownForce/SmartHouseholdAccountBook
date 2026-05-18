"use client"

import { useEffect } from "react"
import { useQuery, type QueryKey } from "@tanstack/react-query"
import { showApiErrorMessage } from "@/lib/api-error-handler"

interface UseExpenseQueryOptions<TData> {
  queryKey: QueryKey
  queryFn: () => Promise<TData>
  errorMessage: string
  enabled?: boolean
}

/**
 * 支出 API 用の TanStack Query ラッパー。
 * エラー時のトースト表示を共通化する。
 */
export function useExpenseQuery<TData>({
  queryKey,
  queryFn,
  errorMessage,
  enabled = true,
}: UseExpenseQueryOptions<TData>) {
  const query = useQuery({
    queryKey,
    queryFn,
    enabled,
  })

  useEffect(() => {
    if (query.isError) {
      showApiErrorMessage(query.error, errorMessage)
    }
  }, [query.isError, query.error, errorMessage])

  return {
    data: query.data,
    isLoaded: !query.isLoading,
  }
}
