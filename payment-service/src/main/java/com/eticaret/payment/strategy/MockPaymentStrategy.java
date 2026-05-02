package com.eticaret.payment.strategy;

import com.eticaret.payment.dto.PaymentRequest;
import com.eticaret.payment.dto.PaymentResult;
import org.springframework.stereotype.Component;

@Component("mock")
public class MockPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult pay(PaymentRequest request) {
        return PaymentResult.success(
                "MOCK-" + System.currentTimeMillis(),
                request.orderId(),
                request.amount(),
                "Mock ödeme başarılı"
        );
    }

    @Override
    public PaymentResult refund(String paymentId, String orderId) {
        return PaymentResult.success(
                paymentId,
                orderId,
                null,
                "Mock iade başarılı"
        );
    }

    @Override
    public String getProviderName() {
        return "mock";
    }
}