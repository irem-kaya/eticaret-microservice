package com.eticaret.cart.dto;

import com.eticaret.cart.domain.CartItem;
import java.math.BigDecimal;

public record CartItemResponse(
        Long productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        BigDecimal totalPrice,
        String imageUrl
) {
    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(
                item.getProductId(),
                item.getProductName(),
                item.getPrice(),
                item.getQuantity(),
                item.getTotalPrice(),
                item.getImageUrl()
        );
    }
}