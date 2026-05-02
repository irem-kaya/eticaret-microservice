package com.eticaret.payment.factory;

import com.eticaret.payment.strategy.PaymentStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    @Value("${app.payment.provider:iyzico}")
    private String defaultProvider;

    // Spring tüm PaymentStrategy implementasyonlarını otomatik inject eder
    public PaymentStrategyFactory(Map<String, PaymentStrategy> strategies) {
        this.strategies = strategies;
    }

    public PaymentStrategy getStrategy() {
        return getStrategy(defaultProvider);
    }

    public PaymentStrategy getStrategy(String provider) {
        PaymentStrategy strategy = strategies.get(provider);
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Bilinmeyen ödeme sağlayıcısı: " + provider);
        }
        return strategy;
    }
}