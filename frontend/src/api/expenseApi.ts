import { fetchAuthSession } from 'aws-amplify/auth';
import { Configuration } from './generated/configuration';
import { DefaultApi } from './generated/api';

//JWTトークンを取得
export async function getJwtToken() {
    const session = await fetchAuthSession();
    return session.tokens?.accessToken?.toString() ?? '';
    
}

//JWT付きAPIクライアントを返す
export function getApiClient() {
    return new DefaultApi(new Configuration({
        basePath: ''
    }));
}
//API呼び出し時にAuthorizationヘッダーを付与するヘルパー関数
export async function withAuthHeader(options: Record<string, any> = {}) {
    const token = await getJwtToken();
    return {
        ...options,
        headers: {
            ...(options.headers || {}),
            Authorization: `Bearer ${token}`,//Bearer+JWTトークンという形式でAuthorizationヘッダーを付与
        },
    };
}