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
    private double amount;          // The amount paid (in INR)
    private String currency;        // Currency code (default: INR)

    // --- Added Fields for better tracking ---

    /**
     * The ID of the booking this payment is associated with.
     */
    private String bookingId;

    /**
     * A unique transaction ID from the payment gateway.
     */
    private String transactionId;

    /**
     * The timestamp of when the payment was processed.
     */
    private LocalDateTime paymentTime;

    // --- Razorpay Integration Fields ---

    /**
     * Razorpay order ID (order_XXXXX)
     */
    private String razorpayOrderId;

    /**
     * Razorpay payment ID (pay_XXXXX) - populated after successful payment
     */
    private String razorpayPaymentId;

    /**
     * Razorpay signature for payment verification
     */
    private String razorpaySignature;

    /**
     * Payment status: PENDING, CREATED, COMPLETED, FAILED, REFUNDED
     */
    private PaymentStatus status;

    /**
     * Receipt number for the payment
     */
    private String receipt;

    /**
     * Any notes or description for the payment
     */
    private String notes;

    public Payment() {
        this.status = PaymentStatus.PENDING;
        this.currency = "INR";
    }

    public enum PaymentStatus {
        PENDING,    // Initial state
        CREATED,    // Razorpay order created
        COMPLETED,  // Payment successful
        FAILED,     // Payment failed
        REFUNDED    // Payment refunded
    }
}

