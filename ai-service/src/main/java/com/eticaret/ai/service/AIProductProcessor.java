// AI Service - src/main/java/com/eticaret/ai/service/AIProductProcessor.java
package com.eticaret.ai.service;

import com.eticaret.ai.dto.RawProductRequest;
import com.eticaret.ai.event.ProductEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class AIProductProcessor {

    private final RabbitTemplate rabbitTemplate; // RabbitMQ entegrasyonu[cite: 1]
    private final UnsplashService unsplashService; // Unsplash API entegrasyonu[cite: 1]

    // Lombok yerine manuel Constructor Injection (Hocanın sevdiği yöntem)
    public AIProductProcessor(RabbitTemplate rabbitTemplate, UnsplashService unsplashService) {
        this.rabbitTemplate = rabbitTemplate;
        this.unsplashService = unsplashService;
    }

    @Async // Asenkron işleme[cite: 1]
    public void processAndSync(List<RawProductRequest> requests) {
        for (RawProductRequest raw : requests) {
            try {
                // AI ile açıklama zenginleştirme simülasyonu
                String enrichedDesc = generateDescription(raw.rawName());

                // Unsplash üzerinden görsel bulma[cite: 1]
                String imageUrl = unsplashService.searchImage(raw.rawName());

                // Olay tabanlı mimari için event oluşturma[cite: 1]
                ProductEvent event = new ProductEvent(
                        raw.rawName(),
                        enrichedDesc,
                        raw.price(),
                        imageUrl,
                        raw.categoryId()
                );

                // RabbitMQ üzerinden Product Service'e (Port: 8082) gönderim
                rabbitTemplate.convertAndSend("product-events", "product.added", event);

            } catch (Exception e) {
                // Retry logic buraya eklenebilir[cite: 1]
                System.err.println("Ürün işlenirken hata oluştu: " + raw.rawName());
            }
        }
    }

    private String generateDescription(String name) {
        return "Yüksek kaliteli " + name + ", konfor ve şıklığı bir arada sunuyor.";
    }
}