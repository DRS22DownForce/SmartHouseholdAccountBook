"use client"

/**
 * 支出の推移チャートコンポーネント
 * 
 * このコンポーネントは、バックエンドAPIから範囲指定で月別サマリーを取得してチャートを表示します。
 * 全expensesデータからではなく、範囲指定APIから取得した月別サマリーから作成することで、
 * 通信量を削減し、フロントエンドでの計算を削減します。
 */

import { useMemo, useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts"
import { getCategoryColor } from "@/lib/category-colors"
import { formatCurrencyForChart } from "@/lib/formatters"
import { MONTH_RANGES } from "@/lib/constants"
import { useMonthlySummaryRange } from "@/hooks/use-monthly-summary-range"
import { calculateMonthRange, generateMonthKeys } from "@/lib/date-utils"
import { transformMonthlySummariesToChartData } from "@/lib/chart-data-transformers"
import { cn } from "@/lib/utils"

interface ExpenseTrendChartProps {
  refreshTrigger?: number // 支出追加後に再取得するためのトリガー
}

interface TooltipProps {
  active?: boolean
  payload?: Array<{ name: string; value: number; fill: string; color: string }>
  label?: string
}

function CustomTooltip({ active, payload, label }: TooltipProps) {
  if (!active || !payload || !payload.length) return null

  const validItems = payload.filter((item) => item.value > 0)
  if (validItems.length === 0) return null

  const total = validItems.reduce((sum, item) => sum + item.value, 0)

  return (
    <div className="bg-popover/95 backdrop-blur-md border border-border/50 rounded-xl shadow-2xl p-4 min-w-[240px] animate-in fade-in-0 zoom-in-95 data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=closed]:zoom-out-95 duration-200">
      <div className="flex items-center justify-between mb-3 pb-3 border-b border-border/50">
        <p className="font-bold text-sm text-foreground">
          {label}
        </p>
        <span className="text-xs font-medium text-muted-foreground bg-muted/50 px-2 py-0.5 rounded-full">
          合計: {total.toLocaleString()}円
        </span>
      </div>
      <div className="space-y-2 max-h-[300px] overflow-y-auto pr-2 custom-scrollbar">
        {validItems.map((item, index) => {
          // Rechartsのitem.fillはグラデーションの場合URL文字列になることがあるため、
          // getCategoryColorを直接呼び出して純色を取得する
          const pureColor = getCategoryColor(item.name);

          return (
            <div
              key={index}
              className="flex items-center justify-between gap-4 p-1.5 rounded-lg hover:bg-muted/50 transition-colors group"
            >
              <div className="flex items-center gap-2.5">
                <div
                  className="w-3 h-3 rounded-full shadow-sm ring-2 ring-transparent group-hover:ring-primary/20 transition-all"
                  style={{
                    backgroundColor: pureColor,
                    boxShadow: `0 0 8px ${pureColor}60`,
                  }}
                />
                <span className="text-xs font-semibold text-foreground/90 group-hover:text-foreground transition-colors">{item.name}</span>
              </div>
              <span className="text-xs font-mono font-bold text-foreground/90 group-hover:text-foreground transition-colors">
                ¥{item.value.toLocaleString()}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  )
}

export function ExpenseTrendChart({ refreshTrigger }: ExpenseTrendChartProps) {
  const [monthRange, setMonthRange] = useState("6")
  const [activeBar, setActiveBar] = useState<number | null>(null)

  // 表示する月数を取得
  const monthsToShow = Number.parseInt(monthRange)

  // 開始月と終了月を計算
  const [startMonth, endMonth] = useMemo(
    () => calculateMonthRange(monthsToShow),
    [monthsToShow]
  )

  // バックエンドAPIから範囲指定で月別サマリーを取得
  const { monthlySummaries, isLoaded, refetch } = useMonthlySummaryRange(startMonth, endMonth)

  // refreshTriggerが変更されたときに再取得
  useEffect(() => {
    if (refreshTrigger !== undefined && refreshTrigger > 0) {
      refetch()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [refreshTrigger])

  // 月のキーリストを生成
  const allMonths = useMemo(() => generateMonthKeys(monthsToShow), [monthsToShow])

  // カテゴリーのリストを取得（月別サマリーから抽出）
  const categories = useMemo(() => {
    const categorySet = new Set<string>()
    monthlySummaries.forEach((summary) => {
      summary.byCategory.forEach((item) => {
        categorySet.add(item.category)
      })
    })
    return Array.from(categorySet)
  }, [monthlySummaries])

  // チャート用のデータを準備
  const chartData = useMemo(() => {
    return transformMonthlySummariesToChartData(monthlySummaries, allMonths, categories)
  }, [monthlySummaries, allMonths, categories])

  return (
    <Card className="group overflow-hidden border-border/40 shadow-sm hover:shadow-xl transition-all duration-300 bg-gradient-to-br from-card to-muted/10">
      <CardHeader className="pb-6 border-b border-border/40 bg-muted/5">
        <div className="flex items-center justify-between">
          <div className="space-y-1">
            <CardTitle className="text-xl font-bold tracking-tight bg-gradient-to-br from-foreground to-muted-foreground bg-clip-text text-transparent flex items-center gap-2">
              <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className="h-5 w-5"
                >
                  <line x1="12" x2="12" y1="20" y2="10" />
                  <line x1="18" x2="18" y1="20" y2="4" />
                  <line x1="6" x2="6" y1="20" y2="16" />
                </svg>
              </span>
              支出の推移
            </CardTitle>
            <p className="text-xs text-muted-foreground font-medium ml-1">
              過去{monthRange}ヶ月間の支出傾向分析
            </p>
          </div>
          <Select value={monthRange} onValueChange={setMonthRange}>
            <SelectTrigger className="w-[140px] h-9 text-xs font-medium rounded-full border-border/60 bg-background/50 backdrop-blur-sm hover:bg-background/80 transition-all focus:ring-1 focus:ring-primary/20 shadow-sm">
              <SelectValue />
            </SelectTrigger>
            <SelectContent align="end" className="rounded-xl border-border/60 shadow-xl">
              {MONTH_RANGES.map((range) => (
                <SelectItem key={range.value} value={range.value} className="text-xs focus:bg-primary/10 rounded-lg cursor-pointer">
                  {range.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </CardHeader>
      <CardContent className="pt-2 px-2 sm:px-6">
        {!isLoaded ? (
          <div className="flex h-[280px] items-center justify-center">
            <div className="flex flex-col items-center gap-2">
              <div className="w-8 h-8 rounded-full border-2 border-primary/30 border-t-primary animate-spin" />
              <p className="text-xs text-muted-foreground font-medium animate-pulse">データを読み込み中...</p>
            </div>
          </div>
        ) : chartData.length > 0 ? (
          <div className="h-[280px] w-full mt-2">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                data={chartData}
                margin={{ top: 10, right: 10, left: -20, bottom: 0 }}
                onMouseMove={(state) => {
                  if (state.activeTooltipIndex !== undefined) {
                    setActiveBar(state.activeTooltipIndex)
                  } else {
                    setActiveBar(null)
                  }
                }}
                onMouseLeave={() => setActiveBar(null)}
              >
                <defs>
                  {categories.map((category, index) => (
                    <linearGradient key={`gradient-${category}`} id={`gradient-${category}`} x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor={getCategoryColor(category)} stopOpacity={1} />
                      <stop offset="100%" stopColor={getCategoryColor(category)} stopOpacity={0.85} />
                    </linearGradient>
                  ))}
                </defs>
                <CartesianGrid
                  strokeDasharray="3 3"
                  vertical={false}
                  stroke="hsl(var(--border))"
                  opacity={0.3}
                />
                <XAxis
                  dataKey="month"
                  className="text-[10px] font-medium"
                  tick={{ fill: "hsl(var(--muted-foreground))" }}
                  axisLine={false}
                  tickLine={false}
                  tickMargin={12}
                />
                <YAxis
                  className="text-[10px] font-medium font-mono"
                  tick={{ fill: "hsl(var(--muted-foreground))" }}
                  tickFormatter={formatCurrencyForChart}
                  axisLine={false}
                  tickLine={false}
                  width={45}
                />
                <Tooltip
                  content={<CustomTooltip />}
                  cursor={{ fill: "hsl(var(--muted))", opacity: 0.2 }}
                />
                <Legend
                  wrapperStyle={{
                    paddingTop: "24px",
                  }}
                  content={({ payload }) => (
                    <div className="flex flex-wrap items-center justify-center gap-x-6 gap-y-2">
                      {payload?.map((entry: any, index) => {
                        const pureColor = getCategoryColor(entry.value);
                        return (
                          <div key={`legend-${index}`} className="flex items-center gap-2 group cursor-default">
                            <div
                              className="w-2.5 h-2.5 rounded-full shadow-sm transition-transform group-hover:scale-125"
                              style={{
                                backgroundColor: pureColor,
                                boxShadow: `0 0 4px ${pureColor}40`
                              }}
                            />
                            <span className="text-[11px] font-bold text-foreground/80 group-hover:text-foreground transition-colors">
                              {entry.value}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  )}
                />
                {categories.map((category) => (
                  <Bar
                    key={category}
                    dataKey={category}
                    stackId="stack"
                    fill={`url(#gradient-${category})`}
                    radius={[2, 2, 2, 2]}
                    maxBarSize={50}
                    className="transition-all duration-300"
                    style={{
                      filter: `drop-shadow(0 0 2px ${getCategoryColor(category)}20)`,
                    }}
                  />
                ))}
              </BarChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <div className="flex h-[280px] items-center justify-center flex-col gap-3 border-2 border-dashed border-muted-foreground/10 rounded-xl bg-muted/5">
            <div className="p-3 bg-muted/20 rounded-full">
              <svg className="w-6 h-6 text-muted-foreground/50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <p className="text-sm text-muted-foreground font-medium">
              表示するデータがありません
            </p>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
