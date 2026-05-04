package com.taskflow.common.web;

import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.dto.ErrorPayload;
import com.taskflow.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mỗi service extends class này và đặt @RestControllerAdvice ở subclass.
 * Pattern: cho phép service override / thêm handler riêng nếu cần.
 */
@Slf4j
public abstract class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<ErrorPayload>> handleBase(BaseException ex) {
        log.warn("Business exception: code={}, status={}, params={}", ex.getCode(), ex.getStatus(), ex.getParams());
        ErrorPayload payload = ErrorPayload.of(ex.getCode(), ex.getParams().isEmpty() ? ex.getMessage() : ex.getParams());
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getStatus(), ex.getMessage(), payload));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorPayload>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        ErrorPayload payload = ErrorPayload.of("validation_error", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "Validation Failed", payload));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorPayload>> handleAll(Exception ex) {
        log.error("Unhandled exception", ex);
        ErrorPayload payload = ErrorPayload.of("internal_server_error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Internal Server Error", payload));
    }
}
