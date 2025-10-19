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
import { DefaultApi } from './generated/api';

/**
 * CognitoからJWTトークンを取得
 * 
 * この関数はCognitoの認証セッションから現在のユーザーのIDトークンを取得します。
 * 
 * IDトークン vs アクセストークン:
 * - IDトークン: ユーザー情報（メールアドレス、名前など）を含む
 * - アクセストークン: APIアクセス権限の証明のみ
 * 
 * バックエンドでユーザーを識別する必要があるため、IDトークンを使用します。
 * 
 * @returns {Promise<string>} JWTトークン文字列（取得失敗時は空文字列）
 * 
 * セキュリティ注意事項:
 * - トークンの有効期限は通常1時間
 * - Amplifyが自動的にトークンの更新を処理
 * - トークンが期限切れの場合、Amplifyが自動的に再取得
 */
export async function getJwtToken(): Promise<string> {
    try {
        // Cognitoから現在の認証セッションを取得
        const session = await fetchAuthSession();

        // IDトークンを文字列として取得
        const token = session.tokens?.idToken?.toString() ?? '';

        // デバッグ用: トークンの情報を出力
        if (token) {
            console.log('JWT Token取得成功');
            console.log('Token先頭:', token.substring(0, 50) + '...');

            // トークンのペイロードをデコード（検証はせず、内容確認のみ）
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                console.log('Token Payload:', {
                    sub: payload.sub,
                    email: payload.email,
                    'cognito:username': payload['cognito:username'],
                    exp: new Date(payload.exp * 1000).toLocaleString(),
                    iss: payload.iss
                });
            } catch (e) {
                console.error('トークンのデコードに失敗:', e);
            }
        } else {
            console.warn('JWT Token が取得できませんでした');
        }

        return token;
    } catch (error) {
        console.error('Failed to get JWT token:', error);
        return '';
    }
}

/**
 * APIクライアントのインスタンスを作成
 * 
 * OpenAPI Generatorが自動生成したDefaultApiクラスのインスタンスを返します。
 * このクライアントを使用して、すべてのAPI呼び出しを行います。
 * 
 * 設定:
 * - basePath: '' - 相対パスでAPIを呼び出す（同じドメインを前提）
 *   本番環境で異なるドメインのAPIを呼び出す場合は、環境変数で設定可能
 * 
 * @returns {DefaultApi} API呼び出し用のクライアントインスタンス
 * 
 * 使用例:
 * ```typescript
 * const api = getApiClient()
 * const response = await api.apiExpensesGet()
 * ```
 */
export function getApiClient(): DefaultApi {
    // 環境変数からベースURLを取得（設定されていない場合は空文字列）
    const basePath = process.env.NEXT_PUBLIC_API_BASE_URL || '';

    return new DefaultApi(new Configuration({
        basePath: basePath
    }));
}

/**
 * Authorizationヘッダーを付与したオプションオブジェクトを作成
 * 
 * この関数は、API呼び出し時にJWTトークンを含むAuthorizationヘッダーを
 * 自動的に追加します。全てのAPI呼び出しでこの関数を使用することで、
 * 認証を一元管理できます。
 * 
 * Bearer認証:
 * - OAuth2/JWTの標準的な認証方式
 * - ヘッダー形式: "Authorization: Bearer <トークン>"
 * - バックエンドはこの形式でトークンを期待
 * 
 * @param {Record<string, any>} options - 既存のAxiosリクエストオプション
 * @returns {Promise<Record<string, any>>} Authorizationヘッダーが追加されたオプション
 * 
 * 使用例:
 * ```typescript
 * const api = getApiClient()
 * const options = await withAuthHeader()
 * const response = await api.apiExpensesGet(options)
 * ```
 * 
 * パフォーマンス:
 * - リクエストごとにトークンを取得しますが、Amplifyがキャッシュを管理
 * - トークンが有効な間は、高速にキャッシュから取得
 */
export async function withAuthHeader(options: Record<string, any> = {}): Promise<Record<string, any>> {
    // 現在のJWTトークンを取得
    const token = await getJwtToken();

    // 既存のオプションとヘッダーをマージ
    return {
        ...options, // 既存のオプションを展開（上書きしない）
        headers: {
            ...(options.headers || {}), // 既存のヘッダーを展開
            Authorization: `Bearer ${token}`, // Authorizationヘッダーを追加
        },
    };
}

