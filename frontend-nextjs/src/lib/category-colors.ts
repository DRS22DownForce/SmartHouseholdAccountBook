export const CATEGORY_COLORS: Record<string, string> = {
  食費: "#FF6B35", // Orange
  交通費: "#4A90E2", // Blue
  娯楽: "#9B59B6", // Purple
  光熱費: "#2ECC71", // Green
  医療費: "#E74C3C", // Red
  その他: "#F39C12", // Yellow
}

const DEFAULT_COLORS = [
  "#FF6B35", // Orange
  "#4A90E2", // Blue
  "#9B59B6", // Purple
  "#2ECC71", // Green
  "#E74C3C", // Red
  "#F39C12", // Yellow
  "#1ABC9C", // Teal
  "#E67E22", // Dark Orange
]

export function getCategoryColor(category: string, index = 0): string {
  return CATEGORY_COLORS[category] || DEFAULT_COLORS[index % DEFAULT_COLORS.length]
}
