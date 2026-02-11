package com.example.backend.exception;

/**
 * AIサービスとの通信でエラーが発生した場合の例外
 * 500 Internal Server Errorのステータスコードを返す
 */
public class AiServiceException extends RuntimeException {
    /**
     * デフォルトコンストラクタ
     */
    public AiServiceException() {
        super("AIサービスとの通信でエラーが発生しました。");
    }

    /**
     * メッセージを指定するコンストラクタ
     * 
     * @param message エラーメッセージ
     */
    public AiServiceException(String message) {
        super(message);
    }

    /**
     * 原因となった例外を含むコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 原因となった例外を含むコンストラクタ
     * 
     * @param cause 原因となった例外
     */
    public AiServiceException(Throwable cause) {
        super("AIサービスとの通信でエラーが発生しました。", cause);
    }
}
