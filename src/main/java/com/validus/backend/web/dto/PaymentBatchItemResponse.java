package com.validus.backend.web.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class PaymentBatchItemResponse {
    private Long id;
    private UUID invoiceId;
    private BigDecimal amount;
    private String supplierId;
}
