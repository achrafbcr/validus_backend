package com.validus.backend.web.controller;

import com.validus.backend.application.service.AuditControlService;
import com.validus.backend.application.service.DafService;
import com.validus.backend.application.service.TreasuryService;
import com.validus.backend.domain.enums.PaymentBatchStatus;
import com.validus.backend.domain.model.PaymentBatch;
import com.validus.backend.domain.repository.PaymentBatchRepository;
import com.validus.backend.web.dto.BatchRejectRequest;
import com.validus.backend.web.dto.PaymentBatchRequest;
import com.validus.backend.web.dto.PaymentBatchResponse;
import com.validus.backend.web.mapper.PaymentBatchMapper;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/batches")
@RequiredArgsConstructor
public class PaymentBatchController {

    private final TreasuryService treasuryService;
    private final AuditControlService auditControlService;
    private final DafService dafService;
    private final PaymentBatchMapper paymentBatchMapper;
    private final PaymentBatchRepository paymentBatchRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('TRESO','ADMIN')")
    public PaymentBatchResponse create(@Valid @RequestBody PaymentBatchRequest request) {
        PaymentBatch batch = treasuryService.createBatch(request.getCompanyCode(), request.getInvoiceIds(), currentUser());
        return paymentBatchMapper.toResponse(batch);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('TRESO','ADMIN')")
    public PaymentBatchResponse submit(@PathVariable UUID id) {
        PaymentBatch batch = treasuryService.submit(id, currentUser());
        return paymentBatchMapper.toResponse(batch);
    }

    @PostMapping("/{id}/approve-audit")
    @PreAuthorize("hasAnyRole('AUDIT','ADMIN')")
    public PaymentBatchResponse approveAudit(@PathVariable UUID id) {
        PaymentBatch batch = auditControlService.approve(id, currentUser());
        return paymentBatchMapper.toResponse(batch);
    }

    @PostMapping("/{id}/approve-daf")
    @PreAuthorize("hasAnyRole('DAF','ADMIN')")
    public PaymentBatchResponse approveDaf(@PathVariable UUID id) {
        PaymentBatch batch = dafService.approve(id, currentUser());
        return paymentBatchMapper.toResponse(batch);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('AUDIT','DAF','ADMIN')")
    public PaymentBatchResponse reject(@PathVariable UUID id, @Valid @RequestBody BatchRejectRequest request) {
        PaymentBatch batch = auditControlService.reject(id, currentUser(), request.getReason());
        return paymentBatchMapper.toResponse(batch);
    }

    @PostMapping("/{id}/execute")
    @PreAuthorize("hasAnyRole('COMPTA','TRESO','ADMIN')")
    public PaymentBatchResponse execute(@PathVariable UUID id) {
        PaymentBatch batch = dafService.execute(id, currentUser());
        return paymentBatchMapper.toResponse(batch);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRESO','COMPTA','AUDIT','DAF','ADMIN')")
    public PaymentBatchResponse get(@PathVariable UUID id) {
        PaymentBatch batch = paymentBatchRepository.findById(id).orElseThrow();
        return paymentBatchMapper.toResponse(batch);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TRESO','COMPTA','AUDIT','DAF','ADMIN')")
    public Page<PaymentBatchResponse> list(@RequestParam(required = false) PaymentBatchStatus status, Pageable pageable) {
        if (status != null) {
            return paymentBatchRepository.findAllByStatus(status, pageable)
                .map(paymentBatchMapper::toResponse);
        }
        return paymentBatchRepository.findAll(pageable).map(paymentBatchMapper::toResponse);
    }

    private String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}
