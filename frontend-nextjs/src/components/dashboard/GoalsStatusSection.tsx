"use client"

/**
 * 目標達成状況セクションコンポーネント
 * 
 * アクティブな目標の達成状況をリッチなビジュアルで表示します。
 * カードレイアウト、プログレスバー、アニメーションを使用して
 * ユーザーのモチベーションを高めるデザインを実現します。
 * 
 * 【初心者向け解説】
 * - 目標達成は「ゲーミフィケーション」の一種で、達成感を視覚化します
 * - 進捗バーが100%に近づくほど、ユーザーのやる気がアップ！
 * - 達成済みの目標は「トロフィー」として表示されます
 */

import Link from "next/link"
import { Card, CardContent } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { 
  Target, 
  CheckCircle2, 
  ArrowUpRight, 
  Trophy, 
  Flame,
  Star,
  Sparkles
} from "lucide-react"
import { formatCurrency } from "@/lib/formatters"
import { cn } from "@/lib/utils"
import { getCategoryIcon } from "@/lib/category-icons"
import { getCategoryColor, getCategoryColorWithAlpha } from "@/lib/category-colors"

/**
 * アクティブな目標の型定義
 */
interface ActiveGoal {
  id: number
  title: string
  targetAmount: number
  currentAmount: number
  percentage: number
  type: "expense" | "reduction" | "saving"
  description?: string
  deadline?: string
  streak?: number  // 連続達成日数
}

/**
 * 達成済み目標の型定義
 */
interface CompletedGoal {
  id: number
  title: string
  completedDate: string
  reward?: string
}

