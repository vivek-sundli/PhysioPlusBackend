package com.healthclub.Physioplus.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BroadcastNotificationRequest {
    private String targetGroup;  // ALL_DOCTORS, ALL_PATIENTS, SPECIFIC
    private List<String> recipientIds;  // For SPECIFIC
    private String title;
    private String message;
    private String templateName;  // WhatsApp template
    private String channel;       // EMAIL, SMS, WHATSAPP, ALL
}
