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
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from "recharts"
import { getCategoryColor } from "@/lib/category-colors"
import { formatCurrency, formatMonth, getCurrentMonthString } from "@/lib/formatters"
import { useMonthlySummary } from "@/hooks/use-monthly-summary"
import { useAvailableMonths } from "@/hooks/use-available-months"

interface SummarySectionProps {
  refreshTrigger?: number // 支出追加後に再取得するためのトリガー
}

export function MonthlySummarySection({
  refreshTrigger,
}: SummarySectionProps) {
  const [selectedMonth, setSelectedMonth] = useState(() => getCurrentMonthString())
  
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
  }, [refreshTrigger, fetchMonthlySummary, fetchAvailableMonths])

  // チャート用のデータを準備（バックエンドから取得したデータを使用）
  const chartData = useMemo(() => {
    if (!monthlySummary || !monthlySummary.byCategory) {
      return []
    }
    return monthlySummary.byCategory.map((item) => ({
      name: item.category,
      value: item.amount,
      color: getCategoryColor(item.category),
    }))
  }, [monthlySummary])

  // 読み込み中の表示
  if (!isSummaryLoaded || !isMonthsLoaded) {
    return (
      <div className="space-y-1.5 md:space-y-2">
        <div className="flex items-center justify-between gap-4 mb-1">
          <h2 className="text-base md:text-lg font-semibold bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent tracking-tight">
            月別サマリー
          </h2>
        </div>
        <div className="flex h-[200px] items-center justify-center">
          <p className="text-muted-foreground">読み込み中...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-1.5 md:space-y-2">
      <div className="flex items-center justify-between gap-4 mb-1">
        <h2 className="text-base md:text-lg font-semibold bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent tracking-tight">
          月別サマリー
        </h2>
        <Select value={selectedMonth} onValueChange={setSelectedMonth}>
          <SelectTrigger className="w-40 md:w-48 rounded-lg border-border/60 hover:border-primary/40 transition-colors">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {availableMonths.map((month) => (
              <SelectItem key={month} value={month}>
                {formatMonth(month)}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="grid gap-2.5 md:grid-cols-3 md:gap-3">
        <Card className="border-border/50 shadow-md hover:shadow-xl hover:scale-[1.02] transition-all duration-300 bg-gradient-to-br from-card to-primary/5 hover:border-primary/50">
          <CardHeader className="pb-1.5">
            <CardTitle className="text-xs md:text-sm font-semibold text-muted-foreground uppercase tracking-wide">
              合計支出
            </CardTitle>
          </CardHeader>
          <CardContent className="pt-0">
            <p className="text-2xl md:text-3xl font-bold text-foreground tracking-tight">
              {formatCurrency(monthlySummary?.total ?? 0)}
            </p>
            <p className="text-xs md:text-sm text-muted-foreground mt-1 font-medium">
              {monthlySummary?.count ?? 0}件の支出
            </p>
          </CardContent>
        </Card>

        <Card className="border-border/50 shadow-md hover:shadow-xl hover:scale-[1.02] transition-all duration-300 bg-gradient-to-br from-card to-card/95 hover:border-primary/30">
          <CardHeader className="pb-1.5">
            <CardTitle className="text-xs md:text-sm font-semibold text-muted-foreground uppercase tracking-wide">
              トップカテゴリー
            </CardTitle>
          </CardHeader>
          <CardContent className="pt-0">
            {monthlySummary && monthlySummary.byCategory.length > 0 ? (
              <div className="space-y-1.5">
                {monthlySummary.byCategory.slice(0, 3).map((item) => (
                  <div
                    key={item.category}
                    className="flex items-center justify-between p-1 rounded-lg hover:bg-muted/50 transition-colors"
                  >
                    <div className="flex items-center gap-1.5">
                      <div
                        className="w-3 h-3 rounded-full shadow-sm"
                        style={{ backgroundColor: getCategoryColor(item.category) }}
                      />
                      <span className="text-xs md:text-sm font-semibold text-foreground">{item.category}</span>
                    </div>
                    <span className="text-xs md:text-sm font-bold text-foreground">{formatCurrency(item.amount)}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs text-muted-foreground">データがありません</p>
            )}
          </CardContent>
        </Card>

        <Card className="border-border/50 shadow-md hover:shadow-xl hover:scale-[1.02] transition-all duration-300 bg-gradient-to-br from-card to-card/95 hover:border-primary/30">
          <CardHeader className="pb-1.5">
            <CardTitle className="text-xs md:text-sm font-semibold text-muted-foreground uppercase tracking-wide">
              カテゴリー別支出の割合
            </CardTitle>
          </CardHeader>
          <CardContent className="pt-0">
            {chartData.length > 0 ? (
              <div className="space-y-1">
                <ResponsiveContainer width="100%" height={120}>
                  <PieChart>
                    <Pie
                      data={chartData}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      outerRadius={48}
                      innerRadius={20}
                      dataKey="value"
                      strokeWidth={2}
                      stroke="hsl(var(--card))"
                    >
                      {chartData.map((entry, index) => (
                        <Cell
                          key={`cell-${index}`}
                          fill={entry.color}
                          stroke={entry.color}
                          strokeWidth={2}
                          style={{
                            filter: `drop-shadow(0 2px 4px ${entry.color}40)`,
                          }}
                        />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{
                        backgroundColor: "hsl(var(--card))",
                        border: "1px solid hsl(var(--border))",
                        borderRadius: "12px",
                        boxShadow: "0 8px 16px -4px rgb(0 0 0 / 0.1)",
                        padding: "10px",
                      }}
                      formatter={(value: number) => formatCurrency(value)}
                      labelStyle={{ fontWeight: 600, marginBottom: "4px", fontSize: "12px" }}
                    />
                  </PieChart>
                </ResponsiveContainer>
                <div className="flex flex-wrap items-center justify-center gap-1.5 px-2">
                  {chartData.map((entry, index) => {
                    const total = chartData.reduce((sum, e) => sum + e.value, 0)
                    const percent = total > 0 ? ((entry.value / total) * 100).toFixed(0) : "0"
                    return (
                      <div key={index} className="flex items-center gap-1">
                        <div
                          className="w-2.5 h-2.5 rounded-full"
                          style={{ backgroundColor: entry.color }}
                        />
                        <span className="text-[10px] md:text-xs font-medium text-foreground">
                          {entry.name} {percent}%
                        </span>
                      </div>
                    )
                  })}
                </div>
              </div>
            ) : (
              <div className="flex h-[120px] items-center justify-center">
                <p className="text-xs text-muted-foreground">データがありません</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

