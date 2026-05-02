package com.eticaret.payment.strategy;

import com.eticaret.payment.dto.PaymentRequest;
import com.eticaret.payment.dto.PaymentResult;

public interface PaymentStrategy {
    PaymentResult pay(PaymentRequest request);
    PaymentResult refund(String paymentId, String orderId);
    String getProviderName();
}