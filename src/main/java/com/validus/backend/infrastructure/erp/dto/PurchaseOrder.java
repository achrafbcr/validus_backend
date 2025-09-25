package com.validus.backend.infrastructure.erp.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PurchaseOrder {
    String number;
    String supplierId;
    String currency;
    BigDecimal remainingAmount;
}
