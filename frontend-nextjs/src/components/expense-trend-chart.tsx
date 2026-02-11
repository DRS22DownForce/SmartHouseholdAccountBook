"use client"

/**
 * 支出の推移チャートコンポーネント
 * 
 * バックエンドAPIから範囲指定で月別サマリーを取得してチャートを表示します。
 * 週間カレンダーセクションと統一感のあるリッチなデザインを採用しています。
 * 
 * 【初心者向け解説】
 * - CSSベースのシンプルな棒グラフで実装
 * - バーの高さで支出額を視覚的に表現
 * - 各月のカードに合計金額とカテゴリー別詳細を常時表示
 * - Rechartsを使わないことで軽量かつ高速に動作
 */

import { useMemo, useState } from "react"
import { Card, CardContent } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { formatCurrency } from "@/lib/formatters"
import { MONTH_RANGES } from "@/lib/constants"
import { useMonthlySummaryRange } from "@/hooks/use-monthly-summary-range"
import { useRefreshTrigger } from "@/hooks/use-refresh-trigger"
import { calculateMonthRange, generateMonthKeys } from "@/lib/date-utils"
import { transformMonthlySummariesToChartData } from "@/lib/chart-data-transformers"
import { cn } from "@/lib/utils"
import { TrendingUp, ChevronLeft, ChevronRight } from "lucide-react"
import { Button } from "@/components/ui/button"

/**
 * コンポーネントのプロパティ定義
 */
interface ExpenseTrendChartProps {
  refreshTrigger?: number // 支出追加後に再取得するためのトリガー
}

/**
 * 月別支出データの型定義
 */
interface MonthlyExpenseData {
  month: string        // 月（例: "2025/01"）
  total: number        // 合計支出額
  [key: string]: string | number // カテゴリ別の金額
}

