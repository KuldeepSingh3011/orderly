package com.orderly.order.service;

import com.orderly.common.constants.OrderStatus;
import com.orderly.common.dto.CartItemDto;
import com.orderly.common.events.OrderPlacedEvent;
import com.orderly.order.entity.Order;
import com.orderly.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Order service handling order creation and management.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08"); // 8% tax

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, CartService cartService, OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Create order from cart.
     * 1. Fetch cart items
     * 2. Calculate totals
     * 3. Save order with PENDING status
     * 4. Publish OrderPlacedEvent
     * 5. Clear cart
     */
    @Transactional
    public Order createOrder(String userId, Order.ShippingAddress shippingAddress) {
        log.info("Creating order for user: {}", userId);

        // Get cart items
        List<CartItemDto> cartItems = cartService.getCart(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cannot create order: cart is empty");
        }

        // Convert cart items to order items
        List<Order.OrderItem> orderItems = cartItems.stream()
                .map(this::toOrderItem)
                .collect(Collectors.toList());

        // Calculate totals
        BigDecimal subtotal = cartItems.stream()
                .map(CartItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(TAX_RATE);
        BigDecimal shippingCost = calculateShipping(subtotal);
        BigDecimal total = subtotal.add(tax).add(shippingCost);

        // Create order
        Order order = Order.builder()
                .userId(userId)
                .items(orderItems)
                .subtotal(subtotal)
                .tax(tax)
                .shippingCost(shippingCost)
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .shippingAddress(shippingAddress)
                .build();

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());

        // Publish event
        OrderPlacedEvent event = buildOrderPlacedEvent(savedOrder);
        eventPublisher.publishOrderPlaced(event);

        // Clear cart
        cartService.clearCart(userId);

        return savedOrder;
    }

    /**
     * Get order by ID.
     */
    public Optional<Order> getOrder(String orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Get all orders for a user.
     */
    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * Update order status.
     */
    public Order updateStatus(String orderId, OrderStatus newStatus, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        order.setStatus(newStatus);
        if (reason != null) {
            order.setFailureReason(reason);
        }

        return orderRepository.save(order);
    }

    private Order.OrderItem toOrderItem(CartItemDto cartItem) {
        return Order.OrderItem.builder()
                .productId(cartItem.getProductId())
                .productName(cartItem.getProductName())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .build();
    }

    private BigDecimal calculateShipping(BigDecimal subtotal) {
        // Free shipping over $50
        if (subtotal.compareTo(new BigDecimal("50")) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("5.99");
    }

    private OrderPlacedEvent buildOrderPlacedEvent(Order order) {
        List<OrderPlacedEvent.OrderItemPayload> items = order.getItems().stream()
                .map(item -> OrderPlacedEvent.OrderItemPayload.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderPlacedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(order.getId())
                .userId(order.getUserId())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .timestamp(Instant.now())
                .build();
    }
}
