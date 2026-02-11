package com.healthclub.Physioplus.Repository;


import com.healthclub.Physioplus.Model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for Payment documents.
 */
@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    /**
     * Finds all payments made by a specific user.
     * @param userId The ID of the user.
     * @return A list of payments.
     */
    List<Payment> findByUserId(String userId);

    /**
     * Finds all payments received by a specific doctor.
     * @param doctorId The ID of the doctor.
     * @return A list of payments.
     */
    List<Payment> findByDoctorId(String doctorId);

    /**
     * Finds a payment associated with a specific booking.
     * @param bookingId The ID of the booking.
     * @return An Optional Payment.
     */
    List<Payment> findByBookingId(String bookingId);

    /**
     * Finds a payment by its external transaction ID.
     * @param transactionId The ID from the payment gateway.
     * @return An Optional Payment.
     */
    List<Payment> findByTransactionId(String transactionId);

    /**
     * Finds a payment by its Razorpay order ID.
     * @param razorpayOrderId The Razorpay order ID.
     * @return An Optional Payment.
     */
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    /**
     * Finds a payment by its Razorpay payment ID.
     * @param razorpayPaymentId The Razorpay payment ID.
     * @return An Optional Payment.
     */
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    /**
     * Finds all payments with a specific status.
     * @param status The payment status.
     * @return A list of payments.
     */
    List<Payment> findByStatus(Payment.PaymentStatus status);
}
