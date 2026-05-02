package com.eticaret.cart.repository;

import com.eticaret.cart.domain.Cart;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.util.Optional;

@Repository
public class RedisCartRepository implements CartRepository {

    private static final String KEY_PREFIX = "cart:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.cart.ttl-minutes:1440}")
    private long ttlMinutes;

    public RedisCartRepository(RedisTemplate<String, String> redisTemplate,
                               ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Cart save(Cart cart) {
        try {
            String key = KEY_PREFIX + cart.getUserId();
            String value = objectMapper.writeValueAsString(cart);
            redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(ttlMinutes));
            return cart;
        } catch (Exception e) {
            throw new RuntimeException("Sepet kaydedilemedi: " + e.getMessage());
        }
    }

    @Override
    public Optional<Cart> findByUserId(String userId) {
        try {
            String key = KEY_PREFIX + userId;
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(value, Cart.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteByUserId(String userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}