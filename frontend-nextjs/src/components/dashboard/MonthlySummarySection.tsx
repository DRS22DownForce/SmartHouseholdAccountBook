"use client"

/**
 * 月別サマリーセクションコンポーネント
 *
 * バックエンドAPIから月別サマリーを取得して表示します。
 * AIレポート機能付き：「AIレポート」ボタン押下後にカード内にインライン表示します。
 */

import { useState, useMemo, useEffect } from "react"
import { Card, CardContent } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Button } from "@/components/ui/button"
import { PieChart, Pie, Cell, ResponsiveContainer, Sector } from "recharts"
import { getCategoryColor } from "@/lib/category-colors"
import { formatCurrency, formatMonth, getCurrentMonthString } from "@/lib/formatters"
import { useMonthlySummary } from "@/hooks/use-monthly-summary"
import { useAvailableMonths } from "@/hooks/use-available-months"
import { useRefreshTrigger } from "@/hooks/use-refresh-trigger"
import { useMonthlyReport } from "@/hooks/use-monthly-report"
import { transformMonthlySummaryToChartData } from "@/lib/chart-data-transformers"
import {
  TrendingUp,
  Calendar,
  Wallet,
  Sparkles,
  PieChart as PieChartIcon,
  Target,
  FileText,
  Loader2,
  AlertCircle,
  CheckCircle2,
  RefreshCw,
  Clock,
} from "lucide-react"
import { cn } from "@/lib/utils"

interface SummarySectionProps {
  refreshTrigger?: number
}

/**
 * アクティブなセグメント（ホバー中）を描画するコンポーネント
 */
const renderActiveShape = (props: any) => {
  const {
    cx, cy, innerRadius, outerRadius, startAngle, endAngle,
    fill, payload,
  } = props

  return (
    <g>
      <Sector
        cx={cx}
        cy={cy}
        innerRadius={innerRadius - 4}
        outerRadius={outerRadius + 12}
        startAngle={startAngle}
        endAngle={endAngle}
        fill={fill}
        style={{
          filter: `drop-shadow(0 8px 24px ${payload.color}60)`,
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        }}
      />
      <Sector
        cx={cx}
        cy={cy}
        innerRadius={outerRadius + 14}
        outerRadius={outerRadius + 18}
        startAngle={startAngle}
        endAngle={endAngle}
        fill={payload.color}
        opacity={0.3}
      />
    </g>
  )
}

