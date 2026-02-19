package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Model.FamilyProfile;
import com.healthclub.Physioplus.Service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/family")
@Tag(name = "Family", description = "Family profile management APIs")
public class FamilyController {

    private final FamilyService familyService;

    @Autowired
    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    // ==================== Family Profile ====================

    @PostMapping
    @Operation(summary = "Create family profile")
    public ResponseEntity<FamilyProfile> createFamilyProfile(@RequestBody Map<String, String> request) {
        FamilyProfile profile = familyService.createFamilyProfile(
                request.get("primaryUserId"),
                request.get("familyName")
        );
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{primaryUserId}")
    @Operation(summary = "Get family profile")
    public ResponseEntity<FamilyProfile> getFamilyProfile(@PathVariable String primaryUserId) {
        return familyService.getFamilyProfile(primaryUserId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{primaryUserId}/settings")
    @Operation(summary = "Update family settings")
    public ResponseEntity<FamilyProfile> updateFamilySettings(
            @PathVariable String primaryUserId,
            @RequestBody Map<String, Boolean> request) {
        return familyService.updateFamilySettings(
                        primaryUserId,
                        request.getOrDefault("sharedMedicalHistory", false),
                        request.getOrDefault("sharedPaymentMethod", true))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{primaryUserId}")
    @Operation(summary = "Delete family profile")
    public ResponseEntity<Void> deleteFamilyProfile(@PathVariable String primaryUserId) {
        if (familyService.deleteFamilyProfile(primaryUserId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ==================== Family Members ====================

    @PostMapping("/{primaryUserId}/members")
    @Operation(summary = "Add family member")
    public ResponseEntity<FamilyProfile> addFamilyMember(
            @PathVariable String primaryUserId,
            @Valid @RequestBody FamilyProfile.FamilyMember member) {
        return ResponseEntity.ok(familyService.addFamilyMember(primaryUserId, member));
    }

    @GetMapping("/{primaryUserId}/members/{memberId}")
    @Operation(summary = "Get family member details")
    public ResponseEntity<FamilyProfile.FamilyMember> getFamilyMember(
            @PathVariable String primaryUserId,
            @PathVariable String memberId) {
        return familyService.getFamilyMember(primaryUserId, memberId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{primaryUserId}/members/{memberId}")
    @Operation(summary = "Update family member")
    public ResponseEntity<FamilyProfile> updateFamilyMember(
            @PathVariable String primaryUserId,
            @PathVariable String memberId,
            @Valid @RequestBody FamilyProfile.FamilyMember member) {
        return familyService.updateFamilyMember(primaryUserId, memberId, member)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{primaryUserId}/members/{memberId}")
    @Operation(summary = "Remove family member")
    public ResponseEntity<FamilyProfile> removeFamilyMember(
            @PathVariable String primaryUserId,
            @PathVariable String memberId) {
        return familyService.removeFamilyMember(primaryUserId, memberId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Member Permissions ====================

    @PutMapping("/{primaryUserId}/members/{memberId}/permissions")
    @Operation(summary = "Update member permissions")
    public ResponseEntity<FamilyProfile> updateMemberPermissions(
            @PathVariable String primaryUserId,
            @PathVariable String memberId,
            @RequestBody Map<String, Boolean> permissions) {
        return familyService.updateMemberPermissions(
                        primaryUserId,
                        memberId,
                        permissions.getOrDefault("canBookAppointments", true),
                        permissions.getOrDefault("canViewHistory", false),
                        permissions.getOrDefault("canMakePayments", false))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
