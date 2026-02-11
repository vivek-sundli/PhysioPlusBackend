package com.healthclub.Physioplus.Dto;

import com.healthclub.Physioplus.Model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {

    private boolean success;
    private String message;
    private String token;
    private User user;

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, String token, User user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.user = user;
    }

    public static AuthResponse success(String message, String token, User user) {
        return new AuthResponse(true, message, token, user);
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(false, message);
    }
}
