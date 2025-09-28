package com.validus.backend.common.exception;

import com.validus.backend.common.error.ErrorCode;

public class StateException extends BusinessException {

    public StateException(String message) {
        super(ErrorCode.INV_422_STATE, message);
    }
}
