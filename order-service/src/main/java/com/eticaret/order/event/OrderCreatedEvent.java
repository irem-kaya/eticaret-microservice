package com.eticaret.order.event;

import com.eticaret.order.domain.Order;
import com.eticaret.order.domain.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        String eventId,
        String eventType,
        Long orderId,
        String userId,
        BigDecimal totalAmount,
        List<Long> productIds,
        LocalDateTime occurredAt
) {
    public static OrderCreatedEvent of(Order order) {
        return new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                "ORDER_CREATED",
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getItems().stream().map(OrderItem::getProductId).toList(),
                LocalDateTime.now()
        );
    }
}