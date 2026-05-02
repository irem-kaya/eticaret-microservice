package com.eticaret.payment.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotBlank String orderId,
        @NotBlank String userId,
        @NotNull @DecimalMin("0.0") BigDecimal amount,
        @NotBlank String cardHolderName,
        @NotBlank String cardNumber,
        @NotBlank String expireMonth,
        @NotBlank String expireYear,
        @NotBlank String cvc
) {}