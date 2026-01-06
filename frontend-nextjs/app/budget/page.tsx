"use client"

/**
 * 予算管理ページコンポーネント
 * 
 * 予算の設定、確認、管理を行うページです。
 * ホームページと統一感のあるリッチなデザインを採用しています。
 * 
 * 【初心者向け解説】
 * - 予算とは「使っても良い金額の上限」のことです
 * - カテゴリ別に予算を設定して、使いすぎを防ぎます
 * - 円形プログレスで予算の消化状況を視覚的に表示します
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
  Wallet, 
  AlertTriangle, 
  CheckCircle2, 
  TrendingUp,
  PiggyBank,
  Sparkles,
  ArrowUpRight
} from "lucide-react"
import { formatCurrency } from "@/lib/formatters"
import { cn } from "@/lib/utils"
import { getCategoryIcon } from "@/lib/category-icons"
import { getCategoryColor, getCategoryColorWithAlpha } from "@/lib/category-colors"

/**
 * 円形プログレスコンポーネント
 * SVGを使用して円形の進捗バーを描画します
 */
function CircularProgress({ 
  percentage, 
  size = 100, 
  strokeWidth = 8,
  color = "hsl(var(--primary))"
}: { 
  percentage: number
  size?: number
  strokeWidth?: number
  color?: string
}) {
  const radius = (size - strokeWidth) / 2
  const circumference = radius * 2 * Math.PI
  const offset = circumference - (percentage / 100) * circumference

  return (
    <div className="relative" style={{ width: size, height: size }}>
      <svg className="transform -rotate-90" width={size} height={size}>
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="currentColor"
          strokeWidth={strokeWidth}
          className="text-muted/20"
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={color}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          className="transition-all duration-1000 ease-out"
        />
      </svg>
      <div className="absolute inset-0 flex items-center justify-center">
        <span className="text-xl font-black tabular-nums">{percentage}%</span>
      </div>
    </div>
  )
}

