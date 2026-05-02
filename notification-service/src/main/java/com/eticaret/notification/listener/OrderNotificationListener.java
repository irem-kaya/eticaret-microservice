package com.eticaret.notification.listener;

import com.eticaret.notification.dto.OrderCreatedEvent;
import com.eticaret.notification.dto.PaymentCompletedEvent;
import com.eticaret.notification.dto.PaymentFailedEvent;
import com.eticaret.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationListener.class);

    private final NotificationService notificationService;

    public OrderNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.order-created}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("📦 Sipariş oluşturuldu bildirimi: orderId={}, userId={}",
                event.orderId(), event.userId());
        notificationService.sendOrderConfirmation(event);
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.payment-completed}")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("✅ Ödeme tamamlandı bildirimi: paymentId={}, orderId={}",
                event.paymentId(), event.orderId());
        notificationService.sendPaymentSuccess(event);
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.payment-failed}")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("❌ Ödeme başarısız bildirimi: orderId={}, message={}",
                event.orderId(), event.message());
        notificationService.sendPaymentFailure(event);
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.low-stock}")
    public void handleLowStock(java.util.Map<String, Object> event) {
        log.warn("⚠️ Düşük stok bildirimi: productId={}, productName={}, stock={}",
                event.get("productId"), event.get("productName"), event.get("stock"));
        notificationService.sendLowStockAlert(event);
    }
}