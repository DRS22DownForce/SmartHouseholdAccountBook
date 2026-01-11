package com.example.backend.exception;

/**
 * OpenAI APIの利用枠（クォータ）を超過した場合の例外
 * 429 Too Many Requestsのステータスコードを返す
 */
public class QuotaExceededException extends RuntimeException {
    /**
     * デフォルトコンストラクタ
     */
    public QuotaExceededException() {
        super("OpenAI APIの利用枠（クォータ）を超過しました。しばらく時間をおいてから再度お試しください。");
    }

    /**
     * 原因となった例外を含むコンストラクタ
     * 
     * @param cause 原因となった例外
     */
    public QuotaExceededException(Throwable cause) {
        super("OpenAI APIの利用枠（クォータ）を超過しました。しばらく時間をおいてから再度お試しください。", cause);
    }
}
