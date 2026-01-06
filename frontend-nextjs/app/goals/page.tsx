"use client"

/**
 * 目標設定ページコンポーネント
 * 
 * 目標の設定、確認、管理を行うページです。
 * ゲーミフィケーション要素（ストリーク、トロフィー等）を取り入れ、
 * ユーザーのモチベーションを高めるデザインを採用しています。
 * 
 * 【初心者向け解説】
 * - 目標設定はお金を管理する上で非常に重要な機能です
 * - 「月間支出を〇〇円以下に抑える」などの目標を設定できます
 * - 達成するとトロフィーが表示され、達成感を味わえます
 */

import { useMemo } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { AppLayout } from "@/components/layout/AppLayout"
import { getUserDisplayName } from "@/lib/user-utils"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { 
  Plus, 
  Target, 
  Edit, 
  Trash2, 
  CheckCircle2, 
  Trophy,
  Flame,
  Star,
  Sparkles,
  ArrowUpRight,
  Zap
} from "lucide-react"
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
      deadline: "2024-01-31",
      streak: 5,
    },
    {
      id: 2,
      title: "食費削減目標",
      targetAmount: 100000,
      currentAmount: 80000,
      percentage: 80,
      type: "reduction",
      description: "前月比-10%",
      streak: 12,
    },
  ]

  const mockCompletedGoals = [
    { 
      id: 3, 
      title: "2024年1月: 月間支出目標達成", 
      completedDate: "2024-01-31",
      reward: "節約マスターバッジ獲得！"
    },
  ]

  // 目標の総数を計算
  const totalGoals = mockActiveGoals.length + mockCompletedGoals.length

  /**
   * 達成率に応じた色を取得
   */
  const getProgressStyle = (percentage: number) => {
    if (percentage >= 90) return { color: "#10b981", label: "もう少し！" }
    if (percentage >= 70) return { color: "#3b82f6", label: "順調" }
    if (percentage >= 50) return { color: "#f59e0b", label: "がんばろう" }
    return { color: "#f43f5e", label: "スタート" }
  }

  return (
    <AppLayout username={username} onLogout={signOut}>
      <div className="space-y-6 md:space-y-8">
        {/* ==================== */}
        {/* ヘッダーセクション */}
        {/* ==================== */}
        <div className="animate-fade-in">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              {/* グローイングアイコン */}
              <div className="relative">
                <div className="absolute inset-0 bg-amber-500/20 rounded-xl blur-md" />
                <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-amber-400 to-orange-500 text-white shadow-lg">
                  <Target className="h-5 w-5" />
                </div>
              </div>
              <div>
                <h1 className="text-2xl md:text-3xl font-black text-foreground tracking-tight">
                  目標設定
                </h1>
                <p className="text-sm text-muted-foreground flex items-center gap-1">
                  <Sparkles className="w-3 h-3" />
                  目標を設定して達成状況を確認できます
                </p>
              </div>
            </div>
            <Button className={cn(
              "gap-2 shadow-lg",
              "bg-gradient-to-r from-amber-400 to-orange-500 hover:from-amber-500 hover:to-orange-600",
              "transition-all duration-300 hover:shadow-xl hover:-translate-y-0.5"
            )}>
              <Plus className="h-4 w-4" />
              目標を追加
            </Button>
          </div>

          {/* 目標サマリーバッジ */}
          <div className="mt-4 flex flex-wrap gap-3">
            <div className={cn(
              "inline-flex items-center gap-2 px-3 py-2 rounded-xl",
              "bg-amber-500/10 border border-amber-500/20",
              "animate-fade-in stagger-1"
            )}>
              <Target className="w-4 h-4 text-amber-500" />
              <span className="text-xs text-muted-foreground">アクティブ</span>
              <span className="text-sm font-bold text-amber-600 dark:text-amber-400">{mockActiveGoals.length}件</span>
            </div>
            <div className={cn(
              "inline-flex items-center gap-2 px-3 py-2 rounded-xl",
              "bg-emerald-500/10 border border-emerald-500/20",
              "animate-fade-in stagger-2"
            )}>
              <Trophy className="w-4 h-4 text-emerald-500" />
              <span className="text-xs text-muted-foreground">達成済み</span>
              <span className="text-sm font-bold text-emerald-600 dark:text-emerald-400">{mockCompletedGoals.length}件</span>
            </div>
            <div className={cn(
              "inline-flex items-center gap-2 px-3 py-2 rounded-xl",
              "bg-orange-500/10 border border-orange-500/20",
              "animate-fade-in stagger-3"
            )}>
              <Flame className="w-4 h-4 text-orange-500" />
              <span className="text-xs text-muted-foreground">最長ストリーク</span>
              <span className="text-sm font-bold text-orange-600 dark:text-orange-400">12日</span>
            </div>
          </div>
        </div>

        {/* ==================== */}
        {/* アクティブな目標一覧 */}
        {/* ==================== */}
        <div className="space-y-4">
          <div className="flex items-center justify-between px-1">
            <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
              <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-blue-400 to-cyan-500 flex items-center justify-center text-white">
                <Zap className="h-4 w-4" />
              </div>
              アクティブな目標
            </h2>
          </div>

          {mockActiveGoals.length === 0 ? (
            <Card className={cn(
              "border-border/40 shadow-rich",
              "bg-gradient-to-br from-card to-muted/10"
            )}>
              <CardContent className="pt-10 pb-10">
                <div className="flex flex-col items-center gap-4">
                  <div className="w-16 h-16 rounded-full bg-muted/20 flex items-center justify-center">
                    <Target className="w-8 h-8 text-muted-foreground/30" />
                  </div>
                  <div className="text-center">
                    <p className="text-muted-foreground font-medium mb-2">
                      アクティブな目標がありません
                    </p>
                    <p className="text-xs text-muted-foreground/60">
                      目標を設定して、節約を始めましょう！
                    </p>
                  </div>
                  <Button variant="outline" className="gap-2 mt-2">
                    <Plus className="w-4 h-4" />
                    最初の目標を設定
                  </Button>
                </div>
              </CardContent>
            </Card>
          ) : (
            <div className="grid gap-4 md:grid-cols-2">
              {mockActiveGoals.map((goal, index) => {
                const categoryPart = goal.title.replace("削減目標", "").replace("支出目標", "")
                const Icon = getCategoryIcon(categoryPart)
                const color = getCategoryColor(categoryPart)
                const bgColor = getCategoryColorWithAlpha(categoryPart, 0.05)
                const progressStyle = getProgressStyle(goal.percentage)

                return (
                  <Card
                    key={goal.id}
                    className={cn(
                      "relative overflow-hidden group",
                      "border-border/40 shadow-rich",
                      "hover:shadow-xl transition-all duration-500",
                      "hover:-translate-y-1",
                      "animate-fade-in",
                      index === 0 ? "stagger-1" : "stagger-2"
                    )}
                  >
                    {/* 背景グラデーション */}
                    <div 
                      className="absolute inset-0"
                      style={{ background: `linear-gradient(135deg, ${bgColor} 0%, transparent 50%)` }}
                    />
                    
                    {/* 装飾的な円 */}
                    <div className="absolute -top-10 -right-10 w-32 h-32 rounded-full bg-gradient-to-br from-primary/5 to-transparent group-hover:from-primary/10 transition-colors" />

                    <CardContent className="relative p-6 space-y-5">
                      {/* ヘッダー行 */}
                      <div className="flex items-start justify-between">
                        <div className="flex items-center gap-4">
                          <div
                            className={cn(
                              "w-14 h-14 rounded-2xl flex items-center justify-center text-white shadow-lg",
                              "transition-all duration-300 group-hover:scale-110 group-hover:rotate-6"
                            )}
                            style={{ backgroundColor: color || "hsl(var(--primary))" }}
                          >
                            <Icon className="w-7 h-7" />
                          </div>
                          <div className="space-y-1">
                            <h3 className="text-lg font-bold text-foreground">
                              {goal.title}
                            </h3>
                            {goal.description && (
                              <p className="text-xs font-bold text-muted-foreground uppercase tracking-widest">
                                {goal.description}
                              </p>
                            )}
                          </div>
                        </div>
                        <div className="flex items-center gap-1">
                          <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full opacity-0 group-hover:opacity-100 transition-opacity">
                            <Edit className="h-4 w-4" />
                          </Button>
                          <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full text-destructive hover:bg-destructive/10 opacity-0 group-hover:opacity-100 transition-opacity">
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>

                      {/* ストリークバッジ */}
                      {goal.streak && goal.streak > 0 && (
                        <div className={cn(
                          "inline-flex items-center gap-2 px-3 py-2 rounded-xl",
                          "bg-gradient-to-r from-orange-500/10 to-amber-500/10",
                          "border border-orange-500/20"
                        )}>
                          <Flame className="w-4 h-4 text-orange-500" />
                          <span className="text-xs font-bold text-orange-600 dark:text-orange-400">
                            {goal.streak}日連続達成中！
                          </span>
                        </div>
                      )}

                      {/* 金額情報 */}
                      <div className="grid grid-cols-2 gap-6">
                        <div className="space-y-1">
                          <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">目標額</p>
                          <p className="text-xl font-black text-foreground tabular-nums">
                            {formatCurrency(goal.targetAmount)}
                          </p>
                        </div>
                        <div className="space-y-1 text-right">
                          <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">達成率</p>
                          <div className="flex items-center justify-end gap-2">
                            <p className="text-xl font-black tabular-nums" style={{ color: progressStyle.color }}>
                              {goal.percentage}%
                            </p>
                            {goal.percentage >= 90 && (
                              <Sparkles className="w-5 h-5 text-amber-400 animate-pulse" />
                            )}
                          </div>
                        </div>
                      </div>

                      {/* プログレスバー */}
                      <div className="space-y-2">
                        <div className="flex items-center justify-between text-[11px] font-bold text-muted-foreground">
                          <span>現在: {formatCurrency(goal.currentAmount)}</span>
                          <span>残り: {formatCurrency(goal.targetAmount - goal.currentAmount)}</span>
                        </div>
                        <Progress
                          value={goal.percentage}
                          className="h-3 bg-muted/30"
                        />
                        {goal.deadline && (
                          <p className="text-[10px] text-muted-foreground text-right">
                            期限: {goal.deadline}
                          </p>
                        )}
                      </div>
                    </CardContent>
                  </Card>
                )
              })}
            </div>
          )}
        </div>

        {/* ==================== */}
        {/* 達成済み目標 */}
        {/* ==================== */}
        {mockCompletedGoals.length > 0 && (
          <div className="space-y-4">
            <div className="flex items-center justify-between px-1">
              <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
                <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center text-white">
                  <Trophy className="h-4 w-4" />
                </div>
                達成済み目標
              </h2>
              <Button variant="ghost" size="sm" className="gap-1 text-xs">
                すべて見る
                <ArrowUpRight className="w-3 h-3" />
              </Button>
            </div>

            <div className="grid gap-4">
              {mockCompletedGoals.map((goal, index) => (
                <Card 
                  key={goal.id}
                  className={cn(
                    "relative overflow-hidden group",
                    "border-amber-500/30 shadow-rich",
                    "hover:shadow-lg transition-all duration-300",
                    "animate-fade-in stagger-3"
                  )}
                >
                  {/* 背景グラデーション */}
                  <div className="absolute inset-0 bg-gradient-to-r from-amber-500/5 via-yellow-500/5 to-orange-500/5" />
                  
                  {/* キラキラエフェクト */}
                  <div className="absolute top-2 right-8 w-2 h-2 rounded-full bg-amber-400/50 animate-pulse" />
                  <div className="absolute top-6 right-4 w-1.5 h-1.5 rounded-full bg-yellow-400/50 animate-pulse" style={{ animationDelay: "0.5s" }} />
                  <div className="absolute top-3 right-16 w-1 h-1 rounded-full bg-orange-400/50 animate-pulse" style={{ animationDelay: "1s" }} />

                  <CardContent className="relative p-5">
                    <div className="flex items-center gap-4">
                      {/* トロフィーアイコン */}
                      <div className={cn(
                        "w-14 h-14 rounded-2xl flex items-center justify-center",
                        "bg-gradient-to-br from-amber-400 to-orange-500",
                        "text-white shadow-lg",
                        "transform transition-all duration-300 group-hover:scale-110 group-hover:rotate-12"
                      )}>
                        <Trophy className="h-7 w-7" />
                      </div>
                      
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-1">
                          <CheckCircle2 className="w-5 h-5 text-emerald-500 flex-shrink-0" />
                          <p className="text-base font-bold text-foreground truncate">
                            {goal.title}
                          </p>
                        </div>
                        {goal.reward && (
                          <p className="text-sm font-bold text-amber-600 dark:text-amber-400 flex items-center gap-1 mb-1">
                            <Star className="w-4 h-4" />
                            {goal.reward}
                          </p>
                        )}
                        <p className="text-xs text-muted-foreground">
                          達成日: {goal.completedDate}
                        </p>
                      </div>
                      
                      <ArrowUpRight className="w-5 h-5 text-muted-foreground/30 opacity-0 group-hover:opacity-100 transition-opacity" />
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        )}
      </div>
    </AppLayout>
  )
}
