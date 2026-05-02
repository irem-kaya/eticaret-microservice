package com.eticaret.order.validation;

import com.eticaret.order.dto.CreateOrderRequest;

public abstract class OrderValidationHandler {

    private OrderValidationHandler next;

    public OrderValidationHandler setNext(OrderValidationHandler next) {
        this.next = next;
        return next;
    }

    public abstract void handle(CreateOrderRequest request);

    protected void handleNext(CreateOrderRequest request) {
        if (next != null) {
            next.handle(request);
        }
    }
}