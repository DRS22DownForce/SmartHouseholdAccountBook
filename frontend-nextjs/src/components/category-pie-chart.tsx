"use client"

import { useMemo } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from "recharts"
import type { Expense } from "@/lib/types"
import { getCategoryColor } from "@/lib/category-colors"

interface CategoryPieChartProps {
  expenses: Expense[]
  selectedMonth: string
}

export function CategoryPieChart({ expenses, selectedMonth }: CategoryPieChartProps) {
  const chartData = useMemo(() => {
    const filtered = expenses.filter((expense) => expense.date.startsWith(selectedMonth))

    const byCategory = filtered.reduce(
      (acc, expense) => {
        acc[expense.category] = (acc[expense.category] || 0) + expense.amount
        return acc
      },
      {} as Record<string, number>,
    )

    return Object.entries(byCategory)
      .map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value)
  }, [expenses, selectedMonth])

  const formatCurrency = (value: number) => {
    return `¥${value.toLocaleString()}`
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>カテゴリー別支出</CardTitle>
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
                outerRadius={80}
                dataKey="value"
                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
              >
                {chartData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={getCategoryColor(entry.name, index)} />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{
                  backgroundColor: "hsl(var(--card))",
                  border: "1px solid hsl(var(--border))",
                  borderRadius: "8px",
                }}
                formatter={(value: number) => formatCurrency(value)}
              />
              <Legend wrapperStyle={{ fontSize: "14px" }} />
            </PieChart>
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
