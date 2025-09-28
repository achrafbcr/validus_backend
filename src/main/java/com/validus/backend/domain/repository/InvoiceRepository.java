package com.validus.backend.domain.repository;

import com.validus.backend.domain.enums.InvoiceStatus;
import com.validus.backend.domain.model.Invoice;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findBySupplierIdAndNumberAndInvoiceYear(String supplierId, String number, Integer invoiceYear);

    Optional<Invoice> findByHashContent(String hashContent);

    @Query("select i from Invoice i where (:status is null or i.status = :status) "
        + "and (:companyCode is null or i.companyCode = :companyCode) "
        + "and (:supplierId is null or i.supplierId = :supplierId) "
        + "and (:from is null or i.invoiceDate >= :from) "
        + "and (:to is null or i.invoiceDate <= :to)")
    Page<Invoice> search(@Param("status") InvoiceStatus status,
                         @Param("companyCode") String companyCode,
                         @Param("supplierId") String supplierId,
                         @Param("from") LocalDate from,
                         @Param("to") LocalDate to,
                         Pageable pageable);
}
