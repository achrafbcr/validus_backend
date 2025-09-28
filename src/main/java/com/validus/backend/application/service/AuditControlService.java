package com.validus.backend.application.service;

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
public class AuditControlService {

    private final PaymentBatchRepository paymentBatchRepository;
    private final InvoiceRepository invoiceRepository;
    private final AuditService auditService;

    @Transactional
    public PaymentBatch approve(UUID batchId, String userId) {
        PaymentBatch batch = paymentBatchRepository.findById(batchId)
            .orElseThrow(() -> new NotFoundException("Payment batch not found"));
        if (batch.getStatus() != PaymentBatchStatus.SUBMITTED) {
            throw new StateException("Batch must be submitted for audit approval");
        }
        batch.setStatus(PaymentBatchStatus.APPROVED_AUDIT);
        batch.getItems().forEach(item -> invoiceRepository.findById(item.getInvoiceId()).ifPresent(invoice ->
            auditService.recordInvoiceAction(invoice, AuditAction.PAYMENT_BATCH_APPROVED_AUDIT, userId,
                Map.of("batchId", batchId))));
        return batch;
    }

    @Transactional
    public PaymentBatch reject(UUID batchId, String userId, String reason) {
        PaymentBatch batch = paymentBatchRepository.findById(batchId)
            .orElseThrow(() -> new NotFoundException("Payment batch not found"));
        if (batch.getStatus() != PaymentBatchStatus.SUBMITTED && batch.getStatus() != PaymentBatchStatus.APPROVED_AUDIT) {
            throw new StateException("Batch cannot be rejected at this stage");
        }
        batch.setStatus(PaymentBatchStatus.REJECTED);
        batch.getItems().forEach(item -> invoiceRepository.findById(item.getInvoiceId()).ifPresent(invoice -> {
            invoice.setStatus(InvoiceStatus.ACCOUNTED);
            auditService.recordInvoiceAction(invoice, AuditAction.PAYMENT_BATCH_REJECTED, userId,
                Map.of("batchId", batchId, "reason", reason));
        }));
        return batch;
    }
}
