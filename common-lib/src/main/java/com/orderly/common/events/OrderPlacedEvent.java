package com.orderly.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Event published when a new order is placed.
 * Consumed by: InventoryService, NotificationService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {

    private String eventId;
    private String orderId;
    private String userId;
    private List<OrderItemPayload> items;
    private BigDecimal totalAmount;
    private Instant timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemPayload {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal price;
    }
}
