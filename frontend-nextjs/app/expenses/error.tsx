"use client"

import { useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { AlertCircle } from "lucide-react"

interface ExpensesErrorProps {
  error: Error & { digest?: string }
  reset: () => void
}

/** 支出一覧ページのエラー境界 */
export default function ExpensesError({ error, reset }: ExpensesErrorProps) {
  useEffect(() => {
    console.error("Expenses page error:", error)
  }, [error])

  return (
    <div className="min-h-screen flex items-center justify-center p-6">
      <Card className="max-w-md w-full p-8 text-center space-y-4 border-destructive/20">
        <AlertCircle className="h-10 w-10 text-destructive mx-auto" />
        <h2 className="text-lg font-bold text-foreground">画面の読み込みに失敗しました</h2>
        <p className="text-sm text-muted-foreground">
          一時的な問題の可能性があります。もう一度お試しください。
        </p>
        <Button onClick={reset} className="w-full">
          再読み込み
        </Button>
      </Card>
    </div>
  )
}
