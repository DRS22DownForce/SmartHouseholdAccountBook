package com.smarthouseholdaccountbook.backend.exception;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.List;

/**
 * Controllerで発生した例外を RFC 9457 Problem Details 形式へ変換するグローバル例外ハンドラー。
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String VALIDATION_ERROR_DETAIL = "入力内容を確認してください。";
    private static final String UNEXPECTED_ERROR_DETAIL = "予期しないエラーが発生しました。";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request) {
        logByStatus(exception.getStatus(), exception);

        ProblemDetail problemDetail = buildProblemDetail(
                exception.getStatus(),
                exception.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(exception.getStatus()).body(problemDetail);
    }

    // TODO: 入力・ドメイン検証用のプロジェクト例外を導入したら、
    // IllegalArgumentException の一括400変換は段階的に縮小する。
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request) {
        logger.warn("不正な引数が渡されました: {}", exception.getMessage());

        ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI());

        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ProblemDetail> handleRequestNotPermitted(
            RequestNotPermitted exception,
            HttpServletRequest request) {
        logger.warn("リクエスト数が上限を超えました: {}", exception.getMessage());

        ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                "リクエスト数が上限を超えました。しばらく待ってから再試行してください。",
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(problemDetail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        logger.warn("リクエストパラメータの検証に失敗しました: {}", exception.getMessage());

        List<ValidationError> errors = exception.getConstraintViolations().stream()
                .map(violation -> new ValidationError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .toList();

        ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                VALIDATION_ERROR_DETAIL,
                request.getRequestURI());
        problemDetail.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {
        logger.error("予期しないエラーが発生しました", exception);

        ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                UNEXPECTED_ERROR_DETAIL,
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    /**
     * バリデーション系の例外ではProblem Details の errors プロパティにバリデーションエラーのフィールド別詳細を格納するため、
     * Spring標準の例外ハンドラーをオーバーライドする。
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        logger.warn("リクエストボディの検証に失敗しました: {}", exception.getMessage());

        List<ValidationError> errors = exception.getBindingResult().getAllErrors().stream()
                .map(this::toValidationError)
                .toList();

        ProblemDetail problemDetail = buildProblemDetail(
                status,
                VALIDATION_ERROR_DETAIL,
                getRequestUri(request));
        problemDetail.setProperty("errors", errors);

        return handleExceptionInternal(exception, problemDetail, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        logger.warn("リクエストパラメータの検証に失敗しました: {}", exception.getMessage());

        List<ValidationError> errors = exception.getAllErrors().stream()
                .map(error -> new ValidationError(
                        error.getCodes() != null && error.getCodes().length > 0 ? error.getCodes()[0] : "request",
                        defaultMessage(error)))
                .toList();

        ProblemDetail problemDetail = buildProblemDetail(
                status,
                VALIDATION_ERROR_DETAIL,
                getRequestUri(request));
        problemDetail.setProperty("errors", errors);

        return handleExceptionInternal(exception, problemDetail, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problemDetail = buildProblemDetail(
                status,
                "Content-Typeを確認してください。",
                getRequestUri(request));

        return handleExceptionInternal(exception, problemDetail, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problemDetail = buildProblemDetail(
                status,
                "リクエストボディの形式を確認してください。",
                getRequestUri(request));

        return handleExceptionInternal(exception, problemDetail, headers, status, request);
    }

    private ProblemDetail buildProblemDetail(HttpStatusCode status, String detail, String requestUri) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(reasonPhrase(status));
        problemDetail.setInstance(URI.create(requestUri));
        return problemDetail;
    }

    private ValidationError toValidationError(ObjectError error) {
        String field = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
        return new ValidationError(field, defaultMessage(error));
    }

    private String defaultMessage(ObjectError error) {
        return error.getDefaultMessage() != null ? error.getDefaultMessage() : "値が不正です。";
    }

    private String defaultMessage(MessageSourceResolvable error) {
        return error.getDefaultMessage() != null ? error.getDefaultMessage() : "値が不正です。";
    }

    private String getRequestUri(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return "/";
    }

    private String reasonPhrase(HttpStatusCode status) {
        HttpStatus httpStatus = HttpStatus.resolve(status.value());
        return httpStatus != null ? httpStatus.getReasonPhrase() : "HTTP " + status.value();
    }

    private void logByStatus(HttpStatus status, BusinessException exception) {
        if (status.is5xxServerError()) {
            logger.error("業務例外が発生しました: {}", exception.getMessage(), exception);
            return;
        }
        logger.warn("業務例外が発生しました: {}", exception.getMessage());
    }
    // Problem Details の errors プロパティに格納するバリデーションエラーのフィールド別詳細用DTO。
    private record ValidationError(String field, String message) {
    }
}
