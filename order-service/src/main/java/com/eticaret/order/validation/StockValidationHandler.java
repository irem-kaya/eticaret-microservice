package com.eticaret.order.validation;

import com.eticaret.order.dto.CreateOrderRequest;
import com.eticaret.order.exception.OrderValidationException;
import org.springframework.stereotype.Component;

@Component
public class StockValidationHandler extends OrderValidationHandler {

    @Override
    public void handle(CreateOrderRequest request) {
        request.items().forEach(item -> {
            if (item.quantity() <= 0) {
                throw new OrderValidationException(
                        "Geçersiz miktar: " + item.productName());
            }
        });
        handleNext(request);
    }
}