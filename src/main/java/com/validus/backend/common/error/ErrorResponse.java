package com.validus.backend.common.error;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse {
    Instant timestamp;
    String path;
    ErrorCode code;
    String message;
    List<Map<String, Object>> details;
}
