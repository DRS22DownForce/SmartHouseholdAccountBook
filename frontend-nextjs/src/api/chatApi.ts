/**
 * Chat API クライアント
 */

import { getChatApiClient } from './apiClient';
import { withAuthHeader } from './authUtils';
import type { ChatRequest, ChatResponse } from './generated/api';

/**
 * AIチャットにメッセージを送信し、応答を取得する
 */
export async function sendChatMessage(message: string): Promise<ChatResponse> {
    const api = getChatApiClient();
    const options = await withAuthHeader();

    const request: ChatRequest = {
        message: message,
    };

    const response = await api.apiChatPost(request, options);
    return response.data;
}
