package com.healthclub.Physioplus.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendOtpRequest {

    private String email;

    private String phone;

    @NotBlank(message = "OTP type is required")
    private String type;  // EMAIL or SMS

    private String name;

    private UserRole role;  // PATIENT or DOCTOR
}
