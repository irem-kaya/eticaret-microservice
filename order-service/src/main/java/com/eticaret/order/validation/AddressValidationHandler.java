package com.eticaret.order.validation;

import com.eticaret.order.dto.CreateOrderRequest;
import com.eticaret.order.exception.OrderValidationException;
import org.springframework.stereotype.Component;

@Component
public class AddressValidationHandler extends OrderValidationHandler {

    @Override
    public void handle(CreateOrderRequest request) {
        if (request.shippingAddress() == null
                || request.shippingAddress().trim().length() < 10) {
            throw new OrderValidationException(
                    "Geçerli bir teslimat adresi giriniz");
        }
        handleNext(request);
    }
}