package com.validus.backend.application.service;

import com.validus.backend.common.exception.NotFoundException;
import com.validus.backend.common.exception.StateException;
import com.validus.backend.common.exception.ValidationException;
import com.validus.backend.domain.enums.AuditAction;
import com.validus.backend.domain.enums.InvoiceStatus;
import com.validus.backend.domain.enums.PaymentBatchStatus;
import com.validus.backend.domain.model.Invoice;
import com.validus.backend.domain.model.PaymentBatch;
import com.validus.backend.domain.model.PaymentBatchItem;
import com.validus.backend.domain.repository.InvoiceRepository;
import com.validus.backend.domain.repository.PaymentBatchRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TreasuryService {

    private final PaymentBatchRepository paymentBatchRepository;
    private final InvoiceRepository invoiceRepository;
    private final AuditService auditService;

    @Transactional
    public PaymentBatch createBatch(String companyCode, List<UUID> invoiceIds, String userId) {
        if (invoiceIds == null || invoiceIds.isEmpty()) {
            throw new ValidationException("At least one invoice is required to create a payment batch");
        }
        List<Invoice> invoices = invoiceRepository.findAllById(invoiceIds);
        if (invoices.size() != invoiceIds.size()) {
            throw new NotFoundException("One or more invoices not found");
        }
        for (Invoice invoice : invoices) {
            if (invoice.getStatus() != InvoiceStatus.READY_FOR_PAYMENT) {
                throw new StateException("Invoice " + invoice.getId() + " is not ready for payment");
            }
            if (!invoice.getCompanyCode().equals(companyCode)) {
                throw new ValidationException("All invoices must belong to the same company");
            }
        }
        PaymentBatch batch = new PaymentBatch();
        batch.setId(UUID.randomUUID());
        batch.setCode("PB-" + System.currentTimeMillis());
        batch.setCompanyCode(companyCode);
        batch.setCreatedBy(userId);
        batch.setStatus(PaymentBatchStatus.DRAFT);
        BigDecimal total = invoices.stream().map(Invoice::getAmountTTC).reduce(BigDecimal.ZERO, BigDecimal::add);
        batch.setTotalAmount(total);
        invoices.forEach(invoice -> {
            PaymentBatchItem item = new PaymentBatchItem();
            item.setInvoiceId(invoice.getId());
            item.setAmount(invoice.getAmountTTC());
            item.setSupplierId(invoice.getSupplierId());
            batch.addItem(item);
        });
        PaymentBatch saved = paymentBatchRepository.save(batch);
        invoices.forEach(invoice -> auditService.recordInvoiceAction(invoice, AuditAction.PAYMENT_BATCH_CREATED, userId,
            Map.of("batchId", saved.getId())));
        return saved;
    }

    @Transactional
    public PaymentBatch submit(UUID batchId, String userId) {
        PaymentBatch batch = paymentBatchRepository.findById(batchId)
            .orElseThrow(() -> new NotFoundException("Payment batch not found"));
        if (batch.getStatus() != PaymentBatchStatus.DRAFT) {
            throw new StateException("Only draft batches can be submitted");
        }
        batch.setStatus(PaymentBatchStatus.SUBMITTED);
        batch.getItems().forEach(item -> invoiceRepository.findById(item.getInvoiceId()).ifPresent(invoice ->
            auditService.recordInvoiceAction(invoice, AuditAction.PAYMENT_BATCH_SUBMITTED, userId,
                Map.of("batchId", batchId))));
        return batch;
    }
}
