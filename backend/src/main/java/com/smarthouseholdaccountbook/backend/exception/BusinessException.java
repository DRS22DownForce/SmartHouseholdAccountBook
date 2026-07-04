package com.smarthouseholdaccountbook.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * APIとして想定済みの業務エラーを表す基底例外。
 *
 * サービス層は「何が起きたか」を例外で表現し、
 * GlobalExceptionHandler がこのステータスを使って HTTP レスポンスへ変換します。
 */
public abstract class BusinessException extends RuntimeException {
    private final HttpStatus status;

    protected BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    protected BusinessException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
