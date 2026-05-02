package com.eticaret.notification.dto;

import java.math.BigDecimal;

// ── Ödeme tamamlandı event'i ─────────────────────────────────────────────────
public record PaymentCompletedEvent(
        String paymentId,
        String orderId,
        String userId,
        String userEmail,
        BigDecimal amount
) {}

