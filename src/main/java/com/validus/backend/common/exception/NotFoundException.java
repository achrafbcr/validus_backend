package com.validus.backend.common.exception;

import com.validus.backend.common.error.ErrorCode;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(ErrorCode.INV_404_NOTFOUND, message);
    }
}
