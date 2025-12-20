package com.example.backend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatMessageエンティティのテストクラス
 * 
 * チャットメッセージエンティティのバリデーションをテストします。
 * エンティティは識別子（ID）を持ち、状態が変更可能です。
 */
class ChatMessageTest {

    @Test
    @DisplayName("正常なチャットメッセージエンティティを作成できる（userロール）")
    void createChatMessage_正常な値_userロール() {
        // テストデータの準備: ユーザーを作成
        User user = new User("cognitoSub", "test@example.com");
        String role = "user";
        String content = "こんにちは";

        // テスト実行: ChatMessageエンティティを作成
        ChatMessage message = new ChatMessage(role, content, user);

        // 検証: 正常に作成され、値が正しく設定されていることを確認
        assertNotNull(message);
        assertEquals("user", message.getRole());
        assertEquals("こんにちは", message.getContent());
        assertEquals(user, message.getUser());
        assertNotNull(message.getCreatedAt());
        // 作成日時が現在時刻に近いことを確認（1秒以内）
        assertTrue(message.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(message.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    @DisplayName("正常なチャットメッセージエンティティを作成できる（assistantロール）")
    void createChatMessage_正常な値_assistantロール() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        String role = "assistant";
        String content = "こんにちは！何かお手伝いできることはありますか？";

        // テスト実行
        ChatMessage message = new ChatMessage(role, content, user);

        // 検証
        assertNotNull(message);
        assertEquals("assistant", message.getRole());
        assertEquals("こんにちは！何かお手伝いできることはありますか？", message.getContent());
        assertEquals(user, message.getUser());
    }

    @Test
    @DisplayName("正常なチャットメッセージエンティティを作成できる（systemロール）")
    void createChatMessage_正常な値_systemロール() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        String role = "system";
        String content = "システムメッセージ";

        // テスト実行
        ChatMessage message = new ChatMessage(role, content, user);

        // 検証
        assertNotNull(message);
        assertEquals("system", message.getRole());
        assertEquals("システムメッセージ", message.getContent());
        assertEquals(user, message.getUser());
    }

    @Test
    @DisplayName("ロールがnullなら例外")
    void createChatMessage_ロールがnullなら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        String content = "テストメッセージ";

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ChatMessage(null, content, user));

        assertEquals("ロールは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("ロールが空文字列なら例外")
    void createChatMessage_ロールが空文字列なら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        String content = "テストメッセージ";

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ChatMessage("", content, user));

        assertEquals("ロールは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("ロールが空白のみなら例外")
    void createChatMessage_ロールが空白のみなら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        String content = "テストメッセージ";

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ChatMessage("   ", content, user));

        assertEquals("ロールは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("ロールが無効な値なら例外")
    void createChatMessage_ロールが無効な値なら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        String content = "テストメッセージ";

        // テスト実行と検証: "invalid"は有効なロールではない
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ChatMessage("invalid", content, user));

        assertEquals("ロールは'user'、'assistant'、'system'のいずれかである必要があります。", exception.getMessage());
    }

    @Test
    @DisplayName("メッセージ内容がnullなら例外")
    void createChatMessage_メッセージ内容がnullなら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        String role = "user";

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ChatMessage(role, null, user));

        assertEquals("メッセージ内容は必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("メッセージ内容が空文字列なら例外")
    void createChatMessage_メッセージ内容が空文字列なら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        String role = "user";

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ChatMessage(role, "", user));

        assertEquals("メッセージ内容は必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("メッセージ内容が空白のみなら例外")
    void createChatMessage_メッセージ内容が空白のみなら例外() {
        // テストデータの準備
        User user = new User("cognitoSub", "test@example.com");
        String role = "user";

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ChatMessage(role, "   ", user));

        assertEquals("メッセージ内容は必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("ユーザーがnullなら例外")
    void createChatMessage_ユーザーがnullなら例外() {
        // テストデータの準備
        String role = "user";
        String content = "テストメッセージ";

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ChatMessage(role, content, null));

        assertEquals("ユーザーは必須です。", exception.getMessage());
    }
}



