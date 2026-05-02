package com.eticaret.product.dto;

import com.eticaret.product.domain.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String imageUrl,
        boolean active,
        String categoryName,
        LocalDateTime createdAt
) {
    public static ProductResponse from(Product product) {
        return from(product, false);
    }

    public static ProductResponse from(Product product, boolean hideStock) {
        Integer stockValue = hideStock ? null : product.getStock();
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                stockValue,
                product.getImageUrl(),
                product.isActive(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCreatedAt()
        );
    }
}