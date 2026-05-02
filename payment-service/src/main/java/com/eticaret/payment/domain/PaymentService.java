package com.eticaret.payment.domain;

import com.eticaret.payment.dto.*;
import com.eticaret.payment.event.PaymentCompletedEvent;
import com.eticaret.payment.factory.PaymentStrategyFactory;
import com.eticaret.payment.strategy.PaymentStrategy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentStrategyFactory strategyFactory;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.payment-completed}")
    private String paymentCompletedKey;

    @Value("${app.rabbitmq.routing-key.payment-failed}")
    private String paymentFailedKey;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentStrategyFactory strategyFactory,
                          RabbitTemplate rabbitTemplate) {
        this.paymentRepository = paymentRepository;
        this.strategyFactory = strategyFactory;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public PaymentResult processPayment(PaymentRequest request) {
        PaymentStrategy strategy = strategyFactory.getStrategy();
        PaymentResult result = strategy.pay(request);

        Payment payment = new Payment();
        payment.setPaymentId(result.paymentId());
        payment.setOrderId(request.orderId());
        payment.setUserId(request.userId());
        payment.setAmount(request.amount());
        payment.setProvider(strategy.getProviderName());
        payment.setMessage(result.message());

        if (result.success()) {
            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);
            rabbitTemplate.convertAndSend(exchange, paymentCompletedKey,
                    PaymentCompletedEvent.of(result, request.userId()));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            rabbitTemplate.convertAndSend(exchange, paymentFailedKey, result);
        }

        return result;
    }

    @Transactional
    public PaymentResult refund(RefundRequest request) {
        PaymentStrategy strategy = strategyFactory.getStrategy();
        PaymentResult result = strategy.refund(request.paymentId(), request.orderId());

        paymentRepository.findByPaymentId(request.paymentId())
                .ifPresent(p -> {
                    p.setStatus(PaymentStatus.REFUNDED);
                    paymentRepository.save(p);
                });

        return result;
    }

    public List<Payment> getPaymentsByOrder(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}