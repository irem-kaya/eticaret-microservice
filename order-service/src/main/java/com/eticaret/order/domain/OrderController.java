package com.eticaret.order.domain;

import com.eticaret.common.ApiResponse;
import com.eticaret.order.dto.*;
import com.eticaret.order.facade.OrderFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "Sipariş yönetimi")
public class OrderController {

    private final OrderFacade orderFacade;
    private final OrderRepository orderRepository;

    public OrderController(OrderFacade orderFacade, OrderRepository orderRepository) {
        this.orderFacade = orderFacade;
        this.orderRepository = orderRepository;
    }

    private String getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String[] parts = token.split("\\.");
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<?, ?> claims = mapper.readValue(payload, Map.class);
                return (String) claims.get("sub");
            } catch (Exception e) {
                throw new RuntimeException("Geçersiz token");
            }
        }
        throw new RuntimeException("Token bulunamadı");
    }

    @PostMapping
    @Operation(summary = "Sipariş oluştur — CoR doğrulama zinciri ile")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sipariş oluşturuldu",
                        orderFacade.createOrder(userId, request)));
    }

    @GetMapping
    @Operation(summary = "Kullanıcının siparişlerini listele")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                orderFacade.getUserOrders(userId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Sipariş detayı")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderFacade.getOrderById(id)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Sipariş durumu güncelle")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Durum güncellendi",
                orderFacade.updateStatus(id, status)));
    }

    @GetMapping("/best-sellers")
    @Operation(summary = "En çok satan ürün ID'lerini getir")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBestSellers(
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = orderRepository.findBestSellingProductIds(pageable);
        List<Map<String, Object>> bestSellers = results.stream()
                .map(row -> Map.of(
                        "productId", row[0],
                        "totalSold", row[1]
                ))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(bestSellers));
    }
}