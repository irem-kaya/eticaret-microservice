package com.eticaret.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Ürün adı boş olamaz")
        String name,

        String description,

        @NotNull(message = "Fiyat boş olamaz")
        @DecimalMin(value = "0.0", inclusive = false, message = "Fiyat 0'dan büyük olmalı")
        BigDecimal price,

        @NotNull(message = "Stok boş olamaz")
        @Min(value = 0, message = "Stok negatif olamaz")
        Integer stock,

        @NotNull(message = "Kategori boş olamaz")
        Long categoryId,

        String imageUrl
) {}