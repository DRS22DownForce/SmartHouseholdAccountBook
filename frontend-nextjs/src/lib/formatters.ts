/**
 * フォーマット関連のユーティリティ関数
 */

import { formatYearMonth } from "@/lib/date-utils"

/**
 * 金額を日本語の通貨形式でフォーマット
 */
export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat("ja-JP", {
    style: "currency",
    currency: "JPY",
  }).format(amount)
}

/**
 * YYYY-MM 文字列を日本語形式でフォーマット（例: "2024年01月"）
 * 月別サマリーの Select など、API の月キー向け。
 */
export function formatMonth(month: string): string {
  const [year, monthNum] = month.split("-")
  return `${year}年${monthNum}月`
}

/**
 * 年月をチャート用の短い形式でフォーマット（例: "2024/01"）
 */
export function formatMonthForChart(month: string): string {
  const [year, monthNum] = month.split("-")
  return `${year}/${monthNum}`
}

/**
 * 現在の日付を YYYY-MM-DD 形式で取得（ローカルタイムゾーン）
 */
export function getCurrentDateString(): string {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, "0")
  const d = String(now.getDate()).padStart(2, "0")
  return `${y}-${m}-${d}`
}

/**
 * 現在の年月を YYYY-MM 形式で取得（ローカルタイムゾーン）
 */
export function getCurrentMonthString(): string {
  return formatYearMonth(new Date())
}

