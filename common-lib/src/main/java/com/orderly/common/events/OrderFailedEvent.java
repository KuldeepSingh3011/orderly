package com.orderly.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event published when an order cannot be fulfilled.
 * Reasons: insufficient stock, payment failure, etc.
 * Consumed by: OrderService (to update status), NotificationService (to notify customer)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderFailedEvent {

    private String eventId;
    private String orderId;
    private String userId;
    private String reason;
    private FailureType failureType;
    private Instant timestamp;

    public enum FailureType {
        INSUFFICIENT_STOCK,
        PAYMENT_FAILED,
        VALIDATION_ERROR,
        SYSTEM_ERROR
    }
}
