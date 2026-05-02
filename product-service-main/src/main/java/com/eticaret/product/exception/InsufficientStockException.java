package com.eticaret.product.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName) {
        super("Yetersiz stok: " + productName);
    }
}