"use client"

/**
 * 予算達成状況セクションコンポーネント
 * 
 * 予算の達成状況を表示します。
 */

import Link from "next/link"
import { Card, CardContent } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { Wallet, ChevronRight, ArrowUpRight } from "lucide-react"
import { formatCurrency } from "@/lib/formatters"
import { cn } from "@/lib/utils"
import { getCategoryIcon } from "@/lib/category-icons"
import { getCategoryColor, getCategoryColorWithAlpha } from "@/lib/category-colors"

export function BudgetStatusSection() {
  // モックデータ（実際のロジックは将来的に実装）
  const mockData = {
    totalBudget: 400000,
    used: 280000,
    remaining: 120000,
    percentage: 70,
    isOverBudget: false,
    overBudgetCategories: [
      { category: "光熱費", overAmount: 5000, percentage: 110 },
    ],
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between px-1">
        <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
          <Wallet className="h-5 w-5 text-primary" />
          予算達成状況
        </h2>
        <Link
          href="/budget"
          className="text-xs font-bold text-primary hover:text-primary/80 flex items-center gap-1 transition-colors group"
        >
          詳細を見る
          <ArrowUpRight className="w-3.5 h-3.5 transition-transform group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
        </Link>
      </div>

      <Card className="border-border/40 shadow-sm overflow-hidden bg-gradient-to-br from-card to-muted/5 group hover:shadow-md transition-all duration-300">
        <CardContent className="p-6 space-y-6">
          <div className="grid gap-6 md:grid-cols-3">
            <div className="space-y-1">
              <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">予算総額</p>
              <p className="text-2xl font-black text-foreground tabular-nums">
                {formatCurrency(mockData.totalBudget)}
              </p>
            </div>
            <div className="space-y-1">
              <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">使用済み</p>
              <div className="flex items-baseline gap-2">
                <p className="text-2xl font-black text-foreground tabular-nums">
                  {formatCurrency(mockData.used)}
                </p>
                <span className="text-xs font-bold text-primary bg-primary/10 px-1.5 py-0.5 rounded">
                  {mockData.percentage}%
                </span>
              </div>
            </div>
            <div className="space-y-1">
              <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">残り予算</p>
              <p className="text-2xl font-black text-primary tabular-nums">
                {formatCurrency(mockData.remaining)}
              </p>
            </div>
          </div>

          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-muted-foreground flex items-center gap-1.5">
                <div className="w-2 h-2 rounded-full bg-primary" />
                現在の進捗状況
              </span>
              <span className="text-sm font-black text-foreground">{mockData.percentage}%</span>
            </div>
            <Progress value={mockData.percentage} className="h-2.5 bg-muted/50" />
          </div>
        </CardContent>
      </Card>

      {/* 予算超過の警告 - カテゴリーアイコンを追加してカラフルに */}
      {mockData.overBudgetCategories.length > 0 && (
        <div className="grid gap-3">
          {mockData.overBudgetCategories.map((item, index) => {
            const Icon = getCategoryIcon(item.category);
            const color = getCategoryColor(item.category);
            const bgColor = getCategoryColorWithAlpha(item.category, 0.1);

            return (
              <Card
                key={index}
                className="border-destructive/30 overflow-hidden shadow-sm hover:shadow-md transition-shadow"
                style={{ backgroundColor: bgColor }}
              >
                <CardContent className="p-4 flex items-center gap-4">
                  <div
                    className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-sm"
                    style={{ backgroundColor: color }}
                  >
                    <Icon className="w-5 h-5" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-bold text-foreground">
                      {item.category}が予算を超過
                    </p>
                    <p className="text-xs font-medium text-destructive">
                      予算を {item.percentage - 100}% 超過しています
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-black text-destructive">
                      {formatCurrency(item.overAmount)}
                    </p>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  )
}

