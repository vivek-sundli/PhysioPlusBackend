package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Model.GeoLocation;
import com.healthclub.Physioplus.Service.GeoLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/geo")
@Tag(name = "GeoLocation", description = "IP-based location detection APIs")
public class GeoLocationController {

    private final GeoLocationService geoLocationService;

    @Autowired
    public GeoLocationController(GeoLocationService geoLocationService) {
        this.geoLocationService = geoLocationService;
    }

    @GetMapping("/detect")
    @Operation(summary = "Detect location from request IP")
    public ResponseEntity<GeoLocation> detectLocation(HttpServletRequest request) {
        String ipAddress = geoLocationService.extractIpAddress(
                request.getRemoteAddr(),
                request.getHeader("X-Forwarded-For")
        );
        GeoLocation location = geoLocationService.detectLocation(ipAddress);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/detect/{ip}")
    @Operation(summary = "Detect location for specific IP address")
    public ResponseEntity<GeoLocation> detectLocationForIp(@PathVariable String ip) {
        GeoLocation location = geoLocationService.detectLocation(ip);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/cached/{ip}")
    @Operation(summary = "Get cached location for IP (if exists)")
    public ResponseEntity<GeoLocation> getCachedLocation(@PathVariable String ip) {
        return geoLocationService.getByIpAddress(ip)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
