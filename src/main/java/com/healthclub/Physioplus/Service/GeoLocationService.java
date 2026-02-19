package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Model.GeoLocation;
import com.healthclub.Physioplus.Repository.GeoLocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class GeoLocationService {

    private static final Logger log = LoggerFactory.getLogger(GeoLocationService.class);
    private static final String IP_API_URL = "http://ip-api.com/json/";

    private final GeoLocationRepository geoLocationRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public GeoLocationService(GeoLocationRepository geoLocationRepository) {
        this.geoLocationRepository = geoLocationRepository;
        this.restTemplate = new RestTemplate();
    }

    @Transactional(readOnly = true)
    public Optional<GeoLocation> getByIpAddress(String ipAddress) {
        return geoLocationRepository.findByIpAddress(ipAddress);
    }

    public GeoLocation detectLocation(String ipAddress) {
        // Check cache first
        Optional<GeoLocation> cached = geoLocationRepository.findByIpAddress(ipAddress);
        if (cached.isPresent()) {
            log.debug("Returning cached location for IP: {}", ipAddress);
            return cached.get();
        }

        try {
            // Call ip-api.com (free tier: 45 requests/minute)
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(IP_API_URL + ipAddress, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                GeoLocation location = new GeoLocation();
                location.setIpAddress(ipAddress);
                location.setCountry((String) response.get("country"));
                location.setCountryCode((String) response.get("countryCode"));
                location.setRegion((String) response.get("regionName"));
                location.setCity((String) response.get("city"));
                location.setPostalCode((String) response.get("zip"));
                location.setTimezone((String) response.get("timezone"));
                location.setIsp((String) response.get("isp"));

                // Set latitude and longitude
                if (response.get("lat") != null) {
                    location.setLatitude(((Number) response.get("lat")).doubleValue());
                }
                if (response.get("lon") != null) {
                    location.setLongitude(((Number) response.get("lon")).doubleValue());
                }

                // Set currency based on country code
                location.setCurrency(getCurrencyForCountry((String) response.get("countryCode")));

                location.setDetectedAt(Instant.now());
                log.info("Detected location for IP {}: {}, {}", ipAddress, location.getCity(), location.getCountry());

                return geoLocationRepository.save(location);
            }
        } catch (Exception e) {
            log.error("Failed to detect location for IP: {}", ipAddress, e);
        }

        // Return default location on failure
        GeoLocation defaultLocation = new GeoLocation();
        defaultLocation.setIpAddress(ipAddress);
        defaultLocation.setCountry("India");
        defaultLocation.setCountryCode("IN");
        defaultLocation.setCurrency("INR");
        defaultLocation.setTimezone("Asia/Kolkata");
        defaultLocation.setDetectedAt(Instant.now());
        return geoLocationRepository.save(defaultLocation);
    }

    private String getCurrencyForCountry(String countryCode) {
        if (countryCode == null) return "INR";
        return switch (countryCode) {
            case "US" -> "USD";
            case "GB" -> "GBP";
            case "EU", "DE", "FR", "IT", "ES" -> "EUR";
            case "JP" -> "JPY";
            case "AU" -> "AUD";
            case "CA" -> "CAD";
            case "AE" -> "AED";
            case "SG" -> "SGD";
            default -> "INR";
        };
    }

    public String extractIpAddress(String remoteAddr, String xForwardedFor) {
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        return remoteAddr;
    }
}
