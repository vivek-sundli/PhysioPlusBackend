package com.healthclub.Physioplus.Dto;

import lombok.Data;
import java.util.List;

@Data
public class AppSettingsDto {
    private Double consultationFeeBase;
    private Double platformFeePercentage;
    private boolean maintenanceMode;
    private List<String> allowedPaymentMethods;
}
