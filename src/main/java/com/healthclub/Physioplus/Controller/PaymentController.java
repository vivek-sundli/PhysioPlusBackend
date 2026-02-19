package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Dto.PageResponse;
import com.healthclub.Physioplus.Dto.PaymentOrderRequest;
import com.healthclub.Physioplus.Dto.PaymentOrderResponse;
import com.healthclub.Physioplus.Dto.PaymentVerifyRequest;
import com.healthclub.Physioplus.Model.Payment;
import com.healthclub.Physioplus.Service.PaymentService;
import com.healthclub.Physioplus.Service.RazorpayService;
import jakarta.validation.Valid;
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
    private final RazorpayService razorpayService;

    @Autowired
    public PaymentController(PaymentService paymentService, RazorpayService razorpayService) {
        this.paymentService = paymentService;
        this.razorpayService = razorpayService;
    }

    /**
     * POST /api/payments : Creates a new payment (initiates it).
     */
    @PostMapping
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody Payment payment) {
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
     * GET /api/payments/user/{userId} : Gets all payments for a specific user with pagination.
     * @param page Page number (0-indexed), defaults to 0.
     * @param size Page size, defaults to 20.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponse<Payment>> getPaymentsByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Payment> payments = paymentService.getPaymentsByUserId(userId, page, size);
        return ResponseEntity.ok(payments);
    }

    /**
     * GET /api/payments/doctor/{doctorId} : Gets all payments for a specific doctor with pagination.
     * @param page Page number (0-indexed), defaults to 0.
     * @param size Page size, defaults to 20.
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<PageResponse<Payment>> getPaymentsByDoctorId(
            @PathVariable String doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Payment> payments = paymentService.getPaymentsByDoctorId(doctorId, page, size);
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

    // ==================== Razorpay Integration Endpoints ====================

    /**
     * POST /api/payments/create-order
     * Create a new Razorpay order for payment
     *
     * Request body:
     * {
     *   "amount": 500.00,
     *   "currency": "INR",
     *   "bookingId": "booking123",
     *   "userId": "user123",
     *   "doctorId": "doctor123",
     *   "userEmail": "user@example.com",
     *   "userContactNumber": "+919876543210",
     *   "notes": "Consultation fee"
     * }
     */
    @PostMapping("/create-order")
    public ResponseEntity<PaymentOrderResponse> createOrder(@Valid @RequestBody PaymentOrderRequest request) {
        PaymentOrderResponse response = razorpayService.createOrder(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * POST /api/payments/verify
     * Verify Razorpay payment after completion
     *
     * Request body:
     * {
     *   "razorpayOrderId": "order_xxx",
     *   "razorpayPaymentId": "pay_xxx",
     *   "razorpaySignature": "signature_xxx"
     * }
     */
    @PostMapping("/verify")
    public ResponseEntity<PaymentOrderResponse> verifyPayment(@Valid @RequestBody PaymentVerifyRequest request) {
        PaymentOrderResponse response = razorpayService.verifyPayment(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * GET /api/payments/order/{orderId}
     * Get payment details by Razorpay order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable String orderId) {
        return razorpayService.getPaymentByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/payments/booking/{bookingId}
     * Get all payments for a specific booking
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<Payment>> getPaymentsByBookingId(@PathVariable String bookingId) {
        List<Payment> payments = paymentService.getPaymentsByBookingId(bookingId);
        return ResponseEntity.ok(payments);
    }
}

