package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "HealthProfiles")
public class HealthProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String patientId;

    // Basic Health Info
    private Double height;         // cm
    private Double weight;         // kg
    private String bloodGroup;
    private LocalDate dateOfBirth;
    private String gender;

    // Medical History
    private List<String> allergies;
    private List<String> chronicConditions;
    private List<Medication> currentMedications;
    private List<Surgery> pastSurgeries;
    private List<String> familyHistory;

    // Physiotherapy Specific
    private String primaryComplaint;
    private String injuryType;
    private LocalDate injuryDate;
    private String affectedArea;
    private Integer painLevel;  // 1-10 scale

    // Lifestyle
    private String occupation;
    private String activityLevel;  // SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE
    private boolean smoker;
    private boolean alcohol;
    private Integer sleepHours;

    // Emergency Contact
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;

    // Insurance
    private String insuranceProvider;
    private String insurancePolicyNumber;
    private LocalDate insuranceExpiryDate;

    // Progress Photos
    private List<ProgressPhoto> progressPhotos;

    private Instant createdAt;
    private Instant updatedAt;

    public HealthProfile() {
        this.allergies = new ArrayList<>();
        this.chronicConditions = new ArrayList<>();
        this.currentMedications = new ArrayList<>();
        this.pastSurgeries = new ArrayList<>();
        this.familyHistory = new ArrayList<>();
        this.progressPhotos = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @Getter
    @Setter
    public static class Medication {
        private String name;
        private String dosage;
        private String frequency;
        private LocalDate startDate;
        private LocalDate endDate;
        private String prescribedBy;
    }

    @Getter
    @Setter
    public static class Surgery {
        private String name;
        private LocalDate date;
        private String hospital;
        private String surgeon;
        private String notes;
    }

    @Getter
    @Setter
    public static class ProgressPhoto {
        private String url;
        private String description;
        private LocalDate date;
        private String bodyPart;
    }
}
