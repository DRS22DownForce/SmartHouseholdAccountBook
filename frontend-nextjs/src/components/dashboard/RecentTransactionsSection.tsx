"use client"

/**
 * 最近の取引セクションコンポーネント
 * 
 * 直近の収入・支出の取引を表示します。
 * 現在はUIのみ実装されており、実際のロジックは未実装です。
 */

import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { ArrowUpCircle, ArrowDownCircle, ChevronRight } from "lucide-react"
import { formatCurrency } from "@/lib/formatters"
import { formatDate } from "@/lib/date-formatters"
import Link from "next/link"
import { cn } from "@/lib/utils"

export function RecentTransactionsSection() {
  // モックデータ（実際のロジックは未実装）
  const mockTransactions = [
    { id: 1, type: "income", date: "2024-01-20", category: "給与", description: "基本給", amount: 250000 },
    { id: 2, type: "expense", date: "2024-01-19", category: "食費", description: "スーパーマーケット", amount: 3500 },
    { id: 3, type: "expense", date: "2024-01-18", category: "交通費", description: "電車代", amount: 2000 },
    { id: 4, type: "expense", date: "2024-01-17", category: "光熱費", description: "電気代", amount: 5000 },
    { id: 5, type: "income", date: "2024-01-15", category: "副業", description: "フリーランス", amount: 50000 },
  ]

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg md:text-xl font-semibold text-foreground">最近の取引</h2>
        <Link href="/expenses">
          <Button variant="ghost" size="sm" className="gap-1">
            すべて見る
            <ChevronRight className="h-4 w-4" />
          </Button>
        </Link>
      </div>

      <Card className="border-border/50 shadow-md">
        <CardContent className="pt-6">
          {mockTransactions.length === 0 ? (
            <p className="text-center text-muted-foreground py-8">
              取引データがありません
            </p>
          ) : (
            <div className="space-y-3">
              {mockTransactions.map((transaction) => (
                <div
                  key={transaction.id}
                  className="flex items-center justify-between p-3 rounded-lg hover:bg-muted/50 transition-colors"
                >
                  <div className="flex items-center gap-3 flex-1 min-w-0">
                    <div
                      className={cn(
                        "flex h-10 w-10 items-center justify-center rounded-full flex-shrink-0",
                        transaction.type === "income"
                          ? "bg-green-100 dark:bg-green-900/30"
                          : "bg-red-100 dark:bg-red-900/30"
                      )}
                    >
                      {transaction.type === "income" ? (
                        <ArrowUpCircle className="h-5 w-5 text-green-600 dark:text-green-400" />
                      ) : (
                        <ArrowDownCircle className="h-5 w-5 text-red-600 dark:text-red-400" />
                      )}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-semibold text-foreground truncate">
                        {transaction.description}
                      </p>
                      <div className="flex items-center gap-2 mt-1">
                        <span className="text-xs text-muted-foreground">
                          {formatDate(transaction.date)}
                        </span>
                        <span className="text-xs text-muted-foreground">•</span>
                        <span className="text-xs text-muted-foreground">
                          {transaction.category}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="flex-shrink-0 ml-4">
                    <p
                      className={cn(
                        "text-lg font-bold",
                        transaction.type === "income"
                          ? "text-green-700 dark:text-green-400"
                          : "text-red-700 dark:text-red-400"
                      )}
                    >
                      {transaction.type === "income" ? "+" : "-"}
                      {formatCurrency(transaction.amount)}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

