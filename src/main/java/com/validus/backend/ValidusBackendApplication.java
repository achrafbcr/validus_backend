package com.validus.backend;

import com.validus.backend.config.AiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({AiProperties.class})
public class ValidusBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidusBackendApplication.class, args);
    }
}
