import { Card } from "@/components/ui/card"

/** 支出一覧ページのローディング UI（App Router の Suspense 境界） */
export default function ExpensesLoading() {
  return (
    <div className="min-h-screen p-6 md:p-8 space-y-6">
      <div className="h-10 w-48 rounded-lg bg-muted/60 animate-pulse" />
      <div className="grid gap-4 md:grid-cols-3">
        {[1, 2, 3].map((i) => (
          <Card key={i} className="h-28 animate-pulse bg-muted/40 border-border/40" />
        ))}
      </div>
      <Card className="h-[320px] animate-pulse bg-muted/30 border-border/40" />
      <Card className="h-[500px] animate-pulse bg-muted/30 border-border/40" />
    </div>
  )
}
