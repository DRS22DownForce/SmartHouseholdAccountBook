package com.example.backend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Userエンティティのテストクラス
 * 
 * ユーザーエンティティのバリデーションをテストします。
 * ユーザーはCognitoのsubとメールアドレスを持ちます。
 */
class UserTest {

    @Test
    @DisplayName("正常なユーザーエンティティを作成できる")
    void createUser_正常な値() {
        // テストデータの準備: 有効なcognitoSubとemail
        String cognitoSub = "cognitoSub123";
        String email = "test@example.com";

        // テスト実行: Userエンティティを作成
        User user = new User(cognitoSub, email);

        // 検証: 正常に作成され、値が正しく設定されていることを確認
        assertNotNull(user);
        assertEquals("cognitoSub123", user.getCognitoSub());
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    @DisplayName("cognitoSubがnullなら例外")
    void createUser_cognitoSubがnullなら例外() {
        // テストデータの準備
        String email = "test@example.com";

        // テスト実行と検証: nullのcognitoSubを渡すと例外が発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new User(null, email));

        // 例外メッセージが正しいことを確認
        assertEquals("cognitoSubは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("cognitoSubが空文字列なら例外")
    void createUser_cognitoSubが空文字列なら例外() {
        // テストデータの準備
        String email = "test@example.com";

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new User("", email));

        assertEquals("cognitoSubは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("cognitoSubが空白のみなら例外")
    void createUser_cognitoSubが空白のみなら例外() {
        // テストデータの準備
        String email = "test@example.com";

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new User("   ", email));

        assertEquals("cognitoSubは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("emailがnullなら例外")
    void createUser_emailがnullなら例外() {
        // テストデータの準備
        String cognitoSub = "cognitoSub123";

        // テスト実行と検証: nullのemailを渡すと例外が発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new User(cognitoSub, null));

        assertEquals("emailは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("emailが空文字列なら例外")
    void createUser_emailが空文字列なら例外() {
        // テストデータの準備
        String cognitoSub = "cognitoSub123";

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new User(cognitoSub, ""));

        assertEquals("emailは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("emailが空白のみなら例外")
    void createUser_emailが空白のみなら例外() {
        // テストデータの準備
        String cognitoSub = "cognitoSub123";

        // テスト実行と検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new User(cognitoSub, "   "));

        assertEquals("emailは必須です。", exception.getMessage());
    }

    @Test
    @DisplayName("emailに@が含まれていない場合は例外")
    void createUser_emailにアットマークが含まれていないなら例外() {
        // テストデータの準備: @が含まれていない無効なメールアドレス
        String cognitoSub = "cognitoSub123";
        String invalidEmail = "invalidemail.com";

        // テスト実行と検証: @が含まれていないメールアドレスを渡すと例外が発生することを確認
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new User(cognitoSub, invalidEmail));

        assertEquals("メールアドレスの形式が正しくありません。", exception.getMessage());
    }

    @Test
    @DisplayName("正常なメールアドレス形式なら作成できる")
    void createUser_正常なメールアドレス形式() {
        // テストデータの準備: 様々な有効なメールアドレス形式
        String cognitoSub = "cognitoSub123";

        // テスト実行と検証: 様々な有効なメールアドレス形式で作成できることを確認
        User user1 = new User(cognitoSub, "test@example.com");
        assertNotNull(user1);
        assertEquals("test@example.com", user1.getEmail());

        User user2 = new User(cognitoSub, "user.name@example.co.jp");
        assertNotNull(user2);
        assertEquals("user.name@example.co.jp", user2.getEmail());

        User user3 = new User(cognitoSub, "test+tag@example.com");
        assertNotNull(user3);
        assertEquals("test+tag@example.com", user3.getEmail());
    }
}

