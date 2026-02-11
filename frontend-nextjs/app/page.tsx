"use client"

/**
 * ルートページコンポーネント
 * 
 * ルート（/）へのアクセスを支出ページ（/expenses）にリダイレクトします。
 * バックエンドと連携している支出管理機能をメイン機能として提供するため、
 * ホームページを削除し、支出ページをデフォルトページとしました。
 * 
 * 【初心者向け解説】
 * - useEffect: コンポーネントが表示されたときに実行される処理を定義
 * - useRouter: Next.jsのルーティング機能を使用するためのフック
 * - replace: ブラウザの履歴を残さずにページ遷移（戻るボタンで戻れない）
 */

import { useEffect } from "react"
import { useRouter } from "next/navigation"

export default function RootPage() {
  const router = useRouter()

  // コンポーネントが表示されたときに支出ページにリダイレクト
  useEffect(() => {
    router.replace("/expenses")
  }, [router])

  // リダイレクト中は何も表示しない（またはローディング表示）
  return null
}
