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
@Document(collection = "Prescriptions")
public class Prescription {

    @Id
    private String id;

    @Indexed
    private String bookingId;

    @Indexed
    private String doctorId;

    @Indexed
    private String patientId;

    private String diagnosis;
    private String notes;
    private List<Medicine> medicines;
    private List<String> labTestsRecommended;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Getter
    @Setter
    public static class Medicine {
        private String name;
        private String dosage;      // e.g., "500mg"
        private String frequency;   // e.g., "1-0-1" (Morning-Afternoon-Night)
        private String duration;    // e.g., "5 days"
        private String instructions; // e.g., "After food"
    }
}
