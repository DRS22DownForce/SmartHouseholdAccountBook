package com.example.backend.exception;

/**
 * ユーザーが見つからない場合の例外
 * 404 Not Foundのステータスコードを返す
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("ユーザーが見つかりませんでした");
    }
}
