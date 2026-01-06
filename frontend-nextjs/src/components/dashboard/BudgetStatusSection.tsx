"use client"

/**
 * 予算達成状況セクションコンポーネント
 * 
 * 予算の達成状況を視覚的にリッチに表示します。
 * 円形のプログレスインジケーターとカテゴリ別の詳細を提供します。
 * 
 * 【初心者向け解説】
 * - プログレスバー: 目標までの進捗を視覚的に表示するバー
 * - 円形インジケーター: SVGを使用して円形の進捗を描画
 * - stroke-dasharray: 円周を点線で表現し、進捗率を表示
 */

import Link from "next/link"
import { Card, CardContent } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { Wallet, ChevronRight, ArrowUpRight, AlertTriangle, CheckCircle2 } from "lucide-react"
import { formatCurrency } from "@/lib/formatters"
import { cn } from "@/lib/utils"
import { getCategoryIcon } from "@/lib/category-icons"
import { getCategoryColor, getCategoryColorWithAlpha } from "@/lib/category-colors"

/**
 * 円形プログレスコンポーネント
 * SVGを使用して円形の進捗バーを描画します
 * 
 * @param percentage - 進捗率（0-100）
 * @param size - 円のサイズ（ピクセル）
 * @param strokeWidth - 線の太さ
 */
function CircularProgress({ 
  percentage, 
  size = 120, 
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
      {/* 背景の円 */}
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
        {/* プログレスの円 */}
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
      {/* 中央のパーセンテージ */}
      <div className="absolute inset-0 flex items-center justify-center">
        <span className="text-2xl font-black tabular-nums">{percentage}%</span>
      </div>
    </div>
  )
}

