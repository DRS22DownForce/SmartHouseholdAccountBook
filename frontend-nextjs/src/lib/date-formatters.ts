/**
 * 日付フォーマット関連のユーティリティ関数
 * 
 * 日付を日本語形式でフォーマットする関数を提供します。
 */

/**
 * 日付を日本語形式でフォーマット
 * 
 * 日付文字列（YYYY-MM-DD形式）を日本語の日付形式に変換します。
 * 例: "2024-01-15" → "2024年1月15日"
 * 
 * @param dateString - フォーマットする日付文字列（YYYY-MM-DD形式）
 * @returns フォーマットされた日付文字列（日本語形式）
 * 
 * @example
 * ```typescript
 * const formatted = formatDate("2024-01-15")
 * // 結果: "2024年1月15日"
 * ```
 */
export function formatDate(dateString: string): string {
    const date = new Date(dateString)
    return new Intl.DateTimeFormat("ja-JP", {
        year: "numeric",
        month: "long",
        day: "numeric",
    }).format(date)
}

/**
 * 年月を日本語形式でフォーマット
 * 
 * Dateオブジェクトから年月のみを日本語形式で取得します。
 * 例: 2024年1月のDate → "2024年1月"
 * 
 * @param date - フォーマットするDateオブジェクト
 * @returns フォーマットされた年月文字列（日本語形式）
 * 
 * @example
 * ```typescript
 * const formatted = formatMonthYear(new Date(2024, 0, 15))
 * // 結果: "2024年1月"
 * ```
 */
export function formatMonthYear(date: Date): string {
    return new Intl.DateTimeFormat("ja-JP", {
        year: "numeric",
        month: "long",
    }).format(date)
}

