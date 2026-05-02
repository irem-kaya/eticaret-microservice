package com.eticaret.cart.domain;

import com.eticaret.cart.dto.*;
import com.eticaret.cart.event.CartCheckedOutEvent;
import com.eticaret.cart.exception.CartNotFoundException;
import com.eticaret.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock RabbitTemplate rabbitTemplate;
    @InjectMocks CartService cartService;

    private static final String USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cartService, "exchange", "test-exchange");
        ReflectionTestUtils.setField(cartService, "cartCheckedOutKey", "cart.checkedout");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private AddItemRequest addItemReq(Long productId, String name, BigDecimal price, int qty) {
        return new AddItemRequest(productId, name, price, qty, null);
    }

    // ── getCart ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getCart()")
    class GetCart {

        @Test
        @DisplayName("sepet mevcut → CartResponse döner")
        void getCart_exists_returnsResponse() {
            Cart cart = new Cart(USER_ID);
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

            CartResponse resp = cartService.getCart(USER_ID);

            assertThat(resp.userId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("sepet yok → boş sepet oluşturulur (exception yok)")
        void getCart_notExists_returnsEmptyCart() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            CartResponse resp = cartService.getCart(USER_ID);

            assertThat(resp.userId()).isEqualTo(USER_ID);
            assertThat(resp.items()).isEmpty();
        }
    }

    // ── addItem ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addItem()")
    class AddItem {

        @Test
        @DisplayName("yeni ürün → sepete eklenir ve kaydedilir")
        void addItem_newProduct_addsToCart() {
            Cart cart = new Cart(USER_ID);
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

            CartResponse resp = cartService.addItem(USER_ID,
                    addItemReq(10L, "Laptop", new BigDecimal("15000"), 1));

            verify(cartRepository).save(any(Cart.class));
            assertThat(resp.items()).hasSize(1);
        }

        @Test
        @DisplayName("sepet yoksa yeni sepet oluşturulur")
        void addItem_noExistingCart_createsNewCart() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            CartResponse resp = cartService.addItem(USER_ID,
                    addItemReq(5L, "Mouse", new BigDecimal("250"), 2));

            verify(cartRepository).save(any(Cart.class));
            assertThat(resp.items()).hasSize(1);
        }

        @Test
        @DisplayName("aynı ürün tekrar eklenince miktar güncellenir")
        void addItem_sameProduct_updatesQuantity() {
            Cart cart = new Cart(USER_ID);
            cart.addItem(new CartItem(10L, "Laptop", new BigDecimal("15000"), 1, null));
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

            cartService.addItem(USER_ID, addItemReq(10L, "Laptop", new BigDecimal("15000"), 2));

            // Cart.addItem mantığına göre miktar 3 olmalı
            CartItem item = cart.getItems().stream()
                    .filter(i -> i.getProductId().equals(10L))
                    .findFirst().orElseThrow();
            assertThat(item.getQuantity()).isEqualTo(3);
        }
    }

    // ── removeItem ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("removeItem()")
    class RemoveItem {

        @Test
        @DisplayName("mevcut ürün → sepetten çıkarılır")
        void removeItem_exists_removesFromCart() {
            Cart cart = new Cart(USER_ID);
            cart.addItem(new CartItem(10L, "Laptop", BigDecimal.TEN, 1, null));
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

            cartService.removeItem(USER_ID, 10L);

            verify(cartRepository).save(any(Cart.class));
            assertThat(cart.getItems()).isEmpty();
        }

        @Test
        @DisplayName("sepet bulunamadı → CartNotFoundException")
        void removeItem_noCart_throws() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.removeItem(USER_ID, 1L))
                    .isInstanceOf(CartNotFoundException.class);
        }
    }

    // ── updateQuantity ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateQuantity()")
    class UpdateQuantity {

        @Test
        @DisplayName("geçerli güncelleme → kaydedilir")
        void updateQuantity_valid_savesCart() {
            Cart cart = new Cart(USER_ID);
            cart.addItem(new CartItem(10L, "Laptop", BigDecimal.TEN, 1, null));
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

            cartService.updateQuantity(USER_ID, 10L, new UpdateQuantityRequest(5));

            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        @DisplayName("sepet yok → CartNotFoundException")
        void updateQuantity_noCart_throws() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.updateQuantity(USER_ID, 1L, new UpdateQuantityRequest(2)))
                    .isInstanceOf(CartNotFoundException.class);
        }
    }

    // ── checkout ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("checkout()")
    class Checkout {

        @Test
        @DisplayName("checkout → event yayınlanır ve sepet temizlenir")
        void checkout_valid_publishesEventAndClearsCart() {
            Cart cart = new Cart(USER_ID);
            cart.addItem(new CartItem(10L, "Laptop", BigDecimal.TEN, 1, null));
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

            cartService.checkout(USER_ID);

            verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("cart.checkedout"), any(CartCheckedOutEvent.class));
            verify(cartRepository).save(any(Cart.class));
            assertThat(cart.getItems()).isEmpty();
        }

        @Test
        @DisplayName("sepet yok → CartNotFoundException")
        void checkout_noCart_throws() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.checkout(USER_ID))
                    .isInstanceOf(CartNotFoundException.class);
        }
    }

    // ── clearCart ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("clearCart()")
    class ClearCart {

        @Test
        @DisplayName("geçerli kullanıcı → sepet boşaltılır")
        void clearCart_valid_clearsAndSaves() {
            Cart cart = new Cart(USER_ID);
            cart.addItem(new CartItem(1L, "X", BigDecimal.TEN, 1, null));
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

            cartService.clearCart(USER_ID);

            verify(cartRepository).save(any(Cart.class));
            assertThat(cart.getItems()).isEmpty();
        }
    }
}