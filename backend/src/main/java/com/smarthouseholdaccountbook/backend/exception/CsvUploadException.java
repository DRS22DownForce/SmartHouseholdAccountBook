package com.smarthouseholdaccountbook.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * CSVアップロード処理でエラーが発生した場合の例外
 * 
 * ファイル読み込みなど、リクエストとして受け取ったCSVを処理できない場合に使用します。
 */
public class CsvUploadException extends BusinessException {
    /**
     * メッセージを指定するコンストラクタ
     * 
     * @param message エラーメッセージ
     */
    public CsvUploadException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 原因となった例外を指定するコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public CsvUploadException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
    }
}
