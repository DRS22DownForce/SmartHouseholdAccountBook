/**
 * アプリケーション全体で使用される定数
 */

export const EXPENSE_CATEGORIES = [
  "食費",
  "交通費",
  "住居費",
  "光熱費",
  "通信費",
  "娯楽費",
  "医療費",
  "衣服費",
  "日用品",
  "投資",
  "教育費",
  "その他",
] as const

export type ExpenseCategory = (typeof EXPENSE_CATEGORIES)[number]

export const MONTH_RANGES = [
  { value: "3", label: "3ヶ月" },
  { value: "6", label: "6ヶ月" },
  { value: "12", label: "12ヶ月" },
  { value: "24", label: "24ヶ月" },
] as const

export const DATE_FORMAT = {
  ISO_DATE: "YYYY-MM-DD",
  DISPLAY_MONTH: "YYYY年MM月",
  CHART_MONTH: "YYYY/MM",
} as const

