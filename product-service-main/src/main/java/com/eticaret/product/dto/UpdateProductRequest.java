package com.eticaret.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
        @NotNull @Min(0) Integer stock,
        String imageUrl
) {}