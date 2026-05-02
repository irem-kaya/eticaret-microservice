package com.eticaret.cart.domain;

import com.eticaret.cart.dto.*;
import com.eticaret.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Sepet yönetimi")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    @Operation(summary = "Sepete ürün ekle")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @RequestBody AddItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Ürün eklendi",
                cartService.addItem("test-user-id", request)));
    }

    @GetMapping
    @Operation(summary = "Sepeti getir")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.getCart("test-user-id")));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Sepetten ürün çıkar")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Ürün çıkarıldı",
                cartService.removeItem("test-user-id", productId)));
    }

    @PatchMapping("/items/{productId}")
    @Operation(summary = "Ürün miktarını güncelle")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @PathVariable Long productId,
            @RequestBody UpdateQuantityRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Miktar güncellendi",
                cartService.updateQuantity("test-user-id", productId, request)));
    }

    @PostMapping("/checkout")
    @Operation(summary = "Sepeti onayla")
    public ResponseEntity<ApiResponse<Void>> checkout() {
        cartService.checkout("test-user-id");
        return ResponseEntity.ok(ApiResponse.success("Sipariş oluşturuldu", null));
    }

    @DeleteMapping
    @Operation(summary = "Sepeti temizle")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartService.clearCart("test-user-id");
        return ResponseEntity.ok(ApiResponse.success("Sepet temizlendi", null));
    }
}