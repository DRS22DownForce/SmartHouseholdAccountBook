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

interface ExpenseTrendChartProps {
  // expensesプロップは削除（バックエンドAPIから取得するため）
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
    <div className="bg-card/98 backdrop-blur-sm border border-border/60 rounded-xl shadow-xl p-4 min-w-[220px] ring-1 ring-border/20">
      <p className="font-bold text-sm mb-3 pb-3 border-b border-border/60 text-foreground">
        {label}
      </p>
      <div className="space-y-2">
        {validItems.map((item, index) => (
          <div
            key={index}
            className="flex items-center justify-between gap-4 p-1.5 rounded-md hover:bg-muted/50 transition-colors"
          >
            <div className="flex items-center gap-2.5">
              <div
                className="w-3.5 h-3.5 rounded-md shadow-sm"
                style={{
                  backgroundColor: item.fill || item.color,
                  boxShadow: `0 2px 4px ${item.fill || item.color}40`,
                }}
              />
              <span className="text-xs font-semibold text-foreground">{item.name}</span>
            </div>
            <span className="text-xs font-bold text-foreground">
              ¥{item.value.toLocaleString()}
            </span>
          </div>
        ))}
      </div>
      <div className="flex items-center justify-between gap-4 mt-3 pt-3 border-t border-border/60">
        <span className="text-xs font-bold text-foreground uppercase tracking-wide">合計</span>
        <span className="text-sm font-bold text-primary">¥{total.toLocaleString()}</span>
      </div>
    </div>
  )
}

export function ExpenseTrendChart({ refreshTrigger }: ExpenseTrendChartProps) {
  const [monthRange, setMonthRange] = useState("6")

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
  }, [refreshTrigger]) // refetchは安定した参照であることを前提

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

  // チャート用のデータを準備（バックエンドから取得した月別サマリーを使用）
  const chartData = useMemo(() => {
    return transformMonthlySummariesToChartData(monthlySummaries, allMonths, categories)
  }, [monthlySummaries, allMonths, categories])

  return (
    <Card className="border-border/50 shadow-md hover:shadow-xl hover:scale-[1.01] transition-all duration-300 bg-gradient-to-br from-card to-card/95 hover:border-primary/30">
      <CardHeader className="pb-1.5">
        <div className="flex items-center justify-between">
          <CardTitle className="text-base md:text-lg font-semibold bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent tracking-tight">
            支出の推移
          </CardTitle>
          <Select value={monthRange} onValueChange={setMonthRange}>
            <SelectTrigger className="w-[140px] md:w-[160px] rounded-lg border-border/60 hover:border-primary/40 transition-colors">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {MONTH_RANGES.map((range) => (
                <SelectItem key={range.value} value={range.value}>
                  {range.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </CardHeader>
      <CardContent className="pt-0">
        {!isLoaded ? (
          <div className="flex h-[220px] items-center justify-center">
            <p className="text-sm md:text-base text-muted-foreground font-medium">読み込み中...</p>
          </div>
        ) : chartData.length > 0 ? (
          <ResponsiveContainer width="100%" height={220}>
            <BarChart
              data={chartData}
              barSize={45}
              margin={{ top: 5, right: 8, left: 0, bottom: 3 }}
            >
              <CartesianGrid
                strokeDasharray="3 3"
                stroke="hsl(var(--muted-foreground))"
                opacity={0.2}
              />
              <XAxis
                dataKey="month"
                className="text-xs"
                tick={{ fill: "hsl(var(--muted-foreground))", fontSize: 12 }}
                axisLine={{ stroke: "hsl(var(--border))" }}
                tickLine={{ stroke: "hsl(var(--border))" }}
              />
              <YAxis
                className="text-xs"
                tick={{ fill: "hsl(var(--muted-foreground))", fontSize: 12 }}
                tickFormatter={formatCurrencyForChart}
                axisLine={{ stroke: "hsl(var(--border))" }}
                tickLine={{ stroke: "hsl(var(--border))" }}
              />
              <Tooltip content={<CustomTooltip />} />
              <Legend
                wrapperStyle={{
                  fontSize: "12px",
                  fontWeight: 600,
                  paddingTop: "8px",
                }}
                iconType="square"
                iconSize={12}
                formatter={(value, entry: any) => (
                  <span
                    style={{
                      color: entry.color || "hsl(var(--foreground))",
                      fontWeight: 600,
                    }}
                  >
                    {value}
                  </span>
                )}
              />
              {categories.map((category) => (
                <Bar
                  key={category}
                  dataKey={category}
                  stackId="stack"
                  fill={getCategoryColor(category)}
                  name={category}
                  radius={[0, 0, 4, 4]}
                  style={{
                    filter: `drop-shadow(0 2px 4px ${getCategoryColor(category)}30)`,
                  }}
                />
              ))}
            </BarChart>
          </ResponsiveContainer>
        ) : (
          <div className="flex h-[220px] items-center justify-center border-2 border-dashed border-muted-foreground/20 rounded-lg bg-muted/20">
            <p className="text-sm md:text-base text-muted-foreground font-medium">
              データがありません
            </p>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
