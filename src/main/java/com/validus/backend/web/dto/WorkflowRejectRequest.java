package com.validus.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowRejectRequest {
    @NotBlank
    private String reason;
}
