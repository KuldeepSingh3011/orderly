package com.orderly.order.service;

import com.orderly.common.constants.KafkaTopics;
import com.orderly.common.constants.OrderStatus;
import com.orderly.common.events.OrderConfirmedEvent;
import com.orderly.common.events.OrderFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Listens for order status update events from Kafka.
 */
@Service
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final OrderService orderService;

    public OrderEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CONFIRMED, containerFactory = "confirmedListenerFactory")
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent for order: {}", event.getOrderId());

        try {
            orderService.updateStatus(event.getOrderId(), OrderStatus.CONFIRMED, null);
            log.info("Order {} status updated to CONFIRMED", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to update order {} status: {}", event.getOrderId(), e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaTopics.ORDER_FAILED, containerFactory = "failedListenerFactory")
    public void handleOrderFailed(OrderFailedEvent event) {
        log.info("Received OrderFailedEvent for order: {}", event.getOrderId());

        try {
            orderService.updateStatus(event.getOrderId(), OrderStatus.FAILED, event.getReason());
            log.info("Order {} status updated to FAILED: {}", event.getOrderId(), event.getReason());
        } catch (Exception e) {
            log.error("Failed to update order {} status: {}", event.getOrderId(), e.getMessage());
        }
    }
}
