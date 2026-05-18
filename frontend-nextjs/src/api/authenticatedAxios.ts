/**
 * 認証付き Axios インスタンス。
 * リクエスト時に JWT を自動付与し、401 時はトークン更新を試みる。
 */

import axios, { type AxiosError, type InternalAxiosRequestConfig } from "axios"
import { fetchAuthSession, signOut } from "aws-amplify/auth"
import { toast } from "sonner"

/** 401 再試行済みフラグ（無限ループ防止） */
interface RetryableConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

async function resolveJwtToken(forceRefresh = false): Promise<string> {
  const session = await fetchAuthSession({ forceRefresh })
  const token = session.tokens?.idToken?.toString()
  if (!token) {
    throw new Error("認証トークンの取得に失敗しました")
  }
  return token
}

export const authenticatedAxios = axios.create()

// 全リクエストに Bearer トークンを付与
authenticatedAxios.interceptors.request.use(async (config) => {
  const token = await resolveJwtToken(false)
  config.headers.Authorization = `Bearer ${token}`
  return config
})

// 401 時: トークン強制更新 → 再試行。失敗時はサインアウト
authenticatedAxios.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const config = error.config as RetryableConfig | undefined
    const status = error.response?.status

    if (status === 401 && config && !config._retry) {
      config._retry = true
      try {
        const token = await resolveJwtToken(true)
        config.headers.Authorization = `Bearer ${token}`
        return authenticatedAxios.request(config)
      } catch {
        await signOut()
        toast.error("セッションの有効期限が切れました。再度ログインしてください。")
      }
    }

    return Promise.reject(error)
  }
)
