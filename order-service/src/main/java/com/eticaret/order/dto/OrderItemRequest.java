package com.eticaret.order.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record OrderItemRequest(
        @NotNull Long productId,
        @NotBlank String productName,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @NotNull @Min(1) Integer quantity
) {}