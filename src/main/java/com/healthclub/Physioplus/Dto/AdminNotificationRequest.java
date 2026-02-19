package com.healthclub.Physioplus.Dto;

import lombok.Data;
import java.util.List;

@Data
public class AdminNotificationRequest {
    private String title;
    private String message;
    private String type; // USER, GROUP, BROADCAST
    private String targetUserId;
    private List<String> targetUserIds;
    private String targetGroup; // DOCTORS, PATIENTS
}
