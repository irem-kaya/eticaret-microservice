package com.eticaret.product.event;

import java.math.BigDecimal;

public record ProductEvent(
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        Long categoryId
) {}