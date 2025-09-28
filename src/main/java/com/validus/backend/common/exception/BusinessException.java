package com.validus.backend.common.exception;

import com.validus.backend.common.error.ErrorCode;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode code;
    private final Map<String, Object> details;

    public BusinessException(ErrorCode code, String message) {
        this(code, message, Collections.emptyMap());
    }

    public BusinessException(ErrorCode code, String message, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.details = details;
    }
}
