"use client"

import { usePathname } from "next/navigation"
import { Wallet, Home, List } from "lucide-react"
import Link from "next/link"
import { ExpenseForm } from "@/components/expense-form"
import { CsvUploadDialog } from "@/components/csv-upload-dialog"
import { UserMenu } from "@/components/user-menu"
import { AiChatDialog } from "@/components/ai-chat-dialog"
import { cn } from "@/lib/utils"
import type { Expense, ExpenseFormData } from "@/lib/types"

interface HeaderProps {
  expenses: Expense[]
  username: string
  onLogout: () => void
  onAddExpense: (data: ExpenseFormData) => void
  onAddExpenses: (expenses: ExpenseFormData[]) => void
}

const navigationItems = [
  { href: "/", label: "ホーム", icon: Home },
  { href: "/expenses", label: "支出一覧", icon: List },
] as const

export function Header({
  expenses,
  username,
  onLogout,
  onAddExpense,
  onAddExpenses,
}: HeaderProps) {
  const pathname = usePathname()

  return (
    <header className="sticky top-0 z-50 border-b border-border/50 bg-card/98 backdrop-blur-xl supports-[backdrop-filter]:bg-card/80 shadow-sm">
      <div className="container mx-auto px-4 py-4 md:py-5">
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-3 md:gap-4 flex-shrink-0">
            <Link href="/" className="flex items-center gap-3 md:gap-4 hover:opacity-80 transition-opacity">
              <div className="flex h-10 w-10 md:h-12 md:w-12 items-center justify-center rounded-xl bg-gradient-to-br from-primary via-primary/90 to-primary/80 shadow-lg shadow-primary/20 ring-1 ring-primary/10 flex-shrink-0 transition-transform hover:scale-105">
                <Wallet className="h-5 w-5 md:h-6 md:w-6 text-primary-foreground" />
              </div>
              <div>
                <h1 className="text-xl md:text-2xl lg:text-3xl font-bold text-foreground tracking-tight whitespace-nowrap bg-gradient-to-r from-foreground to-foreground/80 bg-clip-text">
                  家計簿
                </h1>
                <p className="text-[11px] md:text-xs text-muted-foreground hidden sm:block font-medium">
                  支出管理アプリ
                </p>
              </div>
            </Link>
            
            <nav className="hidden md:flex items-center gap-2 ml-4 pl-4 border-l border-border/50">
              {navigationItems.map(({ href, label, icon: Icon }) => {
                const isActive = pathname === href
                return (
                  <Link
                    key={href}
                    href={href}
                    className={cn(
                      "flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-colors",
                      isActive
                        ? "text-foreground bg-muted"
                        : "text-muted-foreground hover:text-foreground hover:bg-muted/50"
                    )}
                  >
                    <Icon className="h-4 w-4" />
                    {label}
                  </Link>
                )
              })}
            </nav>
          </div>

          <div className="flex items-center gap-1.5 md:gap-2.5 flex-shrink-0">
            <AiChatDialog expenses={expenses} />
            <CsvUploadDialog onUpload={onAddExpenses} />
            <ExpenseForm onSubmit={onAddExpense} />
            <UserMenu username={username} onLogout={onLogout} />
          </div>
        </div>
      </div>
    </header>
  )
}

