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
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-3xl font-bold text-foreground tracking-tight">月別サマリー</h2>
        <Select value={selectedMonth} onValueChange={onMonthChange}>
          <SelectTrigger className="w-48 rounded-lg">
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

      <div className="grid gap-4 md:grid-cols-2">
        <Card className="border-border/50 shadow-sm hover:shadow-md transition-shadow">
          <CardHeader>
            <CardTitle className="text-sm font-semibold text-muted-foreground uppercase tracking-wide">
              合計支出
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-4xl font-bold text-foreground tracking-tight">{formatCurrency(monthlyData.total)}</p>
            <p className="text-sm text-muted-foreground mt-2 font-medium">{monthlyData.count}件の支出</p>
          </CardContent>
        </Card>

        <Card className="border-border/50 shadow-sm hover:shadow-md transition-shadow">
          <CardHeader>
            <CardTitle className="text-sm font-semibold text-muted-foreground uppercase tracking-wide">
              トップカテゴリー
            </CardTitle>
          </CardHeader>
          <CardContent>
            {monthlyData.byCategory.length > 0 ? (
              <div className="space-y-3">
                {monthlyData.byCategory.slice(0, 3).map(([category, amount]) => (
                  <div key={category} className="flex items-center justify-between">
                    <span className="text-sm font-semibold text-foreground">{category}</span>
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

      <Card className="border-border/50 shadow-sm">
        <CardHeader>
          <CardTitle className="text-lg font-bold">カテゴリー別支出の割合</CardTitle>
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
                  dataKey="value"
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  strokeWidth={0}
                >
                  {chartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} stroke={entry.color} strokeWidth={2} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    backgroundColor: "hsl(var(--card))",
                    border: "1px solid hsl(var(--border))",
                    borderRadius: "12px",
                    boxShadow: "0 4px 6px -1px rgb(0 0 0 / 0.1)",
                  }}
                  formatter={(value: number) => formatCurrency(value)}
                />
                <Legend wrapperStyle={{ fontSize: "14px", fontWeight: 500 }} />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex h-[300px] items-center justify-center">
              <p className="text-sm text-muted-foreground">データがありません</p>
            </div>
          )}
        </CardContent>
      </Card>

      {monthlyData.byCategory.length > 3 && (
        <Card className="border-border/50 shadow-sm">
          <CardHeader>
            <CardTitle className="text-sm font-semibold text-muted-foreground uppercase tracking-wide">
              全カテゴリー
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {monthlyData.byCategory.map(([category, amount]) => (
                <div key={category} className="flex items-center justify-between py-1">
                  <span className="text-sm font-semibold text-foreground">{category}</span>
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
