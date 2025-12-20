/**
 * Chat API クライアント
 */

import { getChatApiClient } from './apiClient';
import { withAuthHeader } from './authUtils';
import type { ChatRequest, ChatResponse, ChatHistoryResponse, ChatMessageDto } from './generated/api';
import type { AxiosResponse } from 'axios';

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

/**
 * 会話履歴を取得する
 * 
 * @returns 会話履歴レスポンス（時系列順、古い順）
 */
export async function getChatHistory(): Promise<ChatMessageDto[]> {
    const api = getChatApiClient();
    const options = await withAuthHeader();

    const response: AxiosResponse<ChatHistoryResponse> = await api.apiChatGet(options);
    return response.data.messages || [];
}
