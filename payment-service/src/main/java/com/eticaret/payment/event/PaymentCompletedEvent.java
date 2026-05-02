package com.eticaret.payment.event;

import com.eticaret.payment.dto.PaymentResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentCompletedEvent(
        String eventId,
        String eventType,
        String paymentId,
        String orderId,
        String userId,
        BigDecimal amount,
        LocalDateTime occurredAt
) {
    public static PaymentCompletedEvent of(PaymentResult result, String userId) {
        return new PaymentCompletedEvent(
                UUID.randomUUID().toString(),
                "PAYMENT_COMPLETED",
                result.paymentId(),
                result.orderId(),
                userId,
                result.amount(),
                LocalDateTime.now()
        );
    }
}