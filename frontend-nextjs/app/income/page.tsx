"use client"

/**
 * 収入管理ページコンポーネント
 * 
 * 収入の一覧表示、追加、編集、削除を行うページです。
 * 現在はUIのみ実装されており、実際のロジックは未実装です。
 */

import { useMemo } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { AppLayout } from "@/components/layout/AppLayout"
import { LoadingSpinner } from "@/components/ui/loading-spinner"
import { getUserDisplayName } from "@/lib/user-utils"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Plus, ArrowDownCircle, TrendingUp, Calendar } from "lucide-react"
import { formatCurrency } from "@/lib/formatters"

export default function IncomePage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const username = useMemo(() => getUserDisplayName(user), [user])

  // モックデータ（実際のロジックは未実装）
  const mockIncomeData = [
    { id: 1, date: "2024-01-15", amount: 250000, category: "給与", description: "基本給" },
    { id: 2, date: "2024-01-20", amount: 50000, category: "副業", description: "フリーランス" },
  ]

  const mockSummary = {
    currentMonth: 500000,
    average: 480000,
    previousMonthChange: 4.2,
  }

  return (
    <AppLayout username={username} onLogout={signOut}>
      <div className="space-y-6">
        {/* ページヘッダー */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-foreground tracking-tight mb-1 flex items-center gap-2">
              <ArrowDownCircle className="h-6 w-6 md:h-7 md:w-7 text-primary" />
              収入管理
            </h1>
            <p className="text-sm text-muted-foreground">
              収入の記録と管理を行います
            </p>
          </div>
          <Button className="gap-2">
            <Plus className="h-4 w-4" />
            収入を追加
          </Button>
        </div>

        {/* 収入サマリーカード */}
        <div className="grid gap-4 md:grid-cols-3">
          <Card className="border-border/50 shadow-md hover:shadow-xl transition-all duration-300">
            <CardHeader className="pb-2">
              <CardTitle className="text-xs md:text-sm font-semibold text-muted-foreground uppercase tracking-wide flex items-center gap-2">
                <Calendar className="h-4 w-4" />
                今月の収入
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl md:text-3xl font-bold text-foreground">
                {formatCurrency(mockSummary.currentMonth)}
              </p>
            </CardContent>
          </Card>

          <Card className="border-border/50 shadow-md hover:shadow-xl transition-all duration-300">
            <CardHeader className="pb-2">
              <CardTitle className="text-xs md:text-sm font-semibold text-muted-foreground uppercase tracking-wide flex items-center gap-2">
                <TrendingUp className="h-4 w-4" />
                平均収入
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl md:text-3xl font-bold text-foreground">
                {formatCurrency(mockSummary.average)}
              </p>
            </CardContent>
          </Card>

          <Card className="border-border/50 shadow-md hover:shadow-xl transition-all duration-300">
            <CardHeader className="pb-2">
              <CardTitle className="text-xs md:text-sm font-semibold text-muted-foreground uppercase tracking-wide">
                前月比較
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl md:text-3xl font-bold text-primary">
                +{mockSummary.previousMonthChange}%
              </p>
            </CardContent>
          </Card>
        </div>

        {/* 収入一覧テーブル */}
        <Card className="border-border/50 shadow-md">
          <CardHeader>
            <CardTitle>収入一覧</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="text-left py-3 px-4 text-sm font-semibold text-muted-foreground">日付</th>
                    <th className="text-left py-3 px-4 text-sm font-semibold text-muted-foreground">金額</th>
                    <th className="text-left py-3 px-4 text-sm font-semibold text-muted-foreground">カテゴリー</th>
                    <th className="text-left py-3 px-4 text-sm font-semibold text-muted-foreground">説明</th>
                    <th className="text-right py-3 px-4 text-sm font-semibold text-muted-foreground">操作</th>
                  </tr>
                </thead>
                <tbody>
                  {mockIncomeData.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="text-center py-8 text-muted-foreground">
                        収入データがありません
                      </td>
                    </tr>
                  ) : (
                    mockIncomeData.map((income) => (
                      <tr key={income.id} className="border-b border-border/50 hover:bg-muted/50 transition-colors">
                        <td className="py-3 px-4 text-sm">{income.date}</td>
                        <td className="py-3 px-4 text-sm font-semibold text-primary">
                          {formatCurrency(income.amount)}
                        </td>
                        <td className="py-3 px-4 text-sm">{income.category}</td>
                        <td className="py-3 px-4 text-sm text-muted-foreground">{income.description}</td>
                        <td className="py-3 px-4 text-right">
                          <div className="flex items-center justify-end gap-2">
                            <Button variant="ghost" size="sm">
                              編集
                            </Button>
                            <Button variant="ghost" size="sm" className="text-destructive hover:text-destructive">
                              削除
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  )
}

