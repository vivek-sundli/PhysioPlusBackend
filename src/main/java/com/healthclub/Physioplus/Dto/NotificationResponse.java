package com.healthclub.Physioplus.Dto;

import com.healthclub.Physioplus.Model.NotificationLog;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationResponse {

    private boolean success;
    private String message;
    private String messageId;
    private NotificationLog notification;

    public NotificationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public NotificationResponse(boolean success, String message, String messageId, NotificationLog notification) {
        this.success = success;
        this.message = message;
        this.messageId = messageId;
        this.notification = notification;
    }

    public static NotificationResponse success(String message, String messageId, NotificationLog notification) {
        return new NotificationResponse(true, message, messageId, notification);
    }

    public static NotificationResponse error(String message) {
        return new NotificationResponse(false, message);
    }
}
