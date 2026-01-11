package com.orderly.common.events;

import java.time.Instant;

/**
 * Event published when inventory is successfully reserved for an order.
 * Consumed by: OrderService (to update status), NotificationService (to send confirmation)
 */
public class OrderConfirmedEvent {

    private String eventId;
    private String orderId;
    private String userId;
    private Instant timestamp;

    public OrderConfirmedEvent() {
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
