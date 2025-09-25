package com.validus.backend.common.exception;

import com.validus.backend.common.error.ErrorCode;
import java.util.Map;

public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(ErrorCode.INV_400_VAL, message);
    }

    public ValidationException(String message, Map<String, Object> details) {
        super(ErrorCode.INV_400_VAL, message, details);
    }
}
