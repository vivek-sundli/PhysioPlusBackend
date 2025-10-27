package com.healthclub.Physioplus.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Represents a payment transaction in the "payments" collection.
 */
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserContactNumber() {
        return userContactNumber;
    }

    public void setUserContactNumber(String userContactNumber) {
        this.userContactNumber = userContactNumber;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }
}

