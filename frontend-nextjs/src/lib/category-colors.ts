/**
 * カテゴリー別の色定義
 * 各支出カテゴリーに対応する色を定義しています。
 * 
 * カラーパレット:
 * - 食費: オレンジ（#FF6B35）
 * - 住居費: オレンジ系（#FF8C42）- 画像に合わせて追加
 * - 交通費: 青（#4A90E2）
 * - 光熱費: 緑（#2ECC71）
 * - 医療費: 赤（#E74C3C）
 * - 娯楽: 紫（#9B59B6）
 * - その他: 黄色（#F39C12）
 */
export const CATEGORY_COLORS: Record<string, string> = {
  食費: "#FF6B35", // Orange
  住居費: "#FF8C42", // Dark Orange - 住居費専用の色
  交通費: "#4A90E2", // Blue
  娯楽: "#9B59B6", // Purple
  光熱費: "#2ECC71", // Green
  医療費: "#E74C3C", // Red
  その他: "#F39C12", // Yellow
}

/**
 * カテゴリーに対応する色を取得する関数
 * 
 * @param category - 支出カテゴリー名（例: "食費", "交通費"）
 * @param index - カテゴリーが定義されていない場合のインデックス（デフォルト: 0）
 * @returns カテゴリーに対応する色コード（例: "#FF6B35"）
 * 
 * 使用例:
 * - getCategoryColor("食費") → "#FF6B35"
 * - getCategoryColor("未定義カテゴリー", 2) → "#4A90E2"（DEFAULT_COLORSから）
 */
export function getCategoryColor(category: string): string {
    return CATEGORY_COLORS[category]
}
