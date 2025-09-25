package com.validus.backend.application.service;

import com.validus.backend.domain.enums.AuditAction;
import com.validus.backend.domain.model.AuditLog;
import com.validus.backend.domain.model.Invoice;
import com.validus.backend.domain.repository.AuditLogRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void recordInvoiceAction(Invoice invoice, AuditAction action, String byUser, Map<String, Object> meta) {
        AuditLog log = new AuditLog();
        log.setInvoice(invoice);
        log.setAction(action);
        log.setByUser(byUser);
        log.setMeta(meta);
        auditLogRepository.save(log);
    }

    @Transactional
    public List<AuditLog> getInvoiceHistory(UUID invoiceId) {
        return auditLogRepository.findAllByInvoiceIdOrderByAtAsc(invoiceId);
    }
}
