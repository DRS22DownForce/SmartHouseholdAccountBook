"use client"

import { useMemo } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from "recharts"
import type { Expense } from "@/lib/types"
import { getCategoryColor } from "@/lib/category-colors"
import { formatCurrency, formatMonth } from "@/lib/formatters"

interface SummarySectionProps {
  expenses: Expense[]
  selectedMonth: string
  onMonthChange: (month: string) => void
}

function extractMonthFromDate(date: string): string {
  return date.substring(0, 7)
}

function getAvailableMonths(expenses: Expense[]): string[] {
  const months = new Set<string>()
  expenses.forEach((expense) => {
    months.add(extractMonthFromDate(expense.date))
  })
  return Array.from(months).sort().reverse()
}

function calculateMonthlyData(expenses: Expense[], selectedMonth: string) {
  const filtered = expenses.filter((expense) => expense.date.startsWith(selectedMonth))
  const total = filtered.reduce((sum, expense) => sum + expense.amount, 0)
  const byCategory = filtered.reduce(
    (acc, expense) => {
      acc[expense.category] = (acc[expense.category] || 0) + expense.amount
      return acc
    },
    {} as Record<string, number>
  )

  return {
    total,
    count: filtered.length,
    byCategory: Object.entries(byCategory).sort((a, b) => b[1] - a[1]),
  }
}

export function SummarySection({
  expenses,
  selectedMonth,
  onMonthChange,
}: SummarySectionProps) {
  const availableMonths = useMemo(() => getAvailableMonths(expenses), [expenses])

  const monthlyData = useMemo(
    () => calculateMonthlyData(expenses, selectedMonth),
    [expenses, selectedMonth]
  )

  const chartData = useMemo(() => {
    return monthlyData.byCategory.map(([name, value], index) => ({
      name,
      value,
      color: getCategoryColor(name, index),
    }))
  }, [monthlyData.byCategory])

  return (
    <div className="space-y-3 md:space-y-4">
      <div className="flex items-center justify-between gap-4 mb-2">
        <h2 className="text-xl md:text-2xl font-semibold bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent tracking-tight">
          月別サマリー
        </h2>
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

      <div className="grid gap-4 md:grid-cols-3 md:gap-5 lg:gap-6">
        <Card className="border-border/50 shadow-md hover:shadow-xl hover:scale-[1.02] transition-all duration-300 bg-gradient-to-br from-card to-primary/5 hover:border-primary/50">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm md:text-base font-semibold text-muted-foreground uppercase tracking-wide">
              合計支出
            </CardTitle>
          </CardHeader>
          <CardContent className="pt-0">
            <p className="text-4xl md:text-5xl font-bold text-foreground tracking-tight">
              {formatCurrency(monthlyData.total)}
            </p>
            <p className="text-base md:text-lg text-muted-foreground mt-2 font-medium">
              {monthlyData.count}件の支出
            </p>
          </CardContent>
        </Card>

        <Card className="border-border/50 shadow-md hover:shadow-xl hover:scale-[1.02] transition-all duration-300 bg-gradient-to-br from-card to-card/95 hover:border-primary/30">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm md:text-base font-semibold text-muted-foreground uppercase tracking-wide">
              トップカテゴリー
            </CardTitle>
          </CardHeader>
          <CardContent className="pt-0">
            {monthlyData.byCategory.length > 0 ? (
              <div className="space-y-2.5">
                {monthlyData.byCategory.slice(0, 3).map(([category, amount], index) => (
                  <div
                    key={category}
                    className="flex items-center justify-between p-2 rounded-lg hover:bg-muted/50 transition-colors"
                  >
                    <div className="flex items-center gap-2.5">
                      <div
                        className="w-4 h-4 rounded-full shadow-sm"
                        style={{ backgroundColor: getCategoryColor(category, index) }}
                      />
                      <span className="text-base md:text-lg font-semibold text-foreground">{category}</span>
                    </div>
                    <span className="text-base md:text-lg font-bold text-foreground">{formatCurrency(amount)}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-base text-muted-foreground">データがありません</p>
            )}
          </CardContent>
        </Card>

        <Card className="border-border/50 shadow-md hover:shadow-xl hover:scale-[1.02] transition-all duration-300 bg-gradient-to-br from-card to-card/95 hover:border-primary/30">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm md:text-base font-semibold text-muted-foreground uppercase tracking-wide">
              カテゴリー別支出の割合
            </CardTitle>
          </CardHeader>
          <CardContent className="pt-0">
            {chartData.length > 0 ? (
              <div className="space-y-2">
                <ResponsiveContainer width="100%" height={160}>
                  <PieChart>
                    <Pie
                      data={chartData}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      outerRadius={60}
                      innerRadius={25}
                      dataKey="value"
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
                        padding: "12px",
                      }}
                      formatter={(value: number) => formatCurrency(value)}
                      labelStyle={{ fontWeight: 600, marginBottom: "4px" }}
                    />
                  </PieChart>
                </ResponsiveContainer>
                <div className="flex flex-wrap items-center justify-center gap-3 px-2">
                  {chartData.map((entry, index) => {
                    const total = chartData.reduce((sum, e) => sum + e.value, 0)
                    const percent = total > 0 ? ((entry.value / total) * 100).toFixed(0) : "0"
                    return (
                      <div key={index} className="flex items-center gap-2">
                        <div
                          className="w-3.5 h-3.5 rounded-full"
                          style={{ backgroundColor: entry.color }}
                        />
                        <span className="text-sm md:text-base font-medium text-foreground">
                          {entry.name} {percent}%
                        </span>
                      </div>
                    )
                  })}
                </div>
              </div>
            ) : (
              <div className="flex h-[160px] items-center justify-center">
                <p className="text-base text-muted-foreground">データがありません</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

