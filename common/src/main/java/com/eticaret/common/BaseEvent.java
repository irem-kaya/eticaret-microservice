package com.eticaret.common;

import java.time.LocalDateTime;
import java.util.UUID;

public record BaseEvent(
        String eventId,
        String eventType,
        LocalDateTime occurredAt
) {
    public static BaseEvent of(String eventType) {
        return new BaseEvent(
                UUID.randomUUID().toString(),
                eventType,
                LocalDateTime.now()
        );
    }
}