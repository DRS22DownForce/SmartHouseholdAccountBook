export interface Expense {
  id: string
  amount: number
  category: string
  description: string
  date: string
  createdAt: string
}

export type ExpenseFormData = Omit<Expense, "id" | "createdAt">
