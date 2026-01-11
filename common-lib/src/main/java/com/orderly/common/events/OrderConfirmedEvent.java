package com.orderly.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event published when inventory is successfully reserved for an order.
 * Consumed by: OrderService (to update status), NotificationService (to send confirmation)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {

    private String eventId;
    private String orderId;
    private String userId;
    private Instant timestamp;
}
