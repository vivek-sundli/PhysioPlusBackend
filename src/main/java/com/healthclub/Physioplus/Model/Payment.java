package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Represents a payment transaction in the "payments" collection.
 */
@Setter
@Getter
@Document(collection = "Payments")
public class Payment {

    @Id
    private String id;

    // --- User Requested Fields ---
    private String doctorId;        // ID of the doctor receiving payment
    private String userId;          // ID of the user making the payment
    private String userContactNumber; // Contact number of the user
    private String userEmail;         // Email of the user
    private double amount;          // The amount paid

    // --- Added Fields for better tracking ---

    /**
     * The ID of the booking this payment is associated with.
     * This is crucial for linking payments to appointments.
     */
    private String bookingId;

    /**
     * A unique transaction ID from the payment gateway (e.g., Stripe, Razorpay).
     */
    private String transactionId;

    /**
     * The timestamp of when the payment was processed.
     */
    private LocalDateTime paymentTime;

    // --- Getters and Setters ---

}

