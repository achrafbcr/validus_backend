package com.validus.backend.web.dto;

import com.validus.backend.domain.enums.PaymentBatchStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class PaymentBatchResponse {
    private UUID id;
    private String code;
    private PaymentBatchStatus status;
    private BigDecimal totalAmount;
    private String companyCode;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private List<PaymentBatchItemResponse> items;
}
