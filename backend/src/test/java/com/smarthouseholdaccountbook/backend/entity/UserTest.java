package com.example.backend.entity;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Userエンティティのテストクラス
 *
 * ユーザーエンティティのバリデーションをテストします。
 */
class UserTest {

    private static final String DEFAULT_COGNITO_SUB = "cognitoSub123";
    private static final String DEFAULT_EMAIL = "test@example.com";

    private static void assertUserConstructorThrows(
            String cognitoSub, String email,
            Class<? extends Throwable> exceptionClass, String message) {
        assertThatThrownBy(() -> new User(cognitoSub, email))
                .isInstanceOf(exceptionClass)
                .hasMessage(message);
    }

    @Nested
    @DisplayName("コンストラクタ（正常系）")
    class ConstructorSuccess {
        @Test
        @DisplayName("正常なユーザーエンティティを作成できる")
        void createWithValidValues() {
            User user = new User(DEFAULT_COGNITO_SUB, DEFAULT_EMAIL);

            assertThat(user).isNotNull();
            assertThat(user.getCognitoSub()).isEqualTo(DEFAULT_COGNITO_SUB);
            assertThat(user.getEmail()).isEqualTo(DEFAULT_EMAIL);
        }

        @Test
        @DisplayName("様々な有効なメールアドレス形式で作成できる")
        void createWithVariousValidEmailFormats() {
            assertThat(new User(DEFAULT_COGNITO_SUB, "test@example.com").getEmail())
                    .isEqualTo("test@example.com");
            assertThat(new User(DEFAULT_COGNITO_SUB, "user.name@example.co.jp").getEmail())
                    .isEqualTo("user.name@example.co.jp");
            assertThat(new User(DEFAULT_COGNITO_SUB, "test+tag@example.com").getEmail())
                    .isEqualTo("test+tag@example.com");
        }
    }

    @Nested
    @DisplayName("コンストラクタ（異常系・cognitoSub）")
    class ConstructorFailureCognitoSub {
        @Test
        @DisplayName("cognitoSubがnullの場合は例外が発生する")
        void createWithNullCognitoSub() {
            assertUserConstructorThrows(
                    null, DEFAULT_EMAIL,
                    NullPointerException.class, "cognitoSubはnullであってはなりません。");
        }

        @Test
        @DisplayName("cognitoSubが空文字列の場合は例外が発生する")
        void createWithEmptyCognitoSub() {
            assertUserConstructorThrows(
                    "", DEFAULT_EMAIL,
                    IllegalArgumentException.class, "cognitoSubは空文字列であってはなりません。");
        }

        @Test
        @DisplayName("cognitoSubが空白のみの場合は例外が発生する")
        void createWithBlankCognitoSub() {
            assertUserConstructorThrows(
                    "   ", DEFAULT_EMAIL,
                    IllegalArgumentException.class, "cognitoSubは空文字列であってはなりません。");
        }
    }

    @Nested
    @DisplayName("コンストラクタ（異常系・email）")
    class ConstructorFailureEmail {
        @Test
        @DisplayName("emailがnullの場合は例外が発生する")
        void createWithNullEmail() {
            assertUserConstructorThrows(
                    DEFAULT_COGNITO_SUB, null,
                    NullPointerException.class, "emailはnullであってはなりません。");
        }

        @Test
        @DisplayName("emailが空文字列の場合は例外が発生する")
        void createWithEmptyEmail() {
            assertUserConstructorThrows(
                    DEFAULT_COGNITO_SUB, "",
                    IllegalArgumentException.class, "emailは空文字列であってはなりません。");
        }

        @Test
        @DisplayName("emailが空白のみの場合は例外が発生する")
        void createWithBlankEmail() {
            assertUserConstructorThrows(
                    DEFAULT_COGNITO_SUB, "   ",
                    IllegalArgumentException.class, "emailは空文字列であってはなりません。");
        }

        @Test
        @DisplayName("emailに@が含まれていない場合は例外が発生する")
        void createWithInvalidEmailFormat() {
            assertUserConstructorThrows(
                    DEFAULT_COGNITO_SUB, "invalidemail.com",
                    IllegalArgumentException.class, "メールアドレスの形式が正しくありません。");
        }
    }
}
