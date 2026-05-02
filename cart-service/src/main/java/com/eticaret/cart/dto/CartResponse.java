package com.eticaret.cart.dto;

import com.eticaret.cart.domain.Cart;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
        String userId,
        List<CartItemResponse> items,
        BigDecimal totalPrice,
        int totalItems,
        LocalDateTime updatedAt
) {
    public static CartResponse from(Cart cart) {
        return new CartResponse(
                cart.getUserId(),
                cart.getItems().stream().map(CartItemResponse::from).toList(),
                cart.getTotalPrice(),
                cart.getTotalItems(),
                cart.getUpdatedAt()
        );
    }
}