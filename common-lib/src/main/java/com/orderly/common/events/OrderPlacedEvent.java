package com.orderly.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Event published when a new order is placed.
 * Consumed by: InventoryService, NotificationService
 */
public class OrderPlacedEvent {

    private String eventId;
    private String orderId;
    private String userId;
    private List<OrderItemPayload> items;
    private BigDecimal totalAmount;
    private Instant timestamp;

    public OrderPlacedEvent() {
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

    public List<OrderItemPayload> getItems() {
        return items;
    }

    public void setItems(List<OrderItemPayload> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final OrderPlacedEvent event = new OrderPlacedEvent();

        public Builder eventId(String eventId) {
            event.eventId = eventId;
            return this;
        }

        public Builder orderId(String orderId) {
            event.orderId = orderId;
            return this;
        }

        public Builder userId(String userId) {
            event.userId = userId;
            return this;
        }

        public Builder items(List<OrderItemPayload> items) {
            event.items = items;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            event.totalAmount = totalAmount;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            event.timestamp = timestamp;
            return this;
        }

        public OrderPlacedEvent build() {
            return event;
        }
    }

    public static class OrderItemPayload {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal price;

        public OrderItemPayload() {
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final OrderItemPayload payload = new OrderItemPayload();

            public Builder productId(String productId) {
                payload.productId = productId;
                return this;
            }

            public Builder productName(String productName) {
                payload.productName = productName;
                return this;
            }

            public Builder quantity(int quantity) {
                payload.quantity = quantity;
                return this;
            }

            public Builder price(BigDecimal price) {
                payload.price = price;
                return this;
            }

            public OrderItemPayload build() {
                return payload;
            }
        }
    }
}
