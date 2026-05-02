package com.eticaret.order.dto;

import com.eticaret.order.domain.Order;
import com.eticaret.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String userId,
        OrderStatus status,
        BigDecimal totalAmount,
        String shippingAddress,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getCreatedAt()
        );
    }
}