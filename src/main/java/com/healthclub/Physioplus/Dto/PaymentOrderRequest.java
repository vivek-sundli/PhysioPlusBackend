package com.healthclub.Physioplus.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentOrderRequest {

    @Min(value = 1, message = "Amount must be at least 1")
    private double amount;

    private String currency = "INR";

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    @NotBlank(message = "User ID is required")
    private String userId;

    private String doctorId;

    private String userEmail;

    private String userContactNumber;

    private String notes;
}
