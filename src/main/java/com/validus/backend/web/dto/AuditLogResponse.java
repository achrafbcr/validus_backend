package com.validus.backend.web.dto;

import com.validus.backend.domain.enums.AuditAction;
import java.time.Instant;
import lombok.Data;

@Data
public class AuditLogResponse {
    private Long id;
    private AuditAction action;
    private String byUser;
    private Instant at;
}
