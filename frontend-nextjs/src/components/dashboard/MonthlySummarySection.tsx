"use client"

/**
 * 月別サマリーセクションコンポーネント
 * 
 * このコンポーネントは、バックエンドAPIから月別サマリーを取得して表示します。
 * フロントエンドでの計算を削減し、通信量を最適化するために使用します。
 */

import { useState, useMemo, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from "recharts"
import { getCategoryColor } from "@/lib/category-colors"
import { formatCurrency, formatMonth, getCurrentMonthString } from "@/lib/formatters"
import { useMonthlySummary } from "@/hooks/use-monthly-summary"
import { useAvailableMonths } from "@/hooks/use-available-months"
import { transformMonthlySummaryToChartData } from "@/lib/chart-data-transformers"
import { TrendingDown, TrendingUp, Calendar, Wallet, Sparkles } from "lucide-react"
import { Button } from "@/components/ui/button"

interface SummarySectionProps {
  refreshTrigger?: number // 支出追加後に再取得するためのトリガー
}

export function MonthlySummarySection({
  refreshTrigger,
}: SummarySectionProps) {
  const [selectedMonth, setSelectedMonth] = useState(() => getCurrentMonthString())
  const [activeIndex, setActiveIndex] = useState<number | undefined>(undefined)

  // バックエンドAPIから月別サマリーを取得
  const { monthlySummary, isLoaded: isSummaryLoaded, fetchMonthlySummary } = useMonthlySummary(selectedMonth)

  // バックエンドAPIから利用可能な月のリストを取得
  const { availableMonths, isLoaded: isMonthsLoaded, fetchAvailableMonths } = useAvailableMonths()

  // refreshTriggerが変更されたときに再取得
  useEffect(() => {
    if (refreshTrigger !== undefined && refreshTrigger > 0) {
      fetchMonthlySummary()
      fetchAvailableMonths()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [refreshTrigger])

  // チャート用のデータを準備
  const chartData = useMemo(() => {
    // データがない場合は空配列を返す
    if (!monthlySummary || !monthlySummary.byCategory) return []

    // 金額が大きい順にソートし、その他をまとめる処理などはtransformMonthlySummaryToChartDataで行われている前提
    return transformMonthlySummaryToChartData(monthlySummary)
  }, [monthlySummary])

  // 読み込み中の表示
  if (!isSummaryLoaded || !isMonthsLoaded) {
    return (
      <Card className="border-border/40 shadow-sm bg-gradient-to-br from-card to-muted/10 h-[400px]">
        <div className="flex h-full items-center justify-center flex-col gap-4">
          <div className="w-8 h-8 rounded-full border-2 border-primary/30 border-t-primary animate-spin" />
          <p className="text-muted-foreground text-sm font-medium animate-pulse">データを読み込み中...</p>
        </div>
      </Card>
    )
  }

  // 合計金額の計算（バックエンドから来るが、チャート表示用に念のため）
  const totalAmount = monthlySummary?.total ?? 0
  const totalCount = monthlySummary?.count ?? 0

  return (
    <Card className="border-border/40 shadow-sm hover:shadow-md transition-all duration-300 bg-gradient-to-br from-card to-muted/10 overflow-hidden">
      <CardHeader className="pb-4 border-b border-border/40 bg-muted/5">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="space-y-1">
            <CardTitle className="text-xl font-bold tracking-tight flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-500/10 text-blue-500 shadow-sm">
                <Calendar className="w-5 h-5" />
              </div>
              <span className="bg-gradient-to-br from-foreground to-muted-foreground bg-clip-text text-transparent">
                月別サマリー
              </span>
            </CardTitle>
            <p className="text-xs text-muted-foreground font-medium ml-1">
              選択した月の収支内訳と分析レポート
            </p>
          </div>
          <Select value={selectedMonth} onValueChange={setSelectedMonth}>
            <SelectTrigger className="w-full md:w-[200px] h-9 text-xs font-medium rounded-full border-border/60 bg-background/50 backdrop-blur-sm hover:bg-background/80 transition-all focus:ring-1 focus:ring-primary/20 shadow-sm">
              <SelectValue />
            </SelectTrigger>
            <SelectContent align="end" className="rounded-xl border-border/60 shadow-xl max-h-[300px]">
              {availableMonths.map((month) => (
                <SelectItem key={month} value={month} className="text-xs focus:bg-primary/10 rounded-lg cursor-pointer">
                  {formatMonth(month)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </CardHeader>

      <CardContent className="p-0">
        <div className="grid md:grid-cols-2 lg:grid-cols-3 divide-y md:divide-y-0 md:divide-x divide-border/40">

          {/* 左カラム: 合計と主要ステータス */}
          <div className="p-6 flex flex-col justify-center space-y-6 bg-gradient-to-br from-primary/5 via-transparent to-transparent">
            {/* 総支出カード */}
            <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-card to-background border border-border/60 p-6 shadow-sm group hover:shadow-lg transition-all duration-300">
              <div className="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity">
                <Wallet className="w-32 h-32 -mr-10 -mt-10 rotate-12 text-primary" />
              </div>

              <div className="relative z-10">
                <div className="flex items-center gap-2 mb-3">
                  <div className="p-2 rounded-full bg-primary/10 text-primary">
                    <Wallet className="w-4 h-4" />
                  </div>
                  <p className="text-sm font-semibold text-muted-foreground">総支出額</p>
                </div>

                <div className="flex items-baseline gap-1">
                  <span className="text-4xl font-extrabold tracking-tight text-foreground">
                    {formatCurrency(totalAmount)}
                  </span>
                </div>

                <div className="mt-4 inline-flex items-center gap-2 text-xs font-medium text-primary bg-primary/10 px-3 py-1.5 rounded-full border border-primary/10">
                  <TrendingUp className="w-3 h-3" />
                  <span>合計 {totalCount} 件の取引</span>
                </div>
              </div>
            </div>

            {/* トップカテゴリー */}
            <div className="space-y-3 pt-2">
              <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider pl-1 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-primary/50" />
                支出内訳トップ3
              </p>
              <div className="space-y-2">
                {monthlySummary && monthlySummary.byCategory.slice(0, 3).map((item, i) => (
                  <div
                    key={item.category}
                    className="group flex items-center justify-between p-2.5 rounded-xl bg-card/40 hover:bg-card border border-transparent hover:border-border/50 hover:shadow-sm transition-all duration-200"
                  >
                    <div className="flex items-center gap-3">
                      <div
                        className="w-1.5 h-8 rounded-full shadow-sm group-hover:scale-y-110 transition-transform"
                        style={{ backgroundColor: getCategoryColor(item.category) }}
                      />
                      <div className="flex flex-col gap-0.5">
                        <span className="text-xs font-bold text-foreground/90">{item.category}</span>
                        <span className="text-[10px] text-muted-foreground font-medium bg-muted/50 px-1.5 py-0.5 rounded-md w-fit">
                          {((item.amount / totalAmount) * 100).toFixed(1)}%
                        </span>
                      </div>
                    </div>
                    <span className="text-sm font-bold font-mono tracking-tight text-foreground/80 group-hover:text-foreground">
                      {formatCurrency(item.amount)}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* 中・右カラム: チャートとAI分析 */}
          <div className="md:col-span-1 lg:col-span-2 p-0">
            <div className="grid grid-cols-1 lg:grid-cols-2 h-full divide-y lg:divide-y-0 lg:divide-x divide-border/40">

              {/* 中央: ドーナツチャート */}
              <div className="p-6 flex flex-col items-center justify-center relative min-h-[300px]">
                {chartData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={chartData}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={90}
                        paddingAngle={2}
                        dataKey="value"
                        onMouseEnter={(_, index) => setActiveIndex(index)}
                        onMouseLeave={() => setActiveIndex(undefined)}
                        stroke="none"
                        cornerRadius={4}
                      >
                        {chartData.map((entry, index) => (
                          <Cell
                            key={`cell-${index}`}
                            fill={entry.color}
                            stroke="none"
                            className="transition-all duration-300 hover:opacity-90 cursor-pointer"
                            style={{
                              filter: activeIndex === index ? `drop-shadow(0 0 10px ${entry.color}70)` : `drop-shadow(0 0 2px ${entry.color}20)`,
                              transform: activeIndex === index ? 'scale(1.03)' : 'scale(1)',
                              transformOrigin: 'center center',
                              outline: 'none'
                            }}
                          />
                        ))}
                      </Pie>
                      <Tooltip
                        content={({ active, payload }) => {
                          if (active && payload && payload.length) {
                            const data = payload[0].payload;
                            return (
                              <div className="bg-popover/95 backdrop-blur-md border border-border/50 rounded-xl shadow-2xl p-4 text-xs animate-in fade-in-0 zoom-in-95">
                                <div className="flex items-center gap-2 mb-2">
                                  <div className="w-3 h-3 rounded-full shadow-sm ring-2 ring-transparent" style={{ backgroundColor: data.color, boxShadow: `0 0 8px ${data.color}80` }} />
                                  <span className="font-bold text-sm">{data.name}</span>
                                </div>
                                <div className="text-xl font-bold font-mono tracking-tight mb-1">
                                  {formatCurrency(data.value)}
                                </div>
                                <div className="flex items-center gap-2">
                                  <span className="text-[10px] text-muted-foreground bg-muted/50 px-1.5 py-0.5 rounded-full">
                                    シェア {((data.value / totalAmount) * 100).toFixed(1)}%
                                  </span>
                                </div>
                              </div>
                            )
                          }
                          return null;
                        }}
                      />
                      <Legend
                        width={150}
                        layout="vertical"
                        verticalAlign="middle"
                        align="right"
                        content={({ payload }) => (
                          <div className="flex flex-col gap-1.5 max-h-[260px] overflow-y-auto pr-2 custom-scrollbar ml-2">
                            {payload?.map((entry: any, index) => (
                              <div
                                key={`legend-${index}`}
                                className={`flex items-center justify-between gap-2 text-xs p-1.5 rounded-lg transition-all cursor-pointer border border-transparent ${activeIndex === index ? 'bg-muted shadow-sm border-border/50 scale-105' : 'hover:bg-muted/50'}`}
                                onMouseEnter={() => setActiveIndex(index)}
                                onMouseLeave={() => setActiveIndex(undefined)}
                              >
                                <div className="flex items-center gap-2">
                                  <div className="w-2 h-2 rounded-full shadow-sm flex-shrink-0" style={{ backgroundColor: entry.color }} />
                                  <span className={`font-medium truncate max-w-[60px] ${activeIndex === index ? 'text-foreground font-bold' : 'text-muted-foreground'}`}>
                                    {entry.value}
                                  </span>
                                </div>
                                <span className="font-mono font-semibold opacity-80">
                                  {((entry.payload.value / totalAmount) * 100).toFixed(0)}%
                                </span>
                              </div>
                            ))}
                          </div>
                        )}
                      />
                      <text x="50%" y="50%" textAnchor="middle" dominantBaseline="middle">
                        <tspan x="50%" dy="-10" fontSize="10" fill="hsl(var(--muted-foreground))" fontWeight="600" letterSpacing="0.05em">
                          TOTAL
                        </tspan>
                        <tspan x="50%" dy="22" fontSize="18" fill="hsl(var(--foreground))" fontWeight="800" letterSpacing="-0.02em">
                          {formatCurrency(totalAmount)}
                        </tspan>
                      </text>
                    </PieChart>
                  </ResponsiveContainer>
                ) : (
                  <div className="flex h-[300px] items-center justify-center flex-col gap-4">
                    <div className="p-6 bg-muted/20 rounded-full border border-border/20">
                      <TrendingDown className="w-10 h-10 text-muted-foreground/30" />
                    </div>
                    <div className="text-center space-y-1">
                      <p className="text-sm text-foreground/80 font-medium">データがありません</p>
                      <p className="text-xs text-muted-foreground">この月の支出データはまだ登録されていません</p>
                    </div>
                  </div>
                )}
              </div>

              {/* 右: AI分析レポート (モック) */}
              <div className="p-6 flex flex-col justify-center bg-muted/5">
                <div className="space-y-4">
                  <div className="flex items-center gap-2 mb-2">
                    <div className="p-2 rounded-lg bg-purple-500/10 text-purple-600">
                      <Sparkles className="w-4 h-4" />
                    </div>
                    <h3 className="font-bold text-foreground">AI家計診断</h3>
                  </div>

                  <div className="space-y-3">
                    <div className="p-4 rounded-xl bg-card border border-border/50 shadow-sm">
                      <p className="text-sm leading-relaxed text-muted-foreground">
                        <span className="font-bold text-foreground">今月の傾向: </span>
                        食費が先月と比較して<span className="text-red-500 font-bold">約15%増加</span>しています。外食の回数が増えているようです。一方で、光熱費は節約できており<span className="text-green-500 font-bold">5%減少</span>しました素晴らしいです！
                      </p>
                    </div>

                    <div className="p-4 rounded-xl bg-card border border-border/50 shadow-sm">
                      <p className="text-sm leading-relaxed text-muted-foreground">
                        <span className="font-bold text-foreground">アドバイス: </span>
                        来週は自炊の頻度を週2回増やすことで、目標予算内に収めることが可能です。スーパーのセール情報を活用しましょう。
                      </p>
                    </div>
                  </div>

                  <div className="pt-2">
                    <Button className="w-full gap-2 bg-purple-600 hover:bg-purple-700 text-white shadow-md shadow-purple-500/20 rounded-xl transition-all hover:scale-[1.02]">
                      <Sparkles className="w-4 h-4" />
                      詳細な分析レポートを見る
                    </Button>
                    <p className="text-[10px] text-center text-muted-foreground mt-2">
                      ※ これはAIによる自動生成された分析のサンプルです
                    </p>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}

