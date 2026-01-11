package com.orderly.inventory.service;

import com.orderly.common.constants.KafkaTopics;
import com.orderly.common.events.OrderConfirmedEvent;
import com.orderly.common.events.OrderFailedEvent;
import com.orderly.common.events.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Consumes order events from Kafka and manages inventory.
 */
@Service
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final InventoryService inventoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Simple in-memory idempotency check (use Redis in production)
    private final Set<String> processedEvents = new HashSet<>();

    public OrderEventConsumer(InventoryService inventoryService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryService = inventoryService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_PLACED, groupId = "inventory-service-group")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        String eventId = event.getEventId();

        // Idempotency check
        if (processedEvents.contains(eventId)) {
            log.info("Event {} already processed, skipping", eventId);
            return;
        }

        log.info("Processing OrderPlacedEvent for order: {}", event.getOrderId());

        boolean allReserved = true;
        StringBuilder failureReason = new StringBuilder();

        // Try to reserve stock for all items
        for (OrderPlacedEvent.OrderItemPayload item : event.getItems()) {
            boolean reserved = inventoryService.reserveStock(item.getProductId(), item.getQuantity());
            if (!reserved) {
                allReserved = false;
                failureReason.append("Insufficient stock for product: ")
                        .append(item.getProductName())
                        .append(". ");
            }
        }

        if (allReserved) {
            // All items reserved successfully
            publishOrderConfirmed(event);
            log.info("Order {} confirmed - all items reserved", event.getOrderId());
        } else {
            // Release any reserved items and publish failure
            for (OrderPlacedEvent.OrderItemPayload item : event.getItems()) {
                inventoryService.releaseStock(item.getProductId(), item.getQuantity());
            }
            publishOrderFailed(event, failureReason.toString());
            log.warn("Order {} failed - {}", event.getOrderId(), failureReason);
        }

        // Mark as processed
        processedEvents.add(eventId);
    }

    private void publishOrderConfirmed(OrderPlacedEvent sourceEvent) {
        OrderConfirmedEvent event = new OrderConfirmedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setOrderId(sourceEvent.getOrderId());
        event.setUserId(sourceEvent.getUserId());
        event.setTimestamp(Instant.now());

        kafkaTemplate.send(KafkaTopics.ORDER_CONFIRMED, sourceEvent.getOrderId(), event);
    }

    private void publishOrderFailed(OrderPlacedEvent sourceEvent, String reason) {
        OrderFailedEvent event = new OrderFailedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setOrderId(sourceEvent.getOrderId());
        event.setUserId(sourceEvent.getUserId());
        event.setReason(reason);
        event.setFailureType(OrderFailedEvent.FailureType.INSUFFICIENT_STOCK);
        event.setTimestamp(Instant.now());

        kafkaTemplate.send(KafkaTopics.ORDER_FAILED, sourceEvent.getOrderId(), event);
    }
}
