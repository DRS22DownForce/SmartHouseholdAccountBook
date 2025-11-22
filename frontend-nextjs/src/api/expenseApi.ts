/**
 * Expense API クライアント
 * 
 * このファイルは支出データのCRUD操作に必要なAPIクライアントとJWT認証機能を提供します。
 * 
 * 主な機能:
 * 1. JWTトークンの取得 - Cognitoから認証トークンを取得
 * 2. APIクライアントの作成 - OpenAPI生成コードを使用
 * 3. 認証ヘッダーの付与 - 全てのAPI呼び出しにJWTトークンを自動付与
 * 
 * セキュリティ:
 * - JWTトークンはリクエストごとに取得（最新の状態を保証）
 * - IDトークンを使用（ユーザー情報を含む）
 * - Bearer認証形式でトークンを送信
 */

import { fetchAuthSession } from 'aws-amplify/auth';
import { Configuration } from './generated/configuration';
import { ExpensesApi } from './generated/api';

/**
 * CognitoからJWTトークン（IDトークン）を取得
 * 
 * バックエンドでユーザーを識別するため、IDトークンを使用します。
 * 
 * @returns JWTトークン文字列
 * @throws 認証セッションが存在しない場合にエラーをスロー
 */
export async function getJwtToken(): Promise<string> {
    try {
        // Cognitoから現在の認証セッションを取得
        const session = await fetchAuthSession();

        // IDトークンを文字列として取得
        const token = session.tokens?.idToken?.toString();

        // デバッグ用: トークンの情報を出力
        if (!token) {
            throw new Error('認証トークンの取得に失敗しました');
        }
        return token;
    } catch (error) {
        throw new Error('認証トークンの取得に失敗しました');
    }
}

/**
 * APIクライアントのインスタンスを作成
 * 
 * @returns API呼び出し用のクライアントインスタンス
 */
export function getApiClient(): ExpensesApi {
    // 環境変数からベースURLを取得（設定されていない場合は空文字列）
    const basePath = process.env.NEXT_PUBLIC_API_BASE_URL || '';

    return new ExpensesApi(new Configuration({
        basePath: basePath
    }));
}

/**
 * Authorizationヘッダーを付与したオプションオブジェクトを作成
 * 
 * API呼び出し時にJWTトークンを含むAuthorizationヘッダーを自動的に追加します。
 * 
 * @returns Authorizationヘッダーが追加されたオプション
 */
export async function withAuthHeader(): Promise<{ headers: { Authorization: string } }> {
    // 現在のJWTトークンを取得
    const token = await getJwtToken();
    return {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    };
}

