// AI Service - src/main/java/com/eticaret/ai/dto/RawProductRequest.java
package com.eticaret.ai.dto;

import java.math.BigDecimal;

/**
 * Kazıma verisini karşılayan DTO.
 * Record kullanımı, Lombok olmadan kodun temiz kalmasını sağlar.
 */
public record RawProductRequest(
        String rawName,
        String categoryName,
        Long categoryId,
        BigDecimal price,
        String sourceUrl
) {}