import axios from "axios"
import { toast } from "sonner"

type ErrorResponseBody = {
  message?: string
}

function extractApiErrorMessage(error: unknown): string | undefined {
  if (!axios.isAxiosError(error)) {
    return undefined
  }
  const data = error.response?.data as ErrorResponseBody | undefined
  if (data?.message && data.message.trim().length > 0) {
    return data.message
  }
  return undefined
}

/**
 * API エラーをユーザー向けトーストに変換する。
 * 401 は authenticatedAxios 側でセッション回復・サインアウトを試みるため、
 * ここでは汎用メッセージのみ表示する。
 */
export function showApiErrorMessage(error: unknown, defaultMessage: string): void {
  const apiMessage = extractApiErrorMessage(error)

  if (axios.isAxiosError(error)) {
    const status = error.response?.status

    if (status === 401) {
      toast.error("認証エラー: 再ログインしてください")
      return
    }

    if (status === 404) {
      toast.error(apiMessage ?? "データが見つかりませんでした")
      return
    }

    if (status === 400) {
      toast.error(apiMessage ?? "入力内容を確認してください")
      return
    }
  }

  toast.error(apiMessage ?? defaultMessage)
}
