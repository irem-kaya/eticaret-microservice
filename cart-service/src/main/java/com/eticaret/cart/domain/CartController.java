package com.eticaret.cart.domain;

import com.eticaret.common.ApiResponse;
import com.eticaret.cart.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Sepet yönetimi")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
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

    @PostMapping("/items")
    @Operation(summary = "Sepete ürün ekle")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @RequestBody AddItemRequest request,
            HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Ürün eklendi",
                cartService.addItem(userId, request)));
    }

    @GetMapping
    @Operation(summary = "Sepeti getir")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                cartService.getCart(userId)));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Sepetten ürün çıkar")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long productId,
            HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Ürün çıkarıldı",
                cartService.removeItem(userId, productId)));
    }

    @PatchMapping("/items/{productId}")
    @Operation(summary = "Ürün miktarını güncelle")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @PathVariable Long productId,
            @RequestBody UpdateQuantityRequest request,
            HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Miktar güncellendi",
                cartService.updateQuantity(userId, productId, request)));
    }

    @PostMapping("/checkout")
    @Operation(summary = "Sepeti onayla")
    public ResponseEntity<ApiResponse<Void>> checkout(
            HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        cartService.checkout(userId);
        return ResponseEntity.ok(ApiResponse.success("Sipariş oluşturuldu", null));
    }

    @DeleteMapping
    @Operation(summary = "Sepeti temizle")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Sepet temizlendi", null));
    }
}