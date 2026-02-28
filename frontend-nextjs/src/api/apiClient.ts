/**
 * APIクライアント作成ユーティリティ
 */

import { Configuration } from './generated/configuration';
import { ExpensesApi, AiApi } from './generated/api';

/**
 * 環境変数からベースURLを取得
 */
function getBasePath(): string {
    return process.env.NEXT_PUBLIC_API_BASE_URL || '';
}

/**
 * Expense APIクライアントのインスタンスを作成
 */
export function getExpenseApiClient(): ExpensesApi {
    return new ExpensesApi(new Configuration({
        basePath: getBasePath()
    }));
}

/**
 * AI APIクライアントのインスタンスを作成
 */
export function getAiApiClient(): AiApi {
    return new AiApi(new Configuration({
        basePath: getBasePath()
    }));
}
