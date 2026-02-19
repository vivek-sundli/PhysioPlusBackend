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
@Document(collection = "Reviews")
@CompoundIndex(name = "doctor_patient_idx", def = "{'doctorId': 1, 'patientId': 1}")
public class Review {

    @Id
    private String id;

    private String doctorId;
    private String patientId;
    private String patientName;  // Display name
    private String bookingId;

    // Ratings (1-5)
    private Integer overallRating;
    private Integer punctualityRating;
    private Integer communicationRating;
    private Integer treatmentRating;
    private Integer clinicRating;

    // Review Content
    private String title;
    private String content;
    private List<String> tags;           // RECOMMENDED, GENTLE, EXPERT, etc.
    private List<String> photoUrls;

    // Doctor Response
    private String doctorResponse;
    private Instant doctorResponseAt;

    // Verification
    private boolean verifiedVisit;
    private boolean anonymous;

    // Moderation
    private ReviewStatus status;
    private String moderationNotes;
    private Instant moderatedAt;

    // Helpfulness
    private Integer helpfulCount;
    private List<String> helpfulByUserIds;

    private Instant createdAt;
    private Instant updatedAt;

    public Review() {
        this.status = ReviewStatus.PENDING;
        this.verifiedVisit = false;
        this.anonymous = false;
        this.helpfulCount = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public enum ReviewStatus {
        PENDING,
        APPROVED,
        REJECTED,
        FLAGGED
    }
}
