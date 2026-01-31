/**
 * カテゴリー別の色定義
 * 各支出カテゴリーに対応する色を定義しています。
 * 
 * カラーパレット:
 * - 食費: オレンジ（#FF6B35）
 * - 交通費: 青（#4A90E2）
 * - 住居費: オレンジ系（#FF8C42）
 * - 光熱費: 緑（#2ECC71）
 * - 通信費: スカイブルー（#3B82F6）
 * - 娯楽費: 紫（#9B59B6）
 * - 医療費: 赤（#E74C3C）
 * - 衣服費: ピンク（#EC4899）
 * - 日用品: エメラルドグリーン（#10B981）
 * - 投資: ゴールド（#F59E0B）
 * - 教育費: ネイビー（#1E40AF）
 * - その他: 黄色（#F39C12）
 */
export const CATEGORY_COLORS: Record<string, string> = {
  食費: "#FF6B35", // Orange
  交通費: "#4A90E2", // Blue
  住居費: "#FF8C42", // Dark Orange
  光熱費: "#2ECC71", // Green
  通信費: "#3B82F6", // Sky Blue
  娯楽費: "#9B59B6", // Purple
  医療費: "#E74C3C", // Red
  衣服費: "#EC4899", // Pink
  日用品: "#10B981", // Emerald Green
  投資: "#F59E0B", // Gold
  教育費: "#1E40AF", // Navy Blue
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
/**
 * カテゴリーに対応する色を取得する関数
 */
export function getCategoryColor(category: string): string {
  return CATEGORY_COLORS[category] || "#94a3b8"
}

/**
 * カテゴリーに対応する色をRGBA形式（透過度指定）で取得する関数
 * 
 * @param category - カテゴリー名
 * @param alpha - 透過度（0.0 〜 1.0）
 * @returns rgba(r, g, b, a) 形式の文字列
 */
export function getCategoryColorWithAlpha(category: string, alpha: number): string {
  const hex = getCategoryColor(category)

  // HEXをRGBに変換
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)

  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

/**
 * カテゴリーに対応するグラデーションを取得する関数
 * 
 * 【初心者向け解説】
 * - グラデーション: 2色以上が滑らかに変化する配色
 * - ホームページと統一感のあるリッチなデザインを実現
 * 
 * @param category - カテゴリー名
 * @returns グラデーションのCSSクラス名
 */
export const CATEGORY_GRADIENTS: Record<string, { gradient: string; shadow: string }> = {
  食費: { 
    gradient: "from-orange-400 to-rose-500", 
    shadow: "shadow-orange-500/30" 
  },
  交通費: { 
    gradient: "from-cyan-400 to-blue-500", 
    shadow: "shadow-cyan-500/30" 
  },
  住居費: { 
    gradient: "from-blue-400 to-indigo-500", 
    shadow: "shadow-blue-500/30" 
  },
  光熱費: { 
    gradient: "from-emerald-400 to-green-500", 
    shadow: "shadow-emerald-500/30" 
  },
  通信費: { 
    gradient: "from-sky-400 to-blue-500", 
    shadow: "shadow-sky-500/30" 
  },
  娯楽費: { 
    gradient: "from-purple-400 to-pink-500", 
    shadow: "shadow-purple-500/30" 
  },
  医療費: { 
    gradient: "from-red-400 to-rose-500", 
    shadow: "shadow-red-500/30" 
  },
  衣服費: { 
    gradient: "from-pink-400 to-rose-500", 
    shadow: "shadow-pink-500/30" 
  },
  日用品: { 
    gradient: "from-emerald-400 to-teal-500", 
    shadow: "shadow-emerald-500/30" 
  },
  投資: { 
    gradient: "from-amber-400 to-yellow-500", 
    shadow: "shadow-amber-500/30" 
  },
  教育費: { 
    gradient: "from-blue-500 to-indigo-600", 
    shadow: "shadow-blue-500/30" 
  },
  その他: { 
    gradient: "from-amber-400 to-orange-500", 
    shadow: "shadow-amber-500/30" 
  },
}

/**
 * カテゴリーに対応するグラデーションを取得
 */
export function getCategoryGradient(category: string): { gradient: string; shadow: string } {
  return CATEGORY_GRADIENTS[category] || { 
    gradient: "from-slate-400 to-slate-500", 
    shadow: "shadow-slate-500/30" 
  }
}
