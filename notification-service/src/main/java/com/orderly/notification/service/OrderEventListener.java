package com.orderly.notification.service;

import com.orderly.common.constants.KafkaTopics;
import com.orderly.common.events.OrderConfirmedEvent;
import com.orderly.common.events.OrderFailedEvent;
import com.orderly.notification.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Listens to order events and sends appropriate notifications.
 */
@Service
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final NotificationService notificationService;

    public OrderEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CONFIRMED, containerFactory = "confirmedListenerFactory")
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent for order: {}", event.getOrderId());

        String subject = "Order Confirmed - #" + event.getOrderId();
        String message = String.format(
                "Great news! Your order #%s has been confirmed. " +
                "We're preparing it for shipment. " +
                "Thank you for shopping with Orderly!",
                event.getOrderId()
        );

        notificationService.sendNotification(
                event.getUserId(),
                event.getOrderId(),
                Notification.NotificationType.ORDER_CONFIRMED,
                subject,
                message
        );
    }

    @KafkaListener(topics = KafkaTopics.ORDER_FAILED, containerFactory = "failedListenerFactory")
    public void handleOrderFailed(OrderFailedEvent event) {
        log.info("Received OrderFailedEvent for order: {}", event.getOrderId());

        String subject = "Order Update - #" + event.getOrderId();
        String message = String.format(
                "We're sorry, but there was an issue with your order #%s. " +
                "Reason: %s. " +
                "Please try again or contact support if you need assistance.",
                event.getOrderId(),
                event.getReason()
        );

        notificationService.sendNotification(
                event.getUserId(),
                event.getOrderId(),
                Notification.NotificationType.ORDER_FAILED,
                subject,
                message
        );
    }
}
