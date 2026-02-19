package com.healthclub.Physioplus.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PrescriptionRequest {

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    @NotBlank(message = "Doctor ID is required")
    private String doctorId;

    @NotBlank(message = "Patient ID is required")
    private String patientId;

    private String diagnosis;
    private String notes;
    private List<MedicineDto> medicines;
    private List<String> labTestsRecommended;

    @Data
    public static class MedicineDto {
        @NotBlank(message = "Medicine name is required")
        private String name;
        private String dosage;
        private String frequency;
        private String duration;
        private String instructions;
    }
}
