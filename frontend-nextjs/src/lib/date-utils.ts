/**
 * 日付計算関連のユーティリティ関数
 *
 * 月の範囲計算や月のキーリスト生成など、日付に関する計算処理を提供します。
 */

/**
 * 月の範囲を計算する（開始月と終了月をYYYY-MM形式で返す）
 */
export function calculateMonthRange(monthsToShow: number): [string, string] {
  const now = new Date()
  const endMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`

  const startDate = new Date(
    now.getFullYear(),
    now.getMonth() - (monthsToShow - 1),
    1
  )
  const startMonth = `${startDate.getFullYear()}-${String(startDate.getMonth() + 1).padStart(2, "0")}`

  return [startMonth, endMonth]
}

/**
 * 月のキーリストを生成する（YYYY-MM形式）
 */
export function generateMonthKeys(monthsToShow: number): string[] {
  const now = new Date()
  const monthKeys: string[] = []

  for (let i = monthsToShow - 1; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`
    monthKeys.push(monthKey)
  }

  return monthKeys
}

/**
 * Date オブジェクトを YYYY-MM 形式の文字列に変換（ローカル TZ）
 */
export function formatYearMonth(date: Date): string {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`
}

/**
 * 前月の文字列（YYYY-MM形式）を取得
 */
export function getPreviousMonthString(): string {
  const now = new Date()
  const prevDate = new Date(now.getFullYear(), now.getMonth() - 1, 1)
  return formatYearMonth(prevDate)
}
