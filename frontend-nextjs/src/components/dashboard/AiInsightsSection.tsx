"use client"

/**
 * AI家計診断セクションコンポーネント
 * 
 * AIによる家計分析結果とアドバイスを視覚的にリッチに表示します。
 * グラスモーフィズム効果とアニメーションを使用して、
 * ユーザーに洗練された体験を提供します。
 * 
 * 【初心者向け解説】
 * - グラスモーフィズム: 半透明でぼかしのかかったガラスのような効果
 * - アニメーション: 要素が動いて見える視覚効果
 * - グラデーション: 2色以上が滑らかに変化する背景色
 */

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { 
  Sparkles, 
  TrendingUp, 
  TrendingDown, 
  Lightbulb, 
  ChevronRight,
  ArrowUpRight,
  Utensils,
  Zap,
  PiggyBank,
  Target
} from "lucide-react"
import { cn } from "@/lib/utils"

/**
 * AI分析の傾向データの型定義
 */
interface TrendItem {
  category: string           // カテゴリ名（例: 食費、光熱費）
  icon: React.ElementType    // 表示アイコン
  change: number            // 変化率（%）
  trend: "up" | "down"      // 上昇か下降か
  description: string       // 説明文
  color: string            // テーマカラー
}

/**
 * AIアドバイスデータの型定義
 */
interface AdviceItem {
  id: number
  icon: React.ElementType
  title: string
  content: string
  priority: "high" | "medium" | "low"
  action?: string
}

