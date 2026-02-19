package com.healthclub.Physioplus.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorApprovalRequest {
    private String doctorId;
    private boolean approved;
    private String rejectionReason;
    private String adminNotes;
}
