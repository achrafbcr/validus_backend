package com.validus.backend.application.port.outgoing;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AiAdapter {

    AiExtractionResult extract(UUID invoiceId, String fileRef);

    List<AiDuplicateResult> findNearDuplicates(UUID invoiceId, Map<String, Object> payload);

    AiAnomalyResult checkAnomaly(UUID invoiceId, Map<String, Object> payload);

    AiSummaryResult summarize(UUID invoiceId);

    record AiExtractionResult(Map<String, Object> fields, Map<String, Double> confidences) {}

    record AiDuplicateResult(UUID invoiceId, double score) {}

    record AiAnomalyResult(double riskScore, List<String> reasons) {}

    record AiSummaryResult(String summary, double confidence) {}
}
