/**
 * API クライアント作成ユーティリティ
 */

import { Configuration } from "./generated/configuration"
import { ExpensesApi, AiApi } from "./generated/api"
import { authenticatedAxios } from "./authenticatedAxios"

function getBasePath(): string {
  return process.env.NEXT_PUBLIC_API_BASE_URL || ""
}

/** Expense API クライアント（認証付き Axios を共有） */
export function getExpenseApiClient(): ExpensesApi {
  return new ExpensesApi(
    new Configuration({ basePath: getBasePath() }),
    undefined,
    authenticatedAxios //認証付き Axios インスタンス
  )
}

/** AI API クライアント（認証付き Axios を共有） */
export function getAiApiClient(): AiApi {
  return new AiApi(
    new Configuration({ basePath: getBasePath() }),
    undefined,
    authenticatedAxios
  )
}
