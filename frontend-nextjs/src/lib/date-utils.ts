/**
 * 日付計算関連のユーティリティ関数
 * 
 * 月の範囲計算や月のキーリスト生成など、日付に関する計算処理を提供します。
 */

/**
 * 月の範囲を計算する（開始月と終了月をYYYY-MM形式で返す）
 * 
 * 現在の月から指定された月数分さかのぼった範囲を計算します。
 * チャート表示などで使用する月の範囲を取得する際に使用します。
 * 
 * @param monthsToShow - 表示する月数（例: 6 → 過去6ヶ月）
 * @returns 開始月と終了月のタプル [startMonth, endMonth]（YYYY-MM形式）
 * 
 * @example
 * ```typescript
 * const [startMonth, endMonth] = calculateMonthRange(6)
 * // 現在が2024年3月の場合: ["2023-10", "2024-03"]
 * ```
 */
export function calculateMonthRange(monthsToShow: number): [string, string] {
    const now = new Date()
    const endMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`

    const startDate = new Date(now.getFullYear(), now.getMonth() - (monthsToShow - 1), 1)
    const startMonth = `${startDate.getFullYear()}-${String(startDate.getMonth() + 1).padStart(2, "0")}`

    return [startMonth, endMonth]
}

/**
 * 月のキーリストを生成する（YYYY-MM形式）
 * 
 * 現在の月から指定された月数分さかのぼった月のリストを生成します。
 * チャートのX軸などで使用する月のリストを取得する際に使用します。
 * 
 * @param monthsToShow - 表示する月数（例: 6 → 過去6ヶ月）
 * @returns 月のキーリスト（YYYY-MM形式の文字列配列）
 * 
 * @example
 * ```typescript
 * const months = generateMonthKeys(6)
 * // 現在が2024年3月の場合: ["2023-10", "2023-11", "2023-12", "2024-01", "2024-02", "2024-03"]
 * ```
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
 * DateオブジェクトをYYYY-MM形式の文字列に変換
 * 
 * 日付オブジェクトから年月のみをYYYY-MM形式で取得します。
 * APIリクエストや月のキー生成で使用する標準フォーマットです。
 * 
 * @param date - 変換するDateオブジェクト
 * @returns YYYY-MM形式の文字列
 * 
 * @example
 * ```typescript
 * const monthKey = formatYearMonth(new Date(2024, 0, 15))
 * // 結果: "2024-01"
 * ```
 */
export function formatYearMonth(date: Date): string {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`
}

/**
 * 前月の文字列（YYYY-MM形式）を取得
 * 
 * 現在の月から1ヶ月前の月をYYYY-MM形式で取得します。
 * 前月比の計算などで使用します。
 * 
 * @returns 前月の文字列（YYYY-MM形式）
 * 
 * @example
 * ```typescript
 * const prevMonth = getPreviousMonthString()
 * // 現在が2024年3月の場合: "2024-02"
 * // 現在が2024年1月の場合: "2023-12"
 * ```
 */
export function getPreviousMonthString(): string {
    const now = new Date()
    const year = now.getFullYear()
    const month = now.getMonth()
    
    // 前月を計算（0月の場合は前年の12月になる）
    const prevDate = new Date(year, month - 1, 1)
    const prevYear = prevDate.getFullYear()
    const prevMonth = prevDate.getMonth() + 1
    
    return `${prevYear}-${String(prevMonth).padStart(2, '0')}`
}
