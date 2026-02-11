package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Dto.NotificationResponse;
import com.healthclub.Physioplus.Dto.SendNotificationRequest;
import com.healthclub.Physioplus.Model.NotificationLog;
import com.healthclub.Physioplus.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Service
public class WhatsAppService {

    @Value("${whatsapp.token}")
    private String accessToken;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    private static final String WHATSAPP_API_URL = "https://graph.facebook.com/v18.0";

    private final NotificationRepository notificationRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public WhatsAppService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        this.restTemplate = new RestTemplate();
    }

    public NotificationResponse sendTemplateMessage(SendNotificationRequest request) {
        try {
            // Create notification log
            NotificationLog notification = new NotificationLog();
            notification.setUserId(request.getUserId());
            notification.setBookingId(request.getBookingId());
            notification.setRecipientPhone(request.getRecipientPhone());
            notification.setRecipientName(request.getRecipientName());
            notification.setTemplateName(request.getTemplateName());
            notification.setType(request.getType() != null ?
                    request.getType() : NotificationLog.NotificationType.CUSTOM);
            notification.setChannel(NotificationLog.NotificationChannel.WHATSAPP);
            notification.setStatus(NotificationLog.DeliveryStatus.PENDING);
            notification.setCreatedAt(Instant.now());
            notification.setUpdatedAt(Instant.now());

            // Build WhatsApp API request
            String url = WHATSAPP_API_URL + "/" + phoneNumberId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, Object> body = buildTemplateMessageBody(request);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, String>> messages = (List<Map<String, String>>) responseBody.get("messages");

                if (messages != null && !messages.isEmpty()) {
                    String messageId = messages.get(0).get("id");
                    notification.setMessageId(messageId);
                    notification.setStatus(NotificationLog.DeliveryStatus.SENT);
                    notification.setUpdatedAt(Instant.now());

                    NotificationLog saved = notificationRepository.save(notification);
                    return NotificationResponse.success("Message sent successfully", messageId, saved);
                }
            }

            notification.setStatus(NotificationLog.DeliveryStatus.FAILED);
            notification.setStatusMessage("Failed to send message");
            notificationRepository.save(notification);
            return NotificationResponse.error("Failed to send WhatsApp message");

        } catch (Exception e) {
            return NotificationResponse.error("Error sending message: " + e.getMessage());
        }
    }

    private Map<String, Object> buildTemplateMessageBody(SendNotificationRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("recipient_type", "individual");
        body.put("to", formatPhoneNumber(request.getRecipientPhone()));
        body.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", request.getTemplateName());

        Map<String, String> language = new HashMap<>();
        language.put("code", request.getLanguageCode() != null ? request.getLanguageCode() : "en");
        template.put("language", language);

        // Add template parameters if provided
        if (request.getTemplateParams() != null && !request.getTemplateParams().isEmpty()) {
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> bodyComponent = new HashMap<>();
            bodyComponent.put("type", "body");

            List<Map<String, String>> parameters = new ArrayList<>();
            for (String value : request.getTemplateParams().values()) {
                Map<String, String> param = new HashMap<>();
                param.put("type", "text");
                param.put("text", value);
                parameters.add(param);
            }
            bodyComponent.put("parameters", parameters);
            components.add(bodyComponent);

            template.put("components", components);
        }

        body.put("template", template);
        return body;
    }

    public NotificationResponse sendTextMessage(String recipientPhone, String message, String userId) {
        try {
            NotificationLog notification = new NotificationLog();
            notification.setUserId(userId);
            notification.setRecipientPhone(recipientPhone);
            notification.setContent(message);
            notification.setType(NotificationLog.NotificationType.CUSTOM);
            notification.setChannel(NotificationLog.NotificationChannel.WHATSAPP);
            notification.setStatus(NotificationLog.DeliveryStatus.PENDING);
            notification.setCreatedAt(Instant.now());

            String url = WHATSAPP_API_URL + "/" + phoneNumberId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("recipient_type", "individual");
            body.put("to", formatPhoneNumber(recipientPhone));
            body.put("type", "text");

            Map<String, Object> text = new HashMap<>();
            text.put("preview_url", false);
            text.put("body", message);
            body.put("text", text);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, String>> messages = (List<Map<String, String>>) responseBody.get("messages");

                if (messages != null && !messages.isEmpty()) {
                    String messageId = messages.get(0).get("id");
                    notification.setMessageId(messageId);
                    notification.setStatus(NotificationLog.DeliveryStatus.SENT);

                    NotificationLog saved = notificationRepository.save(notification);
                    return NotificationResponse.success("Message sent successfully", messageId, saved);
                }
            }

            notification.setStatus(NotificationLog.DeliveryStatus.FAILED);
            notificationRepository.save(notification);
            return NotificationResponse.error("Failed to send WhatsApp message");

        } catch (Exception e) {
            return NotificationResponse.error("Error sending message: " + e.getMessage());
        }
    }

    public void updateDeliveryStatus(String messageId, String status, String timestamp) {
        Optional<NotificationLog> notificationOpt = notificationRepository.findByMessageId(messageId);
        if (notificationOpt.isPresent()) {
            NotificationLog notification = notificationOpt.get();

            switch (status.toLowerCase()) {
                case "sent":
                    notification.setStatus(NotificationLog.DeliveryStatus.SENT);
                    break;
                case "delivered":
                    notification.setStatus(NotificationLog.DeliveryStatus.DELIVERED);
                    notification.setDeliveredAt(Instant.parse(timestamp));
                    break;
                case "read":
                    notification.setStatus(NotificationLog.DeliveryStatus.READ);
                    notification.setReadAt(Instant.parse(timestamp));
                    break;
                case "failed":
                    notification.setStatus(NotificationLog.DeliveryStatus.FAILED);
                    break;
            }

            notification.setUpdatedAt(Instant.now());
            notificationRepository.save(notification);
        }
    }

    public List<NotificationLog> getNotificationsForUser(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    public List<NotificationLog> getNotificationsForBooking(String bookingId) {
        return notificationRepository.findByBookingId(bookingId);
    }

    private String formatPhoneNumber(String phone) {
        String cleaned = phone.replaceAll("[^0-9]", "");
        // Add country code if not present (assuming India +91)
        if (cleaned.length() == 10) {
            return "91" + cleaned;
        }
        return cleaned;
    }

    // Convenience methods for common notification types

    public NotificationResponse sendBookingConfirmation(String phone, String patientName,
                                                         String doctorName, String appointmentTime,
                                                         String userId, String bookingId) {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setRecipientPhone(phone);
        request.setRecipientName(patientName);
        request.setUserId(userId);
        request.setBookingId(bookingId);
        request.setTemplateName("booking_confirmation");
        request.setType(NotificationLog.NotificationType.BOOKING_CONFIRMATION);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("patient_name", patientName);
        params.put("doctor_name", doctorName);
        params.put("appointment_time", appointmentTime);
        request.setTemplateParams(params);

        return sendTemplateMessage(request);
    }

    public NotificationResponse sendAppointmentReminder(String phone, String patientName,
                                                         String doctorName, String appointmentTime,
                                                         String userId, String bookingId) {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setRecipientPhone(phone);
        request.setRecipientName(patientName);
        request.setUserId(userId);
        request.setBookingId(bookingId);
        request.setTemplateName("appointment_reminder");
        request.setType(NotificationLog.NotificationType.APPOINTMENT_REMINDER);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("patient_name", patientName);
        params.put("doctor_name", doctorName);
        params.put("appointment_time", appointmentTime);
        request.setTemplateParams(params);

        return sendTemplateMessage(request);
    }

    public NotificationResponse sendPaymentConfirmation(String phone, String patientName,
                                                         String amount, String transactionId,
                                                         String userId, String bookingId) {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setRecipientPhone(phone);
        request.setRecipientName(patientName);
        request.setUserId(userId);
        request.setBookingId(bookingId);
        request.setTemplateName("payment_confirmation");
        request.setType(NotificationLog.NotificationType.PAYMENT_CONFIRMATION);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("patient_name", patientName);
        params.put("amount", amount);
        params.put("transaction_id", transactionId);
        request.setTemplateParams(params);

        return sendTemplateMessage(request);
    }
}