export function ExpenseTrendChart({ refreshTrigger }: ExpenseTrendChartProps) {
  // 表示する月数の状態管理（デフォルト: 6ヶ月）
  const [monthRange, setMonthRange] = useState("6")

  // 表示月数を数値に変換
  const monthsToShow = Number.parseInt(monthRange)
  
  // 表示する月の範囲を計算（開始月と終了月）
  const [startMonth, endMonth] = useMemo(
    () => calculateMonthRange(monthsToShow),
    [monthsToShow]
  )

  // バックエンドAPIから月別サマリーを取得
  const { monthlySummaries, isLoaded, refetch } = useMonthlySummaryRange(startMonth, endMonth)

  // refreshTriggerが変化したときにデータを再取得
  useRefreshTrigger(refreshTrigger, refetch)

  // 表示する月のキーリストを生成（YYYY-MM形式）
  const allMonths = useMemo(() => generateMonthKeys(monthsToShow), [monthsToShow])

  // 全カテゴリのリストを取得
  const categories = useMemo(() => {
    const categorySet = new Set<string>()
    monthlySummaries.forEach((summary) => {
      summary.byCategory.forEach((item) => {
        categorySet.add(item.category)
      })
    })
    return Array.from(categorySet)
  }, [monthlySummaries])

  // チャート表示用のデータに変換
  const chartData = useMemo(() => {
    return transformMonthlySummariesToChartData(monthlySummaries, allMonths, categories) as MonthlyExpenseData[]
  }, [monthlySummaries, allMonths, categories])

  // 統計情報を計算（合計、平均、最大値）
  const stats = useMemo(() => {
    if (chartData.length === 0) return { total: 0, average: 0, max: 0, maxMonth: "" }
    
    // 各月の合計金額を配列として取得
    const totals = chartData.map(d => (d.total as number) || 0)
    // 期間合計
    const total = totals.reduce((sum, t) => sum + t, 0)
    // 月平均
    const average = Math.round(total / chartData.length)
    // 最大値
    const max = Math.max(...totals)
    // 最大値の月
    const maxIndex = totals.indexOf(max)
    const maxMonth = chartData[maxIndex]?.month || ""
    
    return { total, average, max, maxMonth }
  }, [chartData])

  /**
   * バーの色を統一（グラデーション）
   * 
   * @returns CSSクラス名
   */
  const getBarColor = (): string => {
    return "bg-gradient-to-t from-blue-500 to-blue-400"
  }

  /**
   * バーの高さをパーセンテージで計算
   * 金額に正確に比例した高さを返す
   * 
   * @param amount 支出額
   * @param maxAmount 最大支出額
   * @returns 高さのパーセンテージ（0〜100）
   * 
   * 【初心者向け解説】
   * - 金額が0なら高さも0%
   * - 最大金額のバーが100%になる
   * - 他のバーは最大金額に対する比率で高さが決まる
   * - 例: 最大が10万円で、5万円のバーは50%の高さ
   */
  const getBarHeight = (amount: number, maxAmount: number): number => {
    // 最大金額が0の場合（データがない場合）
    if (maxAmount === 0) return 0
    // 金額が0の場合は高さも0
    if (amount === 0) return 0
    // 金額に比例した高さを計算（最低5%は確保して見やすく）
    const ratio = (amount / maxAmount) * 100
    return Math.max(5, ratio)
  }

  /**
   * 今月かどうかを判定
   * @param monthString 月の文字列（例: "2025/01"）
   */
  const isCurrentMonth = (monthString: string): boolean => {
    const now = new Date()
    const currentYearMonth = `${now.getFullYear()}/${String(now.getMonth() + 1).padStart(2, "0")}`
    return monthString === currentYearMonth
  }

  /**
   * 縦軸の目盛り値を計算
   * 最大値に基づいて適切な間隔で目盛りを生成
   * 
   * @param maxValue 最大値
   * @param tickCount 目盛りの数（デフォルト: 5）
   * @returns 目盛り値の配列
   */
  const calculateYAxisTicks = (maxValue: number, tickCount: number = 5): number[] => {
    if (maxValue === 0) return [0]
    
    // 最大値を丸める（例: 123456 → 150000）
    const magnitude = Math.pow(10, Math.floor(Math.log10(maxValue)))
    const normalized = maxValue / magnitude
    let roundedMax = Math.ceil(normalized) * magnitude
    
    // 目盛りの間隔を計算
    const step = roundedMax / (tickCount - 1)
    
    // 目盛り値を生成
    const ticks: number[] = []
    for (let i = 0; i < tickCount; i++) {
      ticks.push(Math.round(step * i))
    }
    
    return ticks
  }

  return (
    <div className="space-y-4">
      {/* ==================== */}
      {/* セクションヘッダー */}
      {/* ==================== */}
      <div className="flex items-center justify-between px-1">
        <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
          {/* アイコン（青のグラデーション） */}
          <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-blue-500/10 text-blue-500">
            <TrendingUp className="h-4 w-4" />
          </div>
          支出の推移
        </h2>
        
        {/* 期間選択ドロップダウン */}
        <div className="flex items-center gap-2">
          <Select value={monthRange} onValueChange={setMonthRange}>
            <SelectTrigger className={cn(
              "w-[120px] h-8 text-xs font-bold rounded-lg",
              "border-border/60 bg-background/50",
              "hover:bg-background/80 transition-all",
              "focus:ring-2 focus:ring-primary/20"
            )}>
              <SelectValue />
            </SelectTrigger>
            <SelectContent align="end" className="rounded-xl border-border/60 shadow-xl">
              {MONTH_RANGES.map((range) => (
                <SelectItem 
                  key={range.value} 
                  value={range.value} 
                  className="text-xs font-medium focus:bg-primary/10 rounded-lg cursor-pointer"
                >
                  {range.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* ==================== */}
      {/* メインカード */}
      {/* ==================== */}
      <Card className={cn(
        "relative overflow-hidden",
        "border-border/40 shadow-rich shadow-rich-hover",
        // 週間カレンダーと同じ紫系のグラデーション背景
        "bg-gradient-to-br from-card via-card to-blue-500/5",
        "transition-all duration-500"
      )}>
        {/* 背景装飾（グリッドパターン） */}
        <div className="absolute inset-0 pattern-grid pointer-events-none" />
        
        <CardContent className="p-6">
          {/* ==================== */}
          {/* サマリー（週間カレンダーと同じレイアウト）*/}
          {/* ==================== */}
          <div className="flex items-center justify-between mb-6 pb-4 border-b border-border/40">
            <div className="space-y-1">
              <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
                期間合計支出
              </p>
              <p className="text-2xl font-black text-foreground tabular-nums animate-count-up">
                {isLoaded ? formatCurrency(stats.total) : "¥---"}
              </p>
            </div>
            <div className="text-right space-y-1">
              <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
                月平均
              </p>
              <p className="text-lg font-bold text-muted-foreground tabular-nums">
                {isLoaded ? formatCurrency(stats.average) : "¥---"}
              </p>
            </div>
          </div>

          {/* ==================== */}
          {/* チャート本体 */}
          {/* ==================== */}
          {!isLoaded ? (
            // ローディング表示
            <div className="flex h-[200px] items-center justify-center">
              <div className="flex flex-col items-center gap-4">
                <div className="relative w-12 h-12">
                  <div className="absolute inset-0 rounded-full border-2 border-primary/20" />
                  <div className="absolute inset-0 rounded-full border-2 border-primary border-t-transparent animate-spin" />
                </div>
                <p className="text-sm text-muted-foreground font-medium animate-pulse">
                  データを読み込み中...
                </p>
              </div>
            </div>
          ) : chartData.length > 0 ? (
            // バーチャート（縦軸目盛り付き）
            <div className="flex gap-3">
              {/* 縦軸目盛り */}
              <div className="relative h-32 md:h-40 w-20 flex-shrink-0">
                {calculateYAxisTicks(stats.max).reverse().map((tick, index) => {
                  // 目盛りの位置を実際の値に基づいて計算（0円は底、最大値は頂上）
                  const ticks = calculateYAxisTicks(stats.max)
                  const maxTick = Math.max(...ticks)
                  const position = maxTick > 0 ? (tick / maxTick) * 100 : 0
                  return (
                    <div 
                      key={index} 
                      className="absolute flex items-center left-0 right-0"
                      style={{ bottom: `${position}%`, transform: 'translateY(50%)' }}
                    >
                      <span className="text-[10px] text-muted-foreground tabular-nums w-full text-right pr-1">
                        {formatCurrency(tick)}
                      </span>
                    </div>
                  )
                })}
              </div>
              
              {/* バーチャートグリッド */}
              <div 
                className="flex-1 grid gap-2 md:gap-4 relative"
                style={{
                  // 月数に応じてグリッドの列数を動的に変更
                  gridTemplateColumns: `repeat(${chartData.length}, minmax(0, 1fr))`
                }}
              >
                {/* 目盛り線をバーグラフエリアに延長 */}
                <div className="absolute left-0 right-0 pointer-events-none h-32 md:h-40">
                  {calculateYAxisTicks(stats.max).map((tick, index) => {
                    // 目盛り線の位置を実際の値に基づいて計算
                    const ticks = calculateYAxisTicks(stats.max)
                    const maxTick = Math.max(...ticks)
                    const position = maxTick > 0 ? (tick / maxTick) * 100 : 0
                    return (
                      <div
                        key={index}
                        className="absolute left-0 right-0 h-px bg-border/20"
                        style={{ bottom: `${position}%` }}
                      />
                    )
                  })}
                </div>
              {chartData.map((data, index) => {
                const total = (data.total as number) || 0
                const isThisMonth = isCurrentMonth(data.month)
                
                return (
                  <div 
                    key={index}
                    className={cn(
                      "group relative flex flex-col items-center",
                      // スタガードアニメーション（順番に表示）
                      "animate-fade-in",
                      `stagger-${Math.min(index + 1, 5)}`
                    )}
                  >
                    {/* ==================== */}
                    {/* バーコンテナ */}
                    {/* ==================== */}
                    <div className={cn(
                      "relative w-full h-32 md:h-40 rounded-xl overflow-visible",
                      "bg-muted/20",
                      "flex items-end justify-center",
                      "transition-all duration-300"
                    )}>
                      {/* 金額表示（棒グラフの上） */}
                      {total > 0 && (
                        <div className="absolute -top-6 left-0 right-0 flex items-center justify-center">
                          <span className="text-xs font-bold text-foreground tabular-nums">
                            {formatCurrency(total)}
                          </span>
                        </div>
                      )}
                      
                      {/* 支出バー */}
                      <div
                        className={cn(
                          "w-full rounded-t-lg transition-all duration-500 ease-out",
                          getBarColor()
                        )}
                        style={{ height: `${getBarHeight(total, stats.max)}%` }}
                      />
                    </div>

                    {/* ==================== */}
                    {/* 月ラベル */}
                    {/* ==================== */}
                    <div className={cn(
                      "mt-2 px-2 py-1 rounded-lg flex items-center justify-center",
                      "transition-all duration-300",
                      isThisMonth 
                        ? "bg-primary text-primary-foreground font-bold" 
                        : "text-muted-foreground group-hover:bg-muted"
                    )}>
                      <span className="text-xs">{data.month}</span>
                    </div>
                  </div>
                )
              })}
              </div>
            </div>
          ) : (
            // データなし表示
            <div className="flex h-[200px] items-center justify-center flex-col gap-4">
              <div className="w-20 h-20 rounded-2xl bg-muted/20 flex items-center justify-center">
                <TrendingUp className="w-10 h-10 text-muted-foreground/30" />
              </div>
              <div className="text-center">
                <p className="text-sm font-medium text-foreground/80 mb-1">
                  表示するデータがありません
                </p>
                <p className="text-xs text-muted-foreground">
                  支出を記録すると、ここに推移グラフが表示されます
                </p>
              </div>
            </div>
          )}

        </CardContent>
      </Card>
    </div>
  )
}
