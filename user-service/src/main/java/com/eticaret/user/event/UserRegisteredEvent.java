package com.eticaret.user.event;

import java.time.LocalDateTime;

public record UserRegisteredEvent(
        String eventId,
        String eventType,
        Long userId,
        String email,
        String firstName,
        LocalDateTime occurredAt
) {
    public static UserRegisteredEvent of(Long userId, String email, String firstName) {
        return new UserRegisteredEvent(
                java.util.UUID.randomUUID().toString(),
                "USER_REGISTERED",
                userId,
                email,
                firstName,
                LocalDateTime.now()
        );
    }
}