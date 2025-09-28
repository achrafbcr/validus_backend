package com.validus.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public record AiProperties(String baseUrl, String apiKey, int timeoutMs) {
}
