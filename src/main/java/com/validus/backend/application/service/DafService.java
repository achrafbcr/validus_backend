package com.validus.backend.application.service;

import com.validus.backend.application.port.outgoing.ERPAdapter;
import com.validus.backend.common.exception.NotFoundException;
import com.validus.backend.common.exception.StateException;
import com.validus.backend.domain.enums.AuditAction;
import com.validus.backend.domain.enums.InvoiceStatus;
import com.validus.backend.domain.enums.PaymentBatchStatus;
import com.validus.backend.domain.model.Invoice;
import com.validus.backend.domain.model.PaymentBatch;
import com.validus.backend.domain.repository.InvoiceRepository;
import com.validus.backend.domain.repository.PaymentBatchRepository;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DafService {

    private final PaymentBatchRepository paymentBatchRepository;
    private final InvoiceRepository invoiceRepository;
    private final ERPAdapter erpAdapter;
    private final AuditService auditService;

    @Transactional
    public PaymentBatch approve(UUID batchId, String userId) {
        PaymentBatch batch = paymentBatchRepository.findById(batchId)
            .orElseThrow(() -> new NotFoundException("Payment batch not found"));
        if (batch.getStatus() != PaymentBatchStatus.APPROVED_AUDIT) {
            throw new StateException("Batch must be audit-approved before DAF approval");
        }
        batch.setStatus(PaymentBatchStatus.APPROVED_DAF);
        batch.getItems().forEach(item -> invoiceRepository.findById(item.getInvoiceId()).ifPresent(invoice ->
            auditService.recordInvoiceAction(invoice, AuditAction.PAYMENT_BATCH_APPROVED_DAF, userId,
                Map.of("batchId", batchId))));
        return batch;
    }

    @Transactional
    public PaymentBatch execute(UUID batchId, String userId) {
        PaymentBatch batch = paymentBatchRepository.findById(batchId)
            .orElseThrow(() -> new NotFoundException("Payment batch not found"));
        if (batch.getStatus() != PaymentBatchStatus.APPROVED_DAF) {
            throw new StateException("Batch must be DAF-approved before execution");
        }
        erpAdapter.executePaymentBatch(batch);
        batch.setStatus(PaymentBatchStatus.EXECUTED);
        batch.getItems().forEach(item -> {
            Invoice invoice = invoiceRepository.findById(item.getInvoiceId())
                .orElseThrow(() -> new NotFoundException("Invoice not found"));
            invoice.setStatus(InvoiceStatus.PAID);
            auditService.recordInvoiceAction(invoice, AuditAction.PAYMENT_BATCH_EXECUTED, userId,
                Map.of("batchId", batchId));
        });
        return batch;
    }
}