export function AiInsightsSection() {
  // モックデータ: 実際のアプリではAPIから取得します
  // 今月の傾向分析データ
  const mockTrends: TrendItem[] = [
    {
      category: "食費",
      icon: Utensils,
      change: 15,
      trend: "up",
      description: "外食の回数が増加",
      color: "rose"
    },
    {
      category: "光熱費",
      icon: Zap,
      change: 5,
      trend: "down",
      description: "節約に成功！",
      color: "emerald"
    },
  ]

  // AIからのアドバイス
  const mockAdvices: AdviceItem[] = [
    {
      id: 1,
      icon: PiggyBank,
      title: "自炊のすすめ",
      content: "来週は自炊の頻度を週2回増やすことで、目標予算内に収めることが可能です。",
      priority: "high",
      action: "詳細なレシピを見る"
    },
    {
      id: 2,
      icon: Target,
      title: "セール活用",
      content: "お近くのスーパーで週末セールが開催されます。食費削減のチャンスです！",
      priority: "medium",
      action: "セール情報を確認"
    },
  ]

  return (
    <div className="space-y-4">
      {/* セクションヘッダー - AIの特別感を演出 */}
      <div className="flex items-center justify-between px-1">
        <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
          {/* グローイングアイコン - AIらしさを表現 */}
          <div className="relative">
            <div className="absolute inset-0 bg-blue-500/20 rounded-lg blur-md animate-pulse-glow" />
            <div className="relative flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-blue-500 to-purple-600 text-white">
              <Sparkles className="h-4 w-4" />
            </div>
          </div>
          AI家計診断
        </h2>
        <Button 
          variant="ghost" 
          size="sm" 
          className="text-xs font-bold text-primary hover:text-primary/80 gap-1 group"
        >
          詳細な分析レポート
          <ArrowUpRight className="w-3.5 h-3.5 transition-transform group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
        </Button>
      </div>

      {/* メインカード - グラスモーフィズムとグラデーション */}
      <Card className={cn(
        // ベーススタイル
        "relative overflow-hidden",
        // ボーダーとシャドウ
        "border-border/40 shadow-rich shadow-rich-hover",
        // グラデーション背景
        "bg-gradient-to-br from-card via-card to-blue-500/5",
        // トランジション
        "transition-all duration-500"
      )}>
        {/* 背景装飾 - 微妙なパターン */}
        <div className="absolute inset-0 pattern-dots pointer-events-none" />
        
        {/* グラデーションオーバーレイ */}
        <div className="absolute top-0 right-0 w-1/2 h-full bg-gradient-to-l from-purple-500/5 to-transparent pointer-events-none" />

        <CardContent className="p-0">
          {/* グリッドレイアウト - 分割線を使用したデザイン */}
          <div className="grid md:grid-cols-2 lg:grid-cols-3 divide-y md:divide-y-0 md:divide-x divide-border/40">
            
            {/* 左側: 今月の傾向 */}
            <div className="md:col-span-1 lg:col-span-2 p-0">
              <div className="grid grid-cols-1 lg:grid-cols-2 h-full divide-y lg:divide-y-0 lg:divide-x divide-border/40">
                
                {/* 傾向カード1: 支出増加項目 */}
                {mockTrends.map((trend, index) => (
                  <div 
                    key={trend.category}
                    className={cn(
                      "p-6 flex flex-col justify-center",
                      trend.trend === "up" ? "bg-rose-500/5" : "bg-emerald-500/5",
                      // スタガードアニメーション
                      "animate-fade-in",
                      index === 0 ? "stagger-1" : "stagger-2"
                    )}
                  >
                    <div className="flex items-start gap-4">
                      {/* アイコンコンテナ */}
                      <div className={cn(
                        "flex-shrink-0 w-12 h-12 rounded-2xl flex items-center justify-center",
                        "transition-transform hover:scale-110",
                        trend.trend === "up" 
                          ? "bg-rose-500/10 text-rose-500" 
                          : "bg-emerald-500/10 text-emerald-500"
                      )}>
                        <trend.icon className="w-6 h-6" />
                      </div>

                      <div className="flex-1 space-y-2">
                        {/* カテゴリ名とトレンドバッジ */}
                        <div className="flex items-center gap-2">
                          <h3 className="font-bold text-foreground">{trend.category}</h3>
                          <span className={cn(
                            "text-xs font-bold px-2 py-0.5 rounded-full flex items-center gap-1",
                            trend.trend === "up"
                              ? "bg-rose-500/10 text-rose-600 dark:text-rose-400"
                              : "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
                          )}>
                            {trend.trend === "up" ? (
                              <TrendingUp className="w-3 h-3" />
                            ) : (
                              <TrendingDown className="w-3 h-3" />
                            )}
                            {trend.change}%
                          </span>
                        </div>

                        {/* 説明文 */}
                        <p className="text-sm text-muted-foreground">
                          前月と比較して{trend.trend === "up" ? "約" : ""}
                          <span className={cn(
                            "font-bold",
                            trend.trend === "up" ? "text-rose-600 dark:text-rose-400" : "text-emerald-600 dark:text-emerald-400"
                          )}>
                            {trend.change}%{trend.trend === "up" ? "増加" : "減少"}
                          </span>
                          しています。
                        </p>
                        <p className="text-xs text-muted-foreground/80">
                          {trend.description}
                          {trend.trend === "down" && " 素晴らしいです！"}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* 右側: AIアドバイス */}
            <div className="p-6 flex flex-col justify-center bg-muted/5 animate-fade-in stagger-3">
              <div className="space-y-4">
                {/* アドバイスヘッダー */}
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center text-white shadow-lg animate-float">
                    <Lightbulb className="w-4 h-4" />
                  </div>
                  <h3 className="font-bold text-foreground">アドバイス</h3>
                </div>

                {/* アドバイスリスト */}
                <div className="space-y-3">
                  {mockAdvices.map((advice, index) => (
                    <div 
                      key={advice.id}
                      className={cn(
                        "group p-3 rounded-xl transition-all duration-300",
                        "bg-background/50 hover:bg-background",
                        "border border-transparent hover:border-border/50",
                        "cursor-pointer"
                      )}
                    >
                      <div className="flex items-start gap-3">
                        <div className={cn(
                          "flex-shrink-0 w-8 h-8 rounded-lg flex items-center justify-center",
                          "bg-primary/10 text-primary",
                          "transition-transform group-hover:scale-110"
                        )}>
                          <advice.icon className="w-4 h-4" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <h4 className="text-sm font-bold text-foreground truncate">
                            {advice.title}
                          </h4>
                          <p className="text-xs text-muted-foreground mt-1 line-clamp-2">
                            {advice.content}
                          </p>
                          {advice.action && (
                            <span className="inline-flex items-center gap-1 text-xs font-bold text-primary mt-2 group-hover:underline">
                              {advice.action}
                              <ChevronRight className="w-3 h-3 transition-transform group-hover:translate-x-0.5" />
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </CardContent>

        {/* フッター: 注意書き */}
        <div className="px-6 py-3 bg-muted/10 border-t border-border/40">
          <p className="text-[10px] text-muted-foreground/60 flex items-center gap-1">
            <Sparkles className="w-3 h-3" />
            これはAIによる自動生成された分析のサンプルです
          </p>
        </div>
      </Card>
    </div>
  )
}

