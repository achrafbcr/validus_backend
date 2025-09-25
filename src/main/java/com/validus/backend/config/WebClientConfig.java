package com.validus.backend.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient aiWebClient(AiProperties properties) {
        HttpClient client = HttpClient.create()
            .responseTimeout(Duration.ofMillis(properties.timeoutMs()));
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(client))
            .defaultHeader("X-API-KEY", properties.apiKey())
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build())
            .build();
    }
}
