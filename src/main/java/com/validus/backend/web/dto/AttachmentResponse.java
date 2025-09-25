package com.validus.backend.web.dto;

import com.validus.backend.domain.enums.AttachmentType;
import lombok.Data;

@Data
public class AttachmentResponse {
    private Long id;
    private AttachmentType type;
    private String fileName;
    private String mime;
    private Long size;
}
