package com.validus.repository;

import com.validus.domain.Invoice;
import com.validus.domain.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
  boolean existsByInvoiceNumber(String invoiceNumber);
  Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);
}
