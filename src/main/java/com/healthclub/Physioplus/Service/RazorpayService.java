package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Dto.PaymentOrderRequest;
import com.healthclub.Physioplus.Dto.PaymentOrderResponse;
import com.healthclub.Physioplus.Dto.PaymentVerifyRequest;
import com.healthclub.Physioplus.Model.Payment;
import com.healthclub.Physioplus.Repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RazorpayService {

    @Value("${razorpay.key-id:}")
    private String keyId;

    @Value("${razorpay.key-secret:}")
    private String keySecret;

    private RazorpayClient razorpayClient;

    private final PaymentRepository paymentRepository;

    @Autowired
    public RazorpayService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostConstruct
    public void init() {
        if (!isConfigured()) {
            System.out.println("[DEV MODE] Razorpay not configured. Payment features will work in mock mode.");
            return;
        }
        try {
            this.razorpayClient = new RazorpayClient(keyId, keySecret);
        } catch (RazorpayException e) {
            System.err.println("Failed to initialize Razorpay client: " + e.getMessage());
        }
    }

    private boolean isConfigured() {
        return keyId != null && !keyId.isBlank() && keySecret != null && !keySecret.isBlank();
    }

    public PaymentOrderResponse createOrder(PaymentOrderRequest request) {
        if (!isConfigured() || razorpayClient == null) {
            // Mock mode for development
            String mockOrderId = "order_mock_" + UUID.randomUUID().toString().substring(0, 8);
            Payment payment = new Payment();
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
            payment.setBookingId(request.getBookingId());
            payment.setUserId(request.getUserId());
            payment.setDoctorId(request.getDoctorId());
            payment.setUserEmail(request.getUserEmail());
            payment.setUserContactNumber(request.getUserContactNumber());
            payment.setRazorpayOrderId(mockOrderId);
            payment.setReceipt("rcpt_mock_" + UUID.randomUUID().toString().substring(0, 8));
            payment.setNotes(request.getNotes());
            payment.setStatus(Payment.PaymentStatus.CREATED);
            payment.setPaymentTime(LocalDateTime.now());
            Payment savedPayment = paymentRepository.save(payment);
            System.out.println("[DEV MODE] Mock Razorpay order created: " + mockOrderId);
            return PaymentOrderResponse.success(mockOrderId, savedPayment.getId(), request.getAmount(),
                    request.getCurrency() != null ? request.getCurrency() : "INR", "rzp_test_mock");
        }
        try {
            // Create Razorpay order
            JSONObject orderRequest = new JSONObject();
            // Razorpay expects amount in paise (smallest currency unit)
            orderRequest.put("amount", (int) (request.getAmount() * 100));
            orderRequest.put("currency", request.getCurrency() != null ? request.getCurrency() : "INR");
            orderRequest.put("receipt", "rcpt_" + UUID.randomUUID().toString().substring(0, 8));
            orderRequest.put("payment_capture", 1);  // Auto-capture payment

            JSONObject notes = new JSONObject();
            notes.put("bookingId", request.getBookingId());
            notes.put("userId", request.getUserId());
            if (request.getNotes() != null) {
                notes.put("description", request.getNotes());
            }
            orderRequest.put("notes", notes);

            Order order = razorpayClient.orders.create(orderRequest);

            // Create payment record in database
            Payment payment = new Payment();
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
            payment.setBookingId(request.getBookingId());
            payment.setUserId(request.getUserId());
            payment.setDoctorId(request.getDoctorId());
            payment.setUserEmail(request.getUserEmail());
            payment.setUserContactNumber(request.getUserContactNumber());
            payment.setRazorpayOrderId(order.get("id"));
            payment.setReceipt(order.get("receipt"));
            payment.setNotes(request.getNotes());
            payment.setStatus(Payment.PaymentStatus.CREATED);
            payment.setPaymentTime(LocalDateTime.now());

            Payment savedPayment = paymentRepository.save(payment);

            return PaymentOrderResponse.success(
                    order.get("id"),
                    savedPayment.getId(),
                    request.getAmount(),
                    request.getCurrency() != null ? request.getCurrency() : "INR",
                    keyId
            );

        } catch (RazorpayException e) {
            return PaymentOrderResponse.error("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    public PaymentOrderResponse verifyPayment(PaymentVerifyRequest request) {
        if (!isConfigured() || razorpayClient == null) {
            // Mock mode - auto-verify in dev
            Optional<Payment> paymentOpt = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId());
            if (paymentOpt.isEmpty()) {
                return PaymentOrderResponse.error("Payment record not found");
            }
            Payment payment = paymentOpt.get();
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment.setTransactionId(request.getRazorpayPaymentId());
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setPaymentTime(LocalDateTime.now());
            paymentRepository.save(payment);
            System.out.println("[DEV MODE] Mock payment verified: " + request.getRazorpayOrderId());
            return new PaymentOrderResponse(true, "Payment verified successfully (mock mode)");
        }
        try {
            // Verify signature
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", request.getRazorpayOrderId());
            attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
            attributes.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(attributes, keySecret);

            if (!isValid) {
                // Update payment status to failed
                updatePaymentStatus(request.getRazorpayOrderId(), null, null, Payment.PaymentStatus.FAILED);
                return PaymentOrderResponse.error("Invalid payment signature");
            }

            // Update payment record
            Optional<Payment> paymentOpt = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId());
            if (paymentOpt.isEmpty()) {
                return PaymentOrderResponse.error("Payment record not found");
            }

            Payment payment = paymentOpt.get();
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment.setTransactionId(request.getRazorpayPaymentId());
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setPaymentTime(LocalDateTime.now());

            paymentRepository.save(payment);

            return new PaymentOrderResponse(true, "Payment verified successfully");

        } catch (RazorpayException e) {
            return PaymentOrderResponse.error("Payment verification failed: " + e.getMessage());
        }
    }

    public void updatePaymentStatus(String razorpayOrderId, String razorpayPaymentId,
                                     String razorpaySignature, Payment.PaymentStatus status) {
        Optional<Payment> paymentOpt = paymentRepository.findByRazorpayOrderId(razorpayOrderId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            if (razorpayPaymentId != null) {
                payment.setRazorpayPaymentId(razorpayPaymentId);
            }
            if (razorpaySignature != null) {
                payment.setRazorpaySignature(razorpaySignature);
            }
            payment.setStatus(status);
            paymentRepository.save(payment);
        }
    }

    public Optional<Payment> getPaymentByOrderId(String razorpayOrderId) {
        return paymentRepository.findByRazorpayOrderId(razorpayOrderId);
    }

    public String getKeyId() {
        return keyId;
    }
}
