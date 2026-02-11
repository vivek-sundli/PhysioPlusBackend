package com.healthclub.Physioplus.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {

    private String email;

    private String phone;

    @NotBlank(message = "OTP is required")
    private String otp;

    @NotBlank(message = "OTP type is required")
    private String type;  // EMAIL or SMS
}
