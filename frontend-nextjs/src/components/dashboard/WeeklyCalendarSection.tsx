"use client"

/**
 * 週間カレンダーセクションコンポーネント
 * 
 * 過去7日間の支出を視覚的なカレンダー形式で表示します。
 * 日ごとの支出額をドット/バーで表現し、一目で支出パターンが分かります。
 * 
 * 【初心者向け解説】
 * - このコンポーネントは支出の「可視化」を目的としています
 * - 色の濃さで支出額の大きさを表現しています（濃い = 高額）
 * - ホバー時にツールチップで詳細を表示します
 */

import { Card, CardContent } from "@/components/ui/card"
import { Calendar, ChevronLeft, ChevronRight } from "lucide-react"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"
import { formatCurrency } from "@/lib/formatters"

/**
 * 日別支出データの型定義
 */
interface DailyExpense {
  date: Date              // 日付
  dayOfWeek: string       // 曜日（例: "月", "火"）
  dayNumber: number       // 日（例: 15）
  amount: number          // 支出額
  transactions: number    // 取引件数
  isToday: boolean        // 今日かどうか
}

export function WeeklyCalendarSection() {
  // モックデータ: 過去7日間の支出データ
  // 実際のアプリではAPIから取得します
  const mockWeeklyData: DailyExpense[] = [
    { date: new Date(), dayOfWeek: "月", dayNumber: 13, amount: 2500, transactions: 3, isToday: false },
    { date: new Date(), dayOfWeek: "火", dayNumber: 14, amount: 8500, transactions: 5, isToday: false },
    { date: new Date(), dayOfWeek: "水", dayNumber: 15, amount: 1200, transactions: 2, isToday: false },
    { date: new Date(), dayOfWeek: "木", dayNumber: 16, amount: 15000, transactions: 4, isToday: false },
    { date: new Date(), dayOfWeek: "金", dayNumber: 17, amount: 6800, transactions: 6, isToday: false },
    { date: new Date(), dayOfWeek: "土", dayNumber: 18, amount: 12500, transactions: 8, isToday: false },
    { date: new Date(), dayOfWeek: "日", dayNumber: 19, amount: 3200, transactions: 2, isToday: true },
  ]

  // 週間の最大支出額を計算（バーの高さ計算用）
  const maxAmount = Math.max(...mockWeeklyData.map(d => d.amount))
  // 週間の合計支出額
  const totalAmount = mockWeeklyData.reduce((sum, d) => sum + d.amount, 0)
  // 日平均支出額
  const avgAmount = totalAmount / mockWeeklyData.length

  /**
   * 支出額に応じた色の濃さを計算
   * @param amount 支出額
   * @returns 色クラス名
   */
  const getIntensityColor = (amount: number): string => {
    const ratio = amount / maxAmount
    if (ratio > 0.8) return "bg-rose-500"      // 高額
    if (ratio > 0.6) return "bg-rose-400"      // やや高額
    if (ratio > 0.4) return "bg-amber-400"     // 中程度
    if (ratio > 0.2) return "bg-emerald-400"   // 低め
    return "bg-emerald-300"                    // 低額
  }

  /**
   * バーの高さを計算（最小20%、最大100%）
   */
  const getBarHeight = (amount: number): number => {
    return Math.max(20, (amount / maxAmount) * 100)
  }

  return (
    <div className="space-y-4">
      {/* セクションヘッダー */}
      <div className="flex items-center justify-between px-1">
        <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
          <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-violet-500/10 text-violet-500">
            <Calendar className="h-4 w-4" />
          </div>
          週間支出カレンダー
        </h2>
        
        {/* 週の切り替えボタン */}
        <div className="flex items-center gap-1">
          <Button variant="ghost" size="icon" className="h-8 w-8">
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <span className="text-sm font-medium text-muted-foreground px-2">
            今週
          </span>
          <Button variant="ghost" size="icon" className="h-8 w-8">
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      </div>

      {/* メインカード */}
      <Card className={cn(
        "relative overflow-hidden",
        "border-border/40 shadow-rich shadow-rich-hover",
        "bg-gradient-to-br from-card via-card to-violet-500/5",
        "transition-all duration-500"
      )}>
        {/* 背景装飾 */}
        <div className="absolute inset-0 pattern-grid pointer-events-none" />
        
        <CardContent className="p-6">
          {/* 週間サマリー */}
          <div className="flex items-center justify-between mb-6 pb-4 border-b border-border/40">
            <div className="space-y-1">
              <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
                今週の合計支出
              </p>
              <p className="text-2xl font-black text-foreground tabular-nums animate-count-up">
                {formatCurrency(totalAmount)}
              </p>
            </div>
            <div className="text-right space-y-1">
              <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
                日平均
              </p>
              <p className="text-lg font-bold text-muted-foreground tabular-nums">
                {formatCurrency(Math.round(avgAmount))}
              </p>
            </div>
          </div>

          {/* カレンダーグリッド */}
          <div className="grid grid-cols-7 gap-2 md:gap-4">
            {mockWeeklyData.map((day, index) => (
              <div 
                key={index}
                className={cn(
                  "group relative flex flex-col items-center",
                  // スタガードアニメーション
                  "animate-fade-in",
                  `stagger-${Math.min(index + 1, 5)}`
                )}
              >
                {/* 曜日ラベル */}
                <span className={cn(
                  "text-xs font-bold mb-2",
                  day.isToday ? "text-primary" : "text-muted-foreground"
                )}>
                  {day.dayOfWeek}
                </span>

                {/* バーコンテナ */}
                <div className={cn(
                  "relative w-full h-24 md:h-32 rounded-xl overflow-hidden",
                  "bg-muted/20",
                  "flex items-end justify-center",
                  "transition-all duration-300",
                  day.isToday && "ring-2 ring-primary ring-offset-2 ring-offset-background"
                )}>
                  {/* 支出バー */}
                  <div
                    className={cn(
                      "w-full rounded-t-lg transition-all duration-500 ease-out",
                      getIntensityColor(day.amount),
                      "group-hover:opacity-90"
                    )}
                    style={{ height: `${getBarHeight(day.amount)}%` }}
                  />
                  
                  {/* ホバー時のツールチップ */}
                  <div className={cn(
                    "absolute inset-0 flex items-center justify-center",
                    "bg-background/90 backdrop-blur-sm",
                    "opacity-0 group-hover:opacity-100",
                    "transition-opacity duration-200",
                    "rounded-xl"
                  )}>
                    <div className="text-center p-2">
                      <p className="text-sm font-black text-foreground">
                        {formatCurrency(day.amount)}
                      </p>
                      <p className="text-[10px] text-muted-foreground mt-1">
                        {day.transactions}件の取引
                      </p>
                    </div>
                  </div>
                </div>

                {/* 日付 */}
                <div className={cn(
                  "mt-2 w-8 h-8 rounded-full flex items-center justify-center",
                  "transition-all duration-300",
                  day.isToday 
                    ? "bg-primary text-primary-foreground font-bold" 
                    : "text-muted-foreground group-hover:bg-muted"
                )}>
                  <span className="text-sm">{day.dayNumber}</span>
                </div>
              </div>
            ))}
          </div>

          {/* 凡例 */}
          <div className="flex items-center justify-center gap-4 mt-6 pt-4 border-t border-border/40">
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 rounded-full bg-emerald-400" />
              <span className="text-xs text-muted-foreground">低</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 rounded-full bg-amber-400" />
              <span className="text-xs text-muted-foreground">中</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 rounded-full bg-rose-500" />
              <span className="text-xs text-muted-foreground">高</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

