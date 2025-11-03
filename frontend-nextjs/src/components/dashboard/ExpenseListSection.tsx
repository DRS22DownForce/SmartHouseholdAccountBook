/**
 * ExpenseListSectionコンポーネント
 * 
 * 支出一覧を表示するセクションです。
 * 
 * 主な機能:
 * - 月別の支出一覧を表示
 * - 月のナビゲーション（前月・次月への移動）
 * - 各支出の編集・削除機能
 * 
 * 初心者向けの説明:
 * - このセクションでは、選択した月の支出を一覧で確認できます
 * - 月を変更することで、過去や未来の支出も確認できます
 * - 各支出カードにホバーすると、編集・削除ボタンが表示されます
 */

"use client"

import { ExpenseList } from "@/components/expense-list"
import type { Expense, ExpenseFormData } from "@/lib/types"

interface ExpenseListSectionProps {
  expenses: Expense[]
  onUpdate: (id: string, data: ExpenseFormData) => void
  onDelete: (id: string) => void
}

export function ExpenseListSection({
  expenses,
  onUpdate,
  onDelete,
}: ExpenseListSectionProps) {
  return (
    <ExpenseList
      expenses={expenses}
      onUpdate={onUpdate}
      onDelete={onDelete}
    />
  )
}

