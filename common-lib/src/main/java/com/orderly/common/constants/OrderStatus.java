package com.orderly.common.constants;

/**
 * Order lifecycle states.
 * Follows a linear progression with possible failure state.
 */
public enum OrderStatus {

    PENDING("Order placed, awaiting inventory confirmation"),
    CONFIRMED("Inventory reserved, order confirmed"),
    PROCESSING("Order is being prepared"),
    SHIPPED("Order has been shipped"),
    DELIVERED("Order delivered to customer"),
    CANCELLED("Order was cancelled"),
    FAILED("Order failed due to an error");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
