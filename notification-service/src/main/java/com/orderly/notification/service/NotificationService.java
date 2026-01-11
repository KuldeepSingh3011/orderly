package com.orderly.notification.service;

import com.orderly.notification.entity.Notification;
import com.orderly.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service for sending and managing notifications.
 * In production, this would integrate with email/SMS providers.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Send notification (simulated - just logs and saves to DB).
     */
    public Notification sendNotification(String userId, String orderId,
                                         Notification.NotificationType type,
                                         String subject, String message) {
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setOrderId(orderId);
        notification.setType(type);
        notification.setChannel("EMAIL");
        notification.setSubject(subject);
        notification.setMessage(message);
        notification.setStatus(Notification.NotificationStatus.PENDING);

        // Simulate sending email
        log.info("========================================");
        log.info("SENDING EMAIL NOTIFICATION");
        log.info("To: User {}", userId);
        log.info("Subject: {}", subject);
        log.info("Message: {}", message);
        log.info("========================================");

        // Mark as sent
        notification.setStatus(Notification.NotificationStatus.SENT);
        notification.setSentAt(Instant.now());

        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    public List<Notification> getOrderNotifications(String orderId) {
        return notificationRepository.findByOrderId(orderId);
    }
}
