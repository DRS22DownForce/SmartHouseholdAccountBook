"use client"

import { useState, useEffect } from "react"
import type { Expense, ExpenseFormData } from "@/lib/types"

const STORAGE_KEY = "household-expenses"

export function useExpenses() {
  const [expenses, setExpenses] = useState<Expense[]>([])
  const [isLoaded, setIsLoaded] = useState(false)

  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      try {
        setExpenses(JSON.parse(stored))
      } catch (error) {
        console.error("[v0] Failed to parse expenses:", error)
      }
    }
    setIsLoaded(true)
  }, [])

  useEffect(() => {
    if (isLoaded) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(expenses))
    }
  }, [expenses, isLoaded])

  const addExpense = (data: ExpenseFormData) => {
    const newExpense: Expense = {
      ...data,
      id: crypto.randomUUID(),
      createdAt: new Date().toISOString(),
    }
    setExpenses((prev) => [newExpense, ...prev])
  }

  const addExpenses = (dataArray: ExpenseFormData[]) => {
    const newExpenses: Expense[] = dataArray.map((data) => ({
      ...data,
      id: crypto.randomUUID(),
      createdAt: new Date().toISOString(),
    }))
    setExpenses((prev) => [...newExpenses, ...prev])
  }

  const updateExpense = (id: string, data: ExpenseFormData) => {
    setExpenses((prev) => prev.map((expense) => (expense.id === id ? { ...expense, ...data } : expense)))
  }

  const deleteExpense = (id: string) => {
    setExpenses((prev) => prev.filter((expense) => expense.id !== id))
  }

  return {
    expenses,
    addExpense,
    addExpenses,
    updateExpense,
    deleteExpense,
    isLoaded,
  }
}
