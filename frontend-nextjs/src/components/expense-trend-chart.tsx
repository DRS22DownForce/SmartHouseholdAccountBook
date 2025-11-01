"use client"

import { useMemo, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts"
import type { Expense } from "@/lib/types"
import { getCategoryColor } from "@/lib/category-colors"

interface ExpenseTrendChartProps {
  expenses: Expense[]
}

const formatMonth = (month: string) => {
  const [year, monthNum] = month.split("-")
  return `${year}/${monthNum}`
}

const formatCurrency = (value: number) => {
  if (value === 0) return "¥0"
  if (value < 1000) return `¥${Math.round(value)}`
  if (value < 10000) return `¥${(value / 1000).toFixed(1)}k`
  return `¥${Math.round(value / 1000)}k`
}

const CustomTooltip = ({ active, payload, label }: any) => {
  if (!active || !payload || !payload.length) return null

  // 値が0でない項目のみをフィルタリング
  const validItems = payload.filter((item: any) => item.value > 0)

  if (validItems.length === 0) return null

  // 合計金額を計算
  const total = validItems.reduce((sum: number, item: any) => sum + item.value, 0)

  return (
    <div className="bg-card border border-border rounded-lg shadow-lg p-3 min-w-[200px]">
      <p className="font-semibold text-sm mb-2 pb-2 border-b border-border">{label}</p>
      <div className="space-y-1.5">
        {validItems.map((item: any, index: number) => (
          <div key={index} className="flex items-center justify-between gap-4">
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 rounded-sm" style={{ backgroundColor: item.fill || item.color }} />
              <span className="text-xs text-muted-foreground">{item.name}</span>
            </div>
            <span className="text-xs font-medium">¥{item.value.toLocaleString()}</span>
          </div>
        ))}
      </div>
      <div className="flex items-center justify-between gap-4 mt-2 pt-2 border-t border-border">
        <span className="text-xs font-semibold">合計</span>
        <span className="text-xs font-bold">¥{total.toLocaleString()}</span>
      </div>
    </div>
  )
}

export function ExpenseTrendChart({ expenses }: ExpenseTrendChartProps) {
  const [monthRange, setMonthRange] = useState("6")

  const chartData = useMemo(() => {
    const now = new Date()
    const monthsToShow = Number.parseInt(monthRange)
    const allMonths: string[] = []

    for (let i = monthsToShow - 1; i >= 0; i--) {
      const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
      const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`
      allMonths.push(monthKey)
    }

    const monthlyData: Record<string, Record<string, number>> = {}

    expenses.forEach((expense) => {
      const month = expense.date.substring(0, 7)
      if (!monthlyData[month]) {
        monthlyData[month] = {}
      }
      monthlyData[month][expense.category] = (monthlyData[month][expense.category] || 0) + expense.amount
    })

    const allCategories = Array.from(new Set(expenses.map((e) => e.category)))

    return allMonths.map((month) => {
      const data: Record<string, string | number> = {
        month: formatMonth(month),
      }

      allCategories.forEach((category) => {
        data[category] = monthlyData[month]?.[category] || 0
      })

      return data
    })
  }, [expenses, monthRange])

  const categories = useMemo(() => {
    return Array.from(new Set(expenses.map((e) => e.category)))
  }, [expenses])

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>支出の推移</CardTitle>
          <Select value={monthRange} onValueChange={setMonthRange}>
            <SelectTrigger className="w-[140px]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="3">3ヶ月</SelectItem>
              <SelectItem value="6">6ヶ月</SelectItem>
              <SelectItem value="12">12ヶ月</SelectItem>
              <SelectItem value="24">24ヶ月</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </CardHeader>
      <CardContent>
        {chartData.length > 0 ? (
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={chartData} barSize={40}>
              <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
              <XAxis dataKey="month" className="text-xs" tick={{ fill: "hsl(var(--muted-foreground))" }} />
              <YAxis
                className="text-xs"
                tick={{ fill: "hsl(var(--muted-foreground))" }}
                tickFormatter={formatCurrency}
              />
              <Tooltip content={<CustomTooltip />} />
              <Legend />
              {categories.map((category) => (
                <Bar
                  key={category}
                  dataKey={category}
                  stackId="stack"
                  fill={getCategoryColor(category)}
                  name={category}
                />
              ))}
            </BarChart>
          </ResponsiveContainer>
        ) : (
          <div className="flex h-[300px] items-center justify-center">
            <p className="text-sm text-muted-foreground">データがありません</p>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
