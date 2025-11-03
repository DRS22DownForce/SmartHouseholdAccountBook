"use client"

import { useMemo } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from "recharts"
import type { Expense } from "@/lib/types"
import { getCategoryColor } from "@/lib/category-colors"

interface MonthlySummaryProps {
  expenses: Expense[]
  selectedMonth: string
  onMonthChange: (month: string) => void
}

export function MonthlySummary({ expenses, selectedMonth, onMonthChange }: MonthlySummaryProps) {
  const availableMonths = useMemo(() => {
    const months = new Set<string>()
    expenses.forEach((expense) => {
      const month = expense.date.substring(0, 7)
      months.add(month)
    })
    return Array.from(months).sort().reverse()
  }, [expenses])

  const monthlyData = useMemo(() => {
    const filtered = expenses.filter((expense) => expense.date.startsWith(selectedMonth))

    const total = filtered.reduce((sum, expense) => sum + expense.amount, 0)

    const byCategory = filtered.reduce(
      (acc, expense) => {
        acc[expense.category] = (acc[expense.category] || 0) + expense.amount
        return acc
      },
      {} as Record<string, number>,
    )

    return {
      total,
      count: filtered.length,
      byCategory: Object.entries(byCategory).sort((a, b) => b[1] - a[1]),
    }
  }, [expenses, selectedMonth])

  const chartData = useMemo(() => {
    return monthlyData.byCategory.map(([name, value], index) => ({
      name,
      value,
      color: getCategoryColor(name, index),
    }))
  }, [monthlyData.byCategory])

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("ja-JP", {
      style: "currency",
      currency: "JPY",
    }).format(amount)
  }

  const formatMonth = (month: string) => {
    const [year, monthNum] = month.split("-")
    return `${year}年${monthNum}月`
  }

  return (
    <div className="space-y-6">
      {/* ヘッダー: タイトルと月選択 */}
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-2xl md:text-3xl font-bold text-foreground tracking-tight">月別サマリー</h2>
        <Select value={selectedMonth} onValueChange={onMonthChange}>
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

      {/* サマリーカード: 合計支出とトップカテゴリー */}
      <div className="grid gap-4 md:grid-cols-2">
        {/* 合計支出カード: より目立つデザイン */}
        <Card className="border-border/50 shadow-md hover:shadow-lg transition-all duration-300 bg-gradient-to-br from-card to-primary/5 hover:border-primary/30">
          <CardHeader>
            <CardTitle className="text-xs md:text-sm font-semibold text-muted-foreground uppercase tracking-wide">
              合計支出
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl md:text-4xl font-bold text-foreground tracking-tight">
              {formatCurrency(monthlyData.total)}
            </p>
            <p className="text-sm text-muted-foreground mt-3 font-medium">
              {monthlyData.count}件の支出
            </p>
          </CardContent>
        </Card>

        {/* トップカテゴリーカード: より読みやすいレイアウト */}
        <Card className="border-border/50 shadow-md hover:shadow-lg transition-all duration-300 bg-gradient-to-br from-card to-card/95">
          <CardHeader>
            <CardTitle className="text-xs md:text-sm font-semibold text-muted-foreground uppercase tracking-wide">
              トップカテゴリー
            </CardTitle>
          </CardHeader>
          <CardContent>
            {monthlyData.byCategory.length > 0 ? (
              <div className="space-y-3">
                {monthlyData.byCategory.slice(0, 3).map(([category, amount], index) => (
                  <div 
                    key={category} 
                    className="flex items-center justify-between p-2 rounded-lg hover:bg-muted/50 transition-colors"
                  >
                    <div className="flex items-center gap-2">
                      <div 
                        className="w-3 h-3 rounded-full shadow-sm"
                        style={{ backgroundColor: getCategoryColor(category, index) }}
                      />
                      <span className="text-sm font-semibold text-foreground">{category}</span>
                    </div>
                    <span className="text-sm font-bold text-foreground">{formatCurrency(amount)}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-muted-foreground">データがありません</p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* カテゴリー別支出の割合: 円グラフ */}
      <Card className="border-border/50 shadow-md hover:shadow-lg transition-all duration-300 bg-gradient-to-br from-card to-card/95">
        <CardHeader>
          <CardTitle className="text-base md:text-lg font-bold text-foreground">
            カテゴリー別支出の割合
          </CardTitle>
        </CardHeader>
        <CardContent>
          {chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={chartData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  outerRadius={90}
                  innerRadius={40}
                  dataKey="value"
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  strokeWidth={2}
                  stroke="hsl(var(--card))"
                >
                  {chartData.map((entry, index) => (
                    <Cell 
                      key={`cell-${index}`} 
                      fill={entry.color} 
                      stroke={entry.color} 
                      strokeWidth={3}
                      style={{ 
                        filter: `drop-shadow(0 2px 4px ${entry.color}40)`
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
                    padding: "12px",
                  }}
                  formatter={(value: number) => formatCurrency(value)}
                  labelStyle={{ fontWeight: 600, marginBottom: "4px" }}
                />
                <Legend 
                  wrapperStyle={{ fontSize: "14px", fontWeight: 500 }} 
                  iconType="circle"
                  formatter={(value, entry: any) => (
                    <span style={{ color: entry.color, fontWeight: 500 }}>{value}</span>
                  )}
                />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex h-[300px] items-center justify-center">
              <p className="text-sm text-muted-foreground">データがありません</p>
            </div>
          )}
        </CardContent>
      </Card>

      {/* 全カテゴリーリスト: 3つ以上の場合に表示 */}
      {monthlyData.byCategory.length > 3 && (
        <Card className="border-border/50 shadow-md hover:shadow-lg transition-all duration-300 bg-gradient-to-br from-card to-card/95">
          <CardHeader>
            <CardTitle className="text-xs md:text-sm font-semibold text-muted-foreground uppercase tracking-wide">
              全カテゴリー
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {monthlyData.byCategory.map(([category, amount], index) => (
                <div 
                  key={category} 
                  className="flex items-center justify-between py-2 px-2 rounded-lg hover:bg-muted/50 transition-colors"
                >
                  <div className="flex items-center gap-2">
                    <div 
                      className="w-3 h-3 rounded-full shadow-sm"
                      style={{ backgroundColor: getCategoryColor(category, index) }}
                    />
                    <span className="text-sm font-semibold text-foreground">{category}</span>
                  </div>
                  <span className="text-sm font-bold text-foreground">{formatCurrency(amount)}</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
