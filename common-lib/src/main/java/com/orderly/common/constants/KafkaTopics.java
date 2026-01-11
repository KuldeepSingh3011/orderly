package com.orderly.common.constants;

/**
 * Centralized Kafka topic names.
 * Using constants prevents typos and makes refactoring easier.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Prevent instantiation
    }

    // Order lifecycle events
    public static final String ORDER_PLACED = "order.placed";
    public static final String ORDER_CONFIRMED = "order.confirmed";
    public static final String ORDER_FAILED = "order.failed";
    public static final String ORDER_SHIPPED = "order.shipped";
    public static final String ORDER_DELIVERED = "order.delivered";

    // Inventory events
    public static final String INVENTORY_RESERVED = "inventory.reserved";
    public static final String INVENTORY_RELEASED = "inventory.released";
    public static final String INVENTORY_LOW_STOCK = "inventory.low-stock";
}
