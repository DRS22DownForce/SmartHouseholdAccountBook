/**
 * フォームデータ変換関連のユーティリティ関数
 * 
 * 支出データとフォームデータの相互変換処理を提供します。
 */

import type { Expense, ExpenseFormData } from "@/lib/types"
import { getCurrentDateString } from "@/lib/formatters"

/**
 * 初期フォームデータを生成する関数
 * 
 * 新規支出追加時に使用する、デフォルト値が設定されたフォームデータを生成します。
 * 日付は現在の日付が設定され、その他のフィールドは空または0が設定されます。
 * 
 * @returns 初期化されたフォームデータ
 * 
 * @example
 * ```typescript
 * const initialData = getInitialFormData()
 * // 結果: { date: "2024-01-15", amount: 0, category: "", description: "" }
 * ```
 */
export function getInitialFormData(): ExpenseFormData {
    return {
        amount: 0,
        category: "",
        description: "",
        date: getCurrentDateString(),
    }
}

/**
 * 既存の支出データをフォームデータに変換する関数
 * 
 * 編集モードで使用する、既存の支出データをフォームデータ形式に変換します。
 * 支出オブジェクトから必要なフィールドのみを抽出してフォームデータに変換します。
 * 
 * @param expense - 変換する支出データ
 * @returns フォームデータ形式に変換された支出データ
 * 
 * @example
 * ```typescript
 * const expense = { id: "1", date: "2024-01-15", amount: 3500, category: "食費", description: "ランチ" }
 * const formData = expenseToFormData(expense)
 * // 結果: { date: "2024-01-15", amount: 3500, category: "食費", description: "ランチ" }
 * ```
 */
export function expenseToFormData(expense: Expense): ExpenseFormData {
    return {
        amount: expense.amount,
        category: expense.category,
        description: expense.description,
        date: expense.date,
    }
}

