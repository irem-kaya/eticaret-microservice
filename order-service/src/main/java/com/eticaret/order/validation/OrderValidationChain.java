package com.eticaret.order.validation;

import com.eticaret.order.dto.CreateOrderRequest;
import org.springframework.stereotype.Component;

@Component
public class OrderValidationChain {

    private final StockValidationHandler stockHandler;
    private final MinimumAmountHandler minimumAmountHandler;
    private final AddressValidationHandler addressHandler;
    private final PaymentLimitHandler paymentLimitHandler;

    public OrderValidationChain(StockValidationHandler stockHandler,
                                MinimumAmountHandler minimumAmountHandler,
                                AddressValidationHandler addressHandler,
                                PaymentLimitHandler paymentLimitHandler) {
        this.stockHandler = stockHandler;
        this.minimumAmountHandler = minimumAmountHandler;
        this.addressHandler = addressHandler;
        this.paymentLimitHandler = paymentLimitHandler;
    }

    public void validate(CreateOrderRequest request) {
        // Zinciri kur
        stockHandler
                .setNext(minimumAmountHandler)
                .setNext(addressHandler)
                .setNext(paymentLimitHandler);

        // Zinciri başlat
        stockHandler.handle(request);
    }
}