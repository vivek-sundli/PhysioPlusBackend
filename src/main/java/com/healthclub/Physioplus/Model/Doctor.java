package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Document(collection = "Doctors")
public class Doctor {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;  // Reference to User collection

    @Indexed(unique = true, sparse = true)
    private String email;

    private String phone;
    private String name;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private String bio;
    private String profileImageUrl;

    // Onboarding Documents
    private String idProofUrl;           // Government ID
    private String medicalLicenseUrl;    // Medical license document
    private String degreeCertificateUrl; // Degree certificate
    private List<String> additionalDocuments;

    // Verification Status
    private OnboardingStatus onboardingStatus;
    private String rejectionReason;
    private String verifiedBy;  // Admin who verified
    private Instant verifiedAt;

    // Practice Details
    private Double consultationFee;
    private Integer slotDurationMinutes;
    private List<String> availableDays;
    private String clinicAddress;

    // Stats
    private Integer totalConsultations;
    private Double rating;
    private Integer totalRatings;

    private boolean active;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Doctor() {
        this.onboardingStatus = OnboardingStatus.PENDING;
        this.active = false;
        this.totalConsultations = 0;
        this.rating = 0.0;
        this.totalRatings = 0;
    }

    public enum OnboardingStatus {
        PENDING,           // Initial application submitted
        DOCUMENTS_UPLOADED, // All documents uploaded
        UNDER_REVIEW,      // Admin reviewing
        APPROVED,          // Verified and approved
        REJECTED,          // Application rejected
        SUSPENDED          // Account suspended
    }
}
