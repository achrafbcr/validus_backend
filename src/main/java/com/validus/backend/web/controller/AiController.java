package com.validus.backend.web.controller;

import com.validus.backend.application.port.outgoing.AiAdapter.AiAnomalyResult;
import com.validus.backend.application.port.outgoing.AiAdapter.AiDuplicateResult;
import com.validus.backend.application.port.outgoing.AiAdapter.AiExtractionResult;
import com.validus.backend.application.port.outgoing.AiAdapter.AiSummaryResult;
import com.validus.backend.application.service.AiOrchestrationService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/invoices")
@RequiredArgsConstructor
public class AiController {

    private final AiOrchestrationService aiService;

    @PostMapping("/{id}/extract")
    @PreAuthorize("hasAnyRole('RECEPTION','ADMIN')")
    public AiExtractionResult extract(@PathVariable UUID id, @RequestBody(required = false) Map<String, String> payload) {
        Map<String, String> effectivePayload = payload != null ? payload : Map.of();
        String fileRef = effectivePayload.getOrDefault("fileRef", "");
        return aiService.extract(id, fileRef);
    }

    @GetMapping("/{id}/near-duplicate")
    @PreAuthorize("hasAnyRole('RECEPTION','ACHATS','ADMIN')")
    public List<AiDuplicateResult> nearDuplicate(@PathVariable UUID id) {
        return aiService.nearDuplicates(id, Map.of());
    }

    @PostMapping("/{id}/anomaly")
    @PreAuthorize("hasAnyRole('COMPTA','TRESO','ADMIN')")
    public AiAnomalyResult anomaly(@PathVariable UUID id, @RequestBody(required = false) Map<String, Object> payload) {
        return aiService.anomaly(id, payload == null ? Map.of() : payload);
    }

    @GetMapping("/{id}/summary")
    @PreAuthorize("hasAnyRole('METIER','AUDIT','DAF','ADMIN')")
    public AiSummaryResult summary(@PathVariable UUID id) {
        return aiService.summary(id);
    }
}
