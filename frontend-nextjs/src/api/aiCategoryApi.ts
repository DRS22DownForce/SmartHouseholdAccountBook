/**
 * AIカテゴリー自動分類APIクライアント
 */

import { getAiApiClient } from './apiClient';
import { withAuthHeader } from './authUtils';
import type { CategoryPredictionRequest, ErrorResponse } from './generated/api';
import axios from 'axios';

/**
 * 支出の説明文から、AIが適切なカテゴリーを自動分類する
 * 
 * @param description 支出の説明文（例: "コンビニでお弁当を購入"）
 * @returns 推論されたカテゴリー名（例: "食費"）
 * @throws Error API呼び出しに失敗した場合
 */
export async function predictCategory(description: string): Promise<string> {
    const api = getAiApiClient();
    const options = await withAuthHeader();

    // 入力バリデーション: 説明文が空でないことを確認
    if (!description || description.trim().length === 0) {
        throw new Error('説明文は必須です。');
    }

    const request: CategoryPredictionRequest = {
        description: description.trim(),
    };

    try {
        const response = await api.apiAiCategoryPost(request, options);
        return response.data.category;
    } catch (error) {
        // AxiosErrorの場合、HTTPステータスコードとエラーレスポンスボディを確認
        // axios.isAxiosError()は、axiosライブラリが提供する公式の型ガード関数
        if (axios.isAxiosError(error)) {
            const status = error.response?.status;
            const errorResponse = error.response?.data as ErrorResponse | undefined;

            // エラーレスポンスボディからメッセージを取得（存在する場合）
            const errorMessage = errorResponse?.message;

            // HTTPステータスコードに応じて適切なエラーメッセージを返す
            if (status === 429) {
                // クォータ超過エラー
                throw new Error(errorMessage || 'OpenAI APIの利用枠（クォータ）を超過しました。しばらく時間をおいてから再度お試しください。');
            } else if (status === 400) {
                // バリデーションエラー（説明文が空など）
                throw new Error(errorMessage || '不正なリクエストです。説明文を確認してください。');
            } else if (status === 500) {
                // サーバーエラー（AIサービスとの通信エラーなど）
                throw new Error(errorMessage || 'AIサービスとの通信でエラーが発生しました。しばらく時間をおいてから再度お試しください。');
            } else if (errorMessage) {
                // その他のエラーで、エラーメッセージが存在する場合
                throw new Error(errorMessage);
            }
        }

        // AxiosErrorでない場合、またはエラーレスポンスボディがない場合
        if (error instanceof Error) {
            throw new Error(`カテゴリーの自動分類に失敗しました: ${error.message}`);
        }
        throw new Error('カテゴリーの自動分類に失敗しました。');
    }
}
