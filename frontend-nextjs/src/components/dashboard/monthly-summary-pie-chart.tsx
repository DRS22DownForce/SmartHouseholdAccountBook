"use client"

import { useState } from "react"
import { PieChart, Pie, Cell, ResponsiveContainer, Sector } from "recharts"
import { getCategoryColor } from "@/lib/category-colors"
import { formatCurrency } from "@/lib/formatters"
import type { MonthlySummary } from "@/api/expenseMappers"
import { PieChart as PieChartIcon, Target, Wallet, TrendingUp } from "lucide-react"
import { cn } from "@/lib/utils"

interface ChartEntry {
  name: string
  value: number
  color: string
}

interface MonthlySummaryPieChartProps {
  chartData: ChartEntry[]
  monthlySummary: MonthlySummary | null
  totalAmount: number
  totalCount: number
}

const renderActiveShape = (props: {
  cx: number
  cy: number
  innerRadius: number
  outerRadius: number
  startAngle: number
  endAngle: number
  fill: string
  payload: ChartEntry
}) => {
  const { cx, cy, innerRadius, outerRadius, startAngle, endAngle, fill, payload } = props

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
          transition: "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)",
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

/** 左カラム: 総支出・トップ3 / 右カラム: ドーナツチャート */
export function MonthlySummaryPieChart({
  chartData,
  monthlySummary,
  totalAmount,
  totalCount,
}: MonthlySummaryPieChartProps) {
  const [activeIndex, setActiveIndex] = useState<number | undefined>(undefined)

  return (
    <div className="grid md:grid-cols-2 lg:grid-cols-3 divide-y md:divide-y-0 md:divide-x divide-border/40">
      <div className="p-6 space-y-6">
        <div
          className={cn(
            "relative overflow-hidden rounded-2xl p-5",
            "bg-gradient-to-br from-indigo-500/10 via-purple-500/5 to-transparent",
            "border border-indigo-500/20",
            "group hover:border-indigo-500/40 transition-all duration-300"
          )}
        >
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
            <span
              className={cn(
                "inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold",
                "bg-indigo-500/10 text-indigo-600 dark:text-indigo-400",
                "border border-indigo-500/20"
              )}
            >
              <TrendingUp className="w-3 h-3" />
              {totalCount} 件の取引
            </span>
          </div>
        </div>

        <div className="space-y-3">
          <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-2">
            <span className="w-1.5 h-1.5 rounded-full bg-purple-500" />
            支出内訳トップ3
          </p>
          <div className="space-y-2">
            {monthlySummary?.byCategory.slice(0, 3).map((item, i) => {
              const percentage =
                totalAmount > 0
                  ? ((item.amount / totalAmount) * 100).toFixed(1)
                  : "0"
              const color = getCategoryColor(item.category)
              return (
                <div
                  key={item.category}
                  className={cn(
                    "group flex items-center justify-between p-3 rounded-xl",
                    "bg-muted/30 hover:bg-muted/50",
                    "border border-transparent hover:border-border/50",
                    "transition-all duration-200",
                    "animate-fade-in",
                    i === 0 ? "stagger-1" : i === 1 ? "stagger-2" : "stagger-3"
                  )}
                >
                  <div className="flex items-center gap-3">
                    <div
                      className="w-2 h-10 rounded-full shadow-lg"
                      style={{
                        backgroundColor: color,
                        boxShadow: `0 0 10px ${color}40`,
                      }}
                    />
                    <div className="space-y-0.5">
                      <span className="text-sm font-bold text-foreground">
                        {item.category}
                      </span>
                      <span className="block text-[10px] font-bold px-2 py-0.5 rounded-full w-fit bg-muted/50 text-muted-foreground">
                        {percentage}%
                      </span>
                    </div>
                  </div>
                  <span className="text-sm font-black tabular-nums text-foreground/80">
                    {formatCurrency(item.amount)}
                  </span>
                </div>
              )
            })}
          </div>
        </div>
      </div>

      <div className="md:col-span-1 lg:col-span-2 p-0">
        <div className="p-6 flex flex-col items-center justify-center relative min-h-[400px]">
          {chartData.length > 0 ? (
            <div className="relative w-full flex flex-col items-center gap-6">
              <div className="relative w-[280px] h-[280px]">
                <div className="absolute inset-0 rounded-full bg-gradient-to-br from-muted/30 to-muted/10 blur-xl scale-110" />
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <defs>
                      {chartData.map((entry, index) => (
                        <linearGradient
                          key={`gradient-pie-${index}`}
                          id={`gradient-pie-${index}`}
                          x1="0"
                          y1="0"
                          x2="1"
                          y2="1"
                        >
                          <stop offset="0%" stopColor={entry.color} stopOpacity={1} />
                          <stop
                            offset="100%"
                            stopColor={entry.color}
                            stopOpacity={0.75}
                          />
                        </linearGradient>
                      ))}
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
                      // @ts-expect-error Recharts ActiveShape の型定義が厳密すぎるため
                      activeShape={renderActiveShape}
                      onMouseEnter={(_, index) => setActiveIndex(index)}
                      onMouseLeave={() => setActiveIndex(undefined)}
                      stroke="none"
                      cornerRadius={8}
                    >
                      {chartData.map((entry, index) => (
                        <Cell
                          key={`cell-${index}`}
                          fill={`url(#gradient-pie-${index})`}
                          style={{
                            filter:
                              activeIndex === undefined || activeIndex === index
                                ? `drop-shadow(0 4px 12px ${entry.color}40)`
                                : "none",
                            opacity:
                              activeIndex === undefined || activeIndex === index
                                ? 1
                                : 0.4,
                          }}
                        />
                      ))}
                    </Pie>
                  </PieChart>
                </ResponsiveContainer>
                <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                  <div
                    className={cn(
                      "flex flex-col items-center justify-center",
                      "w-[130px] h-[130px] rounded-full",
                      "bg-gradient-to-br from-background via-background to-muted/20",
                      "shadow-inner border border-border/20"
                    )}
                  >
                    {activeIndex !== undefined && chartData[activeIndex] ? (
                      <>
                        <div
                          className="w-3 h-3 rounded-full mb-1.5"
                          style={{
                            backgroundColor: chartData[activeIndex].color,
                            boxShadow: `0 0 16px ${chartData[activeIndex].color}80`,
                          }}
                        />
                        <span className="text-xs font-bold text-muted-foreground truncate max-w-[100px]">
                          {chartData[activeIndex].name}
                        </span>
                        <span className="text-xl font-black text-foreground tabular-nums">
                          {formatCurrency(chartData[activeIndex].value)}
                        </span>
                      </>
                    ) : (
                      <>
                        <Target className="w-4 h-4 text-muted-foreground mb-1" />
                        <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">
                          Total
                        </span>
                        <span className="text-2xl font-black text-foreground tabular-nums">
                          {formatCurrency(totalAmount)}
                        </span>
                      </>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className="flex h-[320px] items-center justify-center flex-col gap-4">
              <PieChartIcon className="w-10 h-10 text-muted-foreground/40" />
              <p className="text-sm font-bold text-foreground/80">データがありません</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
