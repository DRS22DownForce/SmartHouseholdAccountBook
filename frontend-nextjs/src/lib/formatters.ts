/**
 * フォーマット関連のユーティリティ関数
 */

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
 * 年月を日本語形式でフォーマット（例: "2024年1月"）
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
 * グラフ用の金額フォーマット（簡易版）
 */
export function formatCurrencyForChart(value: number): string {
  if (value === 0) return "¥0"
  if (value < 1000) return `¥${Math.round(value)}`
  if (value < 10000) return `¥${(value / 1000).toFixed(1)}k`
  return `¥${Math.round(value / 1000)}k`
}

/**
 * 現在の日付をYYYY-MM-DD形式で取得
 */
export function getCurrentDateString(): string {
  return new Date().toISOString().split("T")[0]
}

/**
 * 現在の年月をYYYY-MM形式で取得
 */
export function getCurrentMonthString(): string {
  return new Date().toISOString().substring(0, 7)
}

