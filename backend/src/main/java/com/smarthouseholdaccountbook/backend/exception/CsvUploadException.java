package com.example.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * CSVアップロード処理でエラーが発生した場合の例外
 * 
 * ファイルの読み込みエラーやCSV処理中のエラーを表現します。
 * HTTPステータスコードを保持して、適切なエラーレスポンスを返すために使用されます。
 */
public class CsvUploadException extends RuntimeException {
    private final HttpStatus httpStatus;

    /**
     * メッセージとHTTPステータスコードを指定するコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param httpStatus HTTPステータスコード（400 Bad Request または 500 Internal Server Error）
     */
    public CsvUploadException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    /**
     * 原因となった例外とHTTPステータスコードを指定するコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     * @param httpStatus HTTPステータスコード
     */
    public CsvUploadException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    /**
     * HTTPステータスコードを取得
     * 
     * @return HTTPステータスコード
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
