import { generateText } from "ai"

export async function POST(req: Request) {
  try {
    const { messages, expenses } = await req.json()

    // 支出データの分析サマリーを作成
    const expenseSummary = analyzeExpenses(expenses)

    const { text } = await generateText({
      model: "openai/gpt-4o-mini",
      messages: [
        {
          role: "system",
          content: `あなたは家計管理のアドバイザーです。ユーザーの支出データを分析し、改善方法を提案してください。

現在の支出状況：
${expenseSummary}

以下の点に注目してアドバイスしてください：
- 支出の傾向と問題点
- 節約できる可能性のあるカテゴリ
- 予算配分の改善案
- 具体的な行動提案

親しみやすく、実践的なアドバイスを心がけてください。`,
        },
        ...messages,
      ],
    })

    return Response.json({ message: text })
  } catch (error) {
    console.error("[v0] Chat API error:", error)
    return Response.json({ error: "Failed to generate response" }, { status: 500 })
  }
}

function analyzeExpenses(expenses: any[]) {
  if (!expenses || expenses.length === 0) {
    return "支出データがまだありません。"
  }

  // 月別の合計
  const monthlyTotals: Record<string, number> = {}
  const categoryTotals: Record<string, number> = {}

  expenses.forEach((expense) => {
    const month = expense.date.substring(0, 7)
    monthlyTotals[month] = (monthlyTotals[month] || 0) + expense.amount
    categoryTotals[expense.category] = (categoryTotals[expense.category] || 0) + expense.amount
  })

  const months = Object.keys(monthlyTotals).sort().reverse().slice(0, 3)
  const monthlyAverage = months.length > 0 ? months.reduce((sum, m) => sum + monthlyTotals[m], 0) / months.length : 0

  const categoryBreakdown = Object.entries(categoryTotals)
    .sort(([, a], [, b]) => b - a)
    .map(([category, amount]) => `  - ${category}: ¥${amount.toLocaleString()}`)
    .join("\n")

  return `
総支出件数: ${expenses.length}件
月平均支出: ¥${Math.round(monthlyAverage).toLocaleString()}
カテゴリ別支出:
${categoryBreakdown}
  `.trim()
}
