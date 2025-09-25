package com.validus.backend.web.dto;

import com.validus.backend.domain.enums.InvoiceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class InvoiceResponse {
    private UUID id;
    private String number;
    private String supplierId;
    private String companyCode;
    private String poNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String currency;
    private BigDecimal amountHT;
    private BigDecimal amountTVA;
    private BigDecimal amountTTC;
    private InvoiceStatus status;
    private String workflowId;
    private String yardiTxnId;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean hasPaperOriginal;
    private List<InvoiceLineDto> lines;
}
