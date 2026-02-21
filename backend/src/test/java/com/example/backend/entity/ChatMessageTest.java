package com.example.backend.entity;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ChatMessageエンティティのテストクラス
 *
 * チャットメッセージエンティティのバリデーションをテストします。
 */
class ChatMessageTest {

    private static User defaultUser() {
        return new User("cognitoSub", "test@example.com");
    }

    private static void assertChatMessageConstructorThrows(
            ChatMessage.Role role, String content, User user,
            Class<? extends Throwable> exceptionClass, String message) {
        assertThatThrownBy(() -> new ChatMessage(role, content, user))
                .isInstanceOf(exceptionClass)
                .hasMessage(message);
    }

    @Nested
    @DisplayName("コンストラクタ（正常系）")
    class ConstructorSuccess {
        @Test
        @DisplayName("正常なチャットメッセージを作成できる（USERロール）")
        void createWithValidValuesUserRole() {
            User user = defaultUser();
            String content = "こんにちは";

            ChatMessage message = new ChatMessage(ChatMessage.Role.USER, content, user);

            assertThat(message).isNotNull();
            assertThat(message.getRole()).isEqualTo(ChatMessage.Role.USER);
            assertThat(message.getContent()).isEqualTo("こんにちは");
            assertThat(message.getUser()).isEqualTo(user);
            assertThat(message.getCreatedAt()).isNotNull();
            assertThat(message.getCreatedAt())
                    .isBefore(LocalDateTime.now().plusSeconds(1))
                    .isAfter(LocalDateTime.now().minusSeconds(1));
        }

        @Test
        @DisplayName("正常なチャットメッセージを作成できる（ASSISTANTロール）")
        void createWithValidValuesAssistantRole() {
            User user = defaultUser();
            String content = "こんにちは！何かお手伝いできることはありますか？";

            ChatMessage message = new ChatMessage(ChatMessage.Role.ASSISTANT, content, user);

            assertThat(message).isNotNull();
            assertThat(message.getRole()).isEqualTo(ChatMessage.Role.ASSISTANT);
            assertThat(message.getContent()).isEqualTo(content);
            assertThat(message.getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("正常なチャットメッセージを作成できる（SYSTEMロール）")
        void createWithValidValuesSystemRole() {
            User user = defaultUser();
            String content = "システムメッセージ";

            ChatMessage message = new ChatMessage(ChatMessage.Role.SYSTEM, content, user);

            assertThat(message).isNotNull();
            assertThat(message.getRole()).isEqualTo(ChatMessage.Role.SYSTEM);
            assertThat(message.getContent()).isEqualTo("システムメッセージ");
            assertThat(message.getUser()).isEqualTo(user);
        }
    }

    @Nested
    @DisplayName("コンストラクタ（異常系・ロール）")
    class ConstructorFailureRole {
        @Test
        @DisplayName("ロールがnullの場合は例外が発生する")
        void createWithNullRole() {
            assertChatMessageConstructorThrows(
                    null, "テストメッセージ", defaultUser(),
                    NullPointerException.class, "ロールはnullであってはなりません。");
        }
    }

    @Nested
    @DisplayName("コンストラクタ（異常系・メッセージ内容）")
    class ConstructorFailureContent {
        @Test
        @DisplayName("メッセージ内容がnullの場合は例外が発生する")
        void createWithNullContent() {
            assertChatMessageConstructorThrows(
                    ChatMessage.Role.USER, null, defaultUser(),
                    NullPointerException.class, "メッセージ内容はnullであってはなりません。");
        }

        @Test
        @DisplayName("メッセージ内容が空文字列の場合は例外が発生する")
        void createWithEmptyContent() {
            assertChatMessageConstructorThrows(
                    ChatMessage.Role.USER, "", defaultUser(),
                    IllegalArgumentException.class, "メッセージ内容は空文字列であってはなりません。");
        }

        @Test
        @DisplayName("メッセージ内容が空白のみの場合は例外が発生する")
        void createWithBlankContent() {
            assertChatMessageConstructorThrows(
                    ChatMessage.Role.USER, "   ", defaultUser(),
                    IllegalArgumentException.class, "メッセージ内容は空文字列であってはなりません。");
        }
    }

    @Nested
    @DisplayName("コンストラクタ（異常系・ユーザー）")
    class ConstructorFailureUser {
        @Test
        @DisplayName("ユーザーがnullの場合は例外が発生する")
        void createWithNullUser() {
            assertChatMessageConstructorThrows(
                    ChatMessage.Role.USER, "テストメッセージ", null,
                    NullPointerException.class, "ユーザーはnullであってはなりません。");
        }
    }
}
