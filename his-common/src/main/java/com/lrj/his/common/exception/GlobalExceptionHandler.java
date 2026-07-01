package com.lrj.his.common.exception;

import com.lrj.his.common.web.Result;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 统一异常处理。各服务无需重复, import 即生效。
 * HTTP 状态始终配合 Result.code 表达细粒度业务码。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException ex) {
        ResultCode rc = ex.getResultCode();
        log.warn("业务异常 code={} msg={}", rc.getCode(), ex.getMessage());
        return ResponseEntity.status(httpStatusOf(rc))
                .body(Result.fail(rc.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatField)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Result.fail(ResultCode.BAD_REQUEST, msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraint(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(Result.fail(ResultCode.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception ex) {
        log.error("未捕获异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(ResultCode.INTERNAL_ERROR));
    }

    private static String formatField(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }

    private static HttpStatus httpStatusOf(ResultCode rc) {
        return switch (rc) {
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND, PATIENT_NOT_FOUND, DICTIONARY_NOT_FOUND, SCHEDULE_NOT_FOUND,
                 ENCOUNTER_NOT_FOUND, ORDER_NOT_FOUND, INVOICE_NOT_FOUND, DOCUMENT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT, SLOT_SOLD_OUT, APPOINTMENT_DUPLICATED, PATIENT_DUPLICATED,
                 ORDER_STATE_ILLEGAL, INVOICE_ALREADY_PAID -> HttpStatus.CONFLICT;
            case INTERNAL_ERROR, REMOTE_CALL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
