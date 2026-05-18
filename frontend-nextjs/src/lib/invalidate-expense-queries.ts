import type { QueryClient } from "@tanstack/react-query"
import { expenseKeys } from "@/lib/query-keys"

/** 支出関連のキャッシュを一括で無効化（CRUD 成功後に呼ぶ） */
export function invalidateExpenseQueries(queryClient: QueryClient): void {
  void queryClient.invalidateQueries({ queryKey: expenseKeys.all })
}
