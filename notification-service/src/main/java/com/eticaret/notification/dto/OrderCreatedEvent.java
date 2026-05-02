package com.eticaret.notification.dto;

import java.math.BigDecimal;

// ── Sipariş oluşturuldu event'i ──────────────────────────────────────────────
public record OrderCreatedEvent(
        String orderId,
        String userId,
        String userEmail,
        BigDecimal totalAmount,
        String deliveryAddress
) {}