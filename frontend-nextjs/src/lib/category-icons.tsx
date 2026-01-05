import {
    Utensils,
    Home,
    Bus,
    Zap,
    HeartPulse,
    Gamepad2,
    MoreHorizontal,
    LucideIcon
} from "lucide-react"

/**
 * カテゴリー別のアイコン定義
 */
export const CATEGORY_ICONS: Record<string, LucideIcon> = {
    食費: Utensils,
    住居費: Home,
    交通費: Bus,
    光熱費: Zap,
    医療費: HeartPulse,
    娯楽: Gamepad2,
    その他: MoreHorizontal,
}

/**
 * カテゴリーに対応するアイコンを取得する
 * 
 * @param category - カテゴリー名
 * @returns LucideIcon コンポーネント
 */
export function getCategoryIcon(category: string): LucideIcon {
    return CATEGORY_ICONS[category] || MoreHorizontal
}
