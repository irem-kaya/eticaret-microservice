package com.eticaret.ai.event;


import java.math.BigDecimal;

/**
 * RabbitMQ üzerinden Product Service'e gönderilecek veri formatı.
 */
public record ProductEvent(
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        Long categoryId
) {}
