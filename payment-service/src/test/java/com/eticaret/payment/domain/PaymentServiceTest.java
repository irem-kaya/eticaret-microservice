package com.eticaret.payment.domain;

import com.eticaret.payment.dto.*;
import com.eticaret.payment.event.PaymentCompletedEvent;
import com.eticaret.payment.factory.PaymentStrategyFactory;
import com.eticaret.payment.strategy.PaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceTest {

    @Mock PaymentRepository paymentRepository;
    @Mock PaymentStrategyFactory strategyFactory;
    @Mock PaymentStrategy paymentStrategy;
    @Mock RabbitTemplate rabbitTemplate;
    @InjectMocks PaymentService paymentService;

    private static final String ORDER_ID = "order-001";
    private static final String USER_ID  = "user-123";

    // PaymentRequest(orderId, userId, amount, cardHolderName, cardNumber, expireMonth, expireYear, cvc)
    private PaymentRequest buildRequest(BigDecimal amount) {
        return new PaymentRequest(ORDER_ID, USER_ID, amount,
                "Test User", "5528790000000008", "12", "2030", "123");
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "exchange", "test-exchange");
        ReflectionTestUtils.setField(paymentService, "paymentCompletedKey", "payment.completed");
        ReflectionTestUtils.setField(paymentService, "paymentFailedKey", "payment.failed");
        when(strategyFactory.getStrategy()).thenReturn(paymentStrategy);
        when(paymentStrategy.getProviderName()).thenReturn("IYZICO");
    }

    // ── processPayment ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("processPayment()")
    class ProcessPayment {

        @Test
        @DisplayName("başarılı ödeme → COMPLETED kaydedilir, payment.completed eventi yayınlanır")
        void processPayment_success_savesCompletedAndPublishesEvent() {
            PaymentRequest req = buildRequest(new BigDecimal("1500"));
            PaymentResult result = new PaymentResult(
                    true, UUID.randomUUID().toString(), ORDER_ID,
                    new BigDecimal("1500"), "Ödeme başarılı", "IYZICO", null);

            when(paymentStrategy.pay(req)).thenReturn(result);
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PaymentResult resp = paymentService.processPayment(req);

            assertThat(resp.success()).isTrue();

            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.COMPLETED);

            verify(rabbitTemplate).convertAndSend(
                    eq("test-exchange"), eq("payment.completed"), any(PaymentCompletedEvent.class));
        }

        @Test
        @DisplayName("başarısız ödeme → FAILED kaydedilir, payment.failed eventi yayınlanır")
        void processPayment_failure_savesFailedAndPublishesFailedEvent() {
            PaymentRequest req = buildRequest(new BigDecimal("1500"));
            PaymentResult result = new PaymentResult(
                    false, null, ORDER_ID,
                    new BigDecimal("1500"), "Kart reddedildi", "IYZICO", null);

            when(paymentStrategy.pay(req)).thenReturn(result);
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PaymentResult resp = paymentService.processPayment(req);

            assertThat(resp.success()).isFalse();

            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);

            verify(rabbitTemplate).convertAndSend(
                    eq("test-exchange"), eq("payment.failed"), any(PaymentResult.class));
        }

        @Test
        @DisplayName("her ödeme işleminde strateji factory'den alınır (Strategy Pattern)")
        void processPayment_alwaysCallsFactory() {
            PaymentRequest req = buildRequest(BigDecimal.TEN);
            PaymentResult result = new PaymentResult(
                    true, "pid", ORDER_ID, BigDecimal.TEN, "ok", "IYZICO", null);

            when(paymentStrategy.pay(req)).thenReturn(result);
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            paymentService.processPayment(req);

            verify(strategyFactory, times(1)).getStrategy();
        }

        @Test
        @DisplayName("sıfır tutarlı ödeme → işlenir (validation dışarıda)")
        void processPayment_zeroAmount_processed() {
            PaymentRequest req = buildRequest(BigDecimal.ZERO);
            PaymentResult result = new PaymentResult(
                    false, null, ORDER_ID, BigDecimal.ZERO, "Geçersiz tutar", "IYZICO", null);

            when(paymentStrategy.pay(req)).thenReturn(result);
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PaymentResult resp = paymentService.processPayment(req);

            assertThat(resp.success()).isFalse();
        }
    }

    // ── refund ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("refund()")
    class Refund {

        @Test
        @DisplayName("başarılı iade → ödeme REFUNDED olarak güncellenir")
        void refund_success_updatesStatusToRefunded() {
            String paymentId = "pay-abc";
            Payment existing = new Payment();
            existing.setPaymentId(paymentId);
            existing.setStatus(PaymentStatus.COMPLETED);

            RefundRequest req = new RefundRequest(paymentId, ORDER_ID,null);
            PaymentResult result = new PaymentResult(
                    true, paymentId, ORDER_ID, BigDecimal.TEN, "İade başarılı", "IYZICO", null);

            when(paymentStrategy.refund(paymentId, ORDER_ID)).thenReturn(result);
            when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(existing));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PaymentResult resp = paymentService.refund(req);

            assertThat(resp.success()).isTrue();
            assertThat(existing.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            verify(paymentRepository).save(existing);
        }

        @Test
        @DisplayName("ödeme kaydı yok → exception fırlatılmaz, sadece sonuç döner")
        void refund_noRecord_noException() {
            String paymentId = "pay-xyz";
            RefundRequest req = new RefundRequest(paymentId, ORDER_ID,null);
            PaymentResult result = new PaymentResult(
                    true, paymentId, ORDER_ID, BigDecimal.TEN, "İade tamam", "IYZICO", null);

            when(paymentStrategy.refund(paymentId, ORDER_ID)).thenReturn(result);
            when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.empty());

            assertThatNoException().isThrownBy(() -> paymentService.refund(req));
        }
    }

    // ── getPaymentsByOrder ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("getPaymentsByOrder()")
    class GetPaymentsByOrder {

        @Test
        @DisplayName("mevcut sipariş → liste döner")
        void getPaymentsByOrder_exists_returnsList() {
            Payment p = new Payment();
            p.setOrderId(ORDER_ID);
            when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(p));

            List<Payment> result = paymentService.getPaymentsByOrder(ORDER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOrderId()).isEqualTo(ORDER_ID);
        }

        @Test
        @DisplayName("ödemesi olmayan sipariş → boş liste")
        void getPaymentsByOrder_noPayments_returnsEmpty() {
            when(paymentRepository.findByOrderId("no-order")).thenReturn(List.of());

            assertThat(paymentService.getPaymentsByOrder("no-order")).isEmpty();
        }
    }
}