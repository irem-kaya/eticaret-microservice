package com.eticaret.payment.domain;

import com.eticaret.common.ApiResponse;
import com.eticaret.payment.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "Ödeme işlemleri")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Ödeme al — Strategy + Factory pattern")
    public ResponseEntity<ApiResponse<PaymentResult>> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.processPayment(request)));
    }

    @PostMapping("/refund")
    @Operation(summary = "İade işlemi")
    public ResponseEntity<ApiResponse<PaymentResult>> refund(
            @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.refund(request)));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Sipariş ödemelerini getir")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentsByOrder(
            @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getPaymentsByOrder(orderId)));
    }
}