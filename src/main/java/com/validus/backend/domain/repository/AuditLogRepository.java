package com.validus.backend.domain.repository;

import com.validus.backend.domain.model.AuditLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByInvoiceIdOrderByAtAsc(UUID invoiceId);
}
