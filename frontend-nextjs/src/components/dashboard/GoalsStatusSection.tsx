"use client"

/**
 * 目標達成状況セクションコンポーネント
 * 
 * アクティブな目標の達成状況を表示します。
 */

import Link from "next/link"
import { Card, CardContent } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { Target, CheckCircle2, ArrowUpRight } from "lucide-react"
import { formatCurrency } from "@/lib/formatters"
import { cn } from "@/lib/utils"
import { getCategoryIcon } from "@/lib/category-icons"
import { getCategoryColor, getCategoryColorWithAlpha } from "@/lib/category-colors"

export function GoalsStatusSection() {
  // モックデータ（実際のロジックは将来的に実装）
  const mockActiveGoals = [
    {
      id: 1,
      title: "月間支出目標",
      targetAmount: 400000,
      currentAmount: 280000,
      percentage: 70,
      type: "expense",
    },
    {
      id: 2,
      title: "食費削減目標",
      targetAmount: 100000,
      currentAmount: 80000,
      percentage: 80,
      type: "reduction",
      description: "前月比-10%",
    },
  ]

  const mockCompletedGoals = [
    { id: 3, title: "2024年1月: 月間支出目標達成", completedDate: "2024-01-31" },
  ]

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between px-1">
        <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
          <Target className="h-5 w-5 text-primary" />
          目標達成状況
        </h2>
        <Link
          href="/goals"
          className="text-xs font-bold text-primary hover:text-primary/80 flex items-center gap-1 transition-colors group"
        >
          詳細を見る
          <ArrowUpRight className="w-3.5 h-3.5 transition-transform group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
        </Link>
      </div>

      {/* アクティブな目標 */}
      {mockActiveGoals.length === 0 ? (
        <Card className="border-border/40 bg-muted/5">
          <CardContent className="pt-10 pb-10">
            <div className="flex flex-col items-center gap-3">
              <Target className="w-8 h-8 text-muted-foreground/20" />
              <p className="text-center text-muted-foreground text-sm font-medium">
                アクティブな目標がありません
              </p>
            </div>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {mockActiveGoals.map((goal) => {
            const Icon = getCategoryIcon(goal.title.replace("削減目標", "").replace("支出目標", "")); // 簡易的なマッピング
            const color = getCategoryColor(goal.title.replace("削減目標", "").replace("支出目標", ""));
            const bgColor = getCategoryColorWithAlpha(goal.title.replace("削減目標", "").replace("支出目標", ""), 0.05);

            return (
              <Card
                key={goal.id}
                className="border-border/40 shadow-sm overflow-hidden bg-gradient-to-br from-card to-muted/5 group hover:shadow-md transition-all duration-300"
                style={{ backgroundColor: bgColor }}
              >
                <CardContent className="p-5 space-y-4">
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-3">
                      <div
                        className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-sm transition-transform group-hover:scale-110"
                        style={{ backgroundColor: color || "hsl(var(--primary))" }}
                      >
                        <Icon className="w-5 h-5" />
                      </div>
                      <div className="space-y-0.5">
                        <h3 className="font-bold text-foreground truncate max-w-[150px]">
                          {goal.title}
                        </h3>
                        {goal.description && (
                          <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">{goal.description}</p>
                        )}
                      </div>
                    </div>
                    <span
                      className="text-xs font-black px-2 py-0.5 rounded-full"
                      style={{ backgroundColor: color ? `${color}20` : "rgba(var(--primary), 0.1)", color: color || "hsl(var(--primary))" }}
                    >
                      {goal.percentage}%
                    </span>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-0.5">
                      <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">目標額</p>
                      <p className="text-sm font-black text-foreground tabular-nums">
                        {formatCurrency(goal.targetAmount)}
                      </p>
                    </div>
                    <div className="space-y-0.5">
                      <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">現在の進捗</p>
                      <p
                        className="text-sm font-black tabular-nums"
                        style={{ color: color || "hsl(var(--primary))" }}
                      >
                        {formatCurrency(goal.currentAmount)}
                      </p>
                    </div>
                  </div>

                  <div className="space-y-1.5">
                    <Progress
                      value={goal.percentage}
                      className="h-2 bg-muted/50"
                    // Note: CSS Variable --progress-fill might need to be set or use a custom Progress for colored bars
                    />
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      {/* 達成済み目標 - よりコンパクトに */}
      {mockCompletedGoals.length > 0 && (
        <Card className="border-primary/20 bg-primary/5 group hover:bg-primary/10 transition-all duration-300">
          <CardContent className="p-4">
            <div className="flex items-center gap-4">
              <div className="w-9 h-9 rounded-full bg-primary/10 flex items-center justify-center text-primary group-hover:scale-110 transition-transform">
                <CheckCircle2 className="h-5 w-5" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-bold text-foreground truncate">{mockCompletedGoals[0].title}</p>
                <p className="text-xs font-medium text-muted-foreground">
                  達成日: {mockCompletedGoals[0].completedDate}
                </p>
              </div>
              <ArrowUpRight className="w-4 h-4 text-primary opacity-0 group-hover:opacity-100 transition-opacity" />
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}

