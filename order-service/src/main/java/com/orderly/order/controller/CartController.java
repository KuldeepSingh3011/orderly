package com.orderly.order.controller;

import com.orderly.common.dto.ApiResponse;
import com.orderly.common.dto.CartItemDto;
import com.orderly.order.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<ApiResponse<Void>> addToCart(
            @PathVariable String userId,
            @Valid @RequestBody CartItemDto item) {

        cartService.addToCart(userId, item);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", null));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCart(@PathVariable String userId) {
        List<CartItemDto> items = cartService.getCart(userId);
        BigDecimal total = cartService.getCartTotal(userId);

        Map<String, Object> cartData = Map.of(
                "items", items,
                "itemCount", items.size(),
                "total", total
        );

        return ResponseEntity.ok(ApiResponse.success(cartData));
    }

    @PutMapping("/{userId}/items/{productId}")
    public ResponseEntity<ApiResponse<Void>> updateQuantity(
            @PathVariable String userId,
            @PathVariable String productId,
            @RequestParam int quantity) {

        if (quantity <= 0) {
            cartService.removeFromCart(userId, productId);
        } else {
            cartService.updateQuantity(userId, productId, quantity);
        }
        return ResponseEntity.ok(ApiResponse.success("Cart updated", null));
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @PathVariable String userId,
            @PathVariable String productId) {

        cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", null));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
