package com.healthclub.Physioplus.Dto;

import com.healthclub.Physioplus.Model.NotificationLog;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SendNotificationRequest {

    @NotBlank(message = "Recipient phone is required")
    private String recipientPhone;

    private String recipientName;

    private String userId;

    private String bookingId;

    @NotBlank(message = "Template name is required")
    private String templateName;

    private NotificationLog.NotificationType type;

    private Map<String, String> templateParams;

    private String languageCode = "en";
}
