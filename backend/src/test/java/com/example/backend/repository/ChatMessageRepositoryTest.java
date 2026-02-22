package com.example.backend.repository;

import com.example.backend.entity.ChatMessage;
import com.example.backend.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        testUser = userRepository.save(new User("cognitoSub", "test@example.com"));
    }

    private ChatMessage createMessage(ChatMessage.Role role, String content, User user) {
        return chatMessageRepository.save(new ChatMessage(role, content, user));
    }

    @Nested
    @DisplayName("findByUserOrderByCreatedAtDesc - ユーザー指定でメッセージ取得（作成日時降順）")
    class FindByUserOrderByCreatedAtDesc {

        @Test
        @DisplayName("ユーザーを指定してメッセージを取得できる")
        void returnsMessagesForUser() {
            // given
            createMessage(ChatMessage.Role.USER, "こんにちは", testUser);
            createMessage(ChatMessage.Role.ASSISTANT, "こんにちは！何かお手伝いできることはありますか？", testUser);

            // when
            List<ChatMessage> messages = chatMessageRepository.findByUserOrderByCreatedAtDesc(testUser);

            // then
            assertThat(messages).hasSize(2);
            assertThat(messages)
                    .extracting(ChatMessage::getCreatedAt)
                    .isSortedAccordingTo(Comparator.reverseOrder());
        }

        @Test
        @DisplayName("ユーザーを指定してメッセージを取得する際、他のユーザーのメッセージは含まれない")
        void excludesOtherUsersMessages() {
            // given
            User otherUser = userRepository.save(new User("otherCognitoSub", "other@example.com"));
            createMessage(ChatMessage.Role.USER, "テストユーザーのメッセージ", testUser);
            createMessage(ChatMessage.Role.USER, "別ユーザーのメッセージ", otherUser);

            // when
            List<ChatMessage> messages = chatMessageRepository.findByUserOrderByCreatedAtDesc(testUser);

            // then
            assertThat(messages).hasSize(1);
            assertThat(messages.get(0).getContent()).isEqualTo("テストユーザーのメッセージ");
            assertThat(messages.get(0).getUser()).isEqualTo(testUser);
        }

        @Test
        @DisplayName("メッセージが存在しない場合は空のリストを返す")
        void returnsEmptyWhenNoMessages() {
            // when
            List<ChatMessage> messages = chatMessageRepository.findByUserOrderByCreatedAtDesc(testUser);

            // then
            assertThat(messages).isEmpty();
        }

        @Test
        @DisplayName("複数のメッセージが時系列順（降順）で取得される")
        void returnsMessagesSortedByCreatedAtDesc() {
            // given
            createMessage(ChatMessage.Role.USER, "1番目のメッセージ", testUser);
            createMessage(ChatMessage.Role.ASSISTANT, "2番目のメッセージ", testUser);
            createMessage(ChatMessage.Role.USER, "3番目のメッセージ", testUser);

            // when
            List<ChatMessage> messages = chatMessageRepository.findByUserOrderByCreatedAtDesc(testUser);

            // then
            assertThat(messages).hasSize(3);
            assertThat(messages)
                    .extracting(ChatMessage::getCreatedAt)
                    .isSortedAccordingTo(Comparator.reverseOrder());
        }
    }
}
