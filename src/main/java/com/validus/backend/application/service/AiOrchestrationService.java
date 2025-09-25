package com.validus.backend.application.service;

import com.validus.backend.application.port.outgoing.AiAdapter;
import com.validus.backend.application.port.outgoing.AiAdapter.AiAnomalyResult;
import com.validus.backend.application.port.outgoing.AiAdapter.AiDuplicateResult;
import com.validus.backend.application.port.outgoing.AiAdapter.AiExtractionResult;
import com.validus.backend.application.port.outgoing.AiAdapter.AiSummaryResult;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiOrchestrationService {

    private final AiAdapter aiAdapter;

    public AiExtractionResult extract(UUID invoiceId, String fileRef) {
        return aiAdapter.extract(invoiceId, fileRef);
    }

    public List<AiDuplicateResult> nearDuplicates(UUID invoiceId, Map<String, Object> payload) {
        return aiAdapter.findNearDuplicates(invoiceId, payload);
    }

    public AiAnomalyResult anomaly(UUID invoiceId, Map<String, Object> payload) {
        return aiAdapter.checkAnomaly(invoiceId, payload);
    }

    public AiSummaryResult summary(UUID invoiceId) {
        return aiAdapter.summarize(invoiceId);
    }
}
