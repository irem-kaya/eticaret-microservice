package com.eticaret.cart.repository;

import com.eticaret.cart.domain.Cart;
import java.util.Optional;

public interface CartRepository {
    Cart save(Cart cart);
    Optional<Cart> findByUserId(String userId);
    void deleteByUserId(String userId);
}