/**
 * 認証ユーティリティ
 */

import { fetchAuthSession } from 'aws-amplify/auth';

/**
 * CognitoからJWTトークン（IDトークン）を取得
 */
async function getJwtToken(): Promise<string> {
    const session = await fetchAuthSession();
    const token = session.tokens?.idToken?.toString();

    if (!token) {
        throw new Error('認証トークンの取得に失敗しました');
    }
    return token;
}

/**
 * Authorizationヘッダーを付与したオプションオブジェクトを作成
 */
export async function withAuthHeader(): Promise<{ headers: { Authorization: string } }> {
    const token = await getJwtToken();
    return {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    };
}
