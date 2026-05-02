package com.eticaret.cart.event;

import com.eticaret.cart.domain.Cart;
import com.eticaret.cart.domain.CartItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CartCheckedOutEvent(
        String eventId,
        String eventType,
        String userId,
        List<CartItem> items,
        BigDecimal totalPrice,
        LocalDateTime occurredAt
) {
    public static CartCheckedOutEvent of(String userId, Cart cart) {
        return new CartCheckedOutEvent(
                UUID.randomUUID().toString(),
                "CART_CHECKED_OUT",
                userId,
                cart.getItems(),
                cart.getTotalPrice(),
                LocalDateTime.now()
        );
    }
}