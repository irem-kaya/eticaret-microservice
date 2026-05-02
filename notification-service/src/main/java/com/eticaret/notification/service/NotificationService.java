package com.eticaret.notification.service;

import com.eticaret.notification.dto.OrderCreatedEvent;
import com.eticaret.notification.dto.PaymentCompletedEvent;
import com.eticaret.notification.dto.PaymentFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${app.notification.from-email:noreply@tmarket.com}")
    private String fromEmail;

    @Value("${app.notification.admin-email:admin@tmarket.com}")
    private String adminEmail;

    @Value("${app.notification.email-enabled:false}")
    private boolean emailEnabled;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(OrderCreatedEvent event) {
        String subject = "Siparişiniz Alındı - #" + event.orderId();
        String body = """
                Sayın Müşterimiz,
                
                %s numaralı siparişiniz başarıyla alınmıştır.
                Toplam tutar: %.2f TL
                
                Siparişinizi takip etmek için hesabınıza giriş yapabilirsiniz.
                
                TMarket Pro
                """.formatted(event.orderId(), event.totalAmount());

        log.info("📧 Sipariş onay maili gönderiliyor: userId={}, orderId={}", event.userId(), event.orderId());
        sendEmail(event.userEmail(), subject, body);
    }

    public void sendPaymentSuccess(PaymentCompletedEvent event) {
        String subject = "Ödemeniz Onaylandı - #" + event.orderId();
        String body = """
                Sayın Müşterimiz,
                
                %s numaralı siparişinizin ödemesi başarıyla alınmıştır.
                Ödeme ID: %s
                Tutar: %.2f TL
                
                Siparişiniz en kısa sürede kargoya verilecektir.
                
                TMarket Pro
                """.formatted(event.orderId(), event.paymentId(), event.amount());

        log.info("📧 Ödeme onay maili gönderiliyor: orderId={}", event.orderId());
        sendEmail(event.userEmail(), subject, body);
    }

    public void sendPaymentFailure(PaymentFailedEvent event) {
        String subject = "Ödeme Başarısız - #" + event.orderId();
        String body = """
                Sayın Müşterimiz,
                
                %s numaralı siparişinizin ödemesi başarısız olmuştur.
                Sebep: %s
                
                Lütfen farklı bir ödeme yöntemi deneyiniz.
                
                TMarket Pro
                """.formatted(event.orderId(), event.message());

        log.warn("📧 Ödeme başarısız maili gönderiliyor: orderId={}", event.orderId());
        sendEmail(event.userEmail(), subject, body);
    }

    public void sendLowStockAlert(Map<String, Object> event) {
        String subject = "⚠️ Düşük Stok Uyarısı: " + event.get("productName");
        String body = """
                Admin,
                
                Ürün stoğu kritik seviyeye düştü:
                Ürün ID: %s
                Ürün Adı: %s
                Mevcut Stok: %s
                
                Lütfen stok güncellemesi yapınız.
                
                TMarket Pro Sistem
                """.formatted(event.get("productId"), event.get("productName"), event.get("stock"));

        log.warn("📧 Düşük stok uyarısı admin'e gönderiliyor: productName={}", event.get("productName"));
        sendEmail(adminEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("📬 [EMAIL MOCK] To: {} | Subject: {}", to, subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("✅ Email gönderildi: {}", to);
        } catch (Exception e) {
            log.error("❌ Email gönderilemedi: {} - {}", to, e.getMessage());
        }
    }
}