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
import type { ChatRequest, ChatResponse } from "./generated/api";
import { ChatApi } from "./generated/api";

/**
 * AIチャットにメッセージを送信し、応答を取得する
 * 
 * @param message ユーザーからのメッセージ
 * @returns AIからの応答メッセージを含むオブジェクト
 */
export async function sendChatMessage(message: string): Promise<ChatResponse> {
    const api = new ChatApi();
    const options = await withAuthHeader();

    const request: ChatRequest = {
        message: message,
    };

    try {
        const response = await api.apiChatPost(request, options);
        return response.data;
    } catch (error) {
        console.error("Chat API Error:", error);
        throw new Error("Failed to get response from AI Chat API");
    }
}
