package com.validus.backend.application.service;

import com.validus.backend.common.exception.NotFoundException;
import com.validus.backend.common.exception.StateException;
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
public class WorkflowService {

    private static final String BUYER_APPROVER_KEY = "buyerApprover";
    private static final String BUSINESS_APPROVER_KEY = "businessApprover";

    private final InvoiceRepository invoiceRepository;
    private final AuditService auditService;

    @Transactional
    public Invoice assignApprovers(UUID invoiceId, String buyerApprover, String businessApprover, String userId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));
        if (invoice.getStatus() != InvoiceStatus.UNDER_REVIEW && invoice.getStatus() != InvoiceStatus.RECEIVED) {
            throw new StateException("Approvers can only be assigned when invoice is under review");
        }
        invoice.getFlags().put(BUYER_APPROVER_KEY, buyerApprover);
        invoice.getFlags().put(BUSINESS_APPROVER_KEY, businessApprover);
        auditService.recordInvoiceAction(invoice, AuditAction.INVOICE_UPDATED, userId,
            Map.of("buyerApprover", buyerApprover, "businessApprover", businessApprover));
        return invoice;
    }

    @Transactional
    public Invoice approve(UUID invoiceId, String approverId, String comment) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));
        if (invoice.getStatus() != InvoiceStatus.UNDER_REVIEW && invoice.getStatus() != InvoiceStatus.APPROVED_BUYER) {
            throw new StateException("Invoice not in review");
        }
        if (invoice.getStatus() == InvoiceStatus.UNDER_REVIEW) {
            validateApprover(invoice, approverId, BUYER_APPROVER_KEY);
            invoice.setStatus(InvoiceStatus.APPROVED_BUYER);
            auditService.recordInvoiceAction(invoice, AuditAction.INVOICE_APPROVED_BUYER, approverId,
                Map.of("comment", comment));
        } else {
            validateApprover(invoice, approverId, BUSINESS_APPROVER_KEY);
            invoice.setStatus(InvoiceStatus.APPROVED_BUSINESS);
            auditService.recordInvoiceAction(invoice, AuditAction.INVOICE_APPROVED_BUSINESS, approverId,
                Map.of("comment", comment));
        }
        return invoice;
    }

    @Transactional
    public Invoice reject(UUID invoiceId, String approverId, String reason) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new StateException("Paid invoice cannot be rejected");
        }
        invoice.setStatus(InvoiceStatus.REJECTED);
        auditService.recordInvoiceAction(invoice, AuditAction.INVOICE_REJECTED, approverId, Map.of("reason", reason));
        return invoice;
    }

    private void validateApprover(Invoice invoice, String approverId, String key) {
        Object expected = invoice.getFlags().get(key);
        if (expected == null || !expected.equals(approverId)) {
            throw new StateException("Approver not authorized for this step");
        }
    }
}
