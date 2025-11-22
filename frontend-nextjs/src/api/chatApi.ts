/**
 * Chat API クライアント
 *
 * AIチャット機能に関連するAPI呼び出しを管理します。
 * 
 * 役割:
 * 1. チャットメッセージの送信
 * 2. 認証ヘッダーの自動付与（expenseApiを利用）
 */
import { withAuthHeader } from "./expenseApi";

export interface ChatResponse {
    message: string;
}

/**
 * AIチャットにメッセージを送信し、応答を取得する
 * 
 * @param message ユーザーからのメッセージ
 * @returns AIからの応答メッセージを含むオブジェクト
 */
export async function sendChatMessage(message: string): Promise<ChatResponse> {
    const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || "";

    // 認証ヘッダーを取得（JWTトークンを含む）
    const { headers } = await withAuthHeader();

    const response = await fetch(`${baseUrl}/api/chat`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            ...headers, // Authorizationヘッダーを展開して追加
        },
        body: JSON.stringify({
            message: message,
        }),
    });

    if (!response.ok) {
        throw new Error("Failed to get response");
    }

    return response.json();
}
