package com.validus.backend.domain.repository;

import com.validus.backend.domain.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, String> {
}
