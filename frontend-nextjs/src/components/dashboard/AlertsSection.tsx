"use client"

/**
 * アラート・通知セクションコンポーネント
 * 
 * 重要な通知やアラートをリッチなカード形式で表示します。
 * 予算超過、目標達成、支出傾向などの情報をユーザーに通知します。
 * 
 * 【初心者向け解説】
 * - アラートには3種類あります: warning（警告）、success（成功）、info（情報）
 * - 各アラートは優先度に応じて色分けされています
 * - ホバー時にインタラクティブなフィードバックを提供します
 */

import { Card, CardContent } from "@/components/ui/card"
import { 
  AlertTriangle, 
  CheckCircle2, 
  TrendingDown, 
  Bell, 
  ChevronRight,
  Info,
  X
} from "lucide-react"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"

/**
 * アラートデータの型定義
 */
interface Alert {
  id: number
  type: "warning" | "success" | "info"
  icon: React.ElementType
  title: string
  message: string
  action?: string
  dismissible?: boolean
}

/**
 * アラートタイプに応じたスタイルを取得
 */
const getAlertStyle = (type: "warning" | "success" | "info") => {
  const styles = {
    warning: {
      iconBg: "bg-gradient-to-br from-amber-400 to-orange-500",
      titleColor: "text-amber-600 dark:text-amber-400",
      bgHover: "hover:bg-amber-500/5",
      border: "border-l-amber-500"
    },
    success: {
      iconBg: "bg-gradient-to-br from-emerald-400 to-green-500",
      titleColor: "text-emerald-600 dark:text-emerald-400",
      bgHover: "hover:bg-emerald-500/5",
      border: "border-l-emerald-500"
    },
    info: {
      iconBg: "bg-gradient-to-br from-blue-400 to-cyan-500",
      titleColor: "text-blue-600 dark:text-blue-400",
      bgHover: "hover:bg-blue-500/5",
      border: "border-l-blue-500"
    }
  }
  return styles[type]
}

export function AlertsSection() {
  // モックデータ（実際のロジックは将来的に実装）
  const mockAlerts: Alert[] = [
    {
      id: 1,
      type: "warning",
      icon: AlertTriangle,
      title: "予算超過の警告",
      message: "光熱費が予算を10%超過しています。今月の残り日数を考慮して支出を見直しましょう。",
      action: "詳細を確認",
      dismissible: true
    },
    {
      id: 2,
      type: "success",
      icon: CheckCircle2,
      title: "目標達成おめでとう！",
      message: "食費削減目標を達成しました！この調子で節約を続けましょう。",
      action: "達成記録を見る"
    },
    {
      id: 3,
      type: "info",
      icon: TrendingDown,
      title: "支出傾向のお知らせ",
      message: "今月の支出は前月比-5%です。良いペースで節約できています。",
    },
  ]

  // アラートがない場合は何も表示しない
  if (mockAlerts.length === 0) {
    return null
  }

  return (
    <div className="space-y-4">
      {/* セクションヘッダー */}
      <div className="flex items-center justify-between px-1">
        <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
          <div className="relative">
            {/* 通知があることを示すアニメーション */}
            <div className="absolute -top-1 -right-1 w-3 h-3 bg-rose-500 rounded-full animate-pulse" />
            <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-rose-400 to-pink-500 text-white shadow-lg">
              <Bell className="h-4 w-4" />
            </div>
          </div>
          お知らせ・アラート
        </h2>
        <span className="text-xs font-bold bg-primary/10 text-primary px-3 py-1 rounded-full">
          {mockAlerts.length} 件
        </span>
      </div>

      {/* アラートカード */}
      <Card className={cn(
        "relative overflow-hidden",
        "border-border/40 shadow-rich shadow-rich-hover",
        "bg-gradient-to-br from-card via-card to-rose-500/5",
        "transition-all duration-500 animate-fade-in"
      )}>
        {/* 背景装飾 */}
        <div className="absolute inset-0 pattern-dots pointer-events-none opacity-30" />

        <CardContent className="p-0 divide-y divide-border/40">
          {mockAlerts.map((alert, index) => {
            const Icon = alert.icon
            const style = getAlertStyle(alert.type)

            return (
              <div
                key={alert.id}
                className={cn(
                  "group relative flex items-start gap-4 p-4 md:p-5",
                  "transition-all duration-300",
                  "cursor-pointer",
                  style.bgHover,
                  // 左ボーダーでタイプを示す
                  "border-l-4",
                  style.border,
                  // スタガードアニメーション
                  "animate-fade-in",
                  `stagger-${Math.min(index + 1, 5)}`
                )}
              >
                {/* アイコン */}
                <div className={cn(
                  "flex-shrink-0 w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-lg",
                  "transform transition-all duration-300 group-hover:scale-110",
                  style.iconBg
                )}>
                  <Icon className="h-5 w-5" />
                </div>

                {/* コンテンツ */}
                <div className="flex-1 min-w-0 space-y-1">
                  <div className="flex items-center gap-2">
                    <h3 className={cn(
                      "text-sm font-bold",
                      style.titleColor
                    )}>
                      {alert.title}
                    </h3>
                  </div>
                  <p className="text-sm text-muted-foreground leading-relaxed group-hover:text-foreground/80 transition-colors">
                    {alert.message}
                  </p>
                  {alert.action && (
                    <span className="inline-flex items-center gap-1 text-xs font-bold text-primary mt-2 group-hover:underline">
                      {alert.action}
                      <ChevronRight className="w-3 h-3 transition-transform group-hover:translate-x-0.5" />
                    </span>
                  )}
                </div>

                {/* 閉じるボタン（dismissible な場合） */}
                {alert.dismissible && (
                  <Button
                    variant="ghost"
                    size="icon"
                    className="flex-shrink-0 w-8 h-8 opacity-0 group-hover:opacity-100 transition-opacity"
                  >
                    <X className="w-4 h-4" />
                  </Button>
                )}

                {/* ホバー時の矢印 */}
                {!alert.dismissible && (
                  <ChevronRight className="flex-shrink-0 w-5 h-5 text-muted-foreground/30 opacity-0 group-hover:opacity-100 transition-all group-hover:translate-x-0.5" />
                )}
              </div>
            )
          })}
        </CardContent>
      </Card>
    </div>
  )
}
