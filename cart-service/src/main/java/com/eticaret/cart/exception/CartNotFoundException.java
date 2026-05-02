package com.eticaret.cart.exception;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String userId) {
        super("Sepet bulunamadı: " + userId);
    }
}