package com.orderly.order.service;

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

    private final RedisTemplate<String, Object> redisTemplate;

    public CartService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Add item to cart. If item exists, quantity is updated.
     */
    public void addToCart(String userId, CartItemDto item) {
        String cartKey = getCartKey(userId);
        log.debug("Adding item {} to cart for user {}", item.getProductId(), userId);

        redisTemplate.opsForHash().put(cartKey, item.getProductId(), item);
        redisTemplate.expire(cartKey, CART_TTL_HOURS, TimeUnit.HOURS);
    }

    /**
     * Update item quantity in cart.
     */
    public void updateQuantity(String userId, String productId, int quantity) {
        String cartKey = getCartKey(userId);

        CartItemDto item = getCartItem(userId, productId);
        if (item != null) {
            item.setQuantity(quantity);
            redisTemplate.opsForHash().put(cartKey, productId, item);
            redisTemplate.expire(cartKey, CART_TTL_HOURS, TimeUnit.HOURS);
            log.debug("Updated quantity for product {} to {} for user {}", productId, quantity, userId);
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
            if (value instanceof CartItemDto) {
                items.add((CartItemDto) value);
            }
        }
        return items;
    }

    /**
     * Get specific item from cart.
     */
    public CartItemDto getCartItem(String userId, String productId) {
        String cartKey = getCartKey(userId);
        Object item = redisTemplate.opsForHash().get(cartKey, productId);
        return item instanceof CartItemDto ? (CartItemDto) item : null;
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
