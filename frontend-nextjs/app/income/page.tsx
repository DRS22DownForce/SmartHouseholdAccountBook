"use client"

/**
 * 収入管理ページコンポーネント
 * 
 * 収入の一覧表示、追加、編集、削除を行うページです。
 * ホームページと統一感のあるリッチなデザインを採用しています。
 * 
 * 【初心者向け解説】
 * - 収入は給与、副業、投資収益など様々な種類があります
 * - このページで収入を記録し、収支バランスを把握できます
 * - カード形式で収入を視覚的に表示します
 */

import { useMemo } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { AppLayout } from "@/components/layout/AppLayout"
import { getUserDisplayName } from "@/lib/user-utils"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { 
  Plus, 
  ArrowUpCircle, 
  TrendingUp, 
  Calendar,
  Sparkles,
  Briefcase,
  DollarSign,
  Clock,
  Edit,
  Trash2,
  ChevronRight,
  ArrowUpRight
} from "lucide-react"
import { formatCurrency } from "@/lib/formatters"
import { cn } from "@/lib/utils"

/**
 * 収入カテゴリに応じたアイコンとスタイルを取得
 */
const getCategoryStyle = (category: string) => {
  const styles: Record<string, { icon: React.ElementType; gradient: string; color: string }> = {
    "給与": { 
      icon: Briefcase, 
      gradient: "from-blue-400 to-cyan-500",
      color: "text-blue-600 dark:text-blue-400"
    },
    "副業": { 
      icon: DollarSign, 
      gradient: "from-emerald-400 to-green-500",
      color: "text-emerald-600 dark:text-emerald-400"
    },
  }
  return styles[category] || { 
    icon: DollarSign, 
    gradient: "from-gray-400 to-gray-500",
    color: "text-gray-600 dark:text-gray-400"
  }
}

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
    sources: 2,
  }

  // サマリーカードの設定
  const summaryCards = [
    {
      title: "今月の収入",
      value: mockSummary.currentMonth,
      icon: Calendar,
      gradient: "from-emerald-400 to-green-500",
      valueColor: "text-emerald-600 dark:text-emerald-400"
    },
    {
      title: "平均収入",
      value: mockSummary.average,
      icon: TrendingUp,
      gradient: "from-blue-400 to-cyan-500",
      valueColor: "text-blue-600 dark:text-blue-400"
    },
    {
      title: "前月比",
      value: `+${mockSummary.previousMonthChange}%`,
      isPercentage: true,
      icon: ArrowUpCircle,
      gradient: "from-violet-400 to-purple-500",
      valueColor: "text-violet-600 dark:text-violet-400"
    },
  ]

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
                <div className="absolute inset-0 bg-emerald-500/20 rounded-xl blur-md" />
                <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-400 to-green-500 text-white shadow-lg">
                  <ArrowUpCircle className="h-5 w-5" />
                </div>
              </div>
              <div>
                <h1 className="text-2xl md:text-3xl font-black text-foreground tracking-tight">
                  収入管理
                </h1>
                <p className="text-sm text-muted-foreground flex items-center gap-1">
                  <Sparkles className="w-3 h-3" />
                  収入の記録と管理を行います
                </p>
              </div>
            </div>
            <Button className={cn(
              "gap-2 shadow-lg",
              "bg-gradient-to-r from-emerald-400 to-green-500 hover:from-emerald-500 hover:to-green-600",
              "transition-all duration-300 hover:shadow-xl hover:-translate-y-0.5"
            )}>
              <Plus className="h-4 w-4" />
              収入を追加
            </Button>
          </div>

          {/* クイック統計バッジ */}
          <div className="mt-4 flex flex-wrap gap-3">
            <div className={cn(
              "inline-flex items-center gap-2 px-3 py-2 rounded-xl",
              "bg-emerald-500/10 border border-emerald-500/20",
              "animate-fade-in stagger-1"
            )}>
              <DollarSign className="w-4 h-4 text-emerald-500" />
              <span className="text-xs text-muted-foreground">収入源</span>
              <span className="text-sm font-bold text-emerald-600 dark:text-emerald-400">{mockSummary.sources}件</span>
            </div>
            <div className={cn(
              "inline-flex items-center gap-2 px-3 py-2 rounded-xl",
              "bg-blue-500/10 border border-blue-500/20",
              "animate-fade-in stagger-2"
            )}>
              <TrendingUp className="w-4 h-4 text-blue-500" />
              <span className="text-xs text-muted-foreground">前年比</span>
              <span className="text-sm font-bold text-blue-600 dark:text-blue-400">+12.5%</span>
            </div>
          </div>
        </div>

        {/* ==================== */}
        {/* 収入サマリーカード */}
        {/* ==================== */}
        <div className="grid gap-4 md:grid-cols-3">
          {summaryCards.map((card, index) => {
            const Icon = card.icon
            return (
              <Card 
                key={card.title}
                className={cn(
                  "relative overflow-hidden group",
                  "border-border/40 shadow-rich",
                  "hover:shadow-xl transition-all duration-500",
                  "hover:-translate-y-1",
                  "animate-fade-in",
                  index === 0 ? "stagger-1" : index === 1 ? "stagger-2" : "stagger-3"
                )}
              >
                {/* 背景グラデーション */}
                <div className={cn(
                  "absolute inset-0 bg-gradient-to-br opacity-5",
                  card.gradient
                )} />
                
                {/* 装飾的な円 */}
                <div className="absolute -top-10 -right-10 w-32 h-32 rounded-full bg-current opacity-[0.03] group-hover:opacity-[0.06] transition-opacity" />

                <CardContent className="relative p-5 md:p-6">
                  {/* アイコン */}
                  <div className={cn(
                    "flex h-12 w-12 items-center justify-center rounded-2xl text-white shadow-lg mb-4",
                    "transform transition-all duration-300 group-hover:scale-110 group-hover:rotate-6",
                    `bg-gradient-to-br ${card.gradient}`
                  )}>
                    <Icon className="h-6 w-6" />
                  </div>

                  {/* タイトル */}
                  <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-2">
                    {card.title}
                  </p>

                  {/* 金額 */}
                  <p className={cn(
                    "text-2xl md:text-3xl font-black tracking-tight tabular-nums",
                    "transition-transform duration-300 group-hover:scale-[1.02]",
                    card.valueColor
                  )}>
                    {card.isPercentage ? card.value : formatCurrency(card.value as number)}
                  </p>
                </CardContent>
              </Card>
            )
          })}
        </div>

        {/* ==================== */}
        {/* 収入一覧 */}
        {/* ==================== */}
        <div className="space-y-4">
          <div className="flex items-center justify-between px-1">
            <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
              <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-indigo-400 to-purple-500 flex items-center justify-center text-white">
                <Clock className="h-4 w-4" />
              </div>
              収入履歴
            </h2>
            <Button variant="ghost" size="sm" className="gap-1 text-xs">
              すべて見る
              <ArrowUpRight className="w-3 h-3" />
            </Button>
          </div>

          <Card className={cn(
            "relative overflow-hidden",
            "border-border/40 shadow-rich shadow-rich-hover",
            "bg-gradient-to-br from-card via-card to-emerald-500/5",
            "transition-all duration-500 animate-fade-in"
          )}>
            {/* 背景装飾 */}
            <div className="absolute inset-0 pattern-dots pointer-events-none opacity-50" />

            <CardContent className="relative p-0">
              {mockIncomeData.length === 0 ? (
                <div className="p-8 text-center">
                  <div className="w-16 h-16 mx-auto rounded-full bg-muted/20 flex items-center justify-center mb-4">
                    <DollarSign className="w-8 h-8 text-muted-foreground/30" />
                  </div>
                  <p className="text-muted-foreground mb-4">
                    収入データがありません
                  </p>
                  <Button variant="outline" className="gap-2">
                    <Plus className="w-4 h-4" />
                    最初の収入を追加
                  </Button>
                </div>
              ) : (
                <div className="divide-y divide-border/40">
                  {mockIncomeData.map((income, index) => {
                    const style = getCategoryStyle(income.category)
                    const Icon = style.icon

                    return (
                      <div
                        key={income.id}
                        className={cn(
                          "group relative flex items-center gap-4 p-4 md:p-5",
                          "hover:bg-muted/5 transition-all duration-300",
                          "cursor-pointer",
                          "animate-fade-in",
                          `stagger-${Math.min(index + 1, 5)}`
                        )}
                      >
                        {/* アイコン */}
                        <div className={cn(
                          "flex-shrink-0 w-12 h-12 rounded-2xl flex items-center justify-center text-white shadow-lg",
                          "transform transition-all duration-300 group-hover:scale-110 group-hover:rotate-6",
                          `bg-gradient-to-br ${style.gradient}`
                        )}>
                          <Icon className="h-5 w-5" />
                        </div>

                        {/* 収入情報 */}
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-1">
                            <p className="font-bold text-foreground truncate">
                              {income.description}
                            </p>
                            <span className={cn(
                              "flex-shrink-0 text-[10px] font-bold px-2 py-0.5 rounded-full",
                              "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
                            )}>
                              収入
                            </span>
                          </div>
                          <div className="flex items-center gap-3 text-xs text-muted-foreground">
                            <span className="flex items-center gap-1">
                              <Clock className="w-3 h-3" />
                              {income.date}
                            </span>
                            <span>•</span>
                            <span className={cn(
                              "px-2 py-0.5 rounded-full",
                              "bg-muted/50",
                              style.color,
                              "font-medium"
                            )}>
                              {income.category}
                            </span>
                          </div>
                        </div>

                        {/* 金額 */}
                        <div className="flex-shrink-0 text-right">
                          <p className={cn(
                            "text-lg md:text-xl font-black tabular-nums",
                            "text-emerald-600 dark:text-emerald-400",
                            "transition-transform duration-300 group-hover:scale-105"
                          )}>
                            +{formatCurrency(income.amount)}
                          </p>
                        </div>

                        {/* アクションボタン */}
                        <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                          <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full">
                            <Edit className="h-4 w-4" />
                          </Button>
                          <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full text-destructive hover:bg-destructive/10">
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>

                        <ChevronRight className="w-5 h-5 text-muted-foreground/30 opacity-0 group-hover:opacity-100 transition-all group-hover:translate-x-0.5" />
                      </div>
                    )
                  })}
                </div>
              )}
            </CardContent>

            {/* フッター */}
            {mockIncomeData.length > 0 && (
              <div className="px-4 py-3 bg-muted/5 border-t border-border/40">
                <Button 
                  variant="ghost" 
                  className="w-full justify-center gap-2 text-sm font-bold text-muted-foreground hover:text-foreground"
                >
                  すべての収入を表示
                  <ArrowUpRight className="w-4 h-4" />
                </Button>
              </div>
            )}
          </Card>
        </div>
      </div>
    </AppLayout>
  )
}
