package com.eticaret.order.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        @NotEmpty List<OrderItemRequest> items,
        @NotNull BigDecimal totalAmount,
        @NotBlank String shippingAddress
) {}