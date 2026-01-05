"use client"

/**
 * 目標設定ページコンポーネント
 * 
 * 目標の設定、確認、管理を行うページです。
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
import { Plus, Target, Edit, Trash2, CheckCircle2, ArrowUpRight } from "lucide-react"
import { formatCurrency } from "@/lib/formatters"
import { cn } from "@/lib/utils"
import { getCategoryIcon } from "@/lib/category-icons"
import { getCategoryColor, getCategoryColorWithAlpha } from "@/lib/category-colors"

export default function GoalsPage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const username = useMemo(() => getUserDisplayName(user), [user])

  // モックデータ（実際のロジックは未実装）
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
    <AppLayout username={username} onLogout={signOut}>
      <div className="space-y-6">
        {/* ページヘッダー */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-foreground tracking-tight mb-1 flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-rose-500/10 text-rose-500">
                <Target className="h-5 w-5" />
              </div>
              目標設定
            </h1>
            <p className="text-sm text-muted-foreground">
              目標を設定して達成状況を確認できます
            </p>
          </div>
          <Button className="gap-2">
            <Plus className="h-4 w-4" />
            目標を追加
          </Button>
        </div>

        {/* アクティブな目標一覧 */}
        <div className="space-y-4">
          <h2 className="text-lg font-semibold text-foreground flex items-center gap-2">
            <Target className="h-5 w-5 text-primary" />
            アクティブな目標
          </h2>
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
            <div className="grid gap-6 md:grid-cols-2">
              {mockActiveGoals.map((goal) => {
                const categoryPart = goal.title.replace("削減目標", "").replace("支出目標", "");
                const Icon = getCategoryIcon(categoryPart);
                const color = getCategoryColor(categoryPart);
                const bgColor = getCategoryColorWithAlpha(categoryPart, 0.05);

                return (
                  <Card
                    key={goal.id}
                    className="border-border/40 shadow-sm overflow-hidden bg-gradient-to-br from-card to-muted/5 group hover:shadow-md transition-all duration-300"
                    style={{ backgroundColor: bgColor }}
                  >
                    <CardContent className="p-6 space-y-6">
                      <div className="flex items-start justify-between">
                        <div className="flex items-center gap-4">
                          <div
                            className="w-12 h-12 rounded-lg flex items-center justify-center text-white shadow-md transition-transform group-hover:scale-110"
                            style={{ backgroundColor: color || "hsl(var(--primary))" }}
                          >
                            <Icon className="w-6 h-6" />
                          </div>
                          <div className="space-y-1">
                            <h3 className="text-lg font-bold text-foreground">
                              {goal.title}
                            </h3>
                            {goal.description && (
                              <p className="text-xs font-bold text-muted-foreground uppercase tracking-widest">{goal.description}</p>
                            )}
                          </div>
                        </div>
                        <div className="flex items-center gap-1">
                          <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full hover:bg-background/80">
                            <Edit className="h-3.5 w-3.5" />
                          </Button>
                          <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full text-destructive hover:bg-destructive/10 hover:text-destructive">
                            <Trash2 className="h-3.5 w-3.5" />
                          </Button>
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-6">
                        <div className="space-y-1">
                          <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">目標額</p>
                          <p className="text-xl font-black text-foreground tabular-nums">
                            {formatCurrency(goal.targetAmount)}
                          </p>
                        </div>
                        <div className="space-y-1 text-right">
                          <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">達成率</p>
                          <p
                            className="text-xl font-black tabular-nums"
                            style={{ color: color || "hsl(var(--primary))" }}
                          >
                            {goal.percentage}%
                          </p>
                        </div>
                      </div>

                      <div className="space-y-2">
                        <div className="flex items-center justify-between text-[11px] font-bold text-muted-foreground">
                          <span>現在: {formatCurrency(goal.currentAmount)}</span>
                          <span>残り: {formatCurrency(goal.targetAmount - goal.currentAmount)}</span>
                        </div>
                        <Progress
                          value={goal.percentage}
                          className="h-2.5 bg-muted/50"
                        />
                      </div>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          )}
        </div>

        {/* 達成済み目標 */}
        {mockCompletedGoals.length > 0 && (
          <div className="space-y-4">
            <h2 className="text-lg font-semibold text-foreground flex items-center gap-2">
              <CheckCircle2 className="h-5 w-5 text-primary" />
              達成済み目標
            </h2>
            {mockCompletedGoals.map((goal) => (
              <Card
                key={goal.id}
                className="border-primary/30 bg-primary/5 hover:bg-primary/10 transition-colors"
              >
                <CardContent className="pt-6">
                  <div className="flex items-center gap-3">
                    <CheckCircle2 className="h-5 w-5 text-primary flex-shrink-0" />
                    <div className="flex-1">
                      <p className="font-semibold text-foreground">{goal.title}</p>
                      <p className="text-sm text-muted-foreground">
                        達成日: {goal.completedDate}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </AppLayout>
  )
}

