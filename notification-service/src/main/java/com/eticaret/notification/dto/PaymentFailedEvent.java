package com.eticaret.notification.dto;

public record PaymentFailedEvent(
        String orderId,
        String userId,
        String userEmail,
        String message
) {}