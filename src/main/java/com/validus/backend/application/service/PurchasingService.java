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
public class PurchasingService {

    private final InvoiceRepository invoiceRepository;
    private final ERPAdapter erpAdapter;
    private final AuditService auditService;

    @Transactional
    public Invoice submitForReview(UUID invoiceId, String userId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));
        if (invoice.getStatus() != InvoiceStatus.RECEIVED && invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new StateException("Invoice cannot be submitted from status " + invoice.getStatus());
        }
        validatePurchaseOrder(invoice);
        boolean hasPdf = invoice.getAttachments().stream().anyMatch(att -> att.getType().name().equals("INVOICE_PDF"));
        if (!hasPdf) {
            throw new ValidationException("Invoice PDF attachment is required before submission");
        }
        invoice.setStatus(InvoiceStatus.UNDER_REVIEW);
        invoice.setWorkflowId(UUID.randomUUID().toString());
        auditService.recordInvoiceAction(invoice, AuditAction.INVOICE_SUBMITTED, userId, Map.of());
        return invoice;
    }

    private void validatePurchaseOrder(Invoice invoice) {
        if (invoice.getPoNumber() == null || invoice.getPoNumber().isEmpty()) {
            return;
        }
        var po = erpAdapter.getPurchaseOrder(invoice.getPoNumber());
        if (po == null) {
            throw new ValidationException("Purchase order not found");
        }
        if (!po.getSupplierId().equals(invoice.getSupplierId())) {
            throw new ValidationException("PO supplier mismatch", Map.of("expected", po.getSupplierId()));
        }
        if (po.getRemainingAmount().compareTo(invoice.getAmountTTC()) < 0) {
            throw new ValidationException("Invoice amount exceeds PO remaining amount");
        }
        if (!po.getCurrency().equals(invoice.getCurrency())) {
            throw new ValidationException("Currency mismatch with PO");
        }
    }
}
