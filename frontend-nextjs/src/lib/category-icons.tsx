import {
    Utensils,
    Home,
    Bus,
    Zap,
    HeartPulse,
    Gamepad2,
    MoreHorizontal,
    Wifi,
    ShoppingCart,
    Shirt,
    LucideIcon
} from "lucide-react"

/**
 * カテゴリー別のアイコン定義
 * 各カテゴリーに対応するアイコンを定義しています。
 * lucide-reactライブラリから提供されるアイコンを使用しています。
 */
export const CATEGORY_ICONS: Record<string, LucideIcon> = {
    食費: Utensils,
    交通費: Bus,
    住居費: Home,
    光熱費: Zap,
    通信費: Wifi,
    娯楽費: Gamepad2,
    医療費: HeartPulse,
    衣服費: Shirt,
    日用品: ShoppingCart,
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
