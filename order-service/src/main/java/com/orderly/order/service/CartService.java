package com.orderly.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderly.common.dto.CartItemDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Cart service using Redis for storage.
 * Cart expires after 24 hours of inactivity.
 */
@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);
    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_TTL_HOURS = 24;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public CartService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Add item to cart. If item exists, quantity is updated.
     */
    public void addToCart(String userId, CartItemDto item) {
        String cartKey = getCartKey(userId);
        log.debug("Adding item {} to cart for user {}", item.getProductId(), userId);

        try {
            String json = objectMapper.writeValueAsString(item);
            redisTemplate.opsForHash().put(cartKey, item.getProductId(), json);
            redisTemplate.expire(cartKey, CART_TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize cart item: {}", e.getMessage());
            throw new RuntimeException("Failed to add item to cart", e);
        }
    }

    /**
     * Update item quantity in cart.
     */
    public void updateQuantity(String userId, String productId, int quantity) {
        String cartKey = getCartKey(userId);

        CartItemDto item = getCartItem(userId, productId);
        if (item != null) {
            item.setQuantity(quantity);
            try {
                String json = objectMapper.writeValueAsString(item);
                redisTemplate.opsForHash().put(cartKey, productId, json);
                redisTemplate.expire(cartKey, CART_TTL_HOURS, TimeUnit.HOURS);
                log.debug("Updated quantity for product {} to {} for user {}", productId, quantity, userId);
            } catch (JsonProcessingException e) {
                log.error("Failed to update cart item: {}", e.getMessage());
            }
        }
    }

    /**
     * Remove item from cart.
     */
    public void removeFromCart(String userId, String productId) {
        String cartKey = getCartKey(userId);
        redisTemplate.opsForHash().delete(cartKey, productId);
        log.debug("Removed product {} from cart for user {}", productId, userId);
    }

    /**
     * Get all items in cart.
     */
    public List<CartItemDto> getCart(String userId) {
        String cartKey = getCartKey(userId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey);

        List<CartItemDto> items = new ArrayList<>();
        for (Object value : entries.values()) {
            try {
                CartItemDto item = objectMapper.readValue(value.toString(), CartItemDto.class);
                items.add(item);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize cart item: {}", e.getMessage());
            }
        }
        return items;
    }

    /**
     * Get specific item from cart.
     */
    public CartItemDto getCartItem(String userId, String productId) {
        String cartKey = getCartKey(userId);
        Object value = redisTemplate.opsForHash().get(cartKey, productId);
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value.toString(), CartItemDto.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cart item: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Clear entire cart.
     */
    public void clearCart(String userId) {
        String cartKey = getCartKey(userId);
        redisTemplate.delete(cartKey);
        log.debug("Cleared cart for user {}", userId);
    }

    /**
     * Calculate total cart value.
     */
    public BigDecimal getCartTotal(String userId) {
        return getCart(userId).stream()
                .map(CartItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Check if cart is empty.
     */
    public boolean isCartEmpty(String userId) {
        String cartKey = getCartKey(userId);
        Long size = redisTemplate.opsForHash().size(cartKey);
        return size == null || size == 0;
    }

    private String getCartKey(String userId) {
        return CART_KEY_PREFIX + userId;
    }
}
