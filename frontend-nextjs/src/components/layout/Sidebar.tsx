"use client"

/**
 * サイドバーコンポーネント
 * 
 * アプリケーション全体のナビゲーションを提供するサイドバーです。
 * メインナビゲーション、ツール、ユーザーセクションを含みます。
 * 
 * 機能:
 * - ページ間のナビゲーション
 * - AIチャット、CSVインポートへのアクセス
 * - ユーザーメニュー（ログアウト）
 * - モバイル対応（ドロワーメニュー）
 */

import React, { useState } from "react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import {
  Home,
  List,
  ArrowDownCircle,
  Wallet,
  Target,
  MessageCircle,
  FileUp,
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
import { AiChatDialog } from "@/components/ai-chat-dialog"
import { CsvUploadDialog } from "@/components/csv-upload-dialog"
import { ExpenseForm } from "@/components/expense-form"
import type { ExpenseFormProps } from "@/components/expense-form"
import type { ExpenseFormData } from "@/lib/types"

interface SidebarProps {
  username: string
  onLogout: () => void
  onAddExpense?: (data: ExpenseFormData) => void
  onAddExpenses?: (expenses: ExpenseFormData[]) => void
  onCollapsedChange?: (collapsed: boolean) => void
}

// メインナビゲーションアイテムの定義
const mainNavigationItems = [
  { href: "/", label: "ホーム", icon: Home },
  { href: "/expenses", label: "支出", icon: List },
  { href: "/income", label: "収入", icon: ArrowDownCircle },
  { href: "/budget", label: "予算", icon: Wallet },
  { href: "/goals", label: "目標", icon: Target },
] as const

export function Sidebar({ username, onLogout, onAddExpense, onAddExpenses, onCollapsedChange }: SidebarProps) {
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
    if (href === "/") {
      return pathname === "/"
    }
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
          href="/"
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
        {/* 折りたたみボタン（PCのみ表示、折りたたみ状態のときは右端に表示） */}
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
        {mainNavigationItems.map(({ href, label, icon: Icon }) => {
          const active = isActive(href)
          return (
            <Link
              key={href}
              href={href}
              onClick={() => setIsMobileOpen(false)}
              className={cn(
                "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 group",
                active
                  ? "bg-primary/10 text-primary shadow-sm border-l-4 border-primary"
                  : "text-muted-foreground hover:text-foreground hover:bg-muted/50",
                isCollapsed && "justify-center"
              )}
              title={isCollapsed ? label : undefined}
            >
              <Icon
                className={cn(
                  "h-5 w-5 transition-transform group-hover:scale-110 flex-shrink-0",
                  active && "text-primary"
                )}
              />
              {!isCollapsed && <span>{label}</span>}
            </Link>
          )
        })}
      </nav>

      {/* 区切り線 */}
      <div className="px-4 py-2">
        <div className="h-px bg-border/50" />
      </div>

      {/* ツールセクション */}
      {!isCollapsed && (
        <div className="px-2 py-2 space-y-1">
          <div className="px-3 py-1.5">
            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
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
                      className="w-full justify-start gap-3 px-3 py-2.5 h-auto hover:bg-muted/50 text-muted-foreground hover:text-foreground"
                    >
                      <Plus className="h-5 w-5" />
                      <span className="text-sm font-medium">支出を追加</span>
                    </Button>
                  }
                />
              </div>
            )}
            <div className="w-full">
              <AiChatDialog />
            </div>
            {onAddExpenses && (
              <div className="w-full">
                <CsvUploadDialog onUpload={onAddExpenses} />
              </div>
            )}
          </div>
        </div>
      )}

      {/* 区切り線 */}
      <div className="px-4 py-2">
        <div className="h-px bg-border/50" />
      </div>

      {/* ユーザーセクション */}
      <div className="px-2 py-2">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              className={cn(
                "w-full justify-start gap-3 px-3 py-2.5 h-auto hover:bg-muted/50",
                isCollapsed && "justify-center"
              )}
              title={isCollapsed ? username : undefined}
            >
              <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10 flex-shrink-0">
                <User className="h-4 w-4 text-primary" />
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
          <DropdownMenuContent align="end" className="w-56">
            <DropdownMenuLabel>
              <div className="flex flex-col space-y-1">
                <p className="text-sm font-medium leading-none">ユーザー情報</p>
                <p className="text-xs leading-none text-muted-foreground mt-1">{username}</p>
              </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem className="cursor-pointer">
              <Settings className="mr-2 h-4 w-4" />
              <span>設定</span>
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              onClick={handleLogout}
              className="text-destructive focus:text-destructive cursor-pointer"
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
          className="h-10 w-10"
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
          className="lg:hidden fixed inset-0 bg-black/50 z-40"
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
          
          {/* 折りたたみ状態のときの展開ボタン（サイドバーの内側右端に配置） */}
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

