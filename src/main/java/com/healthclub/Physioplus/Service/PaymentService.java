package com.healthclub.Physioplus.Service;


import com.healthclub.Physioplus.Model.Payment;
import com.healthclub.Physioplus.Repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for handling payment-related business logic.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Creates a new payment record, initially set to PENDING.
     * @param payment The payment object from the request.
     * @return The saved payment object.
     */
    public Payment createPayment(Payment payment) {
 // Set initial status
        payment.setPaymentTime(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    /**
     * Updates the status of an existing payment (e.g., to COMPLETED or FAILED).
     * @param paymentId The ID of the payment to update.
     * @return The updated payment object, or null if not found.
     */
    public Optional<Payment> updatePaymentStatus(String paymentId,String transactionId) {
        Optional<Payment> existingPayment = paymentRepository.findById(paymentId);
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            payment.setTransactionId(transactionId); // Add the transaction ID on confirmation
            payment.setPaymentTime(LocalDateTime.now()); // Update time to confirmation time
            return Optional.of(paymentRepository.save(payment));
        }
        return Optional.empty(); // Not found
    }

    /**
     * Retrieves a payment by its ID.
     * @param paymentId The ID of the payment.
     * @return An Optional containing the payment if found.
     */
    public Optional<Payment> getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId);
    }

    /**
     * Retrieves all payments for a specific user.
     * @param userId The ID of the user.
     * @return A list of payments.
     */
    public List<Payment> getPaymentsByUserId(String userId) {
        return paymentRepository.findByUserId(userId);
    }

    /**
     * Retrieves all payments for a specific doctor.
     * @param doctorId The ID of the doctor.
     * @return A list of payments.
     */
    public List<Payment> getPaymentsByDoctorId(String doctorId) {
        return paymentRepository.findByDoctorId(doctorId);
    }
}
