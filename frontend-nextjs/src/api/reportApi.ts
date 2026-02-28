/**
 * 月次レポート API クライアント
 */

import { getExpenseApiClient } from './apiClient';
import { withAuthHeader } from './authUtils';
import type { MonthlyReportResponse } from './generated/api';

/**
 * 指定月のAI生成月次レポートを取得する
 *
 * @param month    対象月（YYYY-MM形式）
 * @param generate trueの場合キャッシュを無視して再生成。falseの場合はキャッシュのみ（なければ204でnull）
 * @returns 月次レポート。generate=falseでキャッシュがない場合はnull
 */
export async function getMonthlyReport(
    month: string,
    generate = false
): Promise<MonthlyReportResponse | null> {
    const api = getExpenseApiClient();
    const options = await withAuthHeader();
    const response = await api.apiExpensesReportGet(month, generate, options);
    if (response.status === 204 || response.data == null) {
        return null;
    }
    return response.data;
}
