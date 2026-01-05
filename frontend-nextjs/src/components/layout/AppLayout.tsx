"use client"

/**
 * アプリケーションレイアウトコンポーネント
 * 
 * サイドバーとメインコンテンツエリアを含む全体レイアウトです。
 * すべての認証済みページで使用されます。
 * 
 * 機能:
 * - サイドバーの表示
 * - メインコンテンツエリアのレイアウト
 * - レスポンシブ対応（モバイルではドロワーメニュー）
 */

import React, { useState } from "react"
import { Sidebar } from "./Sidebar"
import type { ExpenseFormData } from "@/lib/types"
import { cn } from "@/lib/utils"

interface AppLayoutProps {
  children: React.ReactNode
  username: string
  onLogout: () => void
  onAddExpense?: (data: ExpenseFormData) => void
  onAddExpenses?: (expenses: ExpenseFormData[]) => void
}

export function AppLayout({
  children,
  username,
  onLogout,
  onAddExpense,
  onAddExpenses,
}: AppLayoutProps) {
  const [isCollapsed, setIsCollapsed] = useState(false)

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/20">
      {/* サイドバー */}
      <Sidebar
        username={username}
        onLogout={onLogout}
        onAddExpense={onAddExpense}
        onAddExpenses={onAddExpenses}
        onCollapsedChange={setIsCollapsed}
      />

      {/* メインコンテンツエリア */}
      <main
        className={cn(
          "transition-all duration-300 pt-16 lg:pt-0",
          isCollapsed ? "lg:pl-20" : "lg:pl-64"
        )}
      >
        <div className="h-full px-4 md:px-6 lg:px-8 py-4 md:py-6 lg:py-8">
          {children}
        </div>
      </main>
    </div>
  )
}

