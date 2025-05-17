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
        basePath: '',
        accessToken: async () => await getJwtToken(),
    }));
}