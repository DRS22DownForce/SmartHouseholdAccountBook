"use client"

/**
 * 収支バランスサマリーセクションコンポーネント
 * 
 * 今月の収入、支出、収支バランスを表示します。
 * 現在はUIのみ実装されており、実際のロジックは未実装です。
 */

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { ArrowUpCircle, ArrowDownCircle, TrendingUp, TrendingDown } from "lucide-react"
import { formatCurrency } from "@/lib/formatters"
import { cn } from "@/lib/utils"

interface BalanceSummarySectionProps {
  refreshTrigger?: number
}

export function BalanceSummarySection({ refreshTrigger }: BalanceSummarySectionProps) {
  // モックデータ（実際のロジックは未実装）
  const mockData = {
    income: 500000,
    expenses: 350000,
    balance: 150000,
    previousMonthChange: 4.2,
    previousYearChange: -2.1,
  }

  const isPositive = mockData.balance >= 0

  return (
    <div className="space-y-4">
      <h2 className="text-lg md:text-xl font-semibold text-foreground">収支バランス</h2>
      
      <div className="grid gap-4 md:grid-cols-3">
        {/* 今月の収入 */}
        <Card className="border-border/50 shadow-md hover:shadow-xl transition-all duration-300 bg-gradient-to-br from-green-50 to-green-100/50 dark:from-green-950/20 dark:to-green-900/10">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs md:text-sm font-semibold text-muted-foreground uppercase tracking-wide flex items-center gap-2">
              <ArrowUpCircle className="h-4 w-4 text-green-600 dark:text-green-400" />
              今月の収入
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl md:text-3xl font-bold text-green-700 dark:text-green-400 tracking-tight">
              {formatCurrency(mockData.income)}
            </p>
            <div className="flex items-center gap-1 mt-2">
              <TrendingUp className="h-3 w-3 text-green-600 dark:text-green-400" />
              <p className="text-xs text-muted-foreground">
                前月比 +{mockData.previousMonthChange}%
              </p>
            </div>
          </CardContent>
        </Card>

        {/* 今月の支出 */}
        <Card className="border-border/50 shadow-md hover:shadow-xl transition-all duration-300 bg-gradient-to-br from-red-50 to-red-100/50 dark:from-red-950/20 dark:to-red-900/10">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs md:text-sm font-semibold text-muted-foreground uppercase tracking-wide flex items-center gap-2">
              <ArrowDownCircle className="h-4 w-4 text-red-600 dark:text-red-400" />
              今月の支出
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl md:text-3xl font-bold text-red-700 dark:text-red-400 tracking-tight">
              {formatCurrency(mockData.expenses)}
            </p>
            <div className="flex items-center gap-1 mt-2">
              <TrendingDown className="h-3 w-3 text-red-600 dark:text-red-400" />
              <p className="text-xs text-muted-foreground">
                前月比 -2.5%
              </p>
            </div>
          </CardContent>
        </Card>

        {/* 収支バランス */}
        <Card className={cn(
          "border-border/50 shadow-md hover:shadow-xl transition-all duration-300",
          isPositive
            ? "bg-gradient-to-br from-blue-50 to-blue-100/50 dark:from-blue-950/20 dark:to-blue-900/10"
            : "bg-gradient-to-br from-orange-50 to-orange-100/50 dark:from-orange-950/20 dark:to-orange-900/10"
        )}>
          <CardHeader className="pb-2">
            <CardTitle className="text-xs md:text-sm font-semibold text-muted-foreground uppercase tracking-wide">
              収支バランス
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className={cn(
              "text-2xl md:text-3xl font-bold tracking-tight",
              isPositive
                ? "text-blue-700 dark:text-blue-400"
                : "text-orange-700 dark:text-orange-400"
            )}>
              {isPositive ? "+" : ""}{formatCurrency(mockData.balance)}
            </p>
            <p className="text-xs text-muted-foreground mt-2">
              {isPositive ? "黒字" : "赤字"}
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

