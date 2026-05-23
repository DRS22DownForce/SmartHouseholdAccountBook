"use client"

import { Card, CardContent } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Button } from "@/components/ui/button"
import { formatMonth } from "@/lib/formatters"
import { useMonthlySummarySection } from "@/hooks/use-monthly-summary-section"
import { MonthlySummaryPieChart } from "@/components/dashboard/monthly-summary-pie-chart"
import { MonthlySummaryReportPanel } from "@/components/dashboard/monthly-summary-report-panel"
import { Calendar, Sparkles, FileText, Loader2, RefreshCw } from "lucide-react"
import { cn } from "@/lib/utils"
import { QueryLoadingState } from "@/components/ui/loading-spinner"

/** 月別サマリー（円グラフ + AI レポート） */
export function MonthlySummarySection() {
  const {
    selectedMonth,
    setSelectedMonth,
    monthlySummary,
    availableMonths,
    chartData,
    totalAmount,
    totalCount,
    isLoaded,
    report,
    isReportLoading,
    reportError,
    fetchReport,
    regenerateReport,
  } = useMonthlySummarySection()

  if (!isLoaded) {
    return (
      <QueryLoadingState
        className="relative overflow-hidden border-border/40 shadow-rich bg-gradient-to-br from-card via-card to-indigo-500/5"
        heightClass="h-[500px]"
        message="データを読み込み中..."
      />
    )
  }

  return (
    <Card
      className={cn(
        "relative overflow-hidden",
        "border-border/40 shadow-rich shadow-rich-hover",
        "bg-gradient-to-br from-card via-card to-indigo-500/5",
        "transition-all duration-500"
      )}
    >
      <div className="absolute inset-0 pattern-dots pointer-events-none opacity-30" />

      <div className="relative px-6 py-5 border-b border-border/40">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-400 to-purple-500 text-white shadow-lg">
              <Calendar className="h-5 w-5" />
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
              <SelectTrigger className="w-full md:w-[180px] h-9 text-xs font-bold rounded-xl border-border/60 bg-background/50">
                <SelectValue />
              </SelectTrigger>
              <SelectContent align="end" className="rounded-xl max-h-[300px]">
                {availableMonths.map((month) => (
                  <SelectItem key={month} value={month} className="text-xs">
                    {formatMonth(month)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Button
              variant="outline"
              size="sm"
              onClick={() => fetchReport(selectedMonth)}
              disabled={totalCount === 0 || isReportLoading}
              className="h-9 gap-1.5 rounded-xl text-xs font-bold border-indigo-500/30 text-indigo-600"
            >
              {isReportLoading ? (
                <Loader2 className="h-3.5 w-3.5 animate-spin" />
              ) : (
                <FileText className="h-3.5 w-3.5" />
              )}
              AIレポート
            </Button>

            {report && !isReportLoading && (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => regenerateReport(selectedMonth)}
                disabled={totalCount === 0}
                className="h-9 gap-1.5 rounded-xl text-xs font-bold"
              >
                <RefreshCw className="h-3.5 w-3.5" />
                再生成
              </Button>
            )}
          </div>
        </div>
      </div>

      <CardContent className="relative p-0">
        <MonthlySummaryPieChart
          chartData={chartData}
          monthlySummary={monthlySummary}
          totalAmount={totalAmount}
          totalCount={totalCount}
        />
        <MonthlySummaryReportPanel
          report={report}
          isReportLoading={isReportLoading}
          reportError={reportError}
        />
      </CardContent>
    </Card>
  )
}
