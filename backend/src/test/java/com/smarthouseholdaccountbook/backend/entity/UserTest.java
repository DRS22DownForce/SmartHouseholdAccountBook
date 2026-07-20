package com.smarthouseholdaccountbook.backend.entity;

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

    private static void assertUserConstructorThrows(
            String cognitoSub,
            Class<? extends Throwable> exceptionClass, String message) {
        assertThatThrownBy(() -> new User(cognitoSub))
                .isInstanceOf(exceptionClass)
                .hasMessage(message);
    }

    @Nested
    @DisplayName("コンストラクタ（正常系）")
    class ConstructorSuccess {
        @Test
        @DisplayName("正常なユーザーエンティティを作成できる")
        void createWithValidValues() {
            User user = new User(DEFAULT_COGNITO_SUB);

            assertThat(user).isNotNull();
            assertThat(user.getCognitoSub()).isEqualTo(DEFAULT_COGNITO_SUB);
        }
    }

    @Nested
    @DisplayName("コンストラクタ（異常系・cognitoSub）")
    class ConstructorFailureCognitoSub {
        @Test
        @DisplayName("cognitoSubがnullの場合は例外が発生する")
        void createWithNullCognitoSub() {
            assertUserConstructorThrows(
                    null,
                    NullPointerException.class, "cognitoSubはnullであってはなりません。");
        }

        @Test
        @DisplayName("cognitoSubが空文字列の場合は例外が発生する")
        void createWithEmptyCognitoSub() {
            assertUserConstructorThrows(
                    "",
                    IllegalArgumentException.class, "cognitoSubは空文字列であってはなりません。");
        }

        @Test
        @DisplayName("cognitoSubが空白のみの場合は例外が発生する")
        void createWithBlankCognitoSub() {
            assertUserConstructorThrows(
                    "   ",
                    IllegalArgumentException.class, "cognitoSubは空文字列であってはなりません。");
        }
    }
}
