package com.healthclub.Physioplus.Model;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Model for a Patient in the Patient DB.
 * Stores comprehensive patient information.
 */
@Setter
@Getter
@Document(collection = "Patient")
public class Patient {

    @Id
    private String id;

    @Indexed(unique = true)
    private String patientId; // This can be your internal, human-readable ID

    private String firstName;
    private String lastName;

    private String amountPaid;
    private boolean pendingAmount;

    @Indexed(unique = true)
    private String email;
    private String contactNumber;
    private LocalDate dateOfBirth;
    private String address;

    private List<String> medicalHistory; // List of conditions or notes
    private List<String> currentMedications;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // Getters and Setters

}

