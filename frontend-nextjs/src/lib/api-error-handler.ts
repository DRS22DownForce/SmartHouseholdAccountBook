import { toast } from "sonner"
/**
 * APIエラーハンドリング用ユーティリティ
 * 
 * @param error エラーオブジェクト（unknown型で受け取ることで、あらゆる型のエラーに対応）
 * @param defaultMessage デフォルトのエラーメッセージ（特定のステータスコードに該当しない場合に表示）
 * 
 */
export function showApiErrorMessage(error: unknown, defaultMessage: string): void {
  if (error && typeof error === "object" && "response" in error) {
    const apiError = error as { response?: { status?: number } }
    
    if (apiError.response?.status === 401) {
      toast.error("認証エラー: 再ログインしてください")
      return 
    }
    
    if (apiError.response?.status === 404) {
      toast.error("データが見つかりませんでした")
      return 
    }
  }
  toast.error(defaultMessage)
}

