package com.validus.backend.application.service;

import com.validus.backend.application.port.outgoing.ERPAdapter;
import com.validus.backend.common.exception.NotFoundException;
import com.validus.backend.common.exception.StateException;
import com.validus.backend.common.exception.ValidationException;
import com.validus.backend.domain.enums.AuditAction;
import com.validus.backend.domain.enums.InvoiceStatus;
import com.validus.backend.domain.model.Invoice;
import com.validus.backend.domain.repository.InvoiceRepository;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountingService {

    private final InvoiceRepository invoiceRepository;
    private final ERPAdapter erpAdapter;
    private final AuditService auditService;

    @Transactional
    public Invoice postAccounting(UUID invoiceId, String userId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));
        if (invoice.getStatus() != InvoiceStatus.APPROVED_BUSINESS) {
            throw new StateException("Invoice must be approved by business before accounting");
        }
        if (!invoice.isHasPaperOriginal()) {
            throw new ValidationException("Paper original must be received before accounting");
        }
        String txnId = erpAdapter.postAccountingEntry(invoice);
        invoice.setYardiTxnId(txnId);
        invoice.setStatus(InvoiceStatus.ACCOUNTED);
        auditService.recordInvoiceAction(invoice, AuditAction.INVOICE_ACCOUNTED, userId, Map.of("txnId", txnId));
        return invoice;
    }

    @Transactional
    public Invoice markReadyForPayment(UUID invoiceId, String userId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));
        if (invoice.getStatus() != InvoiceStatus.ACCOUNTED) {
            throw new StateException("Invoice must be accounted before ready for payment");
        }
        invoice.setStatus(InvoiceStatus.READY_FOR_PAYMENT);
        auditService.recordInvoiceAction(invoice, AuditAction.INVOICE_READY_FOR_PAYMENT, userId, Map.of());
        return invoice;
    }
}