export function MonthlySummarySection({
  refreshTrigger,
}: SummarySectionProps) {
  const [selectedMonth, setSelectedMonth] = useState(() => getCurrentMonthString())
  const [activeIndex, setActiveIndex] = useState<number | undefined>(undefined)

  const { monthlySummary, isLoaded: isSummaryLoaded, fetchMonthlySummary } = useMonthlySummary(selectedMonth)
  const { availableMonths, isLoaded: isMonthsLoaded, fetchAvailableMonths } = useAvailableMonths()
  const { report, isLoading: isReportLoading, error: reportError, fetchReport, regenerateReport, loadCachedReport } = useMonthlyReport()

  // refreshTriggerが変化したときにデータを再取得
  useRefreshTrigger(refreshTrigger, fetchMonthlySummary, fetchAvailableMonths)

  // 月が変わったらDBキャッシュから自動ロード（キャッシュなければnullにリセット、OpenAI非呼び出し）
  useEffect(() => {
    loadCachedReport(selectedMonth)
  }, [selectedMonth, loadCachedReport])

  const chartData = useMemo(() => {
    if (!monthlySummary || !monthlySummary.byCategory) return []
    return transformMonthlySummaryToChartData(monthlySummary)
  }, [monthlySummary])

  const handleGenerateReport = () => {
    fetchReport(selectedMonth)
  }

  const handleRegenerateReport = () => {
    regenerateReport(selectedMonth)
  }

  // ローディング表示
  if (!isSummaryLoaded || !isMonthsLoaded) {
    return (
      <Card className={cn(
        "relative overflow-hidden",
        "border-border/40 shadow-rich",
        "bg-gradient-to-br from-card via-card to-indigo-500/5",
        "h-[500px]"
      )}>
        <div className="flex h-full items-center justify-center flex-col gap-4">
          <div className="relative w-12 h-12">
            <div className="absolute inset-0 rounded-full border-2 border-primary/20" />
            <div className="absolute inset-0 rounded-full border-2 border-primary border-t-transparent animate-spin" />
          </div>
          <p className="text-sm text-muted-foreground font-medium animate-pulse">
            データを読み込み中...
          </p>
        </div>
      </Card>
    )
  }

  const totalAmount = monthlySummary?.total ?? 0
  const totalCount = monthlySummary?.count ?? 0

  // レポートセクションの表示状態
  const showReportDefault = !report && !isReportLoading && !reportError
  const showReport = !!report && !isReportLoading

  return (
    <Card className={cn(
      "relative overflow-hidden",
      "border-border/40 shadow-rich shadow-rich-hover",
      "bg-gradient-to-br from-card via-card to-indigo-500/5",
      "transition-all duration-500"
    )}>
      {/* 背景装飾 */}
      <div className="absolute inset-0 pattern-dots pointer-events-none opacity-30" />
      <div className="absolute bottom-0 left-0 w-64 h-64 bg-gradient-to-tr from-purple-500/10 to-transparent rounded-full translate-y-1/2 -translate-x-1/2" />

      {/* ヘッダー */}
      <div className="relative px-6 py-5 border-b border-border/40">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="relative">
              <div className="absolute inset-0 bg-indigo-500/20 rounded-xl blur-md" />
              <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-400 to-purple-500 text-white shadow-lg">
                <Calendar className="h-5 w-5" />
              </div>
            </div>
            <div>
              <h2 className="text-lg font-black text-foreground tracking-tight">
                月別サマリー
              </h2>
              <p className="text-xs text-muted-foreground flex items-center gap-1">
                <Sparkles className="w-3 h-3" />
                選択した月の収支内訳と分析レポート
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <Select value={selectedMonth} onValueChange={setSelectedMonth}>
              <SelectTrigger className={cn(
                "w-full md:w-[180px] h-9 text-xs font-bold rounded-xl",
                "border-border/60 bg-background/50 backdrop-blur-sm",
                "hover:bg-background/80 transition-all",
                "focus:ring-2 focus:ring-primary/20 shadow-sm"
              )}>
                <SelectValue />
              </SelectTrigger>
              <SelectContent align="end" className="rounded-xl border-border/60 shadow-xl max-h-[300px]">
                {availableMonths.map((month) => (
                  <SelectItem
                    key={month}
                    value={month}
                    className="text-xs font-medium focus:bg-primary/10 rounded-lg cursor-pointer"
                  >
                    {formatMonth(month)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            {/* AIレポートボタン */}
            <Button
              variant="outline"
              size="sm"
              onClick={handleGenerateReport}
              disabled={totalCount === 0 || isReportLoading}
              className={cn(
                "h-9 gap-1.5 rounded-xl text-xs font-bold whitespace-nowrap",
                "border-indigo-500/30 text-indigo-600 dark:text-indigo-400",
                "hover:bg-indigo-500/10 hover:border-indigo-500/50",
                "transition-all duration-200",
                "disabled:opacity-40 disabled:cursor-not-allowed"
              )}
            >
              {isReportLoading
                ? <Loader2 className="h-3.5 w-3.5 animate-spin" />
                : <FileText className="h-3.5 w-3.5" />
              }
              AIレポート
            </Button>

            {/* 再生成ボタン（レポート表示中のみ） */}
            {report && !isReportLoading && (
              <Button
                variant="ghost"
                size="sm"
                onClick={handleRegenerateReport}
                disabled={totalCount === 0 || isReportLoading}
                className={cn(
                  "h-9 gap-1.5 rounded-xl text-xs font-bold whitespace-nowrap",
                  "text-muted-foreground hover:text-indigo-600 dark:hover:text-indigo-400",
                  "hover:bg-indigo-500/10",
                  "transition-all duration-200",
                  "disabled:opacity-40 disabled:cursor-not-allowed"
                )}
              >
                <RefreshCw className="h-3.5 w-3.5" />
                再生成
              </Button>
            )}
          </div>
        </div>
      </div>

      {/* メインコンテンツ */}
      <CardContent className="relative p-0">
        <div className="grid md:grid-cols-2 lg:grid-cols-3 divide-y md:divide-y-0 md:divide-x divide-border/40">

          {/* 左カラム: 総支出とトップカテゴリ */}
          <div className="p-6 space-y-6">
            {/* 総支出カード */}
            <div className={cn(
              "relative overflow-hidden rounded-2xl p-5",
              "bg-gradient-to-br from-indigo-500/10 via-purple-500/5 to-transparent",
              "border border-indigo-500/20",
              "group hover:border-indigo-500/40 transition-all duration-300"
            )}>
              <div className="absolute top-0 right-0 p-2 opacity-10 group-hover:opacity-20 transition-opacity">
                <Wallet className="w-24 h-24 -mr-6 -mt-6 rotate-12 text-indigo-500" />
              </div>

              <div className="relative">
                <div className="flex items-center gap-2 mb-3">
                  <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-indigo-400 to-purple-500 flex items-center justify-center text-white shadow-lg">
                    <Wallet className="w-4 h-4" />
                  </div>
                  <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
                    総支出額
                  </p>
                </div>

                <p className="text-3xl font-black text-foreground tracking-tight tabular-nums mb-3">
                  {formatCurrency(totalAmount)}
                </p>

                <div className="flex items-center gap-2">
                  <span className={cn(
                    "inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold",
                    "bg-indigo-500/10 text-indigo-600 dark:text-indigo-400",
                    "border border-indigo-500/20"
                  )}>
                    <TrendingUp className="w-3 h-3" />
                    {totalCount} 件の取引
                  </span>
                </div>
              </div>
            </div>

            {/* トップカテゴリ */}
            <div className="space-y-3">
              <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-purple-500" />
                支出内訳トップ3
              </p>
              <div className="space-y-2">
                {monthlySummary && monthlySummary.byCategory.slice(0, 3).map((item, i) => {
                  const percentage = totalAmount > 0 ? ((item.amount / totalAmount) * 100).toFixed(1) : "0"
                  const color = getCategoryColor(item.category)

                  return (
                    <div
                      key={item.category}
                      className={cn(
                        "group flex items-center justify-between p-3 rounded-xl",
                        "bg-muted/30 hover:bg-muted/50",
                        "border border-transparent hover:border-border/50",
                        "transition-all duration-200 cursor-default",
                        "animate-fade-in",
                        i === 0 ? "stagger-1" : i === 1 ? "stagger-2" : "stagger-3"
                      )}
                    >
                      <div className="flex items-center gap-3">
                        <div
                          className="w-2 h-10 rounded-full shadow-lg transition-transform group-hover:scale-y-110"
                          style={{
                            backgroundColor: color,
                            boxShadow: `0 0 10px ${color}40`
                          }}
                        />
                        <div className="space-y-0.5">
                          <span className="text-sm font-bold text-foreground">{item.category}</span>
                          <span className={cn(
                            "block text-[10px] font-bold px-2 py-0.5 rounded-full w-fit",
                            "bg-muted/50 text-muted-foreground"
                          )}>
                            {percentage}%
                          </span>
                        </div>
                      </div>
                      <span className="text-sm font-black tabular-nums text-foreground/80 group-hover:text-foreground">
                        {formatCurrency(item.amount)}
                      </span>
                    </div>
                  )
                })}
              </div>
            </div>
          </div>

          {/* 右カラム: ドーナツチャート */}
          <div className="md:col-span-1 lg:col-span-2 p-0">
            <div className="p-6 flex flex-col items-center justify-center relative min-h-[400px]">
              {chartData.length > 0 ? (
                <div className="relative w-full flex flex-col items-center gap-6">
                  {/* 円グラフ本体 */}
                  <div className="relative w-[280px] h-[280px]">
                    <div className="absolute inset-0 rounded-full bg-gradient-to-br from-muted/30 to-muted/10 blur-xl scale-110" />

                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <defs>
                          {chartData.map((entry, index) => (
                            <linearGradient
                              key={`gradient-pie-${index}`}
                              id={`gradient-pie-${index}`}
                              x1="0" y1="0" x2="1" y2="1"
                            >
                              <stop offset="0%" stopColor={entry.color} stopOpacity={1} />
                              <stop offset="100%" stopColor={entry.color} stopOpacity={0.75} />
                            </linearGradient>
                          ))}
                          <radialGradient id="centerGlow" cx="50%" cy="50%" r="50%">
                            <stop offset="0%" stopColor="hsl(var(--background))" stopOpacity={1} />
                            <stop offset="100%" stopColor="hsl(var(--muted))" stopOpacity={0.3} />
                          </radialGradient>
                        </defs>
                        <Pie
                          data={chartData}
                          cx="50%"
                          cy="50%"
                          innerRadius={75}
                          outerRadius={115}
                          paddingAngle={4}
                          dataKey="value"
                          activeIndex={activeIndex}
                          activeShape={renderActiveShape}
                          onMouseEnter={(_, index) => setActiveIndex(index)}
                          onMouseLeave={() => setActiveIndex(undefined)}
                          stroke="none"
                          cornerRadius={8}
                          animationBegin={0}
                          animationDuration={800}
                          animationEasing="ease-out"
                        >
                          {chartData.map((entry, index) => (
                            <Cell
                              key={`cell-${index}`}
                              fill={`url(#gradient-pie-${index})`}
                              className="cursor-pointer outline-none"
                              style={{
                                filter: activeIndex === undefined || activeIndex === index
                                  ? `drop-shadow(0 4px 12px ${entry.color}40)`
                                  : 'none',
                                opacity: activeIndex === undefined || activeIndex === index ? 1 : 0.4,
                                transition: 'opacity 0.3s ease, filter 0.3s ease',
                              }}
                            />
                          ))}
                        </Pie>
                      </PieChart>
                    </ResponsiveContainer>

                    {/* 中央表示 */}
                    <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                      <div className={cn(
                        "flex flex-col items-center justify-center",
                        "w-[130px] h-[130px] rounded-full",
                        "bg-gradient-to-br from-background via-background to-muted/20",
                        "shadow-inner border border-border/20",
                        "transition-all duration-300"
                      )}>
                        {activeIndex !== undefined && chartData[activeIndex] ? (
                          <>
                            <div
                              className="w-3 h-3 rounded-full mb-1.5 shadow-lg transition-transform animate-pulse"
                              style={{
                                backgroundColor: chartData[activeIndex].color,
                                boxShadow: `0 0 16px ${chartData[activeIndex].color}80`
                              }}
                            />
                            <span className="text-xs font-bold text-muted-foreground mb-0.5 truncate max-w-[100px]">
                              {chartData[activeIndex].name}
                            </span>
                            <span className="text-xl font-black text-foreground tabular-nums tracking-tight">
                              {formatCurrency(chartData[activeIndex].value)}
                            </span>
                            <span className="text-[10px] font-bold text-muted-foreground mt-0.5 px-2 py-0.5 rounded-full bg-muted/50">
                              シェア {totalAmount > 0
                                ? ((chartData[activeIndex].value / totalAmount) * 100).toFixed(1)
                                : "0"}%
                            </span>
                          </>
                        ) : (
                          <>
                            <Target className="w-4 h-4 text-muted-foreground mb-1" />
                            <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">
                              Total
                            </span>
                            <span className="text-2xl font-black text-foreground tabular-nums tracking-tight">
                              {formatCurrency(totalAmount)}
                            </span>
                          </>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* 凡例 */}
                  <div className="w-full">
                    <div className="flex items-center justify-center gap-2 px-2 flex-wrap">
                      {chartData.map((entry, index) => {
                        const isActive = activeIndex === index
                        const percentage = totalAmount > 0
                          ? ((entry.value / totalAmount) * 100).toFixed(0)
                          : "0"
                        return (
                          <button
                            key={`legend-${index}`}
                            className={cn(
                              "flex items-center gap-2 px-3 py-1.5 rounded-full",
                              "text-xs font-bold",
                              "border transition-all duration-200",
                              "focus:outline-none focus:ring-2 focus:ring-primary/20",
                              "flex-shrink-0",
                              isActive
                                ? "bg-foreground/5 border-border shadow-md scale-105"
                                : "bg-transparent border-transparent hover:bg-muted/50 hover:border-border/50"
                            )}
                            style={{
                              borderColor: isActive ? `${entry.color}40` : undefined,
                              boxShadow: isActive ? `0 4px 12px ${entry.color}20` : undefined,
                            }}
                            onMouseEnter={() => setActiveIndex(index)}
                            onMouseLeave={() => setActiveIndex(undefined)}
                          >
                            <div
                              className="w-2.5 h-2.5 rounded-full shadow-sm flex-shrink-0 transition-all duration-200"
                              style={{
                                backgroundColor: entry.color,
                                boxShadow: isActive ? `0 0 8px ${entry.color}80` : `0 0 4px ${entry.color}40`,
                                transform: isActive ? 'scale(1.2)' : 'scale(1)'
                              }}
                            />
                            <span className={cn(
                              "transition-colors duration-200",
                              isActive ? "text-foreground" : "text-muted-foreground"
                            )}>
                              {entry.name}
                            </span>
                            <span className={cn(
                              "tabular-nums transition-colors duration-200",
                              isActive ? "text-foreground" : "text-muted-foreground/70"
                            )}>
                              {percentage}%
                            </span>
                          </button>
                        )
                      })}
                    </div>
                  </div>
                </div>
              ) : (
                <div className="flex h-[320px] items-center justify-center flex-col gap-4">
                  <div className="relative">
                    <div className="absolute inset-0 bg-muted/20 rounded-full blur-xl animate-pulse" />
                    <div className="relative w-24 h-24 rounded-full bg-gradient-to-br from-muted/30 to-muted/10 flex items-center justify-center border border-border/20">
                      <PieChartIcon className="w-10 h-10 text-muted-foreground/40" />
                    </div>
                  </div>
                  <div className="text-center space-y-1">
                    <p className="text-sm font-bold text-foreground/80">データがありません</p>
                    <p className="text-xs text-muted-foreground max-w-[200px]">
                      この月の支出データはまだ登録されていません
                    </p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* AIレポートセクション */}
        <div className="relative border-t border-border/40 px-6 py-5">
          {/* セクションヘッダー */}
          <div className="flex items-center gap-2 mb-4">
            <div className="relative">
              <div className="absolute inset-0 bg-indigo-500/20 rounded-lg blur-md" />
              <div className="relative flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-indigo-400 to-purple-500 text-white shadow-lg">
                <FileText className="h-3.5 w-3.5" />
              </div>
            </div>
            <p className="text-sm font-black text-foreground tracking-tight">AI改善レポート</p>
            <span className={cn(
              "inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold",
              "bg-indigo-500/10 text-indigo-600 dark:text-indigo-400",
              "border border-indigo-500/20"
            )}>
              <Sparkles className="w-2.5 h-2.5" />
              AI Powered
            </span>
          </div>

          {/* デフォルト表示（未生成） */}
          {showReportDefault && (
            <div className={cn(
              "flex flex-col items-center justify-center py-8 gap-3",
              "rounded-2xl border border-dashed border-border/60",
              "bg-muted/20"
            )}>
              <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-indigo-500/10 to-purple-500/10 flex items-center justify-center border border-indigo-500/20">
                <FileText className="w-6 h-6 text-indigo-400" />
              </div>
              <div className="text-center">
                <p className="text-sm font-bold text-foreground/70">
                  AIレポートを生成しましょう
                </p>
                <p className="text-xs text-muted-foreground mt-0.5">
                  上の「AIレポート」ボタンを押すと、この月の改善提案が表示されます
                </p>
              </div>
            </div>
          )}

          {/* ローディング */}
          {isReportLoading && (
            <div className="flex flex-col items-center justify-center py-8 gap-3">
              <div className="relative">
                <div className="absolute inset-0 bg-indigo-500/10 rounded-full blur-xl animate-pulse" />
                <div className="relative w-12 h-12 rounded-full bg-gradient-to-br from-indigo-500/10 to-purple-500/10 flex items-center justify-center border border-indigo-500/20">
                  <Loader2 className="h-6 w-6 text-indigo-500 animate-spin" />
                </div>
              </div>
              <div className="text-center">
                <p className="text-sm font-bold text-foreground">AIが分析中...</p>
                <p className="text-xs text-muted-foreground mt-0.5">支出データをもとに改善提案を作成しています</p>
              </div>
            </div>
          )}

          {/* エラー */}
          {!isReportLoading && reportError && (
            <div className={cn(
              "flex items-start gap-3 p-4 rounded-xl",
              "bg-destructive/10 border border-destructive/20"
            )}>
              <AlertCircle className="h-5 w-5 text-destructive flex-shrink-0 mt-0.5" />
              <p className="text-sm text-destructive font-medium">{reportError}</p>
            </div>
          )}

          {/* レポート表示 */}
          {showReport && (
            <div className="grid md:grid-cols-2 gap-4">
              {/* 総評 */}
              <div className={cn(
                "p-4 rounded-xl",
                "bg-gradient-to-br from-indigo-500/5 to-purple-500/5",
                "border border-indigo-500/15"
              )}>
                <div className="flex items-center justify-between mb-2">
                  <p className="text-[10px] font-bold text-indigo-600 dark:text-indigo-400 uppercase tracking-wider flex items-center gap-1.5">
                    <Sparkles className="h-3 w-3" />
                    今月の総評
                  </p>
                </div>
                <p className="text-sm text-foreground leading-relaxed">{report.summary}</p>
              </div>

              {/* 改善提案 */}
              <div className="space-y-2">
                <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                  <CheckCircle2 className="h-3 w-3 text-emerald-500" />
                  改善提案
                </p>
                <ul className="space-y-1.5">
                  {report.suggestions.map((suggestion, index) => (
                    <li
                      key={index}
                      className={cn(
                        "flex items-start gap-2.5 p-2.5 rounded-xl",
                        "bg-muted/40 hover:bg-muted/60",
                        "border border-transparent hover:border-border/40",
                        "transition-all duration-200"
                      )}
                    >
                      <span className={cn(
                        "flex-shrink-0 w-4 h-4 rounded-full flex items-center justify-center mt-0.5",
                        "bg-gradient-to-br from-emerald-400 to-teal-500",
                        "text-white text-[9px] font-black shadow-sm"
                      )}>
                        {index + 1}
                      </span>
                      <p className="text-xs text-foreground leading-relaxed">{suggestion}</p>
                    </li>
                  ))}
                </ul>
              </div>

              <div className="md:col-span-2 flex items-center justify-between">
                {report.generatedAt && (
                  <span className="flex items-center gap-1 text-[10px] text-muted-foreground/60">
                    <Clock className="h-3 w-3" />
                    生成日時: {new Date(report.generatedAt).toLocaleString("ja-JP", {
                      year: "numeric", month: "2-digit", day: "2-digit",
                      hour: "2-digit", minute: "2-digit"
                    })}
                  </span>
                )}
                <p className="text-[10px] text-muted-foreground/50 text-right">
                  AIによる提案のため、実際の状況と異なる場合があります。
                </p>
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
