"use client"

/**
 * 月別サマリーセクションコンポーネント
 * 
 * このコンポーネントは、バックエンドAPIから月別サマリーを取得して表示します。
 * フロントエンドでの計算を削減し、通信量を最適化するために使用します。
 */

import { useState, useMemo, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from "recharts"
import { getCategoryColor } from "@/lib/category-colors"
import { formatCurrency, formatMonth, getCurrentMonthString } from "@/lib/formatters"
import { useMonthlySummary } from "@/hooks/use-monthly-summary"
import { useAvailableMonths } from "@/hooks/use-available-months"
import { transformMonthlySummaryToChartData } from "@/lib/chart-data-transformers"
import { TrendingDown, TrendingUp, Calendar, Wallet } from "lucide-react"

interface SummarySectionProps {
  refreshTrigger?: number // 支出追加後に再取得するためのトリガー
}

export function MonthlySummarySection({
  refreshTrigger,
}: SummarySectionProps) {
  const [selectedMonth, setSelectedMonth] = useState(() => getCurrentMonthString())
  const [activeIndex, setActiveIndex] = useState<number | undefined>(undefined)

  // バックエンドAPIから月別サマリーを取得
  const { monthlySummary, isLoaded: isSummaryLoaded, fetchMonthlySummary } = useMonthlySummary(selectedMonth)

  // バックエンドAPIから利用可能な月のリストを取得
  const { availableMonths, isLoaded: isMonthsLoaded, fetchAvailableMonths } = useAvailableMonths()

  // refreshTriggerが変更されたときに再取得
  useEffect(() => {
    if (refreshTrigger !== undefined && refreshTrigger > 0) {
      fetchMonthlySummary()
      fetchAvailableMonths()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [refreshTrigger])

  // チャート用のデータを準備
  const chartData = useMemo(() => {
    // データがない場合は空配列を返す
    if (!monthlySummary || !monthlySummary.byCategory) return []

    // 金額が大きい順にソートし、その他をまとめる処理などはtransformMonthlySummaryToChartDataで行われている前提
    return transformMonthlySummaryToChartData(monthlySummary)
  }, [monthlySummary])

  // 読み込み中の表示
  if (!isSummaryLoaded || !isMonthsLoaded) {
    return (
      <Card className="border-border/40 shadow-sm bg-gradient-to-br from-card to-muted/10 h-[400px]">
        <div className="flex h-full items-center justify-center flex-col gap-4">
          <div className="w-8 h-8 rounded-full border-2 border-primary/30 border-t-primary animate-spin" />
          <p className="text-muted-foreground text-sm font-medium animate-pulse">データを読み込み中...</p>
        </div>
      </Card>
    )
  }

  // 合計金額の計算（バックエンドから来るが、チャート表示用に念のため）
  const totalAmount = monthlySummary?.total ?? 0
  const totalCount = monthlySummary?.count ?? 0

  return (
    <Card className="border-border/40 shadow-sm hover:shadow-md transition-all duration-300 bg-gradient-to-br from-card to-muted/10 overflow-hidden">
      <CardHeader className="pb-2 border-b border-border/40 bg-muted/5">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="space-y-1">
            <CardTitle className="text-lg font-bold tracking-tight flex items-center gap-2">
              <Calendar className="w-4 h-4 text-primary" />
              <span className="bg-gradient-to-br from-foreground to-muted-foreground bg-clip-text text-transparent">
                月別サマリー
              </span>
            </CardTitle>
            <p className="text-xs text-muted-foreground font-medium">
              選択した月の収支内訳と分析
            </p>
          </div>
          <Select value={selectedMonth} onValueChange={setSelectedMonth}>
            <SelectTrigger className="w-full md:w-[180px] h-9 text-xs font-medium rounded-full border-border/60 bg-background/50 backdrop-blur-sm hover:bg-background/80 transition-all focus:ring-1 focus:ring-primary/20">
              <SelectValue />
            </SelectTrigger>
            <SelectContent align="end" className="rounded-xl border-border/60 shadow-xl max-h-[300px]">
              {availableMonths.map((month) => (
                <SelectItem key={month} value={month} className="text-xs focus:bg-primary/10 rounded-lg cursor-pointer">
                  {formatMonth(month)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </CardHeader>

      <CardContent className="p-0">
        <div className="grid md:grid-cols-2 lg:grid-cols-3 divide-y md:divide-y-0 md:divide-x divide-border/40">

          {/* 左カラム: 合計と主要ステータス */}
          <div className="p-6 flex flex-col justify-center space-y-6 bg-gradient-to-br from-primary/5 via-transparent to-transparent">
            <div className="relative overflow-hidden rounded-2xl bg-card border border-border/50 p-6 shadow-sm group hover:shadow-md transition-all duration-300">
              <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
                <Wallet className="w-24 h-24 -mr-8 -mt-8 rotate-12" />
              </div>
              <p className="text-sm font-medium text-muted-foreground mb-1">総支出額</p>
              <div className="flex items-baseline gap-1">
                <span className="text-3xl font-extrabold tracking-tight text-foreground">
                  {formatCurrency(totalAmount)}
                </span>
              </div>
              <div className="mt-4 flex items-center gap-2 text-xs font-medium text-muted-foreground bg-muted/50 w-fit px-2 py-1 rounded-full">
                <span className="flex items-center gap-1">
                  <TrendingUp className="w-3 h-3" />
                  合計 {totalCount} 件の取引
                </span>
              </div>
            </div>

            <div className="space-y-3">
              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider pl-1">
                支出内訳トップ3
              </p>
              <div className="space-y-2">
                {monthlySummary && monthlySummary.byCategory.slice(0, 3).map((item, i) => (
                  <div key={item.category} className="flex items-center justify-between p-2 rounded-lg bg-card/50 hover:bg-muted/50 border border-transparent hover:border-border/50 transition-all">
                    <div className="flex items-center gap-2">
                      <div
                        className="w-2 h-8 rounded-full shadow-sm"
                        style={{ backgroundColor: getCategoryColor(item.category) }}
                      />
                      <div className="flex flex-col">
                        <span className="text-xs font-semibold">{item.category}</span>
                        <span className="text-[10px] text-muted-foreground">
                          {((item.amount / totalAmount) * 100).toFixed(1)}%
                        </span>
                      </div>
                    </div>
                    <span className="text-sm font-bold font-mono">
                      {formatCurrency(item.amount)}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* 右/中央カラム: ドーナツチャート */}
          <div className="p-6 md:col-span-1 lg:col-span-2 flex flex-col items-center justify-center relative min-h-[300px]">
            {chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={chartData}
                    cx="50%"
                    cy="50%"
                    innerRadius={80}
                    outerRadius={110}
                    paddingAngle={0}
                    dataKey="value"
                    onMouseEnter={(_, index) => setActiveIndex(index)}
                    onMouseLeave={() => setActiveIndex(undefined)}
                    stroke="none"
                  >
                    {chartData.map((entry, index) => (
                      <Cell
                        key={`cell-${index}`}
                        fill={entry.color}
                        stroke="none"
                        className="transition-all duration-300 hover:opacity-80 cursor-pointer"
                        style={{
                          filter: activeIndex === index ? `drop-shadow(0 0 8px ${entry.color}60)` : undefined,
                          transform: activeIndex === index ? 'scale(1.02)' : 'scale(1)',
                          transformOrigin: 'center center',
                          outline: 'none'
                        }}
                      />
                    ))}
                  </Pie>
                  <Tooltip
                    content={({ active, payload }) => {
                      if (active && payload && payload.length) {
                        const data = payload[0].payload;
                        return (
                          <div className="bg-popover/95 backdrop-blur-md border border-border/50 rounded-xl shadow-xl p-3 text-xs">
                            <div className="flex items-center gap-2 mb-1">
                              <div className="w-2 h-2 rounded-full" style={{ backgroundColor: data.color }} />
                              <span className="font-bold">{data.name}</span>
                            </div>
                            <div className="text-lg font-bold font-mono">
                              {formatCurrency(data.value)}
                            </div>
                            <div className="text-muted-foreground">
                              {((data.value / totalAmount) * 100).toFixed(1)}%
                            </div>
                          </div>
                        )
                      }
                      return null;
                    }}
                  />
                  <Legend
                    width={160}
                    layout="vertical"
                    verticalAlign="middle"
                    align="right"
                    content={({ payload }) => (
                      <div className="flex flex-col gap-1.5 max-h-[240px] overflow-y-auto pr-2 custom-scrollbar ml-4">
                        {payload?.map((entry: any, index) => (
                          <div
                            key={`legend-${index}`}
                            className={`flex items-center justify-between gap-2 text-xs p-1.5 rounded-md transition-colors cursor-pointer ${activeIndex === index ? 'bg-muted' : 'hover:bg-muted/50'}`}
                            onMouseEnter={() => setActiveIndex(index)}
                            onMouseLeave={() => setActiveIndex(undefined)}
                          >
                            <div className="flex items-center gap-2">
                              <div className="w-2.5 h-2.5 rounded-full shadow-sm" style={{ backgroundColor: entry.color }} />
                              <span className={`font-medium ${activeIndex === index ? 'text-foreground' : 'text-muted-foreground'}`}>
                                {entry.value}
                              </span>
                            </div>
                            <span className="font-mono font-semibold opacity-70">
                              {((entry.payload.value / totalAmount) * 100).toFixed(0)}%
                            </span>
                          </div>
                        ))}
                      </div>
                    )}
                  />
                  <text x="50%" y="50%" textAnchor="middle" dominantBaseline="middle">
                    <tspan x="50%" dy="-10" fontSize="12" fill="hsl(var(--muted-foreground))" fontWeight="500">
                      Total
                    </tspan>
                    <tspan x="50%" dy="24" fontSize="20" fill="hsl(var(--foreground))" fontWeight="bold">
                      {formatCurrency(totalAmount)}
                    </tspan>
                  </text>
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex h-[300px] items-center justify-center flex-col gap-3">
                <div className="p-4 bg-muted/20 rounded-full">
                  <TrendingDown className="w-8 h-8 text-muted-foreground/40" />
                </div>
                <p className="text-sm text-muted-foreground font-medium">データがありません</p>
              </div>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}

