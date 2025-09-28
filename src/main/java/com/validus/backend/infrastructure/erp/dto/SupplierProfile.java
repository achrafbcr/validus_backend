package com.validus.backend.infrastructure.erp.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SupplierProfile {
    String id;
    String name;
    String taxId;
    String rib;
    boolean active;
}
