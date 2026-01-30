"use client"

/**
 * ホームページコンポーネント
 * 
 * ダッシュボードのメインページです。
 * 収支の概要、予算状況、目標達成状況、AI分析など
 * 家計管理に必要な情報を一覧で表示します。
 * 
 * 【初心者向け解説】
 * - useMemo: 計算結果をキャッシュして、無駄な再計算を防ぐ
 * - refreshTrigger: 支出追加後にデータを更新するためのトリガー
 * - AppLayout: サイドバーなどの共通レイアウトを提供
 * 
 * 【レイアウト構成】
 * 1. ヘッダー（タイトル）
 * 2. 収支バランスサマリー（3カラム）
 * 3. AI家計診断 + 週間カレンダー（2カラム）
 * 4. 予算達成状況 + 目標達成状況（2カラム）
 * 5. アラート
 * 6. 最近の取引
 */

import { useMemo } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { AppLayout } from "@/components/layout/AppLayout"
import { BalanceSummarySection } from "@/components/dashboard/BalanceSummarySection"
import { BudgetStatusSection } from "@/components/dashboard/BudgetStatusSection"
import { GoalsStatusSection } from "@/components/dashboard/GoalsStatusSection"
import { RecentTransactionsSection } from "@/components/dashboard/RecentTransactionsSection"
import { AlertsSection } from "@/components/dashboard/AlertsSection"
import { AiInsightsSection } from "@/components/dashboard/AiInsightsSection"
import { WeeklyCalendarSection } from "@/components/dashboard/WeeklyCalendarSection"
import { getUserDisplayName } from "@/lib/user-utils"
import { useHomePageLogic } from "@/hooks/use-home-page-logic"
import { Home, Sparkles } from "lucide-react"

export default function HomePage() {
  // AWS Cognitoの認証状態を取得
  const { user, signOut } = useAuthenticator((context) => [context.user])
  // ユーザー名を取得（メモ化して最適化）
  const username = useMemo(() => getUserDisplayName(user), [user])

  // ホームページのロジック（支出追加処理、リフレッシュトリガー管理）をカスタムフックから取得
  const { refreshTrigger, handleAddExpense, handleAddExpenses, handleCsvUploadComplete } = useHomePageLogic()

  // 各コンポーネントが自分でローディング状態を管理するため、ここでのisLoadedチェックは不要

  return (
    <AppLayout
      username={username}
      onLogout={signOut}
      onAddExpense={handleAddExpense}
      onAddExpenses={handleAddExpenses}
      onCsvUploadComplete={handleCsvUploadComplete}
    >
      {/* メインコンテンツ - 全体の間隔を統一 */}
      <div className="space-y-6 md:space-y-8">
        
        {/* ==================== */}
        {/* ヘッダーセクション */}
        {/* ==================== */}
        <div className="animate-fade-in">
          <div className="flex items-center gap-3 mb-2">
            {/* グローイングアイコン */}
            <div className="relative">
              <div className="absolute inset-0 bg-blue-500/20 rounded-xl blur-md" />
              <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-cyan-500 text-white shadow-lg">
                <Home className="h-5 w-5" />
              </div>
            </div>
            <div>
              <h1 className="text-2xl md:text-3xl font-black text-foreground tracking-tight">
                ダッシュボード
              </h1>
              <p className="text-sm text-muted-foreground flex items-center gap-1">
                <Sparkles className="w-3 h-3" />
                収支の概要と分析を確認できます
              </p>
            </div>
          </div>
        </div>

        {/* ========================= */}
        {/* 収支バランスサマリー */}
        {/* ========================= */}
        <BalanceSummarySection refreshTrigger={refreshTrigger} />

        {/* ========================= */}
        {/* AI分析 + 週間カレンダー（2カラムレイアウト）*/}
        {/* レスポンシブ: モバイルは縦並び、デスクトップは横並び */}
        {/* ========================= */}
        <div className="grid gap-6 lg:grid-cols-2">
          {/* AI家計診断セクション - ユーザーお気に入りのデザイン */}
          <AiInsightsSection />
          
          {/* 週間支出カレンダー - 視覚的な支出パターン表示 */}
          <WeeklyCalendarSection />
        </div>

        {/* ========================= */}
        {/* 予算 + 目標（2カラムレイアウト）*/}
        {/* ========================= */}
        <div className="grid gap-6 lg:grid-cols-2">
          {/* 予算達成状況 */}
          <BudgetStatusSection />
          
          {/* 目標達成状況 */}
          <GoalsStatusSection />
        </div>

        {/* ==================== */}
        {/* アラート・通知 */}
        {/* ==================== */}
        <AlertsSection />

        {/* ==================== */}
        {/* 最近の取引 */}
        {/* ==================== */}
        <RecentTransactionsSection />
      </div>
    </AppLayout>
  )
}
