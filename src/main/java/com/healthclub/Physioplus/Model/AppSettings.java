package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Document(collection = "AppSettings")
public class AppSettings {

    @Id
    private String id; // Should be "GLOBAL"

    private Double consultationFeeBase;
    private Double platformFeePercentage;
    private boolean maintenanceMode;
    private List<String> allowedPaymentMethods; // e.g., ["UPI", "CARD"]

    @LastModifiedDate
    private Instant updatedAt;
}
