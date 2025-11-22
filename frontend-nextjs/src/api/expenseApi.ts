/**
 * Expense API クライアント
 */

import { getExpenseApiClient } from './apiClient';
import { withAuthHeader } from './authUtils';
import {
    toExpense,
    toExpenseRequestDto,
    toMonthlySummary,
} from './expenseMappers';
import type { MonthlySummary } from './expenseMappers';
import type { Expense, ExpenseFormData } from '@/lib/types';


/**
 * 全支出を取得
 */
export async function fetchExpenses(): Promise<Expense[]> {
    const api = getExpenseApiClient();
    const options = await withAuthHeader();
    const response = await api.apiExpensesGet(undefined, options);
    return response.data.map(toExpense);
}

/**
 * 月別支出を取得
 */
export async function fetchMonthlyExpenses(month: string): Promise<Expense[]> {
    const api = getExpenseApiClient();
    const options = await withAuthHeader();
    const response = await api.apiExpensesGet(month, options);
    return response.data.map(toExpense);
}

/**
 * 支出を作成
 */
export async function createExpense(data: ExpenseFormData): Promise<Expense> {
    const api = getExpenseApiClient();
    const options = await withAuthHeader();
    const requestDto = toExpenseRequestDto(data);
    const response = await api.apiExpensesPost(requestDto, options);
    return toExpense(response.data);
}

/**
 * 複数の支出を一括作成
 */
export async function createExpenses(dataArray: ExpenseFormData[]): Promise<Expense[]> {
    const api = getExpenseApiClient();
    const options = await withAuthHeader();
    const responses = await Promise.all(
        dataArray.map((data) => api.apiExpensesPost(toExpenseRequestDto(data), options))
    );
    return responses.map((response) => toExpense(response.data));
}

/**
 * 支出を更新
 */
export async function updateExpense(id: string, data: ExpenseFormData): Promise<Expense> {
    const api = getExpenseApiClient();
    const options = await withAuthHeader();
    const requestDto = toExpenseRequestDto(data);
    const response = await api.apiExpensesIdPut(Number(id), requestDto, options);
    return toExpense(response.data);
}

/**
 * 支出を削除
 */
export async function deleteExpense(id: string): Promise<void> {
    const api = getExpenseApiClient();
    const options = await withAuthHeader();
    await api.apiExpensesIdDelete(Number(id), options);
}

/**
 * 利用可能な月のリストを取得
 */
export async function fetchAvailableMonths(): Promise<string[]> {
    const api = getExpenseApiClient();
    const options = await withAuthHeader();
    const response = await api.apiExpensesMonthsGet(options);
    return response.data ?? [];
}

/**
 * 月別サマリーを取得
 */
export async function fetchMonthlySummary(month: string): Promise<MonthlySummary> {
    const api = getExpenseApiClient();
    const options = await withAuthHeader();
    const response = await api.apiExpensesSummaryGet(month, options);
    return toMonthlySummary(response.data);
}

/**
 * 範囲指定で月別サマリーを取得
 */
export async function fetchMonthlySummaryRange(
    startMonth: string,
    endMonth: string
): Promise<MonthlySummary[]> {
    const api = getExpenseApiClient();
    const options = await withAuthHeader();
    const response = await api.apiExpensesSummaryRangeGet(startMonth, endMonth, options);
    return response.data.map(toMonthlySummary);
}
