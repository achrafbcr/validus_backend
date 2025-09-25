package com.validus.backend.application.service;

import com.validus.backend.application.port.outgoing.AiAdapter;
import com.validus.backend.application.port.outgoing.AttachmentStoragePort;
import com.validus.backend.common.exception.DuplicateInvoiceException;
import com.validus.backend.common.exception.NotFoundException;
import com.validus.backend.common.exception.StateException;
import com.validus.backend.common.exception.ValidationException;
import com.validus.backend.domain.enums.AttachmentType;
import com.validus.backend.domain.enums.AuditAction;
import com.validus.backend.domain.enums.InvoiceStatus;
import com.validus.backend.domain.model.Attachment;
import com.validus.backend.domain.model.Invoice;
import com.validus.backend.domain.repository.InvoiceRepository;
import jakarta.transaction.Transactional;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceptionService {

    private final InvoiceRepository invoiceRepository;
    private final AttachmentStoragePort attachmentStoragePort;
    private final AuditService auditService;
    private final AiAdapter aiAdapter;

    @Transactional
    public Invoice createInvoice(Invoice invoice, String userId) {
        invoice.setId(UUID.randomUUID());
        invoice.setCreatedBy(userId);
        invoice.ensureTotalsConsistency();
        validateAmounts(invoice);
        checkDuplicate(invoice);
        invoice.setStatus(InvoiceStatus.RECEIVED);
        Invoice saved = invoiceRepository.save(invoice);
        auditService.recordInvoiceAction(saved, AuditAction.INVOICE_CREATED, userId, Map.of());
        return saved;
    }

    @Transactional
    public Invoice updateInvoice(UUID id, Invoice updatedInvoice, String userId) {
        Invoice existing = invoiceRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));
        if (existing.isReadOnlyAfterPayment()) {
            throw new StateException("Invoice is read-only after payment");
        }
        if (!(existing.getStatus() == InvoiceStatus.DRAFT || existing.getStatus() == InvoiceStatus.RECEIVED)) {
            throw new StateException("Invoice cannot be modified in status " + existing.getStatus());
        }
        existing.setNumber(updatedInvoice.getNumber());
        existing.setSupplierId(updatedInvoice.getSupplierId());
        existing.setCompanyCode(updatedInvoice.getCompanyCode());
        existing.setPoNumber(updatedInvoice.getPoNumber());
        existing.setInvoiceDate(updatedInvoice.getInvoiceDate());
        existing.setDueDate(updatedInvoice.getDueDate());
        existing.setCurrency(updatedInvoice.getCurrency());
        existing.setAmountHT(updatedInvoice.getAmountHT());
        existing.setAmountTVA(updatedInvoice.getAmountTVA());
        existing.ensureTotalsConsistency();
        existing.setHasPaperOriginal(updatedInvoice.isHasPaperOriginal());
        existing.getLines().clear();
        updatedInvoice.getLines().forEach(line -> {
            line.setInvoice(existing);
            existing.getLines().add(line);
        });
        validateAmounts(existing);
        auditService.recordInvoiceAction(existing, AuditAction.INVOICE_UPDATED, userId, Map.of());
        return existing;
    }

    @Transactional
    public Attachment addAttachment(UUID invoiceId, AttachmentType type, MultipartFile file, String userId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));
        if (invoice.isReadOnlyAfterPayment()) {
            throw new StateException("Invoice is read-only after payment");
        }
        String storageRef;
        try (InputStream inputStream = file.getInputStream()) {
            storageRef = attachmentStoragePort.store(invoiceId.toString(), file.getOriginalFilename(), inputStream);
        } catch (Exception e) {
            throw new ValidationException("Unable to store attachment", Map.of("reason", e.getMessage()));
        }
        Attachment attachment = new Attachment();
        attachment.setType(type);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setMime(file.getContentType());
        attachment.setSize(file.getSize());
        attachment.setSha256(computeSha256(file));
        attachment.setStorageRef(storageRef);
        invoice.addAttachment(attachment);
        auditService.recordInvoiceAction(invoice, AuditAction.ATTACHMENT_ADDED, userId, Map.of("type", type.name()));
        if (type == AttachmentType.INVOICE_PDF) {
            aiAdapter.extract(invoice.getId(), storageRef);
        }
        return attachment;
    }

    @Transactional
    public Invoice cancel(UUID invoiceId, String userId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new StateException("Paid invoices cannot be cancelled");
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        auditService.recordInvoiceAction(invoice, AuditAction.INVOICE_CANCELLED, userId, Map.of());
        return invoice;
    }

    private String computeSha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm missing", e);
        } catch (Exception e) {
            throw new ValidationException("Unable to compute hash", Map.of("reason", e.getMessage()));
        }
    }

    private void validateAmounts(Invoice invoice) {
        if (invoice.getAmountHT() == null || invoice.getAmountTVA() == null || invoice.getAmountTTC() == null) {
            throw new ValidationException("Invoice amounts are required");
        }
        if (invoice.getAmountHT().add(invoice.getAmountTVA()).compareTo(invoice.getAmountTTC()) != 0) {
            throw new ValidationException("Amount TTC must equal amount HT + TVA");
        }
    }

    private void checkDuplicate(Invoice invoice) {
        invoiceRepository.findBySupplierIdAndNumberAndInvoiceYear(
                invoice.getSupplierId(),
                invoice.getNumber(),
                invoice.getInvoiceYear())
            .ifPresent(existing -> {
                throw new DuplicateInvoiceException("Invoice already exists for supplier/year");
            });
        if (invoice.getHashContent() != null) {
            invoiceRepository.findByHashContent(invoice.getHashContent())
                .ifPresent(existing -> {
                    throw new DuplicateInvoiceException("Invoice duplicate detected by hash");
                });
        }
    }

    @Transactional
    public Invoice getInvoice(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));
    }
}
