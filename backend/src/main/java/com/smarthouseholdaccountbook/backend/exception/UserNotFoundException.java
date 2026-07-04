package com.smarthouseholdaccountbook.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * ユーザーが見つからない場合の例外
 * 404 Not Foundのステータスコードを返す
 */
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "ユーザーが見つかりませんでした");
    }
}
