package com.example.backend.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.example.backend.generated.model.ErrorResponse;
import com.example.backend.generated.model.CsvUploadResponseDto;
import com.example.backend.generated.model.CsvUploadResponseDtoErrorsInner;
import java.time.OffsetDateTime;
import java.util.List;
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
                .body(new ErrorResponse(e.getMessage(), OffsetDateTime.now()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage(), OffsetDateTime.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("不正な引数が渡されました: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage(), OffsetDateTime.now()));
    }

    /**
     * OpenAI APIの利用枠（クォータ）超過エラーを処理
     * 429 Too Many Requestsを返す
     */
    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<ErrorResponse> handleQuotaExceededException(QuotaExceededException e) {
        logger.warn("OpenAI APIの利用枠を超過しました: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse(e.getMessage(), OffsetDateTime.now()));
    }

    /**
     * AIサービスとの通信エラーを処理
     * 500 Internal Server Errorを返す
     */
    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ErrorResponse> handleAiServiceException(AiServiceException e) {
        logger.error("AIサービスとの通信でエラーが発生しました: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(e.getMessage(), OffsetDateTime.now()));
    }

    /**
     * CSVアップロード処理で発生したエラーを処理
     * 
     * CSVアップロード専用のレスポンス形式（CsvUploadResponseDto）で返します。
     * 例外に含まれるHTTPステータスコードに応じて、適切なステータスコードを返します。
     * 
     * @param e CSVアップロード例外
     * @return CSVアップロードエラーレスポンス
     */
    @ExceptionHandler(CsvUploadException.class)
    public ResponseEntity<CsvUploadResponseDto> handleCsvUploadException(CsvUploadException e) {
        if (e.getHttpStatus() == HttpStatus.BAD_REQUEST) {
            logger.warn("CSVファイルの読み込みに失敗しました: {}", e.getMessage());
        } else {
            logger.error("CSVの処理中にエラーが発生しました: {}", e.getMessage(), e);
        }

        CsvUploadResponseDtoErrorsInner error = new CsvUploadResponseDtoErrorsInner(0, e.getMessage());
        error.setLineContent("");
        CsvUploadResponseDto response = new CsvUploadResponseDto(0, 0, List.of(error));

        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

}
