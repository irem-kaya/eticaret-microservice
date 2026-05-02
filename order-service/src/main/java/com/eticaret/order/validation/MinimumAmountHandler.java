package com.eticaret.order.validation;

import com.eticaret.order.dto.CreateOrderRequest;
import com.eticaret.order.exception.OrderValidationException;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class MinimumAmountHandler extends OrderValidationHandler {

    private static final BigDecimal MINIMUM_ORDER_AMOUNT = new BigDecimal("10.00");

    @Override
    public void handle(CreateOrderRequest request) {
        if (request.totalAmount().compareTo(MINIMUM_ORDER_AMOUNT) < 0) {
            throw new OrderValidationException(
                    "Minimum sipariş tutarı: " + MINIMUM_ORDER_AMOUNT + " TL");
        }
        handleNext(request);
    }
}