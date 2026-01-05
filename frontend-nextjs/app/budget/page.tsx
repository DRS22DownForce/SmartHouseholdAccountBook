"use client"

/**
 * 予算管理ページコンポーネント
 * 
 * 予算の設定、確認、管理を行うページです。
 * 現在はUIのみ実装されており、実際のロジックは未実装です。
 */

import { useMemo } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { AppLayout } from "@/components/layout/AppLayout"
import { LoadingSpinner } from "@/components/ui/loading-spinner"
import { getUserDisplayName } from "@/lib/user-utils"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { Plus, Wallet, AlertTriangle, CheckCircle2, ArrowUpRight } from "lucide-react"
import { formatCurrency } from "@/lib/formatters"
import { cn } from "@/lib/utils"
import { getCategoryIcon } from "@/lib/category-icons"
import { getCategoryColor, getCategoryColorWithAlpha } from "@/lib/category-colors"

export default function BudgetPage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const username = useMemo(() => getUserDisplayName(user), [user])

  // モックデータ（実際のロジックは未実装）
  const mockBudgetSummary = {
    totalBudget: 400000,
    used: 280000,
    remaining: 120000,
    percentage: 70,
  }

  const mockCategoryBudgets = [
    { category: "食費", budget: 100000, actual: 85000, percentage: 85, status: "good" },
    { category: "光熱費", budget: 50000, actual: 55000, percentage: 110, status: "over" },
    { category: "住居費", budget: 150000, actual: 100000, percentage: 67, status: "good" },
    { category: "交通費", budget: 50000, actual: 30000, percentage: 60, status: "good" },
    { category: "その他", budget: 50000, actual: 20000, percentage: 40, status: "good" },
  ]

  return (
    <AppLayout username={username} onLogout={signOut}>
      <div className="space-y-6">
        {/* ページヘッダー */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-foreground tracking-tight mb-1 flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-500/10 text-indigo-500">
                <Wallet className="h-5 w-5" />
              </div>
              予算管理
            </h1>
            <p className="text-sm text-muted-foreground">
              予算の設定と達成状況を確認できます
            </p>
          </div>
          <Button className="gap-2">
            <Plus className="h-4 w-4" />
            予算を設定
          </Button>
        </div>

        {/* 予算サマリーカード */}
        <Card className="border-border/50 shadow-md bg-gradient-to-br from-primary/5 to-primary/10">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Wallet className="h-5 w-5 text-primary" />
              今月の予算サマリー
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-4 md:grid-cols-3">
              <div>
                <p className="text-sm text-muted-foreground mb-1">予算総額</p>
                <p className="text-2xl font-bold text-foreground">
                  {formatCurrency(mockBudgetSummary.totalBudget)}
                </p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground mb-1">使用済み</p>
                <p className="text-2xl font-bold text-foreground">
                  {formatCurrency(mockBudgetSummary.used)}
                </p>
                <p className="text-xs text-muted-foreground mt-1">
                  ({mockBudgetSummary.percentage}%)
                </p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground mb-1">残り</p>
                <p className="text-2xl font-bold text-primary">
                  {formatCurrency(mockBudgetSummary.remaining)}
                </p>
              </div>
            </div>
            <div className="space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">予算達成率</span>
                <span className="font-semibold">{mockBudgetSummary.percentage}%</span>
              </div>
              <Progress value={mockBudgetSummary.percentage} className="h-2" />
            </div>
          </CardContent>
        </Card>

        {/* カテゴリー別予算一覧 - より鮮やかでモダンなデザインに */}
        <div className="space-y-4">
          <div className="flex items-center justify-between px-1">
            <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
              <Plus className="h-5 w-5 text-primary" />
              カテゴリー別予算
            </h2>
          </div>

          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {mockCategoryBudgets.map((item) => {
              const Icon = getCategoryIcon(item.category);
              const color = getCategoryColor(item.category);
              const bgColor = getCategoryColorWithAlpha(item.category, 0.05);

              return (
                <Card
                  key={item.category}
                  className="border-border/40 shadow-sm overflow-hidden bg-gradient-to-br from-card to-muted/5 group hover:shadow-md transition-all duration-300"
                  style={{ backgroundColor: bgColor }}
                >
                  <CardContent className="p-5 space-y-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div
                          className="w-10 h-10 rounded-lg flex items-center justify-center text-white shadow-sm transition-transform group-hover:scale-110"
                          style={{ backgroundColor: color }}
                        >
                          <Icon className="w-5 h-5" />
                        </div>
                        <div className="space-y-0.5">
                          <h3 className="font-bold text-foreground">{item.category}</h3>
                          {item.status === "over" ? (
                            <span className="text-[10px] font-bold text-destructive flex items-center gap-1">
                              <AlertTriangle className="w-3 h-3" />
                              予算超過
                            </span>
                          ) : (
                            <span className="text-[10px] font-bold text-primary flex items-center gap-1">
                              <CheckCircle2 className="w-3 h-3" />
                              良好
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">達成率</p>
                        <p
                          className={cn(
                            "text-lg font-black",
                            item.status === "over" ? "text-destructive" : "text-primary"
                          )}
                        >
                          {item.percentage}%
                        </p>
                      </div>
                    </div>

                    <div className="space-y-1.5">
                      <div className="flex items-center justify-between text-[11px] font-bold text-muted-foreground">
                        <span>{formatCurrency(item.actual)}</span>
                        <span>{formatCurrency(item.budget)}</span>
                      </div>
                      <Progress
                        value={Math.min(item.percentage, 100)}
                        className={cn(
                          "h-2 bg-muted/50",
                        )}
                      />
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </div>

        {/* 予算アラート */}
        {mockCategoryBudgets.some((item) => item.status === "over") && (
          <Card className="border-destructive/50 bg-destructive/5">
            <CardContent className="pt-6">
              <div className="flex items-start gap-3">
                <AlertTriangle className="h-5 w-5 text-destructive mt-0.5" />
                <div>
                  <p className="font-semibold text-destructive mb-1">予算超過の警告</p>
                  <p className="text-sm text-muted-foreground">
                    光熱費が予算を10%超過しています。支出を見直すことをお勧めします。
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        )}
      </div>
    </AppLayout>
  )
}

