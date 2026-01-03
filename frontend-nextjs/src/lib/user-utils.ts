import type { useAuthenticator } from "@aws-amplify/ui-react"

/**
 * ユーザー表示名を取得するユーティリティ関数
 * 
 * AWS Amplifyの認証ユーザーオブジェクトから、表示用のユーザー名を取得します。
 * ログインIDが存在する場合はそれを優先し、なければユーザー名を使用します。
 * 
 * @param user - AWS Amplifyの認証ユーザーオブジェクト
 * @returns 表示用のユーザー名（文字列）
 * 
 * @example
 * ```typescript
 * const { user } = useAuthenticator()
 * const username = getUserDisplayName(user)
 * ```
 */
export function getUserDisplayName(
    user: ReturnType<typeof useAuthenticator>["user"]
): string {
    return user.signInDetails?.loginId || user.username
}

