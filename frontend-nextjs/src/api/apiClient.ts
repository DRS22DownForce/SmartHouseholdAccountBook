/**
 * API クライアント作成ユーティリティ
 */

import { Configuration } from "./generated/configuration"
import {
  ExpensesApi,
  ExpenseSummaryApi,
  CsvExpensesApi,
  AiApi,
  MonthlyReportApi,
} from "./generated/api"
import { authenticatedAxios } from "./authenticatedAxios"

function getBasePath(): string {
  return process.env.NEXT_PUBLIC_API_BASE_URL || ""
}

function createConfig(): Configuration {
  return new Configuration({ basePath: getBasePath() })
}

/** Expense CRUD API クライアント（認証付き Axios を共有） */
export function getExpenseApiClient(): ExpensesApi {
  return new ExpensesApi(createConfig(), undefined, authenticatedAxios)
}

/** 月次サマリー / 利用可能月 API */
export function getExpenseSummaryApiClient(): ExpenseSummaryApi {
  return new ExpenseSummaryApi(createConfig(), undefined, authenticatedAxios)
}

/** CSV 一括インポート API */
export function getCsvExpensesApiClient(): CsvExpensesApi {
  return new CsvExpensesApi(createConfig(), undefined, authenticatedAxios)
}

/** 月次レポート API */
export function getMonthlyReportApiClient(): MonthlyReportApi {
  return new MonthlyReportApi(createConfig(), undefined, authenticatedAxios)
}

/** AI API クライアント（認証付き Axios を共有） */
export function getAiApiClient(): AiApi {
  return new AiApi(createConfig(), undefined, authenticatedAxios)
}
