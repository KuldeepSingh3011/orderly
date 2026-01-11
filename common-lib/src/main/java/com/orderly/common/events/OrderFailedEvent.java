package com.orderly.common.events;

import java.time.Instant;

/**
 * Event published when an order cannot be fulfilled.
 * Reasons: insufficient stock, payment failure, etc.
 * Consumed by: OrderService (to update status), NotificationService (to notify customer)
 */
public class OrderFailedEvent {

    private String eventId;
    private String orderId;
    private String userId;
    private String reason;
    private FailureType failureType;
    private Instant timestamp;

    public OrderFailedEvent() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public FailureType getFailureType() {
        return failureType;
    }

    public void setFailureType(FailureType failureType) {
        this.failureType = failureType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public enum FailureType {
        INSUFFICIENT_STOCK,
        PAYMENT_FAILED,
        VALIDATION_ERROR,
        SYSTEM_ERROR
    }
}
