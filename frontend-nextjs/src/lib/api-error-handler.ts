import axios from "axios"
import { toast } from "sonner"

/**
 * API エラーをユーザー向けトーストに変換する。
 * 401 は authenticatedAxios 側でセッション回復・サインアウトを試みるため、
 * ここでは汎用メッセージのみ表示する。
 */
export function showApiErrorMessage(error: unknown, defaultMessage: string): void {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status

    if (status === 401) {
      toast.error("認証エラー: 再ログインしてください")
      return
    }

    if (status === 404) {
      toast.error("データが見つかりませんでした")
      return
    }
  }

  toast.error(defaultMessage)
}
