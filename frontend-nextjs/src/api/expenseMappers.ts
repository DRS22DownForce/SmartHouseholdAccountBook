/**
 * Expense API 型変換ユーティリティ
 */

import type { ExpenseDto, ExpenseRequestDto, MonthlySummaryDto } from './generated/api';
import type { Expense, ExpenseFormData } from '@/lib/types';

/**
 * 月別サマリーの型定義
 */
export interface MonthlySummary {
    total: number;
    count: number;
    byCategory: Array<{
        category: string;
        amount: number;
    }>;
}

/**
 * ExpenseDto → Expense 変換
 */
export function toExpense(dto: ExpenseDto): Expense {
    return {
        id: String(dto.id),
        amount: dto.amount ?? 0,
        category: dto.category ?? '',
        description: dto.description ?? '',
        date: dto.date ?? '',
        createdAt: new Date().toISOString(),
    };
}

/**
 * ExpenseFormData → ExpenseRequestDto 変換
 */
export function toExpenseRequestDto(data: ExpenseFormData): ExpenseRequestDto {
    return {
        date: data.date,
        category: data.category,
        amount: data.amount,
        description: data.description,
    };
}

/**
 * MonthlySummaryDto → MonthlySummary 変換
 */
export function toMonthlySummary(dto: MonthlySummaryDto): MonthlySummary {
    return {
        total: dto.total ?? 0,
        count: dto.count ?? 0,
        byCategory: (dto.byCategory ?? []).map((item) => ({
            category: item.category ?? '',
            amount: item.amount ?? 0,
        })),
    };
}
