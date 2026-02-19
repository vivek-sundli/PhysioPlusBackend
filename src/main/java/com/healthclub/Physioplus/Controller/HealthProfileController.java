package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Dto.PageResponse;
import com.healthclub.Physioplus.Model.HealthProfile;
import com.healthclub.Physioplus.Model.PainDiary;
import com.healthclub.Physioplus.Service.HealthProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-profile")
@Tag(name = "Health Profile", description = "Patient health profile and pain diary APIs")
public class HealthProfileController {

    private final HealthProfileService healthProfileService;

    @Autowired
    public HealthProfileController(HealthProfileService healthProfileService) {
        this.healthProfileService = healthProfileService;
    }

    // ==================== Health Profile ====================

    @PostMapping
    @Operation(summary = "Create health profile for patient")
    public ResponseEntity<HealthProfile> createProfile(@Valid @RequestBody HealthProfile profile) {
        return ResponseEntity.ok(healthProfileService.createProfile(profile));
    }

    @GetMapping("/{patientId}")
    @Operation(summary = "Get health profile by patient ID")
    public ResponseEntity<HealthProfile> getProfile(@PathVariable String patientId) {
        return healthProfileService.getProfileByPatientId(patientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{patientId}")
    @Operation(summary = "Update health profile")
    public ResponseEntity<HealthProfile> updateProfile(
            @PathVariable String patientId,
            @Valid @RequestBody HealthProfile profile) {
        return healthProfileService.updateProfile(patientId, profile)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{patientId}/medications")
    @Operation(summary = "Add medication to health profile")
    public ResponseEntity<HealthProfile> addMedication(
            @PathVariable String patientId,
            @Valid @RequestBody HealthProfile.Medication medication) {
        return ResponseEntity.ok(healthProfileService.addMedication(patientId, medication));
    }

    @PostMapping("/{patientId}/allergies")
    @Operation(summary = "Add allergy to health profile")
    public ResponseEntity<HealthProfile> addAllergy(
            @PathVariable String patientId,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(healthProfileService.addAllergy(patientId, request.get("allergy")));
    }

    // ==================== Pain Diary ====================

    @PostMapping("/pain-diary")
    @Operation(summary = "Add pain diary entry")
    public ResponseEntity<PainDiary> addPainEntry(@Valid @RequestBody PainDiary entry) {
        return ResponseEntity.ok(healthProfileService.addPainEntry(entry));
    }

    @GetMapping("/pain-diary/{patientId}")
    @Operation(summary = "Get pain diary entries with pagination")
    public ResponseEntity<PageResponse<PainDiary>> getPainDiaryEntries(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(healthProfileService.getPainDiaryEntries(patientId, page, size));
    }

    @GetMapping("/pain-diary/{patientId}/range")
    @Operation(summary = "Get pain diary entries by date range")
    public ResponseEntity<List<PainDiary>> getPainDiaryByRange(
            @PathVariable String patientId,
            @RequestParam Instant start,
            @RequestParam Instant end) {
        return ResponseEntity.ok(healthProfileService.getPainDiaryEntriesByDateRange(patientId, start, end));
    }

    @GetMapping("/pain-diary/{patientId}/recent")
    @Operation(summary = "Get recent pain diary entries")
    public ResponseEntity<List<PainDiary>> getRecentPainEntries(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(healthProfileService.getRecentPainEntries(patientId, days));
    }

    @PutMapping("/pain-diary/{entryId}")
    @Operation(summary = "Update pain diary entry")
    public ResponseEntity<PainDiary> updatePainEntry(
            @PathVariable String entryId,
            @Valid @RequestBody PainDiary entry) {
        return healthProfileService.updatePainEntry(entryId, entry)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/pain-diary/{entryId}")
    @Operation(summary = "Delete pain diary entry")
    public ResponseEntity<Void> deletePainEntry(@PathVariable String entryId) {
        if (healthProfileService.deletePainEntry(entryId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ==================== Analytics ====================

    @GetMapping("/pain-diary/{patientId}/average")
    @Operation(summary = "Get average pain level over specified days")
    public ResponseEntity<Map<String, Object>> getAveragePainLevel(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "7") int days) {
        Double average = healthProfileService.getAveragePainLevel(patientId, days);
        return ResponseEntity.ok(Map.of(
                "patientId", patientId,
                "days", days,
                "averagePainLevel", average
        ));
    }
}
