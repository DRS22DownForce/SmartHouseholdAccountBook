"use client"

import { Card } from "@/components/ui/card"
import { cn } from "@/lib/utils"

interface QueryLoadingStateProps {
  /** card: Card 内の中央表示 / inline: コンパクトなスピナー */
  variant?: "card" | "inline"
  message?: string
  className?: string
  /** card 時の高さ（例: h-[500px]） */
  heightClass?: string
}

/**
 * データ取得中の共通ローディング表示。
 * TanStack Query の isLoaded === false 時に使う。
 */
export function QueryLoadingState({
  variant = "card",
  message = "データを読み込み中...",
  className,
  heightClass = "h-[200px]",
}: QueryLoadingStateProps) {
  const spinner = (
    <div className="flex flex-col items-center gap-3">
      <div className="relative w-12 h-12">
        <div className="absolute inset-0 rounded-full border-2 border-primary/20" />
        <div className="absolute inset-0 rounded-full border-2 border-primary border-t-transparent animate-spin" />
      </div>
      <p className="text-sm text-muted-foreground font-medium animate-pulse">
        {message}
      </p>
    </div>
  )

  if (variant === "inline") {
    return (
      <div
        className={cn(
          "flex items-center justify-center p-8 flex-col gap-3",
          className
        )}
      >
        <div className="w-6 h-6 rounded-full border-2 border-primary/30 border-t-primary animate-spin" />
        <p className="text-muted-foreground text-sm">{message}</p>
      </div>
    )
  }

  return (
    <Card
      className={cn(
        "border-border/40 shadow-sm bg-gradient-to-r from-card to-muted/10",
        className
      )}
    >
      <div
        className={cn(
          "flex items-center justify-center flex-col gap-4",
          heightClass
        )}
      >
        {spinner}
      </div>
    </Card>
  )
}
