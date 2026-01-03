/**
 * CSV解析関連のユーティリティ関数
 * 
 * CSVファイルの内容を解析して、支出データに変換する処理を提供します。
 */

import type { ExpenseFormData } from "@/lib/types"

/**
 * CSVテキストを解析してExpenseFormDataの配列に変換
 * 
 * CSVファイルのテキスト内容を解析し、支出データの配列に変換します。
 * ヘッダー行から自動的に列を検出し、日付・金額・カテゴリー・説明を抽出します。
 * 
 * 対応するヘッダー名:
 * - 日付: "日付", "date"
 * - 金額: "金額", "amount", "price"
 * - カテゴリー: "カテゴリ", "category"
 * - 説明: "説明", "description", "memo", "メモ"
 * 
 * @param text - CSVファイルのテキスト内容
 * @returns 解析された支出データの配列
 * @throws {Error} CSVファイルが空か、ヘッダー行のみの場合
 * 
 * @example
 * ```typescript
 * const csvText = "日付,金額,カテゴリ,説明\n2024-01-15,3500,食費,スーパーマーケット"
 * const expenses = parseCSV(csvText)
 * // 結果: [{ date: "2024-01-15", amount: 3500, category: "食費", description: "スーパーマーケット" }]
 * ```
 */
export function parseCSV(text: string): ExpenseFormData[] {
    const lines = text.trim().split("\n")
    if (lines.length < 2) {
        throw new Error("CSVファイルが空か、ヘッダー行のみです")
    }

    const headers = lines[0].split(",").map((h) => h.trim().toLowerCase())
    const expenses: ExpenseFormData[] = []

    for (let i = 1; i < lines.length; i++) {
        const values = lines[i].split(",").map((v) => v.trim())
        if (values.length < 3) continue

        // ヘッダーから各列のインデックスを検出
        const dateIndex = headers.findIndex((h) => h.includes("日付") || h.includes("date"))
        const amountIndex = headers.findIndex((h) => h.includes("金額") || h.includes("amount") || h.includes("price"))
        const categoryIndex = headers.findIndex((h) => h.includes("カテゴリ") || h.includes("category"))
        const descriptionIndex = headers.findIndex(
            (h) => h.includes("説明") || h.includes("description") || h.includes("memo") || h.includes("メモ"),
        )

        // 列が見つからない場合はデフォルトの位置を使用
        const date = dateIndex >= 0 ? values[dateIndex] : values[0]
        const amount = amountIndex >= 0 ? values[amountIndex] : values[1]
        const category = categoryIndex >= 0 ? values[categoryIndex] : values[2]
        const description = descriptionIndex >= 0 ? values[descriptionIndex] : values[3] || ""

        // 金額を数値に変換（カンマや通貨記号を除去）
        const parsedAmount = Number.parseFloat(amount.replace(/[^0-9.-]/g, ""))
        if (isNaN(parsedAmount)) continue

        expenses.push({
            date: date,
            amount: Math.abs(parsedAmount), // 負の値も正の値として扱う
            category: category || "その他", // カテゴリーが空の場合は"その他"を設定
            description: description,
        })
    }

    return expenses
}

