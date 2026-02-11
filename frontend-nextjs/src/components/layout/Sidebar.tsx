"use client"

/**
 * サイドバーコンポーネント
 * 
 * アプリケーション全体のナビゲーションを提供するサイドバーです。
 * ホームページと統一感のあるグラデーションアイコンを採用しています。
 * 
 * 【初心者向け解説】
 * - グラデーションアイコン: 2色以上が滑らかに変化する背景
 * - シャドウ: アイコンに立体感を与える影
 * - ホバーエフェクト: マウスを乗せたときのアニメーション
 */

import React, { useState } from "react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import {
  List,
  Wallet,
  BotMessageSquare,
  User,
  Settings,
  LogOut,
  Menu,
  X,
  ChevronLeft,
  ChevronRight,
  Plus,
} from "lucide-react"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { CsvUploadDialog } from "@/components/csv-upload-dialog"
import { ExpenseForm } from "@/components/expense-form"
import type { ExpenseFormProps } from "@/components/expense-form"
import type { ExpenseFormData } from "@/lib/types"

interface SidebarProps {
  username: string
  onLogout: () => void
  onAddExpense?: (data: ExpenseFormData) => void
  onAddExpenses?: (expenses: ExpenseFormData[]) => void
  onCsvUploadComplete?: () => void
  onCollapsedChange?: (collapsed: boolean) => void
}

// メインナビゲーションアイテムの定義（グラデーションスタイル）
const mainNavigationItems = [
  { 
    href: "/expenses", 
    label: "支出", 
    icon: List, 
    gradient: "from-orange-400 to-rose-500",
    shadowColor: "shadow-orange-500/25"
  },
] as const

