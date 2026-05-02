package com.eticaret.product.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record LowStockEvent(
        String eventId,
        String eventType,
        Long productId,
        String productName,
        Integer currentStock,
        LocalDateTime occurredAt
) {
    public static LowStockEvent of(Long productId, String productName, Integer stock) {
        return new LowStockEvent(
                UUID.randomUUID().toString(),
                "LOW_STOCK",
                productId,
                productName,
                stock,
                LocalDateTime.now()
        );
    }
}