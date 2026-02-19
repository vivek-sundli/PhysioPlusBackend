package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "GeoLocations")
public class GeoLocation {

    @Id
    private String id;

    private String userId;
    private String ipAddress;

    // Location Data
    private String country;
    private String countryCode;
    private String region;
    private String city;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private String timezone;

    // Currency & Language
    private String currency;
    private String currencySymbol;
    private String language;

    // ISP Info
    private String isp;
    private String organization;

    // Flags
    private boolean vpnDetected;
    private boolean proxyDetected;

    private Instant detectedAt;
    private Instant lastUpdated;

    public GeoLocation() {
        this.detectedAt = Instant.now();
        this.lastUpdated = Instant.now();
    }
}
