package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Dto.NotificationResponse;
import com.healthclub.Physioplus.Dto.SendNotificationRequest;
import com.healthclub.Physioplus.Model.NotificationLog;
import com.healthclub.Physioplus.Service.WhatsAppService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    @Value("${whatsapp.verify-token}")
    private String verifyToken;

    private final WhatsAppService whatsAppService;

    @Autowired
    public WebhookController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    // ==================== Notification Endpoints ====================

    /**
     * POST /api/notifications/send
     * Send a WhatsApp notification using a template
     *
     * Request body:
     * {
     *   "recipientPhone": "+919876543210",
     *   "recipientName": "John Doe",
     *   "userId": "user123",
     *   "bookingId": "booking123",
     *   "templateName": "booking_confirmation",
     *   "type": "BOOKING_CONFIRMATION",
     *   "templateParams": {
     *     "patient_name": "John Doe",
     *     "doctor_name": "Dr. Smith",
     *     "appointment_time": "2024-01-15 10:00 AM"
     *   },
     *   "languageCode": "en"
     * }
     */
    @PostMapping("/notifications/send")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        NotificationResponse response = whatsAppService.sendTemplateMessage(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * POST /api/notifications/send-text
     * Send a simple text message via WhatsApp
     *
     * Request body:
     * {
     *   "recipientPhone": "+919876543210",
     *   "message": "Hello! Your appointment is confirmed.",
     *   "userId": "user123"
     * }
     */
    @PostMapping("/notifications/send-text")
    public ResponseEntity<NotificationResponse> sendTextMessage(@RequestBody Map<String, String> request) {
        String phone = request.get("recipientPhone");
        String message = request.get("message");
        String userId = request.get("userId");

        if (phone == null || message == null) {
            return ResponseEntity.badRequest().body(
                    NotificationResponse.error("recipientPhone and message are required"));
        }

        NotificationResponse response = whatsAppService.sendTextMessage(phone, message, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * GET /api/notifications/user/{userId}
     * Get all notifications for a user
     */
    @GetMapping("/notifications/user/{userId}")
    public ResponseEntity<List<NotificationLog>> getNotificationsForUser(@PathVariable String userId) {
        List<NotificationLog> notifications = whatsAppService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/notifications/booking/{bookingId}
     * Get all notifications for a booking
     */
    @GetMapping("/notifications/booking/{bookingId}")
    public ResponseEntity<List<NotificationLog>> getNotificationsForBooking(@PathVariable String bookingId) {
        List<NotificationLog> notifications = whatsAppService.getNotificationsForBooking(bookingId);
        return ResponseEntity.ok(notifications);
    }

    // ==================== WhatsApp Webhook Endpoints ====================

    /**
     * GET /api/webhooks/whatsapp
     * Webhook verification endpoint for Meta
     * Meta sends a GET request to verify the webhook URL
     */
    @GetMapping("/webhooks/whatsapp")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).body("Verification failed");
    }

    /**
     * POST /api/webhooks/whatsapp
     * Receive delivery status updates and incoming messages from WhatsApp
     *
     * Webhook payload structure:
     * {
     *   "object": "whatsapp_business_account",
     *   "entry": [{
     *     "changes": [{
     *       "value": {
     *         "messaging_product": "whatsapp",
     *         "statuses": [{
     *           "id": "wamid.xxx",
     *           "status": "delivered",
     *           "timestamp": "1234567890"
     *         }]
     *       }
     *     }]
     *   }]
     * }
     */
    @PostMapping("/webhooks/whatsapp")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String object = (String) payload.get("object");

            if ("whatsapp_business_account".equals(object)) {
                List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");

                if (entries != null) {
                    for (Map<String, Object> entry : entries) {
                        List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get("changes");

                        if (changes != null) {
                            for (Map<String, Object> change : changes) {
                                Map<String, Object> value = (Map<String, Object>) change.get("value");
                                processWebhookValue(value);
                            }
                        }
                    }
                }
            }

            return ResponseEntity.ok("EVENT_RECEIVED");

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage());
            return ResponseEntity.ok("EVENT_RECEIVED");  // Always return 200 to acknowledge
        }
    }

    private void processWebhookValue(Map<String, Object> value) {
        // Process message status updates
        List<Map<String, Object>> statuses = (List<Map<String, Object>>) value.get("statuses");
        if (statuses != null) {
            for (Map<String, Object> status : statuses) {
                String messageId = (String) status.get("id");
                String statusValue = (String) status.get("status");
                String timestamp = (String) status.get("timestamp");

                // Convert Unix timestamp to ISO format if needed
                if (timestamp != null && !timestamp.contains("T")) {
                    timestamp = java.time.Instant.ofEpochSecond(Long.parseLong(timestamp)).toString();
                }

                whatsAppService.updateDeliveryStatus(messageId, statusValue, timestamp);
            }
        }

        // Process incoming messages (optional - for two-way communication)
        List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
        if (messages != null) {
            for (Map<String, Object> message : messages) {
                String from = (String) message.get("from");
                String messageId = (String) message.get("id");
                String type = (String) message.get("type");

                // Log or process incoming message
                log.info("Received message from {}: {} ({})", from, messageId, type);

                // You can add custom handling for incoming messages here
                // e.g., auto-reply, trigger workflows, etc.
            }
        }
    }

    // ==================== Convenience Endpoints ====================

    /**
     * POST /api/notifications/booking-confirmation
     * Send booking confirmation notification
     */
    @PostMapping("/notifications/booking-confirmation")
    public ResponseEntity<NotificationResponse> sendBookingConfirmation(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String patientName = request.get("patientName");
        String doctorName = request.get("doctorName");
        String appointmentTime = request.get("appointmentTime");
        String userId = request.get("userId");
        String bookingId = request.get("bookingId");

        NotificationResponse response = whatsAppService.sendBookingConfirmation(
                phone, patientName, doctorName, appointmentTime, userId, bookingId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * POST /api/notifications/appointment-reminder
     * Send appointment reminder notification
     */
    @PostMapping("/notifications/appointment-reminder")
    public ResponseEntity<NotificationResponse> sendAppointmentReminder(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String patientName = request.get("patientName");
        String doctorName = request.get("doctorName");
        String appointmentTime = request.get("appointmentTime");
        String userId = request.get("userId");
        String bookingId = request.get("bookingId");

        NotificationResponse response = whatsAppService.sendAppointmentReminder(
                phone, patientName, doctorName, appointmentTime, userId, bookingId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * POST /api/notifications/payment-confirmation
     * Send payment confirmation notification
     */
    @PostMapping("/notifications/payment-confirmation")
    public ResponseEntity<NotificationResponse> sendPaymentConfirmation(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String patientName = request.get("patientName");
        String amount = request.get("amount");
        String transactionId = request.get("transactionId");
        String userId = request.get("userId");
        String bookingId = request.get("bookingId");

        NotificationResponse response = whatsAppService.sendPaymentConfirmation(
                phone, patientName, amount, transactionId, userId, bookingId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
