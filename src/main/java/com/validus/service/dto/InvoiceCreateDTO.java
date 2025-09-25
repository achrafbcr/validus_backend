package com.validus.service.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class InvoiceCreateDTO {
  @NotBlank public String supplierName;
  @NotBlank public String invoiceNumber;
  @NotBlank public String company;
  @NotNull @DecimalMin("0.0") public BigDecimal amount;
}
