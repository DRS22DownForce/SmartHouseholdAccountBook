/**
 * 型定義
 * 
 * このファイルは、アプリケーション全体で使用される型定義を提供します。
 * OpenAPI Generatorで生成された型も再エクスポートしています。
 */

// OpenAPI生成型を再エクスポート
// これにより、他のファイルから簡単にインポートできます
export type { ExpenseDto, ExpenseRequestDto, MonthlySummaryDto } from '@/api/generated/api'

// UI用の支出データ型
// use-expensesフックで使用されます
export interface Expense {
  id: string  // UIではstring型で管理（APIはnumber型）
  amount: number
  category: string
  description: string
  date: string
  createdAt: string  // UI用のメタデータ（APIには存在しない）
}

// フォームデータ型（idとcreatedAtを除いたExpense型）
export type ExpenseFormData = Omit<Expense, "id" | "createdAt">
