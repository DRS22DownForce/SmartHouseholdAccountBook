/**
 * ホームページコンポーネント
 * 
 * スマート家計簿アプリのメイン画面です。
 * 
 * 認証について:
 * - このページは AuthProvider によって保護されています
 * - useAuthenticator フックで現在ログイン中のユーザー情報を取得
 * - ユーザーが未認証の場合、AuthProvider が自動的にログイン画面を表示
 * 
 * 主な機能:
 * - 支出の追加、編集、削除
 * - 月別サマリーの表示
 * - トレンドチャートの表示
 * - CSVファイルからのインポート
 * - AI チャットによる支出分析
 */

"use client"

import { useState, useMemo } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { useExpenses } from "@/hooks/use-expenses"
import { ExpenseForm } from "@/components/expense-form"
import { ExpenseList } from "@/components/expense-list"
import { MonthlySummary } from "@/components/monthly-summary"
import { ExpenseTrendChart } from "@/components/expense-trend-chart"
import { CsvUploadDialog } from "@/components/csv-upload-dialog"
import { UserMenu } from "@/components/user-menu"
import { Wallet } from "lucide-react"
import { AiChatDialog } from "@/components/ai-chat-dialog"

export default function HomePage() {
  // Cognito認証情報を取得
  // user: ログイン中のユーザー情報（メールアドレス、属性など）
  // signOut: ログアウト関数
  const { user, signOut } = useAuthenticator((context) => [context.user])

  // 支出データの管理
  const { expenses, addExpense, addExpenses, updateExpense, deleteExpense, isLoaded } = useExpenses()

  // 現在の月を取得（YYYY-MM形式）
  const currentMonth = useMemo(() => {
    return new Date().toISOString().substring(0, 7)
  }, [])

  const [selectedMonth, setSelectedMonth] = useState(currentMonth)

  // データ読み込み中の表示
  if (!isLoaded) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-muted-foreground">読み込み中...</p>
      </div>
    )
  }

  // ユーザー名を取得（メールアドレスまたはusername属性）
  // Cognitoではメールアドレスをユーザー名として使用しています
  const username = user.signInDetails?.loginId || user.username

  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-50 border-b border-border/40 bg-card/95 backdrop-blur supports-[backdrop-filter]:bg-card/60">
        <div className="container mx-auto px-4 py-3 md:py-4">
          <div className="flex items-center justify-between gap-2">
            <div className="flex items-center gap-2 md:gap-3 flex-shrink-0">
              <div className="flex h-9 w-9 md:h-11 md:w-11 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-primary/80 shadow-lg shadow-primary/25 flex-shrink-0">
                <Wallet className="h-5 w-5 md:h-6 md:w-6 text-primary-foreground" />
              </div>
              <div>
                <h1 className="text-lg md:text-2xl font-bold text-foreground tracking-tight whitespace-nowrap">
                  家計簿
                </h1>
                <p className="text-[10px] md:text-xs text-muted-foreground hidden sm:block">支出管理アプリ</p>
              </div>
            </div>
            <div className="flex items-center gap-1 md:gap-2 flex-shrink-0">
              <AiChatDialog expenses={expenses} />
              <CsvUploadDialog onUpload={addExpenses} />
              <ExpenseForm onSubmit={addExpense} />
              {/* signOut関数を渡してログアウト機能を実装 */}
              <UserMenu username={username} onLogout={signOut} />
            </div>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-10">
        <div className="mb-10">
          <ExpenseTrendChart expenses={expenses} />
        </div>

        <div className="grid gap-10 lg:grid-cols-3">
          <div className="lg:col-span-2 space-y-8">
            <div>
              <h2 className="text-3xl font-bold text-foreground mb-6 tracking-tight">支出一覧</h2>
              <ExpenseList expenses={expenses} onUpdate={updateExpense} onDelete={deleteExpense} />
            </div>
          </div>

          <div className="lg:col-span-1">
            <MonthlySummary expenses={expenses} selectedMonth={selectedMonth} onMonthChange={setSelectedMonth} />
          </div>
        </div>
      </main>
    </div>
  )
}
