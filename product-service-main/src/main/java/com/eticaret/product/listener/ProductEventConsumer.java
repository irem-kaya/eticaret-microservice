package com.eticaret.product.listener;

import com.eticaret.product.domain.ProductService;
import com.eticaret.product.event.ProductEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ProductEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);
    private final ProductService productService;

    public ProductEventConsumer(ProductService productService) {
        this.productService = productService;
    }

    @RabbitListener(queues = "product-events")
    public void handleProductEvent(ProductEvent event) {
        log.info("Kuyruktan mesaj alındı: {}", event.name());
        productService.saveFromScrapedEvent(
                event.name(),
                event.description(),
                event.price(),
                event.imageUrl(),
                event.categoryId()
        );
    }
}