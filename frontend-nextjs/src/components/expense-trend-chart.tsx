"use client"

/**
 * 支出の推移チャートコンポーネント
 * 
 * バックエンドAPIから範囲指定で月別サマリーを取得してチャートを表示します。
 * 週間カレンダーセクションと統一感のあるリッチなデザインを採用しています。
 * 
 * 【初心者向け解説】
 * - CSSベースのシンプルな棒グラフで実装
 * - 支出額に応じてバーの色が変わる（緑→黄→赤）
 * - ホバー時にオーバーレイでツールチップを表示
 * - Rechartsを使わないことで軽量かつ高速に動作
 * 
 * 【色の意味】
 * - 緑: 支出が少ない（良い状態）
 * - 黄: 支出が中程度（注意）
 * - 赤: 支出が多い（要注意）
 */

import { useMemo, useState } from "react"
import { Card, CardContent } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { formatCurrency } from "@/lib/formatters"
import { getCategoryColor } from "@/lib/category-colors"
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
  // ホバー中の月のインデックス（ツールチップ表示用）
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null)

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
   * 支出額に応じた色を計算
   * 週間カレンダーと同じ色使いで統一
   * 
   * @param amount 支出額
   * @param maxAmount 最大支出額（比率計算用）
   * @returns CSSクラス名
   */
  const getIntensityColor = (amount: number, maxAmount: number): string => {
    if (maxAmount === 0) return "bg-emerald-300"
    const ratio = amount / maxAmount
    // 支出が多いほど赤く、少ないほど緑に
    if (ratio > 0.8) return "bg-rose-500"      // 高額（危険）
    if (ratio > 0.6) return "bg-rose-400"      // やや高額
    if (ratio > 0.4) return "bg-amber-400"     // 中程度（注意）
    if (ratio > 0.2) return "bg-emerald-400"   // 低め（良好）
    return "bg-emerald-300"                    // 低額（とても良好）
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
            // バーチャートグリッド（週間カレンダーと同じスタイル）
            <div 
              className="grid gap-2 md:gap-4"
              style={{
                // 月数に応じてグリッドの列数を動的に変更
                gridTemplateColumns: `repeat(${chartData.length}, minmax(0, 1fr))`
              }}
            >
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
                    onMouseEnter={() => setHoveredIndex(index)}
                    onMouseLeave={() => setHoveredIndex(null)}
                  >
                    {/* ==================== */}
                    {/* バーコンテナ */}
                    {/* ==================== */}
                    <div className={cn(
                      "relative w-full h-32 md:h-40 rounded-xl overflow-hidden",
                      "bg-muted/20",
                      "flex items-end justify-center",
                      "transition-all duration-300"
                    )}>
                      {/* 支出バー */}
                      <div
                        className={cn(
                          "w-full rounded-t-lg transition-all duration-500 ease-out",
                          getIntensityColor(total, stats.max),
                          "group-hover:opacity-90"
                        )}
                        style={{ height: `${getBarHeight(total, stats.max)}%` }}
                      />
                      
                      {/* ホバー時のツールチップオーバーレイ */}
                      <div className={cn(
                        "absolute inset-0 flex items-center justify-center",
                        "bg-background/95 backdrop-blur-sm",
                        "opacity-0 group-hover:opacity-100",
                        "transition-opacity duration-200",
                        "rounded-xl overflow-hidden"
                      )}>
                        <div className="w-full h-full p-3 overflow-y-auto scrollbar-thin">
                          {/* 合計金額ヘッダー */}
                          <div className="text-center mb-2 pb-2 border-b border-border/40">
                            <p className="text-sm font-black text-foreground">
                              {formatCurrency(total)}
                            </p>
                          </div>
                          
                          {/* カテゴリー別詳細リスト */}
                          <div className="space-y-1">
                            {categories
                              .filter(cat => (data[cat] as number) > 0)
                              .sort((a, b) => (data[b] as number) - (data[a] as number))
                              .map((cat, catIndex) => {
                                const amount = data[cat] as number
                                const percentage = total > 0 ? ((amount / total) * 100).toFixed(0) : "0"
                                return (
                                  <div 
                                    key={catIndex}
                                    className="flex items-center gap-1.5 text-[10px]"
                                  >
                                    {/* カテゴリー色のドット */}
                                    <div 
                                      className="w-2 h-2 rounded-full flex-shrink-0"
                                      style={{ backgroundColor: getCategoryColor(cat) }}
                                    />
                                    {/* カテゴリー名（省略可能） */}
                                    <span className="text-muted-foreground truncate flex-1 min-w-0">
                                      {cat}
                                    </span>
                                    {/* 金額と割合 */}
                                    <span className="font-bold text-foreground tabular-nums flex-shrink-0">
                                      {percentage}%
                                    </span>
                                  </div>
                                )
                              })}
                          </div>
                        </div>
                      </div>
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

          {/* ==================== */}
          {/* 凡例（週間カレンダーと同じ） */}
          {/* ==================== */}
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
