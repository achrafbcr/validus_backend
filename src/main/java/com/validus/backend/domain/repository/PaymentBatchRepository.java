package com.validus.backend.domain.repository;

import com.validus.backend.domain.enums.PaymentBatchStatus;
import com.validus.backend.domain.model.PaymentBatch;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentBatchRepository extends JpaRepository<PaymentBatch, UUID> {

    Optional<PaymentBatch> findByCode(String code);

    Page<PaymentBatch> findAllByStatus(PaymentBatchStatus status, Pageable pageable);
}
