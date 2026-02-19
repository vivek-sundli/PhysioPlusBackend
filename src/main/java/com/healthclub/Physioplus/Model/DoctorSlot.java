package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Document(collection = "DoctorSlots")
@CompoundIndex(name = "doctor_date_idx", def = "{'doctorId': 1, 'date': 1}")
public class DoctorSlot {

    @Id
    private String id;

    private String doctorId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;

    private SlotStatus status;
    private String bookingId;  // If booked
    private String patientId;  // If booked

    // For recurring slots
    private boolean recurring;
    private String recurringPatternId;

    private Instant createdAt;
    private Instant updatedAt;

    public DoctorSlot() {
        this.status = SlotStatus.AVAILABLE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public enum SlotStatus {
        AVAILABLE,
        BOOKED,
        BLOCKED,      // Doctor marked unavailable
        COMPLETED,
        CANCELLED,
        NO_SHOW
    }
}
