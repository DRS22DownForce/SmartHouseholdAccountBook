"use client"

/**
 * アラート・通知セクションコンポーネント
 * 
 * 重要な通知やアラートをコンパクトにまとめて表示します。
 */

import { Card, CardContent } from "@/components/ui/card"
import { AlertTriangle, CheckCircle2, TrendingDown, Bell, ChevronRight } from "lucide-react"
import { cn } from "@/lib/utils"

export function AlertsSection() {
  // モックデータ（実際のロジックは将来的に実装）
  const mockAlerts = [
    {
      id: 1,
      type: "warning",
      icon: AlertTriangle,
      title: "予算超過",
      message: "光熱費が予算を10%超過しています",
    },
    {
      id: 2,
      type: "success",
      icon: CheckCircle2,
      title: "目標達成",
      message: "食費削減目標を達成しました！",
    },
    {
      id: 3,
      type: "info",
      icon: TrendingDown,
      title: "支出傾向",
      message: "今月の支出は前月比-5%です",
    },
  ]

  if (mockAlerts.length === 0) {
    return null
  }

  return (
    <Card className="border-border/40 shadow-sm overflow-hidden bg-gradient-to-br from-card to-muted/5 transition-all duration-300">
      <div className="px-4 py-3 border-b border-border/40 flex items-center justify-between bg-muted/5">
        <h3 className="text-sm font-bold flex items-center gap-2 text-foreground/80">
          <Bell className="w-4 h-4 text-primary" />
          お知らせ・アラート
        </h3>
        <span className="text-[10px] font-bold bg-primary/10 text-primary px-2 py-0.5 rounded-full">
          {mockAlerts.length} 件
        </span>
      </div>
      <CardContent className="p-0 divide-y divide-border/40">
        {mockAlerts.map((alert) => {
          const Icon = alert.icon
          return (
            <div
              key={alert.id}
              className={cn(
                "group relative flex items-center gap-4 p-4 transition-all duration-200 hover:bg-muted/10 cursor-default",
                alert.type === "warning" && "hover:bg-destructive/5"
              )}
            >
              <div className={cn(
                "flex-shrink-0 w-10 h-10 rounded-xl flex items-center justify-center shadow-sm transition-transform group-hover:scale-105",
                alert.type === "warning" ? "bg-destructive/10 text-destructive" :
                  alert.type === "success" ? "bg-primary/10 text-primary" : "bg-primary/10 text-primary"
              )}>
                <Icon className="h-5 w-5" />
              </div>

              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-0.5">
                  <span className={cn(
                    "text-xs font-bold leading-none",
                    alert.type === "warning" ? "text-destructive" :
                      alert.type === "success" ? "text-primary" : "text-foreground"
                  )}>
                    {alert.title}
                  </span>
                </div>
                <p className="text-sm text-muted-foreground line-clamp-1 group-hover:text-foreground/80 transition-colors tracking-tight">
                  {alert.message}
                </p>
              </div>

              <ChevronRight className="w-4 h-4 text-muted-foreground/30 group-hover:text-primary transition-all group-hover:translate-x-0.5" />
            </div>
          )
        })}
      </CardContent>
    </Card>
  )
}

