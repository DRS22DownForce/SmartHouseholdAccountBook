"use client"

import type { MonthlyReportResponse } from "@/api/generated/api"
import {
  Sparkles,
  FileText,
  Loader2,
  AlertCircle,
  CheckCircle2,
  Clock,
} from "lucide-react"
import { cn } from "@/lib/utils"

interface MonthlySummaryReportPanelProps {
  report: MonthlyReportResponse | null
  isReportLoading: boolean
  reportError: string | null
}

/** AI 月次レポートの表示エリア */
export function MonthlySummaryReportPanel({
  report,
  isReportLoading,
  reportError,
}: MonthlySummaryReportPanelProps) {
  const showReportDefault = !report && !isReportLoading && !reportError
  const showReport = !!report && !isReportLoading

  return (
    <div className="relative border-t border-border/40 px-6 py-5">
      <div className="flex items-center gap-2 mb-4">
        <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-indigo-400 to-purple-500 text-white shadow-lg">
          <FileText className="h-3.5 w-3.5" />
        </div>
        <p className="text-sm font-black text-foreground tracking-tight">
          AI改善レポート
        </p>
        <span
          className={cn(
            "inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold",
            "bg-indigo-500/10 text-indigo-600 dark:text-indigo-400",
            "border border-indigo-500/20"
          )}
        >
          <Sparkles className="w-2.5 h-2.5" />
          AI Powered
        </span>
      </div>

      {showReportDefault && (
        <div className="flex flex-col items-center justify-center py-8 gap-3 rounded-2xl border border-dashed border-border/60 bg-muted/20">
          <FileText className="w-6 h-6 text-indigo-400" />
          <p className="text-sm font-bold text-foreground/70">
            AIレポートを生成しましょう
          </p>
          <p className="text-xs text-muted-foreground text-center px-4">
            上の「AIレポート」ボタンを押すと、この月の改善提案が表示されます
          </p>
        </div>
      )}

      {isReportLoading && (
        <div className="flex flex-col items-center justify-center py-8 gap-3">
          <Loader2 className="h-6 w-6 text-indigo-500 animate-spin" />
          <p className="text-sm font-bold text-foreground">AIが分析中...</p>
        </div>
      )}

      {!isReportLoading && reportError && (
        <div className="flex items-start gap-3 p-4 rounded-xl bg-destructive/10 border border-destructive/20">
          <AlertCircle className="h-5 w-5 text-destructive flex-shrink-0" />
          <p className="text-sm text-destructive font-medium">{reportError}</p>
        </div>
      )}

      {showReport && report && (
        <div className="grid md:grid-cols-2 gap-4">
          <div className="p-4 rounded-xl bg-gradient-to-br from-indigo-500/5 to-purple-500/5 border border-indigo-500/15">
            <p className="text-[10px] font-bold text-indigo-600 dark:text-indigo-400 uppercase tracking-wider flex items-center gap-1.5 mb-2">
              <Sparkles className="h-3 w-3" />
              今月の総評
            </p>
            <p className="text-sm text-foreground leading-relaxed">{report.summary}</p>
          </div>

          <div className="space-y-2">
            <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
              <CheckCircle2 className="h-3 w-3 text-emerald-500" />
              改善提案
            </p>
            <ul className="space-y-1.5">
              {report.suggestions.map((suggestion, index) => (
                <li
                  key={index}
                  className="flex items-start gap-2.5 p-2.5 rounded-xl bg-muted/40"
                >
                  <span className="flex-shrink-0 w-4 h-4 rounded-full flex items-center justify-center bg-gradient-to-br from-emerald-400 to-teal-500 text-white text-[9px] font-black">
                    {index + 1}
                  </span>
                  <p className="text-xs text-foreground leading-relaxed">{suggestion}</p>
                </li>
              ))}
            </ul>
          </div>

          <div className="md:col-span-2 flex items-center justify-between flex-wrap gap-2">
            {report.generatedAt && (
              <span className="flex items-center gap-1 text-[10px] text-muted-foreground/60">
                <Clock className="h-3 w-3" />
                生成日時:{" "}
                {new Date(report.generatedAt).toLocaleString("ja-JP", {
                  year: "numeric",
                  month: "2-digit",
                  day: "2-digit",
                  hour: "2-digit",
                  minute: "2-digit",
                })}
              </span>
            )}
            <p className="text-[10px] text-muted-foreground/50">
              AIによる提案のため、実際の状況と異なる場合があります。
            </p>
          </div>
        </div>
      )}
    </div>
  )
}