export function BudgetStatusSection() {
  // モックデータ（実際のロジックは将来的に実装）
  const mockData = {
    totalBudget: 400000,
    used: 280000,
    remaining: 120000,
    percentage: 70,
    isOverBudget: false,
    daysRemaining: 11,  // 今月の残り日数
    dailyAvailable: 10909,  // 残り日数で割った1日あたりの予算
    overBudgetCategories: [
      { category: "光熱費", overAmount: 5000, percentage: 110 },
    ],
  }

  // 予算使用状況のステータスを判定
  const getStatusInfo = (percentage: number) => {
    if (percentage >= 100) return { status: "danger", label: "予算超過", color: "rose" }
    if (percentage >= 80) return { status: "warning", label: "注意", color: "amber" }
    return { status: "safe", label: "順調", color: "emerald" }
  }

  const statusInfo = getStatusInfo(mockData.percentage)

  return (
    <div className="space-y-4">
      {/* セクションヘッダー */}
      <div className="flex items-center justify-between px-1">
        <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
          <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-cyan-400 to-blue-500 text-white shadow-lg">
            <Wallet className="h-4 w-4" />
          </div>
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

      {/* メインカード */}
      <Card className={cn(
        "relative overflow-hidden",
        "border-border/40 shadow-rich shadow-rich-hover",
        "bg-gradient-to-br from-card via-card to-cyan-500/5",
        "transition-all duration-500 animate-fade-in"
      )}>
        {/* 背景装飾 */}
        <div className="absolute inset-0 pattern-dots pointer-events-none opacity-50" />
        <div className="absolute top-0 right-0 w-64 h-64 bg-gradient-to-bl from-cyan-500/10 to-transparent rounded-full -translate-y-1/2 translate-x-1/2" />

        <CardContent className="relative p-6">
          {/* メインコンテンツ: グリッドレイアウト */}
          <div className="grid gap-6 md:grid-cols-[auto_1fr]">
            
            {/* 左側: 円形プログレス */}
            <div className="flex flex-col items-center justify-center">
              <CircularProgress 
                percentage={mockData.percentage}
                size={140}
                strokeWidth={10}
                color={
                  statusInfo.color === "rose" ? "#f43f5e" :
                  statusInfo.color === "amber" ? "#f59e0b" : "#10b981"
                }
              />
              {/* ステータスバッジ */}
              <div className={cn(
                "mt-4 px-3 py-1 rounded-full text-xs font-bold flex items-center gap-1.5",
                statusInfo.color === "rose" && "bg-rose-500/10 text-rose-600 dark:text-rose-400",
                statusInfo.color === "amber" && "bg-amber-500/10 text-amber-600 dark:text-amber-400",
                statusInfo.color === "emerald" && "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
              )}>
                {statusInfo.status === "danger" ? (
                  <AlertTriangle className="w-3 h-3" />
                ) : (
                  <CheckCircle2 className="w-3 h-3" />
                )}
                {statusInfo.label}
              </div>
            </div>

            {/* 右側: 詳細情報 */}
            <div className="space-y-6">
              {/* 予算情報グリッド */}
              <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="space-y-1">
                  <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">予算総額</p>
                  <p className="text-xl font-black text-foreground tabular-nums">
                    {formatCurrency(mockData.totalBudget)}
                  </p>
                </div>
                <div className="space-y-1">
                  <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">使用済み</p>
                  <p className="text-xl font-black text-foreground tabular-nums">
                    {formatCurrency(mockData.used)}
                  </p>
                </div>
                <div className="space-y-1">
                  <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">残り予算</p>
                  <p className={cn(
                    "text-xl font-black tabular-nums",
                    mockData.remaining > 0 ? "text-emerald-600 dark:text-emerald-400" : "text-rose-600 dark:text-rose-400"
                  )}>
                    {formatCurrency(mockData.remaining)}
                  </p>
                </div>
                <div className="space-y-1">
                  <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">1日あたり使える額</p>
                  <div className="flex items-baseline gap-2">
                    <p className="text-xl font-black text-primary tabular-nums">
                      {formatCurrency(mockData.dailyAvailable)}
                    </p>
                    <span className="text-[10px] text-muted-foreground">
                      残り{mockData.daysRemaining}日
                    </span>
                  </div>
                </div>
              </div>

              {/* プログレスバー */}
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-muted-foreground flex items-center gap-1.5">
                    <div className={cn(
                      "w-2 h-2 rounded-full",
                      statusInfo.color === "rose" && "bg-rose-500",
                      statusInfo.color === "amber" && "bg-amber-500",
                      statusInfo.color === "emerald" && "bg-emerald-500"
                    )} />
                    今月の予算消化状況
                  </span>
                  <span className="text-sm font-black text-foreground tabular-nums">{mockData.percentage}%</span>
                </div>
                <div className="relative">
                  <Progress value={mockData.percentage} className="h-3 bg-muted/30" />
                  {/* 警告ライン（80%の位置） */}
                  <div className="absolute top-0 bottom-0 left-[80%] w-px bg-amber-500/50" />
                </div>
                <div className="flex justify-between text-[10px] text-muted-foreground">
                  <span>0%</span>
                  <span className="text-amber-500">80%</span>
                  <span>100%</span>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 予算超過の警告カード */}
      {mockData.overBudgetCategories.length > 0 && (
        <div className="grid gap-3 animate-fade-in stagger-2">
          {mockData.overBudgetCategories.map((item, index) => {
            const Icon = getCategoryIcon(item.category)
            const color = getCategoryColor(item.category)
            const bgColor = getCategoryColorWithAlpha(item.category, 0.1)

            return (
              <Card
                key={index}
                className={cn(
                  "relative overflow-hidden",
                  "border-rose-500/30 shadow-rich",
                  "hover:shadow-lg transition-all duration-300",
                  "group cursor-pointer"
                )}
              >
                {/* 背景グラデーション */}
                <div 
                  className="absolute inset-0 opacity-30"
                  style={{ background: `linear-gradient(135deg, ${bgColor} 0%, transparent 100%)` }}
                />
                
                {/* 警告アイコンの装飾 */}
                <div className="absolute -right-4 -bottom-4 w-24 h-24 rounded-full bg-rose-500/5 group-hover:bg-rose-500/10 transition-colors" />

                <CardContent className="relative p-4 flex items-center gap-4">
                  <div
                    className="w-12 h-12 rounded-2xl flex items-center justify-center text-white shadow-lg transform transition-transform group-hover:scale-110"
                    style={{ backgroundColor: color }}
                  >
                    <Icon className="w-6 h-6" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <p className="text-sm font-bold text-foreground">
                        {item.category}
                      </p>
                      <span className="text-xs font-bold text-rose-600 dark:text-rose-400 bg-rose-500/10 px-2 py-0.5 rounded-full">
                        予算超過
                      </span>
                    </div>
                    <p className="text-xs text-muted-foreground">
                      設定予算を <span className="font-bold text-rose-600 dark:text-rose-400">{item.percentage - 100}%</span> 超過しています
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-xs text-muted-foreground mb-1">超過額</p>
                    <p className="text-lg font-black text-rose-600 dark:text-rose-400 tabular-nums">
                      +{formatCurrency(item.overAmount)}
                    </p>
                  </div>
                  <ChevronRight className="w-5 h-5 text-muted-foreground/30 group-hover:text-primary transition-all group-hover:translate-x-0.5" />
                </CardContent>
              </Card>
            )
          })}
        </div>
      )}
    </div>
  )
}
