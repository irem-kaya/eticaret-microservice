package com.eticaret.order.domain;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PAYMENT_PROCESSING,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}