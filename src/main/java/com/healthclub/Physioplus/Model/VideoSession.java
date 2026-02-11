package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "VideoSessions")
public class VideoSession {

    @Id
    private String id;

    @Indexed(unique = true)
    private String roomId;

    private String roomName;

    @Indexed
    private String bookingId;

    private String doctorId;

    private String patientId;

    private SessionStatus status;

    private LocalDateTime scheduledTime;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private String templateId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public VideoSession() {
        this.status = SessionStatus.SCHEDULED;
    }

    public VideoSession(String bookingId, String doctorId, String patientId, LocalDateTime scheduledTime) {
        this();
        this.bookingId = bookingId;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.scheduledTime = scheduledTime;
        this.roomName = "room_" + bookingId;
    }

    public enum SessionStatus {
        SCHEDULED,   // Room created, waiting for scheduled time
        ACTIVE,      // Session is ongoing
        COMPLETED,   // Session ended normally
        CANCELLED    // Session was cancelled
    }

    public boolean canJoin() {
        return status == SessionStatus.SCHEDULED || status == SessionStatus.ACTIVE;
    }
}
