package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Document(collection = "Feedbacks")
public class Feedback {

    @Id
    private String id;

    private String recipientId;      // User who should fill the form
    private String recipientEmail;
    private String recipientPhone;
    private String recipientName;
    private RecipientType recipientType;

    private String bookingId;        // Related booking (optional)
    private String doctorId;         // Related doctor (optional)

    private String formTitle;
    private String formDescription;
    private Map<String, Object> questions;  // Dynamic form questions

    // Response
    private Map<String, Object> responses;
    private Integer rating;          // Overall rating 1-5
    private String comments;

    private FeedbackStatus status;
    private Instant sentAt;
    private Instant respondedAt;

    @CreatedDate
    private Instant createdAt;

    public Feedback() {
        this.status = FeedbackStatus.PENDING;
    }

    public enum RecipientType {
        PATIENT,
        DOCTOR,
        ALL_PATIENTS,
        ALL_DOCTORS
    }

    public enum FeedbackStatus {
        PENDING,      // Form sent, awaiting response
        COMPLETED,    // Response received
        EXPIRED       // No response within time limit
    }
}
