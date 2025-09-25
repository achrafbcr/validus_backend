package com.validus.backend.application.port.outgoing;

import java.util.Map;

public interface NotificationPort {

    void notify(String template, Map<String, Object> variables);
}
