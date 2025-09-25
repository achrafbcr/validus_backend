package com.validus.backend.common.exception;

import com.validus.backend.common.error.ErrorCode;

public class DuplicateInvoiceException extends BusinessException {

    public DuplicateInvoiceException(String message) {
        super(ErrorCode.INV_409_DUP, message);
    }
}
