package com.eticaret.cart.domain;

import com.eticaret.cart.dto.*;
import com.eticaret.cart.event.CartCheckedOutEvent;
import com.eticaret.cart.exception.CartNotFoundException;
import com.eticaret.cart.repository.CartRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.cart-checkedout}")
    private String cartCheckedOutKey;

    public CartService(CartRepository cartRepository, RabbitTemplate rabbitTemplate) {
        this.cartRepository = cartRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public CartResponse getCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(new Cart(userId));
        return CartResponse.from(cart);
    }

    public CartResponse addItem(String userId, AddItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(new Cart(userId));

        CartItem item = new CartItem(
                request.productId(),
                request.productName(),
                request.price(),
                request.quantity(),
                request.imageUrl()
        );

        cart.addItem(item);
        cartRepository.save(cart);
        return CartResponse.from(cart);
    }

    public CartResponse removeItem(String userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
        cart.removeItem(productId);
        cartRepository.save(cart);
        return CartResponse.from(cart);
    }

    public CartResponse updateQuantity(String userId, Long productId,
                                       UpdateQuantityRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
        cart.updateQuantity(productId, request.quantity());
        cartRepository.save(cart);
        return CartResponse.from(cart);
    }

    public void checkout(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));

        CartCheckedOutEvent event = CartCheckedOutEvent.of(userId, cart);
        rabbitTemplate.convertAndSend(exchange, cartCheckedOutKey, event);

        cart.clear();
        cartRepository.save(cart);
    }

    public void clearCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
        cart.clear();
        cartRepository.save(cart);
    }
}