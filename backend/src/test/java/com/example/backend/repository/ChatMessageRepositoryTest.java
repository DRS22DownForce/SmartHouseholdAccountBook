package com.example.backend.repository;

import com.example.backend.entity.ChatMessage;
import com.example.backend.entity.User;
import com.example.backend.repository.ChatMessageRepository;
import com.example.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatMessageRepositoryのテストクラス
 */
@DataJpaTest
@ActiveProfiles("test")
class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // テスト前にデータベースをクリア（各テストが独立して実行されるように）
        chatMessageRepository.deleteAll();
        userRepository.deleteAll();

        // テスト用のユーザーを作成して保存
        testUser = new User("cognitoSub", "test@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("ユーザーを指定してメッセージを取得できる")
    void findByUserOrderByCreatedAtDesc_正常に取得() {
        ChatMessage message1 = new ChatMessage(ChatMessage.Role.USER, "こんにちは", testUser);
        chatMessageRepository.save(message1);

        // 少し待ってから次のメッセージを作成（作成日時を分けるため）
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ChatMessage message2 = new ChatMessage(ChatMessage.Role.ASSISTANT, "こんにちは！何かお手伝いできることはありますか？", testUser);
        chatMessageRepository.save(message2);

        // テスト実行: ユーザーを指定してメッセージを取得
        List<ChatMessage> messages = chatMessageRepository.findByUserOrderByCreatedAtDesc(testUser);

        // 検証: 2件のメッセージが取得できることを確認
        assertEquals(2, messages.size());
        // 降順で取得されることを確認（新しい順）
        assertTrue(messages.get(0).getCreatedAt().isAfter(messages.get(1).getCreatedAt()) ||
                messages.get(0).getCreatedAt().equals(messages.get(1).getCreatedAt()));
    }

    @Test
    @DisplayName("ユーザーを指定してメッセージを取得する際、他のユーザーのメッセージは含まれない")
    void findByUserOrderByCreatedAtDesc_他のユーザーのメッセージは含まれない() {
        // テストデータの準備: 別のユーザーを作成
        User otherUser = new User("otherCognitoSub", "other@example.com");
        otherUser = userRepository.save(otherUser);

        // テストユーザーのメッセージを作成
        ChatMessage message1 = new ChatMessage(ChatMessage.Role.USER, "テストユーザーのメッセージ", testUser);
        chatMessageRepository.save(message1);

        // 別のユーザーのメッセージを作成
        ChatMessage message2 = new ChatMessage(ChatMessage.Role.USER, "別ユーザーのメッセージ", otherUser);
        chatMessageRepository.save(message2);

        // テスト実行: テストユーザーのメッセージのみを取得
        List<ChatMessage> messages = chatMessageRepository.findByUserOrderByCreatedAtDesc(testUser);

        // 検証: テストユーザーのメッセージのみが取得できることを確認
        assertEquals(1, messages.size());
        assertEquals("テストユーザーのメッセージ", messages.get(0).getContent());
        assertEquals(testUser, messages.get(0).getUser());
    }

    @Test
    @DisplayName("メッセージが存在しない場合は空のリストを返す")
    void findByUserOrderByCreatedAtDesc_メッセージが存在しない場合は空のリスト() {
        // テスト実行: メッセージが存在しないユーザーで取得
        List<ChatMessage> messages = chatMessageRepository.findByUserOrderByCreatedAtDesc(testUser);

        // 検証: 空のリストが返されることを確認
        assertTrue(messages.isEmpty());
    }

    @Test
    @DisplayName("複数のメッセージが時系列順（降順）で取得される")
    void findByUserOrderByCreatedAtDesc_時系列順で取得される() {
        // テストデータの準備: 複数のメッセージを作成
        ChatMessage message1 = new ChatMessage(ChatMessage.Role.USER, "1番目のメッセージ", testUser);
        chatMessageRepository.save(message1);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ChatMessage message2 = new ChatMessage(ChatMessage.Role.ASSISTANT, "2番目のメッセージ", testUser);
        chatMessageRepository.save(message2);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ChatMessage message3 = new ChatMessage(ChatMessage.Role.USER, "3番目のメッセージ", testUser);
        chatMessageRepository.save(message3);

        // テスト実行: メッセージを取得
        List<ChatMessage> messages = chatMessageRepository.findByUserOrderByCreatedAtDesc(testUser);

        // 検証: 3件のメッセージが取得できることを確認
        assertEquals(3, messages.size());
        // 降順（新しい順）で取得されることを確認
        assertEquals("3番目のメッセージ", messages.get(0).getContent());
        assertEquals("2番目のメッセージ", messages.get(1).getContent());
        assertEquals("1番目のメッセージ", messages.get(2).getContent());
    }
}

