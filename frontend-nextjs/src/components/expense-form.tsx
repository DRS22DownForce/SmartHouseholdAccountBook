"use client"

import type React from "react"
import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Plus } from "lucide-react"
import type { Expense, ExpenseFormData } from "@/lib/types"
import { EXPENSE_CATEGORIES } from "@/lib/constants"
import { getCurrentDateString } from "@/lib/formatters"

interface ExpenseFormProps {
  expense?: Expense
  onSubmit: (data: ExpenseFormData) => void
  trigger?: React.ReactNode
}

const getInitialFormData = (): ExpenseFormData => ({
  amount: 0,
  category: "",
  description: "",
  date: getCurrentDateString(),
})

export function ExpenseForm({ expense, onSubmit, trigger }: ExpenseFormProps) {
  const [open, setOpen] = useState(false)
  const [formData, setFormData] = useState<ExpenseFormData>(getInitialFormData())

  useEffect(() => {
    if (expense) {
      setFormData({
        amount: expense.amount,
        category: expense.category,
        description: expense.description,
        date: expense.date,
      })
    } else if (!open) {
      setFormData(getInitialFormData())
    }
  }, [expense, open])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSubmit(formData)
    setOpen(false)
    if (!expense) {
      setFormData(getInitialFormData())
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        {trigger || (
          <Button className="gap-2">
            <Plus className="h-4 w-4" />
            支出を追加
          </Button>
        )}
      </DialogTrigger>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="text-balance">{expense ? "支出を編集" : "新しい支出を追加"}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="amount">金額</Label>
            <Input
              id="amount"
              type="number"
              min="0"
              step="1"
              required
              value={formData.amount || ""}
              onChange={(e) => setFormData({ ...formData, amount: Number(e.target.value) })}
              placeholder="1000"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="category">カテゴリー</Label>
            <Select
              value={formData.category}
              onValueChange={(value) => setFormData({ ...formData, category: value })}
              required
            >
              <SelectTrigger id="category">
                <SelectValue placeholder="カテゴリーを選択" />
              </SelectTrigger>
              <SelectContent>
                {EXPENSE_CATEGORIES.map((category) => (
                  <SelectItem key={category} value={category}>
                    {category}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">説明</Label>
            <Input
              id="description"
              type="text"
              required
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="ランチ代"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="date">日付</Label>
            <Input
              id="date"
              type="date"
              required
              value={formData.date}
              onChange={(e) => setFormData({ ...formData, date: e.target.value })}
            />
          </div>

          <Button type="submit" className="w-full">
            {expense ? "更新" : "追加"}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  )
}
