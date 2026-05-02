package com.eticaret.cart.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart {

    private String userId;
    private List<CartItem> items = new ArrayList<>();
    private LocalDateTime updatedAt;

    public Cart() {}

    public Cart(String userId) {
        this.userId = userId;
        this.updatedAt = LocalDateTime.now();
    }

    public void addItem(CartItem newItem) {
        Optional<CartItem> existing = items.stream()
                .filter(i -> i.getProductId().equals(newItem.getProductId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + newItem.getQuantity());
        } else {
            items.add(newItem);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void removeItem(Long productId) {
        items.removeIf(i -> i.getProductId().equals(productId));
        this.updatedAt = LocalDateTime.now();
    }

    public void updateQuantity(Long productId, int quantity) {
        items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .ifPresent(i -> i.setQuantity(quantity));
        this.updatedAt = LocalDateTime.now();
    }

    public void clear() {
        items.clear();
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}