package com.validus.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "invoices", indexes = {
  @Index(name = "idx_invoice_number", columnList = "invoiceNumber", unique = true),
  @Index(name = "idx_supplier", columnList = "supplierName")
})
public class Invoice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  private String supplierName;

  @NotBlank
  private String invoiceNumber;

  @NotBlank
  private String company;

  @NotNull
  @DecimalMin("0.0")
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  private InvoiceStatus status = InvoiceStatus.RECEIVED;

  private Instant createdAt = Instant.now();
  private Instant updatedAt = Instant.now();

  @PreUpdate
  public void preUpdate() { this.updatedAt = Instant.now(); }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getSupplierName() { return supplierName; }
  public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
  public String getInvoiceNumber() { return invoiceNumber; }
  public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
  public String getCompany() { return company; }
  public void setCompany(String company) { this.company = company; }
  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }
  public InvoiceStatus getStatus() { return status; }
  public void setStatus(InvoiceStatus status) { this.status = status; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
