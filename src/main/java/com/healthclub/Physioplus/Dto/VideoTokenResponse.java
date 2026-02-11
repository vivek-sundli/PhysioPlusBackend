package com.healthclub.Physioplus.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoTokenResponse {

    private boolean success;
    private String message;
    private String token;
    private String roomId;
    private String role;  // host or guest

    public VideoTokenResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public VideoTokenResponse(boolean success, String message, String token, String roomId, String role) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.roomId = roomId;
        this.role = role;
    }

    public static VideoTokenResponse success(String token, String roomId, String role) {
        return new VideoTokenResponse(true, "Token generated successfully", token, roomId, role);
    }

    public static VideoTokenResponse error(String message) {
        return new VideoTokenResponse(false, message);
    }
}
