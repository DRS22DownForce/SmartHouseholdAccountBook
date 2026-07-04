package com.smarthouseholdaccountbook.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * AIサービスとの通信でエラーが発生した場合の例外
 * 502 Bad Gatewayのステータスコードを返す
 */
public class AiServiceException extends BusinessException {
    /**
     * デフォルトコンストラクタ
     */
    public AiServiceException() {
        super(HttpStatus.BAD_GATEWAY, "AIサービスとの通信でエラーが発生しました。");
    }

    /**
     * メッセージを指定するコンストラクタ
     * 
     * @param message エラーメッセージ
     */
    public AiServiceException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }

    /**
     * 原因となった例外を含むコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public AiServiceException(String message, Throwable cause) {
        super(HttpStatus.BAD_GATEWAY, message, cause);
    }

    /**
     * 原因となった例外を含むコンストラクタ
     * 
     * @param cause 原因となった例外
     */
    public AiServiceException(Throwable cause) {
        super(HttpStatus.BAD_GATEWAY, "AIサービスとの通信でエラーが発生しました。", cause);
    }
}
