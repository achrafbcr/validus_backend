package com.validus.backend.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class InvoiceLineDto {

    private Long id;

    @NotBlank
    private String designation;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal quantity;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal unitPrice;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal taxRate;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amountLine;

    private String costCenter;
    private String glAccount;
}
