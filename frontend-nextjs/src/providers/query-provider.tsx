"use client"

import { QueryClient, QueryClientProvider } from "@tanstack/react-query"
import { useState, type ReactNode } from "react"

/**
 * アプリ全体に TanStack Query（キャッシュ管理）を提供するプロバイダ。
 * 各ページやコンポーネントから TanStack Query を利用できるようにする。
 */
export function QueryProvider({ children }: { children: React.ReactNode }) {
  //useStateとラムダ式を使って、初回レンダリング時に一度だけ QueryClient を作成
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 30_000,
            retry: 1,
            refetchOnWindowFocus: false,
          },
        },
      })
  )

  return (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  )
}
