package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "NotificationLogs")
public class NotificationLog {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String bookingId;

    private String recipientPhone;

    private String recipientName;

    private NotificationType type;

    private NotificationChannel channel;

    private String templateName;

    private String messageId; // WhatsApp message ID

    private DeliveryStatus status;

    private String statusMessage;

    private String content;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant deliveredAt;

    private Instant readAt;

    public NotificationLog() {
        this.status = DeliveryStatus.PENDING;
        this.channel = NotificationChannel.WHATSAPP;
    }

    @Indexed
    private String recipientId;

    private String title;

    private String message;

    public enum NotificationType {
        BOOKING_CONFIRMATION,
        APPOINTMENT_REMINDER,
        PAYMENT_CONFIRMATION,
        PRESCRIPTION,
        CUSTOM,
        SYSTEM_ALERT
    }

    public enum NotificationChannel {
        WHATSAPP,
        SMS,
        EMAIL
    }

    public enum DeliveryStatus {
        PENDING, // Initial state
        SENT, // Message sent to provider
        DELIVERED, // Message delivered to recipient
        READ, // Message read by recipient
        FAILED // Message failed to send
    }
}
