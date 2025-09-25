package com.validus.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class PaymentBatchRequest {
    @NotBlank
    private String companyCode;

    @NotEmpty
    private List<UUID> invoiceIds;
}
