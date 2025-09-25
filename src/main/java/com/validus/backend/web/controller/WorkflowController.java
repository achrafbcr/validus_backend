package com.validus.backend.web.controller;

import com.validus.backend.application.service.WorkflowService;
import com.validus.backend.domain.model.Invoice;
import com.validus.backend.web.dto.InvoiceResponse;
import com.validus.backend.web.dto.WorkflowActionRequest;
import com.validus.backend.web.dto.WorkflowAssignRequest;
import com.validus.backend.web.dto.WorkflowRejectRequest;
import com.validus.backend.web.mapper.InvoiceMapper;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final InvoiceMapper invoiceMapper;

    @PostMapping("/{invoiceId}/assign")
    @PreAuthorize("hasAnyRole('ACHATS','ADMIN')")
    public InvoiceResponse assign(@PathVariable UUID invoiceId, @Valid @RequestBody WorkflowAssignRequest request) {
        Invoice invoice = workflowService.assignApprovers(invoiceId, request.getBuyerApprover(),
            request.getBusinessApprover(), currentUser());
        return invoiceMapper.toResponse(invoice);
    }

    @PostMapping("/{invoiceId}/approve")
    @PreAuthorize("hasAnyRole('METIER','ADMIN')")
    public InvoiceResponse approve(@PathVariable UUID invoiceId, @Valid @RequestBody WorkflowActionRequest request) {
        Invoice invoice = workflowService.approve(invoiceId, currentUser(), request.getComment());
        return invoiceMapper.toResponse(invoice);
    }

    @PostMapping("/{invoiceId}/reject")
    @PreAuthorize("hasAnyRole('METIER','ADMIN')")
    public InvoiceResponse reject(@PathVariable UUID invoiceId, @Valid @RequestBody WorkflowRejectRequest request) {
        Invoice invoice = workflowService.reject(invoiceId, currentUser(), request.getReason());
        return invoiceMapper.toResponse(invoice);
    }

    private String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}
