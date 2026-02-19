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
@Document(collection = "FamilyProfiles")
public class FamilyProfile {

    @Id
    private String id;

    @Indexed
    private String primaryUserId;  // Main account holder

    private List<FamilyMember> members;
    private String familyName;

    // Access Control
    private boolean sharedMedicalHistory;
    private boolean sharedPaymentMethod;

    private Instant createdAt;
    private Instant updatedAt;

    public FamilyProfile() {
        this.members = new ArrayList<>();
        this.sharedMedicalHistory = false;
        this.sharedPaymentMethod = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @Getter
    @Setter
    public static class FamilyMember {
        private String id;
        private String name;
        private String relation;      // SPOUSE, CHILD, PARENT, SIBLING, OTHER
        private LocalDate dateOfBirth;
        private String gender;
        private String phone;
        private String email;
        private String profileImageUrl;

        // Health summary
        private String bloodGroup;
        private List<String> allergies;
        private List<String> conditions;

        // Access
        private boolean canBookAppointments;
        private boolean canViewHistory;
        private boolean canMakePayments;

        private String healthProfileId;  // Link to HealthProfile
        private Instant addedAt;
    }
}
