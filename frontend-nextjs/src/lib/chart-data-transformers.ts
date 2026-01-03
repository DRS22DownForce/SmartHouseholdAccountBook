/**
 * チャートデータ変換関連のユーティリティ関数
 * 
 * 月別サマリーデータをチャート表示用のデータ形式に変換する処理を提供します。
 */

import type { MonthlySummary } from "@/api/expenseMappers"
import { getCategoryColor } from "@/lib/category-colors"
import { formatMonthForChart } from "@/lib/formatters"

/**
 * 月別サマリーからチャート用のデータを生成（円グラフ用）
 * 
 * 月別サマリーのカテゴリー別データを、円グラフ（PieChart）で使用する形式に変換します。
 * 各カテゴリーの名前、金額、色情報を含むオブジェクトの配列を返します。
 * 
 * @param monthlySummary - 月別サマリーデータ（nullの場合は空配列を返す）
 * @returns チャート用のデータ配列（name, value, colorを含むオブジェクトの配列）
 * 
 * @example
 * ```typescript
 * const chartData = transformMonthlySummaryToChartData(monthlySummary)
 * // 結果: [
 * //   { name: "食費", value: 50000, color: "#3b82f6" },
 * //   { name: "交通費", value: 10000, color: "#10b981" },
 * // ]
 * ```
 */
export function transformMonthlySummaryToChartData(monthlySummary: MonthlySummary | null) {
    if (!monthlySummary || !monthlySummary.byCategory) {
        return []
    }
    return monthlySummary.byCategory.map((item) => ({
        name: item.category,
        value: item.amount,
        color: getCategoryColor(item.category),
    }))
}

/**
 * 月別サマリーのリストからチャート用のデータを生成（棒グラフ用）
 * 
 * 複数月の月別サマリーデータを、棒グラフ（BarChart）で使用する形式に変換します。
 * 各月のデータを、カテゴリー別の金額を含むオブジェクトの配列として返します。
 * 
 * @param monthlySummaries - 月別サマリーデータの配列
 * @param allMonths - 表示する月のキーリスト（YYYY-MM形式）
 * @param categories - 表示するカテゴリーのリスト
 * @returns チャート用のデータ配列（各月のデータをカテゴリー別に含むオブジェクトの配列）
 * 
 * @example
 * ```typescript
 * const chartData = transformMonthlySummariesToChartData(
 *   monthlySummaries,
 *   ["2024-01", "2024-02"],
 *   ["食費", "交通費"]
 * )
 * // 結果: [
 * //   { month: "2024/01", 食費: 50000, 交通費: 10000 },
 * //   { month: "2024/02", 食費: 45000, 交通費: 12000 },
 * // ]
 * ```
 */
export function transformMonthlySummariesToChartData(
    monthlySummaries: MonthlySummary[],
    allMonths: string[],
    categories: string[]
) {
    // 月別サマリーを月のキーでマップ（高速検索用）
    const summaryMap = new Map<string, MonthlySummary>()
    monthlySummaries.forEach((summary, index) => {
        if (index < allMonths.length) {
            summaryMap.set(allMonths[index], summary)
        }
    })

    // 各月のデータを生成
    return allMonths.map((month) => {
        const data: Record<string, string | number> = {
            month: formatMonthForChart(month), // チャート表示用のフォーマット（例: "2024/01"）
        }

        // 各カテゴリーの金額を設定
        categories.forEach((category) => {
            const summary = summaryMap.get(month)
            const categoryItem = summary?.byCategory.find((item) => item.category === category)
            data[category] = categoryItem?.amount ?? 0 // データがない場合は0を設定
        })

        return data
    })
}

