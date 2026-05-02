package com.eticaret.notification.listener;

import com.eticaret.notification.factory.NotificationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);
    private final NotificationFactory notificationFactory;

    public NotificationListener(NotificationFactory notificationFactory) {
        this.notificationFactory = notificationFactory;
    }

    @RabbitListener(queues = "order.queue")
    public void onOrderCreated(Map<String, Object> event) {
        log.info("Sipariş eventi alındı: {}", event);
        try {
            String userId = (String) event.get("userId");
            String orderId = String.valueOf(event.get("orderId"));

            notificationFactory.getSender().send(
                    userId + "@eticaret.com",
                    "Siparişiniz Alındı! #" + orderId,
                    "Siparişiniz başarıyla oluşturuldu. Sipariş no: " + orderId
            );
        } catch (Exception e) {
            log.error("Bildirim gönderilemedi: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "payment.queue")
    public void onPaymentCompleted(Map<String, Object> event) {
        log.info("Ödeme eventi alındı: {}", event);
        try {
            String userId = (String) event.get("userId");
            String orderId = String.valueOf(event.get("orderId"));

            notificationFactory.getSender().send(
                    userId + "@eticaret.com",
                    "Ödemeniz Onaylandı! #" + orderId,
                    "Ödemeniz başarıyla alındı. Sipariş no: " + orderId
            );
        } catch (Exception e) {
            log.error("Bildirim gönderilemedi: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "user.queue")
    public void onUserRegistered(Map<String, Object> event) {
        log.info("Kullanıcı kayıt eventi alındı: {}", event);
        try {
            String email = (String) event.get("email");
            String firstName = (String) event.get("firstName");

            notificationFactory.getSender().send(
                    email,
                    "Hoş Geldiniz! 🎉",
                    "Merhaba " + firstName + ", e-ticaret platformumuza hoş geldiniz!"
            );
        } catch (Exception e) {
            log.error("Bildirim gönderilemedi: {}", e.getMessage());
        }
    }
}