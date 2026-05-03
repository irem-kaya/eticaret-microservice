package com.eticaret.notification.domain;

import com.eticaret.notification.dto.OrderCreatedEvent;
import com.eticaret.notification.dto.PaymentCompletedEvent;
import com.eticaret.notification.dto.PaymentFailedEvent;
import com.eticaret.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

    @Mock JavaMailSender mailSender;

    @InjectMocks NotificationService notificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "fromEmail", "noreply@tmarket.com");
        ReflectionTestUtils.setField(notificationService, "adminEmail", "admin@tmarket.com");
        ReflectionTestUtils.setField(notificationService, "emailEnabled", true);
    }

    private OrderCreatedEvent buildOrderCreatedEvent() {
        return new OrderCreatedEvent("1", "user-123", "test@example.com", new BigDecimal("30000"), "İstanbul, Türkiye");
    }

    private PaymentCompletedEvent buildPaymentCompletedEvent() {
        return new PaymentCompletedEvent("payment-456", "1", "user-123", "test@example.com", new BigDecimal("30000"));
    }

    private PaymentFailedEvent buildPaymentFailedEvent() {
        return new PaymentFailedEvent("1", "user-123", "test@example.com", "Kart reddi");
    }

    @Nested
    @DisplayName("sendOrderConfirmation()")
    class SendOrderConfirmation {

        @Test
        @DisplayName("geçerli event → mail gönderilir")
        void sendOrderConfirmation_validEvent_sendsEmail() {
            notificationService.sendOrderConfirmation(buildOrderCreatedEvent());
            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("mail hatası → exception dışarı çıkmaz")
        void sendOrderConfirmation_mailFails_noException() {
            doThrow(new RuntimeException("SMTP hatası"))
                    .when(mailSender).send(any(SimpleMailMessage.class));

            org.assertj.core.api.Assertions.assertThatNoException().isThrownBy(
                    () -> notificationService.sendOrderConfirmation(buildOrderCreatedEvent())
            );
        }
    }

    @Nested
    @DisplayName("sendPaymentSuccess()")
    class SendPaymentSuccess {

        @Test
        @DisplayName("geçerli event → mail gönderilir")
        void sendPaymentSuccess_validEvent_sendsEmail() {
            notificationService.sendPaymentSuccess(buildPaymentCompletedEvent());
            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("mail hatası → exception dışarı çıkmaz")
        void sendPaymentSuccess_mailFails_noException() {
            doThrow(new RuntimeException("SMTP hatası"))
                    .when(mailSender).send(any(SimpleMailMessage.class));

            org.assertj.core.api.Assertions.assertThatNoException().isThrownBy(
                    () -> notificationService.sendPaymentSuccess(buildPaymentCompletedEvent())
            );
        }
    }

    @Nested
    @DisplayName("sendPaymentFailure()")
    class SendPaymentFailure {

        @Test
        @DisplayName("geçerli event → mail gönderilir")
        void sendPaymentFailure_validEvent_sendsEmail() {
            notificationService.sendPaymentFailure(buildPaymentFailedEvent());
            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("mail hatası → exception dışarı çıkmaz")
        void sendPaymentFailure_mailFails_noException() {
            doThrow(new RuntimeException("SMTP hatası"))
                    .when(mailSender).send(any(SimpleMailMessage.class));

            org.assertj.core.api.Assertions.assertThatNoException().isThrownBy(
                    () -> notificationService.sendPaymentFailure(buildPaymentFailedEvent())
            );
        }
    }

    @Nested
    @DisplayName("sendLowStockAlert()")
    class SendLowStockAlert {

        @Test
        @DisplayName("düşük stok event → admin mail gönderilir")
        void sendLowStockAlert_validEvent_sendsAdminEmail() {
            Map<String, Object> event = Map.of(
                    "productId", 1L,
                    "productName", "Laptop",
                    "stock", 2
            );

            notificationService.sendLowStockAlert(event);

            verify(mailSender).send(any(SimpleMailMessage.class));
        }
    }
}