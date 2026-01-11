"use client"

import type React from "react"
import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Plus, Sparkles, Loader2 } from "lucide-react"
import { cn } from "@/lib/utils"
import type { ExpenseFormData } from "@/lib/types"
import { EXPENSE_CATEGORIES } from "@/lib/constants"
import { getInitialFormData, expenseToFormData } from "@/lib/form-data-utils"
import type { Expense } from "@/lib/types"
import { predictCategory } from "@/api/aiCategoryApi"
import { toast } from "sonner"

/**
 * ExpenseFormコンポーネントのプロップス型定義
 * 
 * 支出の新規追加または編集を行うためのフォームダイアログコンポーネントのプロップスです。
 * 
 * @property {Expense} [expense] - 編集対象の支出データ。指定されない場合は新規追加モードになります。
 *                                 新規追加時は初期値（今日の日付、空の金額・カテゴリー・説明）が設定されます。
 *                                 編集時は既存の支出データがフォームに自動的に設定されます。
 * 
 * @property {function} onSubmit - フォーム送信時に呼び出されるコールバック関数。
 *                                 ExpenseFormData型のフォームデータを引数として受け取ります。
 *                                 親コンポーネントでこの関数を実装し、データの保存処理を行います。
 * 
 * @property {React.ReactNode} [reactNode] - ダイアログを開くためのトリガー要素（ボタンなど）。
 *                                            指定されない場合は、デフォルトの「支出を追加」ボタンが表示されます。
 *                                            カスタムボタン（例：編集アイコンボタン）を表示したい場合に使用します。
 * 
 */
export interface ExpenseFormProps {
  expense?: Expense
  onSubmit: (data: ExpenseFormData) => void
  reactNode?: React.ReactNode
}

export function ExpenseForm({ expense, onSubmit, reactNode }: ExpenseFormProps) {
  const [open, setOpen] = useState(false)
  const [formData, setFormData] = useState<ExpenseFormData>(getInitialFormData())
  const [isPredictingCategory, setIsPredictingCategory] = useState(false)

  /**
   * フォームデータの初期化とリセットを管理するuseEffect
   * 
   * 以下の2つのケースでフォームデータを更新します：
   * 1. 編集モード（expenseが存在する場合）: 既存の支出データをフォームに設定
   * 2. 新規追加モード（expenseがなく、ダイアログが開いている場合）: フォームを初期状態にリセット
   * 
   * これにより、ダイアログを開くたびに適切な初期値が設定されます。
   */
  useEffect(() => {
    if (expense) {
      // 編集モード: 既存の支出データをフォームに設定
      setFormData(expenseToFormData(expense))
    } else if (open) {
      setFormData(getInitialFormData())
    }
  }, [expense, open])

  /**
   * フォーム送信ハンドラー
   * 
   * フォームの送信時に以下を実行します：
   * 1. 親コンポーネントにデータを渡す
   * 2. ダイアログを閉じる
   */
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSubmit(formData)
    setOpen(false)
  }

  /**
   * フォームフィールドの更新ヘルパー関数
   * 
   * @param field 更新するフィールド名
   * @param value 新しい値
   */
  const updateExpenseFormData = <K extends keyof ExpenseFormData>(
    field: K,
    value: ExpenseFormData[K]
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
  }

  /**
   * AIカテゴリー自動分類ハンドラー
   * 
   * 説明フィールドの内容を分析して、AIが適切なカテゴリーを推論します。
   * ローディング状態を管理し、成功時はカテゴリーを自動選択、失敗時はエラーメッセージを表示します。
   */
  const handlePredictCategory = async () => {
    // 説明フィールドが空の場合は処理をスキップ
    if (!formData.description || formData.description.trim().length === 0) {
      toast.error('説明を入力してからAI自動分類を実行してください。')
      return
    }

    setIsPredictingCategory(true)
    try {
      // AI APIを呼び出してカテゴリーを推論
      const predictedCategory = await predictCategory(formData.description)
      
      // 推論されたカテゴリーをフォームに設定
      updateExpenseFormData('category', predictedCategory)
      
      toast.success(`カテゴリーを「${predictedCategory}」に自動分類しました。`)
    } catch (error) {
      // エラーハンドリング: ユーザーに分かりやすいエラーメッセージを表示
      const errorMessage = error instanceof Error ? error.message : 'カテゴリーの自動分類に失敗しました。'
      toast.error(errorMessage)
    } finally {
      setIsPredictingCategory(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        {reactNode || (
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
          {/* 金額入力フィールド */}
          <div className="space-y-2">
            <Label htmlFor="amount">金額</Label>
            <Input
              id="amount"
              type="number"
              min="0"
              step="1"
              required
              value={formData.amount || ""}
              onChange={(e) => updateExpenseFormData("amount", Number(e.target.value))}
              placeholder="1000"
            />
          </div>

          {/* 説明入力フィールド */}
          <div className="space-y-2">
            <Label htmlFor="description">説明</Label>
            <Input
              id="description"
              type="text"
              required
              value={formData.description}
              onChange={(e) => updateExpenseFormData("description", e.target.value)}
              placeholder="ランチ代"
            />
          </div>

          {/* カテゴリー選択フィールド */}
          <div className="space-y-2">
            <Label htmlFor="category">カテゴリー</Label>
            <div className="flex items-center gap-2">
              <div className="flex-1">
                <Select
                  value={formData.category}
                  onValueChange={(value) => updateExpenseFormData("category", value)}
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
              {/* AI自動分類ボタン */}
              <Button
                type="button"
                onClick={handlePredictCategory}
                disabled={isPredictingCategory || !formData.description || formData.description.trim().length === 0}
                className={cn(
                  "gap-2 shadow-lg rounded-xl flex-shrink-0",
                  "bg-gradient-to-r from-orange-400 to-rose-500 hover:from-orange-500 hover:to-rose-600",
                  "transition-all duration-300 hover:shadow-xl hover:-translate-y-0.5",
                  "disabled:opacity-50 disabled:cursor-not-allowed"
                )}
                title={formData.description && formData.description.trim().length > 0 
                  ? "説明文からカテゴリーを自動分類" 
                  : "説明を入力してから実行してください"}
              >
                {isPredictingCategory ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <Sparkles className="h-4 w-4" />
                )}
                <span className="hidden sm:inline">
                  {isPredictingCategory ? "分類中..." : "AI自動分類"}
                </span>
              </Button>
            </div>
          </div>

          {/* 日付入力フィールド */}
          <div className="space-y-2">
            <Label htmlFor="date">日付</Label>
            <Input
              id="date"
              type="date"
              required
              value={formData.date}
              onChange={(e) => updateExpenseFormData("date", e.target.value)}
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
