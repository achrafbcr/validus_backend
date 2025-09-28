package com.validus.backend.domain.model;

import com.validus.backend.domain.enums.InvoiceStatus;
import com.validus.backend.infrastructure.persistence.converter.JsonMapConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "invoices",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_invoice_supplier_number_year", columnNames = {"supplier_id", "number", "invoice_year"})
    })
public class Invoice {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "number", nullable = false, length = 50)
    private String number;

    @Column(name = "supplier_id", nullable = false, length = 36)
    private String supplierId;

    @Column(name = "company_code", nullable = false, length = 10)
    private String companyCode;

    @Column(name = "po_number", length = 50)
    private String poNumber;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "amount_ht", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountHT;

    @Column(name = "amount_tva", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountTVA;

    @Column(name = "amount_ttc", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountTTC;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "workflow_id", length = 36)
    private String workflowId;

    @Column(name = "yardi_txn_id", length = 50)
    private String yardiTxnId;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "hash_content", length = 128)
    private String hashContent;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "flags", columnDefinition = "nvarchar(max)")
    private Map<String, Object> flags = new HashMap<>();

    @Column(name = "has_paper_original")
    private boolean hasPaperOriginal;

    @Column(name = "invoice_year", nullable = false)
    private Integer invoiceYear;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Attachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AuditLog> auditLogs = new ArrayList<>();

    @Version
    private Long version;

    public void addLine(InvoiceLine line) {
        line.setInvoice(this);
        this.lines.add(line);
    }

    public void addAttachment(Attachment attachment) {
        attachment.setInvoice(this);
        this.attachments.add(attachment);
    }

    public void addAuditLog(AuditLog auditLog) {
        auditLog.setInvoice(this);
        this.auditLogs.add(auditLog);
    }

    public void ensureTotalsConsistency() {
        if (amountHT == null || amountTVA == null) {
            return;
        }
        this.amountTTC = amountHT.add(amountTVA);
    }

    public boolean isReadOnlyAfterPayment() {
        return InvoiceStatus.PAID.equals(this.status);
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
        if (invoiceDate != null) {
            this.invoiceYear = invoiceDate.getYear();
        }
    }
}
