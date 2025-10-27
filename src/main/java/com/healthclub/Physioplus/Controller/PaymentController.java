package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Model.Payment;
import com.healthclub.Physioplus.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for handling payment operations.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /api/payments : Creates a new payment (initiates it).
     */
    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        Payment newPayment = paymentService.createPayment(payment);
        return ResponseEntity.ok(newPayment);
    }

    /**
     * GET /api/payments/{id} : Gets a payment by its unique ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/payments/user/{userId} : Gets all payments for a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUserId(@PathVariable String userId) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    /**
     * GET /api/payments/doctor/{doctorId} : Gets all payments for a specific doctor.
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Payment>> getPaymentsByDoctorId(@PathVariable String doctorId) {
        List<Payment> payments = paymentService.getPaymentsByDoctorId(doctorId);
        return ResponseEntity.ok(payments);
    }

    /**
     * PUT /api/payments/{id}/status : Updates a payment's status.
     * This would typically be called by a payment gateway webhook.
     *
     * Request Body example:
     * {
     * "transactionId": "txn_123abc456"
     * }
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updatePaymentStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        try {
            String transactionId = body.get("transactionId");

            // Assumes the service layer now handles setting the status, e.g., to COMPLETED
            return paymentService.updatePaymentStatus(id, transactionId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            // Catches invalid status strings
            return ResponseEntity.badRequest().build();
        }
    }
}

