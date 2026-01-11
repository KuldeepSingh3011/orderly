package com.orderly.order.service;

import com.orderly.common.constants.KafkaTopics;
import com.orderly.common.events.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Publishes order events to Kafka.
 */
@Service
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish order placed event.
     * Uses orderId as the key for partition assignment (ensures order of events for same order).
     */
    public void publishOrderPlaced(OrderPlacedEvent event) {
        String topic = KafkaTopics.ORDER_PLACED;
        String key = event.getOrderId();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish OrderPlacedEvent for order {}: {}", key, ex.getMessage());
            } else {
                log.info("Published OrderPlacedEvent for order {} to partition {}",
                        key, result.getRecordMetadata().partition());
            }
        });
    }
}