export function GoalsStatusSection() {
  // モックデータ（実際のロジックは将来的に実装）
  const mockActiveGoals: ActiveGoal[] = [
    {
      id: 1,
      title: "月間支出目標",
      targetAmount: 400000,
      currentAmount: 280000,
      percentage: 70,
      type: "expense",
      deadline: "2024-01-31",
      streak: 5
    },
    {
      id: 2,
      title: "食費削減目標",
      targetAmount: 100000,
      currentAmount: 80000,
      percentage: 80,
      type: "reduction",
      description: "前月比-10%",
      streak: 12
    },
  ]

  const mockCompletedGoals: CompletedGoal[] = [
    { 
      id: 3, 
      title: "2024年1月: 月間支出目標達成", 
      completedDate: "2024-01-31",
      reward: "節約マスターバッジ獲得！"
    },
  ]

  /**
   * 目標タイプに応じたアイコンと色を取得
   */
  const getGoalStyle = (type: string, percentage: number) => {
    // 達成率に応じた色
    const progressColor = 
      percentage >= 90 ? "emerald" :
      percentage >= 70 ? "blue" :
      percentage >= 50 ? "amber" : "rose"

    // 目標タイプに応じたスタイル
    const typeStyles = {
      expense: { icon: Target, gradient: "from-blue-400 to-cyan-500" },
      reduction: { icon: Flame, gradient: "from-orange-400 to-rose-500" },
      saving: { icon: Star, gradient: "from-amber-400 to-yellow-500" }
    }

    return {
      ...typeStyles[type as keyof typeof typeStyles] || typeStyles.expense,
      progressColor
    }
  }

  return (
    <div className="space-y-4">
      {/* セクションヘッダー */}
      <div className="flex items-center justify-between px-1">
        <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
          <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-amber-400 to-orange-500 text-white shadow-lg">
            <Target className="h-4 w-4" />
          </div>
          目標達成状況
        </h2>
        <Link
          href="/goals"
          className="text-xs font-bold text-primary hover:text-primary/80 flex items-center gap-1 transition-colors group"
        >
          すべての目標
          <ArrowUpRight className="w-3.5 h-3.5 transition-transform group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
        </Link>
      </div>

      {/* アクティブな目標 */}
      {mockActiveGoals.length === 0 ? (
        <Card className="border-border/40 bg-gradient-to-br from-card to-muted/10">
          <CardContent className="pt-10 pb-10">
            <div className="flex flex-col items-center gap-3">
              <div className="w-16 h-16 rounded-full bg-muted/20 flex items-center justify-center">
                <Target className="w-8 h-8 text-muted-foreground/30" />
              </div>
              <p className="text-center text-muted-foreground text-sm font-medium">
                アクティブな目標がありません
              </p>
              <Link href="/goals">
                <span className="text-xs font-bold text-primary hover:underline flex items-center gap-1">
                  目標を設定する
                  <ArrowUpRight className="w-3 h-3" />
                </span>
              </Link>
            </div>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {mockActiveGoals.map((goal, index) => {
            const goalStyle = getGoalStyle(goal.type, goal.percentage)
            const Icon = goalStyle.icon
            const color = getCategoryColor(goal.title.replace("削減目標", "").replace("支出目標", ""))
            const bgColor = getCategoryColorWithAlpha(goal.title.replace("削減目標", "").replace("支出目標", ""), 0.05)

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

                <CardContent className="relative p-5 space-y-4">
                  {/* ヘッダー行 */}
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-3">
                      {/* グラデーションアイコン */}
                      <div
                        className={cn(
                          "w-12 h-12 rounded-2xl flex items-center justify-center text-white shadow-lg",
                          "transform transition-all duration-300 group-hover:scale-110 group-hover:rotate-6",
                          `bg-gradient-to-br ${goalStyle.gradient}`
                        )}
                      >
                        <Icon className="w-6 h-6" />
                      </div>
                      <div className="space-y-1">
                        <h3 className="font-bold text-foreground">
                          {goal.title}
                        </h3>
                        {goal.description && (
                          <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">
                            {goal.description}
                          </p>
                        )}
                      </div>
                    </div>
                    
                    {/* パーセンテージバッジ */}
                    <div className={cn(
                      "relative px-3 py-1.5 rounded-full font-black text-sm",
                      goalStyle.progressColor === "emerald" && "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400",
                      goalStyle.progressColor === "blue" && "bg-blue-500/10 text-blue-600 dark:text-blue-400",
                      goalStyle.progressColor === "amber" && "bg-amber-500/10 text-amber-600 dark:text-amber-400",
                      goalStyle.progressColor === "rose" && "bg-rose-500/10 text-rose-600 dark:text-rose-400"
                    )}>
                      {goal.percentage}%
                      {/* 90%以上でスパークル */}
                      {goal.percentage >= 90 && (
                        <Sparkles className="absolute -top-1 -right-1 w-4 h-4 text-amber-400 animate-pulse" />
                      )}
                    </div>
                  </div>

                  {/* 連続達成バッジ（ストリーク） */}
                  {goal.streak && goal.streak > 0 && (
                    <div className="flex items-center gap-2 px-3 py-2 rounded-xl bg-gradient-to-r from-orange-500/10 to-amber-500/10">
                      <Flame className="w-4 h-4 text-orange-500" />
                      <span className="text-xs font-bold text-orange-600 dark:text-orange-400">
                        {goal.streak}日連続達成中！
                      </span>
                    </div>
                  )}

                  {/* 金額情報 */}
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-1">
                      <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">目標額</p>
                      <p className="text-lg font-black text-foreground tabular-nums">
                        {formatCurrency(goal.targetAmount)}
                      </p>
                    </div>
                    <div className="space-y-1">
                      <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">現在の進捗</p>
                      <p className={cn(
                        "text-lg font-black tabular-nums",
                        goalStyle.progressColor === "emerald" && "text-emerald-600 dark:text-emerald-400",
                        goalStyle.progressColor === "blue" && "text-blue-600 dark:text-blue-400",
                        goalStyle.progressColor === "amber" && "text-amber-600 dark:text-amber-400",
                        goalStyle.progressColor === "rose" && "text-rose-600 dark:text-rose-400"
                      )}>
                        {formatCurrency(goal.currentAmount)}
                      </p>
                    </div>
                  </div>

                  {/* プログレスバー */}
                  <div className="space-y-2">
                    <Progress
                      value={goal.percentage}
                      className="h-2.5 bg-muted/30"
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

      {/* 達成済み目標 - トロフィーカード */}
      {mockCompletedGoals.length > 0 && (
        <Card className={cn(
          "relative overflow-hidden group",
          "border-amber-500/30 shadow-rich",
          "hover:shadow-lg transition-all duration-300",
          "animate-fade-in stagger-3"
        )}>
          {/* 背景グラデーション */}
          <div className="absolute inset-0 bg-gradient-to-r from-amber-500/5 via-yellow-500/5 to-orange-500/5" />
          
          {/* キラキラエフェクト */}
          <div className="absolute top-2 right-8 w-2 h-2 rounded-full bg-amber-400/50 animate-pulse" />
          <div className="absolute top-6 right-4 w-1.5 h-1.5 rounded-full bg-yellow-400/50 animate-pulse" style={{ animationDelay: "0.5s" }} />
          <div className="absolute top-3 right-16 w-1 h-1 rounded-full bg-orange-400/50 animate-pulse" style={{ animationDelay: "1s" }} />

          <CardContent className="relative p-4">
            <div className="flex items-center gap-4">
              {/* トロフィーアイコン */}
              <div className={cn(
                "w-12 h-12 rounded-2xl flex items-center justify-center",
                "bg-gradient-to-br from-amber-400 to-orange-500",
                "text-white shadow-lg",
                "transform transition-all duration-300 group-hover:scale-110 group-hover:rotate-12"
              )}>
                <Trophy className="h-6 w-6" />
              </div>
              
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                  <p className="text-sm font-bold text-foreground truncate">
                    {mockCompletedGoals[0].title}
                  </p>
                </div>
                {mockCompletedGoals[0].reward && (
                  <p className="text-xs font-bold text-amber-600 dark:text-amber-400 flex items-center gap-1">
                    <Star className="w-3 h-3" />
                    {mockCompletedGoals[0].reward}
                  </p>
                )}
                <p className="text-[10px] text-muted-foreground mt-1">
                  達成日: {mockCompletedGoals[0].completedDate}
                </p>
              </div>
              
              <ArrowUpRight className="w-5 h-5 text-muted-foreground/30 opacity-0 group-hover:opacity-100 transition-opacity" />
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
