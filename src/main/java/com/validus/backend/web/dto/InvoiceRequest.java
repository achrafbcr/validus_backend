package com.validus.backend.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class InvoiceRequest {

    @NotBlank
    private String number;

    @NotBlank
    private String supplierId;

    @NotBlank
    private String companyCode;

    private String poNumber;

    @NotNull
    private LocalDate invoiceDate;

    @NotNull
    private LocalDate dueDate;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$")
    private String currency;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amountHT;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amountTVA;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amountTTC;

    private boolean hasPaperOriginal;

    @Size(min = 1)
    @Valid
    private List<InvoiceLineDto> lines;

    private String hashContent;
}
