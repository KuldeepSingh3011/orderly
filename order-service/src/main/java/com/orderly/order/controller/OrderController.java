package com.orderly.order.controller;

import com.orderly.common.dto.ApiResponse;
import com.orderly.order.entity.Order;
import com.orderly.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Create a new order from the user's cart.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateOrderRequest request) {

        try {
            Order order = orderService.createOrder(userId, request.getShippingAddress());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Order created successfully", order));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get order by ID.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrder(@PathVariable String orderId) {
        return orderService.getOrder(orderId)
                .map(order -> ResponseEntity.ok(ApiResponse.success(order)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Order not found")));
    }

    /**
     * Get all orders for a user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(@PathVariable String userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * Request body for creating an order.
     */
    public static class CreateOrderRequest {
        private Order.ShippingAddress shippingAddress;

        public Order.ShippingAddress getShippingAddress() {
            return shippingAddress;
        }

        public void setShippingAddress(Order.ShippingAddress shippingAddress) {
            this.shippingAddress = shippingAddress;
        }
    }
}
