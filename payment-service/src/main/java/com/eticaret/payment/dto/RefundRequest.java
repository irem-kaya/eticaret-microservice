package com.eticaret.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record RefundRequest(
        @NotBlank String paymentId,
        @NotBlank String orderId,
        String reason
) {}