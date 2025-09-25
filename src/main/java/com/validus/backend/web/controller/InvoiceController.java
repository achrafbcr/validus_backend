package com.validus.backend.web.controller;

import com.validus.backend.application.service.AccountingService;
import com.validus.backend.application.service.AuditService;
import com.validus.backend.application.service.PurchasingService;
import com.validus.backend.application.service.ReceptionService;
import com.validus.backend.application.service.SearchService;
import com.validus.backend.domain.enums.AttachmentType;
import com.validus.backend.domain.enums.InvoiceStatus;
import com.validus.backend.domain.model.Invoice;
import com.validus.backend.web.dto.AttachmentResponse;
import com.validus.backend.web.dto.AuditLogResponse;
import com.validus.backend.web.dto.InvoiceRequest;
import com.validus.backend.web.dto.InvoiceResponse;
import com.validus.backend.web.mapper.InvoiceMapper;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final ReceptionService receptionService;
    private final PurchasingService purchasingService;
    private final AccountingService accountingService;
    private final SearchService searchService;
    private final AuditService auditService;
    private final InvoiceMapper invoiceMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTION','ADMIN')")
    public InvoiceResponse createInvoice(@Valid @RequestBody InvoiceRequest request) {
        Invoice invoice = invoiceMapper.toEntity(request);
        invoice.setHasPaperOriginal(request.isHasPaperOriginal());
        Invoice saved = receptionService.createInvoice(invoice, currentUser());
        return invoiceMapper.toResponse(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTION','ACHATS','ADMIN')")
    public InvoiceResponse updateInvoice(@PathVariable UUID id, @Valid @RequestBody InvoiceRequest request) {
        Invoice invoice = invoiceMapper.toEntity(request);
        invoice.setHasPaperOriginal(request.isHasPaperOriginal());
        Invoice updated = receptionService.updateInvoice(id, invoice, currentUser());
        return invoiceMapper.toResponse(updated);
    }

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('RECEPTION','ACHATS','ADMIN')")
    public AttachmentResponse addAttachment(@PathVariable UUID id,
                                            @RequestParam AttachmentType type,
                                            @RequestParam("file") MultipartFile file) {
        var attachment = receptionService.addAttachment(id, type, file, currentUser());
        AttachmentResponse response = new AttachmentResponse();
        response.setId(attachment.getId());
        response.setType(attachment.getType());
        response.setFileName(attachment.getFileName());
        response.setMime(attachment.getMime());
        response.setSize(attachment.getSize());
        return response;
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ACHATS','ADMIN')")
    public InvoiceResponse submit(@PathVariable UUID id) {
        Invoice invoice = purchasingService.submitForReview(id, currentUser());
        return invoiceMapper.toResponse(invoice);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTION','ACHATS','METIER','COMPTA','TRESO','AUDIT','DAF','ADMIN')")
    public InvoiceResponse getInvoice(@PathVariable UUID id) {
        Invoice invoice = receptionService.getInvoice(id);
        return invoiceMapper.toResponse(invoice);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPTION','ACHATS','METIER','COMPTA','TRESO','AUDIT','DAF','ADMIN')")
    public Page<InvoiceResponse> search(@RequestParam(required = false) InvoiceStatus status,
                                        @RequestParam(required = false) String companyCode,
                                        @RequestParam(required = false) String supplierId,
                                        @RequestParam(required = false) LocalDate from,
                                        @RequestParam(required = false) LocalDate to,
                                        Pageable pageable) {
        return searchService.searchInvoices(status, companyCode, supplierId, from, to, pageable)
            .map(invoiceMapper::toResponse);
    }

    @PostMapping("/{id}/accounting")
    @PreAuthorize("hasAnyRole('COMPTA','ADMIN')")
    public InvoiceResponse accounting(@PathVariable UUID id) {
        Invoice invoice = accountingService.postAccounting(id, currentUser());
        return invoiceMapper.toResponse(invoice);
    }

    @PostMapping("/{id}/ready-for-payment")
    @PreAuthorize("hasAnyRole('COMPTA','TRESO','ADMIN')")
    public InvoiceResponse markReadyForPayment(@PathVariable UUID id) {
        Invoice invoice = accountingService.markReadyForPayment(id, currentUser());
        return invoiceMapper.toResponse(invoice);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public InvoiceResponse cancel(@PathVariable UUID id) {
        Invoice invoice = receptionService.cancel(id, currentUser());
        return invoiceMapper.toResponse(invoice);
    }

    @GetMapping("/{id}/audit")
    @PreAuthorize("hasAnyRole('AUDIT','ADMIN','RECEPTION','ACHATS','METIER','COMPTA','TRESO','DAF')")
    public List<AuditLogResponse> audit(@PathVariable UUID id) {
        return auditService.getInvoiceHistory(id).stream()
            .map(log -> {
                AuditLogResponse response = new AuditLogResponse();
                response.setId(log.getId());
                response.setAction(log.getAction());
                response.setByUser(log.getByUser());
                response.setAt(log.getAt());
                return response;
            })
            .collect(Collectors.toList());
    }

    private String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}