export function Sidebar({ username, onLogout, onAddExpense, onAddExpenses, onCsvUploadComplete, onCollapsedChange }: SidebarProps) {
  const pathname = usePathname()
  const [isMobileOpen, setIsMobileOpen] = useState(false)
  const [isCollapsed, setIsCollapsed] = useState(false)

  // 折りたたみ状態が変更されたときに親コンポーネントに通知
  React.useEffect(() => {
    onCollapsedChange?.(isCollapsed)
  }, [isCollapsed, onCollapsedChange])

  /**
   * ログアウトハンドラー
   */
  const handleLogout = () => {
    onLogout()
    setIsMobileOpen(false)
  }

  /**
   * ナビゲーションアイテムがアクティブかどうかを判定
   */
  const isActive = (href: string) => {
    return pathname.startsWith(href)
  }

  /**
   * サイドバーのコンテンツ
   */
  const SidebarContent = () => (
    <>
      {/* ヘッダーセクション */}
      <div className={cn(
        "flex items-center gap-3 border-b border-border/50 relative",
        isCollapsed ? "px-2 py-4 justify-center" : "px-4 py-4 justify-between"
      )}>
        <Link
          href="/expenses"
          className={cn(
            "flex items-center hover:opacity-80 transition-opacity",
            isCollapsed ? "justify-center" : "gap-2.5 flex-1"
          )}
          onClick={() => setIsMobileOpen(false)}
        >
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-primary via-primary/90 to-primary/80 shadow-lg shadow-primary/20 ring-1 ring-primary/10 transition-transform hover:scale-105 flex-shrink-0">
            <Wallet className="h-5 w-5 text-primary-foreground" />
          </div>
          {!isCollapsed && (
            <div>
              <h1 className="text-lg font-bold text-foreground tracking-tight bg-gradient-to-r from-foreground to-foreground/80 bg-clip-text text-transparent">
                家計簿
              </h1>
              <p className="text-[10px] text-muted-foreground font-medium">
                スマート家計管理
              </p>
            </div>
          )}
        </Link>
        {/* 閉じるボタン（モバイルのみ表示） */}
        {!isCollapsed && (
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setIsMobileOpen(false)}
            className="lg:hidden h-8 w-8 flex-shrink-0"
          >
            <X className="h-4 w-4" />
            <span className="sr-only">サイドバーを閉じる</span>
          </Button>
        )}
        {/* 折りたたみボタン（PCのみ表示） */}
        {!isCollapsed && (
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setIsCollapsed(true)}
            className="hidden lg:flex h-8 w-8 flex-shrink-0"
            title="サイドバーを折りたたむ"
          >
            <ChevronLeft className="h-4 w-4" />
            <span className="sr-only">サイドバーを折りたたむ</span>
          </Button>
        )}
      </div>

      {/* メインナビゲーションセクション */}
      <nav className="flex-1 px-2 py-4 space-y-1">
        {mainNavigationItems.map(({ href, label, icon: Icon, gradient, shadowColor }) => {
          const active = isActive(href)
          return (
            <Link
              key={href}
              href={href}
              onClick={() => setIsMobileOpen(false)}
              className={cn(
                "flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 group relative",
                active
                  ? "bg-muted/80 text-foreground shadow-sm"
                  : "text-muted-foreground hover:text-foreground hover:bg-muted/50",
                isCollapsed && "justify-center"
              )}
              title={isCollapsed ? label : undefined}
            >
              {/* グラデーションアイコン */}
              <div className={cn(
                "flex h-9 w-9 items-center justify-center rounded-xl text-white shadow-lg transition-all duration-300 flex-shrink-0",
                "bg-gradient-to-br",
                gradient,
                shadowColor,
                "group-hover:scale-110 group-hover:rotate-3"
              )}>
                <Icon className="h-5 w-5" />
              </div>
              {!isCollapsed && (
                <div className="flex items-center gap-2 flex-1">
                  <span className={cn(
                    "transition-colors",
                    active && "font-bold"
                  )}>
                    {label}
                  </span>
                </div>
              )}
              {/* アクティブインジケーター */}
              {active && !isCollapsed && (
                <div className={cn(
                  "absolute right-3 w-2 h-2 rounded-full",
                  "bg-gradient-to-br",
                  gradient
                )} />
              )}
            </Link>
          )
        })}
      </nav>

      {/* 区切り線 */}
      <div className="px-4 py-2">
        <div className="h-px bg-gradient-to-r from-border/60 via-border/30 to-transparent" />
      </div>

      {/* ツールセクション */}
      {!isCollapsed && (
        <div className="px-2 py-2 space-y-1">
          <div className="px-3 py-1.5">
            <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
              ツール
            </p>
          </div>
          <div className="space-y-1">
            {onAddExpense && (
              <div className="w-full">
                <ExpenseForm
                  onSubmit={onAddExpense}
                  reactNode={
                    <Button
                      variant="ghost"
                      className="w-full justify-start gap-3 px-3 py-2.5 h-auto hover:bg-muted/50 text-muted-foreground hover:text-foreground group rounded-xl"
                    >
                      {/* グラデーションアイコン */}
                      <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-sky-400 to-blue-500 text-white shadow-lg shadow-sky-500/25 group-hover:scale-110 group-hover:rotate-3 transition-all duration-300 flex-shrink-0">
                        <Plus className="h-5 w-5" />
                      </div>
                      <span className="text-sm font-medium">支出を追加</span>
                    </Button>
                  }
                />
              </div>
            )}
            <div className="w-full">
              <Link
                href="/ai-chat"
                className={cn(
                  "flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 group relative",
                  isActive("/ai-chat")
                    ? "bg-muted/80 text-foreground shadow-sm"
                    : "text-muted-foreground hover:text-foreground hover:bg-muted/50"
                )}
                title="AIチャット"
              >
                {/* グラデーションアイコン */}
                <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-purple-400 to-pink-500 text-white shadow-lg shadow-purple-500/25 group-hover:scale-110 group-hover:rotate-3 transition-all duration-300 flex-shrink-0">
                  <BotMessageSquare className="h-5 w-5" />
                </div>
                <span className={cn(isActive("/ai-chat") && "font-bold")}>AIチャット</span>
                {isActive("/ai-chat") && (
                  <div className="absolute right-3 w-2 h-2 rounded-full bg-gradient-to-br from-purple-400 to-pink-500" />
                )}
              </Link>
            </div>
            {onAddExpenses && (
              <div className="w-full">
                <CsvUploadDialog onUpload={onCsvUploadComplete} />
              </div>
            )}
          </div>
        </div>
      )}

      {/* 区切り線 */}
      <div className="px-4 py-2">
        <div className="h-px bg-gradient-to-r from-border/60 via-border/30 to-transparent" />
      </div>

      {/* ユーザーセクション */}
      <div className="px-2 py-2">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              className={cn(
                "w-full justify-start gap-3 px-3 py-2.5 h-auto hover:bg-muted/50 rounded-xl group",
                isCollapsed && "justify-center"
              )}
              title={isCollapsed ? username : undefined}
            >
              {/* グラデーションアイコン */}
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-slate-400 to-slate-600 text-white shadow-lg shadow-slate-500/25 group-hover:scale-110 transition-all duration-300 flex-shrink-0">
                <User className="h-5 w-5" />
              </div>
              {!isCollapsed && (
                <div className="flex-1 text-left">
                  <p className="text-sm font-medium text-foreground">ユーザー</p>
                  <p className="text-xs text-muted-foreground truncate max-w-[140px]">
                    {username}
                  </p>
                </div>
              )}
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-56 rounded-xl">
            <DropdownMenuLabel>
              <div className="flex flex-col space-y-1">
                <p className="text-sm font-medium leading-none">ユーザー情報</p>
                <p className="text-xs leading-none text-muted-foreground mt-1">{username}</p>
              </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem className="cursor-pointer rounded-lg">
              <Settings className="mr-2 h-4 w-4" />
              <span>設定</span>
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              onClick={handleLogout}
              className="text-destructive focus:text-destructive cursor-pointer rounded-lg"
            >
              <LogOut className="mr-2 h-4 w-4" />
              <span>ログアウト</span>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </>
  )

  return (
    <>
      {/* モバイル用ハンバーガーボタン */}
      <div className="lg:hidden fixed top-0 left-0 z-50 p-4">
        <Button
          variant="outline"
          size="icon"
          onClick={() => setIsMobileOpen(!isMobileOpen)}
          className="h-10 w-10 rounded-xl shadow-lg"
        >
          {isMobileOpen ? (
            <X className="h-5 w-5" />
          ) : (
            <Menu className="h-5 w-5" />
          )}
        </Button>
      </div>

      {/* モバイル用オーバーレイ */}
      {isMobileOpen && (
        <div
          className="lg:hidden fixed inset-0 bg-black/50 backdrop-blur-sm z-40"
          onClick={() => setIsMobileOpen(false)}
        />
      )}

      {/* サイドバー本体 */}
      <aside
        className={cn(
          "fixed left-0 top-0 z-40 h-full border-r border-border/50 bg-card/95 backdrop-blur-xl transition-all duration-300 ease-in-out",
          "lg:translate-x-0 lg:fixed lg:z-auto",
          isMobileOpen ? "translate-x-0 w-64" : "-translate-x-full lg:translate-x-0",
          isCollapsed ? "lg:w-20" : "lg:w-64"
        )}
      >
        <div className="flex h-full flex-col overflow-y-auto relative">
          <SidebarContent />

          {/* 折りたたみ状態のときの展開ボタン */}
          {isCollapsed && (
            <div className="hidden lg:flex absolute top-4 right-2 z-50">
              <Button
                variant="default"
                size="icon"
                onClick={() => setIsCollapsed(false)}
                className="h-7 w-7 rounded-full shadow-lg bg-primary hover:bg-primary/90 border-2 border-background"
                title="サイドバーを展開"
              >
                <ChevronRight className="h-4 w-4" />
                <span className="sr-only">サイドバーを展開</span>
              </Button>
            </div>
          )}
        </div>
      </aside>
    </>
  )
}