export default function BudgetPage() {
  const { user, signOut } = useAuthenticator((context) => [context.user])
  const username = useMemo(() => getUserDisplayName(user), [user])

  // モックデータ（実際のロジックは未実装）
  const mockBudgetSummary = {
    totalBudget: 400000,
    used: 280000,
    remaining: 120000,
    percentage: 70,
    daysRemaining: 11,
    dailyAvailable: 10909,
  }

  const mockCategoryBudgets = [
    { category: "食費", budget: 100000, actual: 85000, percentage: 85, status: "good" },
    { category: "光熱費", budget: 50000, actual: 55000, percentage: 110, status: "over" },
    { category: "住居費", budget: 150000, actual: 100000, percentage: 67, status: "good" },
    { category: "交通費", budget: 50000, actual: 30000, percentage: 60, status: "good" },
    { category: "その他", budget: 50000, actual: 20000, percentage: 40, status: "good" },
  ]

  // ステータスに応じた色を取得
  const getStatusColor = (percentage: number) => {
    if (percentage >= 100) return { color: "#f43f5e", label: "超過", bg: "bg-rose-500/10" }
    if (percentage >= 80) return { color: "#f59e0b", label: "注意", bg: "bg-amber-500/10" }
    return { color: "#10b981", label: "良好", bg: "bg-emerald-500/10" }
  }

  const summaryStatus = getStatusColor(mockBudgetSummary.percentage)

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
                <div className="absolute inset-0 bg-cyan-500/20 rounded-xl blur-md" />
                <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-cyan-500 to-blue-500 text-white shadow-lg">
                  <Wallet className="h-5 w-5" />
                </div>
              </div>
              <div>
                <h1 className="text-2xl md:text-3xl font-black text-foreground tracking-tight">
                  予算管理
                </h1>
                <p className="text-sm text-muted-foreground flex items-center gap-1">
                  <Sparkles className="w-3 h-3" />
                  予算の設定と達成状況を確認できます
                </p>
              </div>
            </div>
            <Button className={cn(
              "gap-2 shadow-lg",
              "bg-gradient-to-r from-cyan-500 to-blue-500 hover:from-cyan-600 hover:to-blue-600",
              "transition-all duration-300 hover:shadow-xl hover:-translate-y-0.5"
            )}>
              <Plus className="h-4 w-4" />
              予算を設定
            </Button>
          </div>
        </div>

        {/* ==================== */}
        {/* 予算サマリーカード */}
        {/* ==================== */}
        <Card className={cn(
          "relative overflow-hidden",
          "border-border/40 shadow-rich shadow-rich-hover",
          "bg-gradient-to-br from-card via-card to-cyan-500/5",
          "transition-all duration-500 animate-fade-in stagger-1"
        )}>
          {/* 背景装飾 */}
          <div className="absolute inset-0 pattern-dots pointer-events-none opacity-50" />
          <div className="absolute top-0 right-0 w-64 h-64 bg-gradient-to-bl from-cyan-500/10 to-transparent rounded-full -translate-y-1/2 translate-x-1/2" />

          <CardContent className="relative p-6">
            <div className="flex items-center gap-2 mb-6">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-cyan-400 to-blue-500 flex items-center justify-center text-white">
                <PiggyBank className="w-4 h-4" />
              </div>
              <h2 className="text-lg font-bold text-foreground">今月の予算サマリー</h2>
            </div>

            <div className="grid gap-6 md:grid-cols-[auto_1fr]">
              {/* 円形プログレス */}
              <div className="flex flex-col items-center justify-center">
                <CircularProgress 
                  percentage={mockBudgetSummary.percentage}
                  size={120}
                  strokeWidth={10}
                  color={summaryStatus.color}
                />
                <div className={cn(
                  "mt-3 px-3 py-1 rounded-full text-xs font-bold flex items-center gap-1.5",
                  summaryStatus.bg,
                  summaryStatus.color === "#f43f5e" && "text-rose-600 dark:text-rose-400",
                  summaryStatus.color === "#f59e0b" && "text-amber-600 dark:text-amber-400",
                  summaryStatus.color === "#10b981" && "text-emerald-600 dark:text-emerald-400"
                )}>
                  {summaryStatus.label === "超過" ? <AlertTriangle className="w-3 h-3" /> : <CheckCircle2 className="w-3 h-3" />}
                  {summaryStatus.label}
                </div>
              </div>

              {/* 詳細情報 */}
              <div className="space-y-6">
                <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                  <div className="space-y-1">
                    <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">予算総額</p>
                    <p className="text-xl font-black text-foreground tabular-nums">
                      {formatCurrency(mockBudgetSummary.totalBudget)}
                    </p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">使用済み</p>
                    <p className="text-xl font-black text-foreground tabular-nums">
                      {formatCurrency(mockBudgetSummary.used)}
                    </p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">残り予算</p>
                    <p className={cn(
                      "text-xl font-black tabular-nums",
                      mockBudgetSummary.remaining > 0 ? "text-emerald-600 dark:text-emerald-400" : "text-rose-600 dark:text-rose-400"
                    )}>
                      {formatCurrency(mockBudgetSummary.remaining)}
                    </p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">1日あたり</p>
                    <div className="flex items-baseline gap-2">
                      <p className="text-xl font-black text-primary tabular-nums">
                        {formatCurrency(mockBudgetSummary.dailyAvailable)}
                      </p>
                      <span className="text-[10px] text-muted-foreground">
                        残り{mockBudgetSummary.daysRemaining}日
                      </span>
                    </div>
                  </div>
                </div>

                {/* プログレスバー */}
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-muted-foreground">予算消化状況</span>
                    <span className="text-sm font-black text-foreground tabular-nums">{mockBudgetSummary.percentage}%</span>
                  </div>
                  <div className="relative">
                    <Progress value={mockBudgetSummary.percentage} className="h-3 bg-muted/30" />
                    <div className="absolute top-0 bottom-0 left-[80%] w-px bg-amber-500/50" />
                  </div>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* ==================== */}
        {/* カテゴリー別予算一覧 */}
        {/* ==================== */}
        <div className="space-y-4">
          <div className="flex items-center justify-between px-1">
            <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
              <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-violet-400 to-purple-500 flex items-center justify-center text-white">
                <TrendingUp className="h-4 w-4" />
              </div>
              カテゴリー別予算
            </h2>
            <span className="text-xs font-bold bg-primary/10 text-primary px-3 py-1 rounded-full">
              {mockCategoryBudgets.length} カテゴリ
            </span>
          </div>

          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {mockCategoryBudgets.map((item, index) => {
              const Icon = getCategoryIcon(item.category)
              const color = getCategoryColor(item.category)
              const bgColor = getCategoryColorWithAlpha(item.category, 0.05)
              const statusInfo = getStatusColor(item.percentage)

              return (
                <Card
                  key={item.category}
                  className={cn(
                    "relative overflow-hidden group",
                    "border-border/40 shadow-rich",
                    "hover:shadow-xl transition-all duration-500",
                    "hover:-translate-y-1",
                    "animate-fade-in",
                    `stagger-${Math.min(index + 1, 5)}`
                  )}
                >
                  {/* 背景グラデーション */}
                  <div 
                    className="absolute inset-0"
                    style={{ background: `linear-gradient(135deg, ${bgColor} 0%, transparent 50%)` }}
                  />

                  <CardContent className="relative p-5 space-y-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div
                          className={cn(
                            "w-12 h-12 rounded-2xl flex items-center justify-center text-white shadow-lg",
                            "transition-all duration-300 group-hover:scale-110 group-hover:rotate-6"
                          )}
                          style={{ backgroundColor: color }}
                        >
                          <Icon className="w-6 h-6" />
                        </div>
                        <div className="space-y-0.5">
                          <h3 className="font-bold text-foreground">{item.category}</h3>
                          <span className={cn(
                            "text-[10px] font-bold flex items-center gap-1",
                            item.status === "over" ? "text-rose-600 dark:text-rose-400" : "text-emerald-600 dark:text-emerald-400"
                          )}>
                            {item.status === "over" ? (
                              <>
                                <AlertTriangle className="w-3 h-3" />
                                予算超過
                              </>
                            ) : (
                              <>
                                <CheckCircle2 className="w-3 h-3" />
                                良好
                              </>
                            )}
                          </span>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">達成率</p>
                        <p className={cn(
                          "text-2xl font-black tabular-nums transition-transform duration-300 group-hover:scale-105"
                        )} style={{ color: statusInfo.color }}>
                          {item.percentage}%
                        </p>
                      </div>
                    </div>

                    <div className="space-y-2">
                      <div className="flex items-center justify-between text-[11px] font-bold text-muted-foreground">
                        <span>使用: {formatCurrency(item.actual)}</span>
                        <span>予算: {formatCurrency(item.budget)}</span>
                      </div>
                      <Progress
                        value={Math.min(item.percentage, 100)}
                        className="h-2.5 bg-muted/30"
                      />
                    </div>
                  </CardContent>
                </Card>
              )
            })}
          </div>
        </div>

        {/* ==================== */}
        {/* 予算アラート */}
        {/* ==================== */}
        {mockCategoryBudgets.some((item) => item.status === "over") && (
          <Card className={cn(
            "relative overflow-hidden",
            "border-rose-500/30 shadow-rich",
            "animate-fade-in"
          )}>
            {/* 背景グラデーション */}
            <div className="absolute inset-0 bg-gradient-to-r from-rose-500/5 to-orange-500/5" />
            
            <CardContent className="relative p-5">
              <div className="flex items-start gap-4">
                <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-rose-400 to-orange-500 flex items-center justify-center text-white shadow-lg">
                  <AlertTriangle className="h-6 w-6" />
                </div>
                <div className="flex-1">
                  <h3 className="font-bold text-rose-600 dark:text-rose-400 mb-1">
                    予算超過の警告
                  </h3>
                  <p className="text-sm text-muted-foreground mb-3">
                    光熱費が予算を10%超過しています。残りの日数を考慮して、支出を見直すことをお勧めします。
                  </p>
                  <Button variant="outline" size="sm" className="gap-1 border-rose-500/30 text-rose-600 hover:bg-rose-500/10">
                    詳細を確認
                    <ArrowUpRight className="w-3 h-3" />
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        )}
      </div>
    </AppLayout>
  )
}
