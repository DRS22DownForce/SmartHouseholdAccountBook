package com.example.backend.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.example.backend.generated.model.ErrorResponse;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ControllerAdviceアノテーションにより全てのControllerで発生する例外を処理するグローバル例外ハンドラー
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExpenseNotFoundException(ExpenseNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage(), Instant.now().atOffset(ZoneOffset.UTC)));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage(), Instant.now().atOffset(ZoneOffset.UTC)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("不正な引数が渡されました: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage(), Instant.now().atOffset(ZoneOffset.UTC)));
    }

    /**
     * OpenAI APIの利用枠（クォータ）超過エラーを処理
     * 429 Too Many Requestsを返す
     */
    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<ErrorResponse> handleQuotaExceededException(QuotaExceededException e) {
        logger.warn("OpenAI APIの利用枠を超過しました: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse(e.getMessage(), Instant.now().atOffset(ZoneOffset.UTC)));
    }

    /**
     * AIサービスとの通信エラーを処理
     * 500 Internal Server Errorを返す
     */
    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ErrorResponse> handleAiServiceException(AiServiceException e) {
        logger.error("AIサービスとの通信でエラーが発生しました: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(e.getMessage(), Instant.now().atOffset(ZoneOffset.UTC)));
    }

    /**
     * CSVアップロード処理で発生したエラーを処理
     * 
     * バリデーションエラー（BAD_REQUEST）の場合はErrorResponseを返します。
     * 処理中のエラー（INTERNAL_SERVER_ERROR）の場合もErrorResponseを返します。
     * 
     * 部分成功の場合は、ExpenseControllerでCsvUploadResponseDtoを返すため、
     * このハンドラーは処理全体が失敗した場合のみ処理します。
     * 
     * @param e CSVアップロード例外
     * @return エラーレスポンス
     */
    @ExceptionHandler(CsvUploadException.class)
    public ResponseEntity<ErrorResponse> handleCsvUploadException(CsvUploadException e) {
        if (e.getHttpStatus() == HttpStatus.BAD_REQUEST) {
            logger.warn("CSVファイルの読み込みに失敗しました: {}", e.getMessage());
        } else {
            logger.error("CSVの処理中にエラーが発生しました: {}", e.getMessage(), e);
        }

        // OpenAPI定義に合わせてErrorResponseを返す
        return ResponseEntity.status(e.getHttpStatus())
                .body(new ErrorResponse(e.getMessage(), Instant.now().atOffset(ZoneOffset.UTC)));
    }

}
