package com.validus.backend.infrastructure.ai;

import com.validus.backend.application.port.outgoing.AiAdapter;
import com.validus.backend.config.AiProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiRestAdapter implements AiAdapter {

    private final WebClient aiWebClient;
    private final AiProperties properties;

    @Override
    @CircuitBreaker(name = "ai", fallbackMethod = "fallbackExtraction")
    @Retry(name = "ai")
    public AiExtractionResult extract(UUID invoiceId, String fileRef) {
        return aiWebClient.post()
            .uri(properties.baseUrl() + "/invoices/" + invoiceId + "/extract")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("invoiceId", invoiceId.toString(), "fileRef", fileRef))
            .retrieve()
            .bodyToMono(AiExtractionResult.class)
            .block(Duration.ofMillis(properties.timeoutMs()));
    }

    @Override
    @CircuitBreaker(name = "ai", fallbackMethod = "fallbackDuplicates")
    @Retry(name = "ai")
    public List<AiDuplicateResult> findNearDuplicates(UUID invoiceId, Map<String, Object> payload) {
        return aiWebClient.post()
            .uri(properties.baseUrl() + "/invoices/" + invoiceId + "/near-duplicate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToFlux(AiDuplicateResult.class)
            .collectList()
            .block(Duration.ofMillis(properties.timeoutMs()));
    }

    @Override
    @CircuitBreaker(name = "ai", fallbackMethod = "fallbackAnomaly")
    @Retry(name = "ai")
    public AiAnomalyResult checkAnomaly(UUID invoiceId, Map<String, Object> payload) {
        return aiWebClient.post()
            .uri(properties.baseUrl() + "/invoices/" + invoiceId + "/anomaly")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(AiAnomalyResult.class)
            .block(Duration.ofMillis(properties.timeoutMs()));
    }

    @Override
    @CircuitBreaker(name = "ai", fallbackMethod = "fallbackSummary")
    @Retry(name = "ai")
    public AiSummaryResult summarize(UUID invoiceId) {
        return aiWebClient.get()
            .uri(properties.baseUrl() + "/invoices/" + invoiceId + "/summary")
            .retrieve()
            .bodyToMono(AiSummaryResult.class)
            .block(Duration.ofMillis(properties.timeoutMs()));
    }

    private AiExtractionResult fallbackExtraction(UUID invoiceId, String fileRef, Throwable throwable) {
        log.warn("AI extract fallback for invoice {}: {}", invoiceId, throwable.getMessage());
        return new AiExtractionResult(Map.of(), Map.of());
    }

    private List<AiDuplicateResult> fallbackDuplicates(UUID invoiceId, Map<String, Object> payload, Throwable throwable) {
        log.warn("AI duplicate fallback for invoice {}", invoiceId);
        return List.of();
    }

    private AiAnomalyResult fallbackAnomaly(UUID invoiceId, Map<String, Object> payload, Throwable throwable) {
        log.warn("AI anomaly fallback for invoice {}", invoiceId);
        return new AiAnomalyResult(0.0, List.of("AI service unavailable"));
    }

    private AiSummaryResult fallbackSummary(UUID invoiceId, Throwable throwable) {
        log.warn("AI summary fallback for invoice {}", invoiceId);
        return new AiSummaryResult("Summary unavailable", 0.0);
    }
}
