package com.healthclub.Physioplus.Dto;

import com.healthclub.Physioplus.Model.Feedback;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SendFeedbackRequest {
    private String recipientId;          // Specific user (optional)
    private Feedback.RecipientType recipientType;  // PATIENT, DOCTOR, ALL_PATIENTS, ALL_DOCTORS
    private String bookingId;            // Optional
    private String doctorId;             // Optional
    private String formTitle;
    private String formDescription;
    private Map<String, Object> questions;
    private List<String> recipientIds;   // For bulk sending
}
