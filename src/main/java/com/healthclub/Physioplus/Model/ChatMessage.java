package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Document(collection = "ChatMessages")
@CompoundIndex(name = "conversation_time_idx", def = "{'conversationId': 1, 'timestamp': -1}")
public class ChatMessage {

    @Id
    private String id;

    private String conversationId;
    private String senderId;
    private String senderType;       // PATIENT, DOCTOR, ADMIN, SYSTEM
    private String recipientId;

    // Message Content
    private MessageType type;
    private String content;
    private List<Attachment> attachments;

    // Status
    private MessageStatus status;
    private Instant sentAt;
    private Instant deliveredAt;
    private Instant readAt;

    // Reply/Thread
    private String replyToMessageId;
    private boolean edited;
    private Instant editedAt;

    // Metadata
    private String bookingId;        // Related booking if any
    private boolean systemMessage;

    private Instant timestamp;

    public ChatMessage() {
        this.timestamp = Instant.now();
        this.sentAt = Instant.now();
        this.status = MessageStatus.SENT;
        this.edited = false;
        this.systemMessage = false;
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        VOICE_NOTE,
        VIDEO,
        PRESCRIPTION,
        EXERCISE_PLAN,
        APPOINTMENT_CARD,
        PAYMENT_REQUEST,
        SYSTEM_NOTIFICATION
    }

    public enum MessageStatus {
        SENDING,
        SENT,
        DELIVERED,
        READ,
        FAILED
    }

    @Getter
    @Setter
    public static class Attachment {
        private String url;
        private String fileName;
        private String fileType;
        private Long fileSize;
        private String thumbnailUrl;
    }
}
