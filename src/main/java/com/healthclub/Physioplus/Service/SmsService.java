package com.healthclub.Physioplus.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Value("${msg91.auth-key:}")
    private String authKey;

    @Value("${msg91.template-id:}")
    private String templateId;

    @Value("${msg91.sender-id:PHYSIO}")
    private String senderId;

    private static final String MSG91_API_URL = "https://control.msg91.com/api/v5/flow/";

    private final RestTemplate restTemplate;

    public SmsService() {
        this.restTemplate = new RestTemplate();
    }

    private boolean isConfigured() {
        return authKey != null && !authKey.isBlank() && templateId != null && !templateId.isBlank();
    }

    public boolean sendOtp(String phoneNumber, String otp) {
        if (!isConfigured()) {
            log.info("[DEV MODE] SMS not configured. OTP for {}: {}", phoneNumber, otp);
            return true;  // Return true in dev mode to allow testing
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authkey", authKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("template_id", templateId);
            requestBody.put("sender", senderId);
            requestBody.put("short_url", "0");
            requestBody.put("mobiles", formatPhoneNumber(phoneNumber));

            Map<String, String> variables = new HashMap<>();
            variables.put("otp", otp);
            requestBody.put("VAR1", otp);

            List<Map<String, Object>> recipients = List.of(
                Map.of("mobiles", formatPhoneNumber(phoneNumber), "otp", otp)
            );
            requestBody.put("recipients", recipients);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            restTemplate.postForEntity(MSG91_API_URL, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("Failed to send OTP SMS to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    private String formatPhoneNumber(String phone) {
        // Remove any non-digit characters
        String cleaned = phone.replaceAll("[^0-9]", "");
        // Add country code if not present (assuming India +91)
        if (cleaned.length() == 10) {
            return "91" + cleaned;
        }
        return cleaned;
    }

    public boolean sendAppointmentReminder(String phoneNumber, String patientName, String doctorName, String appointmentTime) {
        if (!isConfigured()) {
            log.info("[DEV MODE] SMS not configured. Appointment reminder for {}", phoneNumber);
            return true;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authkey", authKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("template_id", templateId);
            requestBody.put("sender", senderId);
            requestBody.put("mobiles", formatPhoneNumber(phoneNumber));

            List<Map<String, Object>> recipients = List.of(
                Map.of(
                    "mobiles", formatPhoneNumber(phoneNumber),
                    "patient_name", patientName,
                    "doctor_name", doctorName,
                    "appointment_time", appointmentTime
                )
            );
            requestBody.put("recipients", recipients);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            restTemplate.postForEntity(MSG91_API_URL, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("Failed to send appointment reminder SMS to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }
}
