package com.validus.backend.web.exception;

import com.validus.backend.common.error.ErrorCode;
import com.validus.backend.common.error.ErrorResponse;
import com.validus.backend.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        log.warn("Business exception: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .code(ex.getCode())
            .message(ex.getMessage())
            .details(ex.getDetails().isEmpty() ? Collections.emptyList() :
                Collections.singletonList(ex.getDetails()))
            .build();
        return ResponseEntity.status(resolveStatus(ex.getCode())).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        ErrorResponse response = ErrorResponse.builder()
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .code(ErrorCode.INV_400_VAL)
            .message("Validation error")
            .details(bindingResult.getFieldErrors().stream()
                .map(error -> Map.of(
                    "field", error.getField(),
                    "message", resolveMessage(error)
                ))
                .toList())
            .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        ErrorResponse response = ErrorResponse.builder()
            .timestamp(Instant.now())
            .path(request.getDescription(false))
            .code(ErrorCode.INV_500_ERP)
            .message("Internal server error")
            .details(Collections.emptyList())
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String resolveMessage(FieldError error) {
        try {
            return messageSource.getMessage(error, error.getDefaultMessage());
        } catch (Exception e) {
            return error.getDefaultMessage();
        }
    }

    private HttpStatus resolveStatus(ErrorCode code) {
        return switch (code) {
            case INV_400_VAL -> HttpStatus.BAD_REQUEST;
            case INV_403_AUTH -> HttpStatus.FORBIDDEN;
            case INV_404_NOTFOUND -> HttpStatus.NOT_FOUND;
            case INV_409_DUP -> HttpStatus.CONFLICT;
            case INV_422_STATE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case INV_500_ERP, INV_500_AI -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
