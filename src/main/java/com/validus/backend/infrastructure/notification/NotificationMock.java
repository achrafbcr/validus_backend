package com.validus.backend.infrastructure.notification;

import com.validus.backend.application.port.outgoing.NotificationPort;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"dev", "default"})
public class NotificationMock implements NotificationPort {

    @Override
    public void notify(String template, Map<String, Object> variables) {
        log.info("Mock notification using template {} with variables {}", template, variables);
    }
}
