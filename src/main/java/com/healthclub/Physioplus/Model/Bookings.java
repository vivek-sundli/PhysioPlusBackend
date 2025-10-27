package com.healthclub.Physioplus.Model;

import com.healthclub.Physioplus.Dto.BookingStatus;
import com.healthclub.Physioplus.Dto.Mode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * This class represents a single booking document in the "bookings" collection.
 * The @Document annotation marks this as a MongoDB document.
 */
@Getter
@Setter
@Document(collection = "Bookings")
public class Bookings {

    /**
     * The @Id annotation marks this field as the primary key.
     * MongoDB will automatically generate a unique ObjectId for this if it's null.
     */
    @Id
    private String id;

    private String patientName;
    private String patientId; // ID of the user making the booking
    private String doctorId;  // ID of the doctor being booked
    private LocalDateTime appointmentTime;
    private BookingStatus status;
    private String notes;
    private Enum mode;
    private String mailId;

    // Constructors
    public Bookings() {
    }

    public Bookings(String patientName, String patientId, String doctorId, LocalDateTime appointmentTime, String notes,String mailId) {
        this.patientName = patientName;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentTime = appointmentTime;
        this.notes = notes;
        this.status = BookingStatus.REQUESTED;
        this.mailId = mailId;
        this.mode = Mode.ONLINE;// Default status on creation
    }

}