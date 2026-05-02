package com.eticaret.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResult(
        boolean success,
        String paymentId,
        String orderId,
        BigDecimal amount,
        String message,
        String provider,
        LocalDateTime processedAt
) {
    public static PaymentResult success(String paymentId, String orderId,
                                        BigDecimal amount, String message) {
        return new PaymentResult(true, paymentId, orderId,
                amount, message, null, LocalDateTime.now());
    }

    public static PaymentResult failure(String orderId, String message) {
        return new PaymentResult(false, null, orderId,
                null, message, null, LocalDateTime.now());
    }
}