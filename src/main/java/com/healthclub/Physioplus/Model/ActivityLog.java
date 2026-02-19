package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "ActivityLogs")
public class ActivityLog {

    @Id
    private String id;

    private String actionType; // e.g., REGISTRATION, BOOKING, PAYMENT, SECURITY
    private String description;
    private String actorId; // ID of the user/admin who performed the action
    private String targetId; // ID of the affected entity (user, booking, etc.)

    @CreatedDate
    private Instant createdAt;

    public ActivityLog() {
    }

    public ActivityLog(String actionType, String description, String actorId, String targetId) {
        this.actionType = actionType;
        this.description = description;
        this.actorId = actorId;
        this.targetId = targetId;
        this.createdAt = Instant.now();
    }
}
