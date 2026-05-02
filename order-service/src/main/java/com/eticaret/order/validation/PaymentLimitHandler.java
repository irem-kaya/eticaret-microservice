package com.eticaret.order.validation;

import com.eticaret.order.dto.CreateOrderRequest;
import com.eticaret.order.exception.OrderValidationException;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class PaymentLimitHandler extends OrderValidationHandler {

    private static final BigDecimal MAX_ORDER_AMOUNT = new BigDecimal("50000.00");

    @Override
    public void handle(CreateOrderRequest request) {
        if (request.totalAmount().compareTo(MAX_ORDER_AMOUNT) > 0) {
            throw new OrderValidationException(
                    "Sipariş tutarı limiti aşıldı. Maksimum: " + MAX_ORDER_AMOUNT + " TL");
        }
        handleNext(request);
    }
}