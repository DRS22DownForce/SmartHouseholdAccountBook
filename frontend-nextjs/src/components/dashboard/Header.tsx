"use client"

import React from "react"
import { usePathname } from "next/navigation"
import { BotMessageSquare, Wallet, List } from "lucide-react"
import Link from "next/link"
import { ExpenseForm } from "@/components/expense-form"
import type { ExpenseFormProps } from "@/components/expense-form"
import { CsvUploadDialog } from "@/components/csv-upload-dialog"
import { UserMenu } from "@/components/user-menu"
import { cn } from "@/lib/utils"
import type { Expense, ExpenseFormData } from "@/lib/types"

interface HeaderProps {
  username: string
  onLogout: () => void
  onAddExpense: (data: ExpenseFormData) => void
  onAddExpenses: (expenses: ExpenseFormData[]) => void
  onCsvUploadComplete?: () => void
}

const navigationItems = [
  { href: "/expenses", label: "支出一覧", icon: List },
] as const

/**
 * ヘッダーコンポーネント
 * 
 * アプリケーションのヘッダー部分を表示します。
 * ナビゲーション、支出追加、CSVインポートなどの機能を提供します。
 * 
 * React.memoでメモ化されており、propsが変更されない限り再レンダリングされません。
 */
export const Header = React.memo(function Header({
  username,
  onLogout,
  onAddExpense,
  onAddExpenses,
  onCsvUploadComplete,
}: HeaderProps) {
  const pathname = usePathname()

  /**
   * 新規追加用のExpenseFormのPropsを生成
   */
  const addExpenseFormProps: ExpenseFormProps = {
    onSubmit: onAddExpense,
  }

  return (
    <header className="sticky top-0 z-50 border-b border-border/50 bg-card/98 backdrop-blur-xl supports-[backdrop-filter]:bg-card/80 shadow-sm">
      <div className="container mx-auto px-4 py-2.5 md:py-3">
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2.5 md:gap-3 flex-shrink-0">
            <Link href="/expenses" className="flex items-center gap-2 md:gap-2.5 hover:opacity-80 transition-opacity">
              <div className="flex h-8 w-8 md:h-10 md:w-10 items-center justify-center rounded-xl bg-gradient-to-br from-primary via-primary/90 to-primary/80 shadow-lg shadow-primary/20 ring-1 ring-primary/10 flex-shrink-0 transition-transform hover:scale-105">
                <Wallet className="h-4 w-4 md:h-5 md:w-5 text-primary-foreground" />
              </div>
              <div>
                <h1 className="text-base md:text-lg lg:text-xl font-bold text-foreground tracking-tight whitespace-nowrap bg-gradient-to-r from-foreground to-foreground/80 bg-clip-text text-transparent">
                  家計簿
                </h1>
                <p className="text-[9px] md:text-[10px] text-muted-foreground hidden sm:block font-medium">
                  支出管理アプリ
                </p>
              </div>
            </Link>

            <nav className="hidden md:flex items-center gap-1.5 ml-3 pl-3 border-l border-border/50">
              {navigationItems.map(({ href, label, icon: Icon }) => {
                const isActive = pathname === href
                return (
                  <Link
                    key={href}
                    href={href}
                    className={cn(
                      "flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs md:text-sm font-medium transition-colors",
                      isActive
                        ? "text-foreground bg-muted"
                        : "text-muted-foreground hover:text-foreground hover:bg-muted/50"
                    )}
                  >
                    <Icon className="h-3.5 w-3.5 md:h-4 md:w-4" />
                    {label}
                  </Link>
                )
              })}
            </nav>
          </div>

          <div className="flex items-center gap-1.5 md:gap-2.5 flex-shrink-0">
            <Link href="/ai-chat" title="AIチャット">
              <div className="flex h-8 w-8 md:h-10 md:w-10 items-center justify-center rounded-xl bg-purple-500/10 text-purple-600 hover:bg-purple-500/20 transition-colors shadow-sm ring-1 ring-purple-500/10 group">
                <BotMessageSquare className="h-4 w-4 md:h-5 md:w-5 group-hover:scale-110 transition-transform" />
              </div>
            </Link>
            <CsvUploadDialog onUpload={onCsvUploadComplete} />
            <ExpenseForm {...addExpenseFormProps} />
            <UserMenu username={username} onLogout={onLogout} />
          </div>
        </div>
      </div>
    </header>
  )
})

