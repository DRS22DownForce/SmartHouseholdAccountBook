"use client"

import { ExpenseTrendChart } from "@/components/expense-trend-chart"
import type { Expense } from "@/lib/types"

interface ExpenseTrendSectionProps {
  expenses: Expense[]
}

export function ExpenseTrendSection({ expenses }: ExpenseTrendSectionProps) {
  return <ExpenseTrendChart expenses={expenses} />
}

