package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Document(collection = "Conversations")
public class Conversation {

    @Id
    private String id;

    private List<String> participantIds;
    private String participant1Id;
    private String participant1Type;  // PATIENT, DOCTOR
    private String participant2Id;
    private String participant2Type;

    // Related entities
    private String bookingId;
    private String doctorId;
    private String patientId;

    // Last message preview
    private String lastMessageId;
    private String lastMessagePreview;
    private Instant lastMessageAt;
    private String lastMessageSenderId;

    // Unread counts
    private Integer unreadCount1;  // For participant1
    private Integer unreadCount2;  // For participant2

    // Status
    private boolean active;
    private boolean archived;
    private boolean muted1;
    private boolean muted2;

    private Instant createdAt;
    private Instant updatedAt;

    public Conversation() {
        this.active = true;
        this.archived = false;
        this.muted1 = false;
        this.muted2 = false;
        this.unreadCount1 = 0;
        this.unreadCount2 = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
