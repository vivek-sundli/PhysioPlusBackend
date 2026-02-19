package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Model.FamilyProfile;
import com.healthclub.Physioplus.Repository.FamilyProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FamilyService {

    private static final Logger log = LoggerFactory.getLogger(FamilyService.class);

    private final FamilyProfileRepository familyProfileRepository;

    @Autowired
    public FamilyService(FamilyProfileRepository familyProfileRepository) {
        this.familyProfileRepository = familyProfileRepository;
    }

    // ==================== Family Profile ====================

    public FamilyProfile createFamilyProfile(String primaryUserId, String familyName) {
        // Check if family profile already exists
        Optional<FamilyProfile> existing = familyProfileRepository.findByPrimaryUserId(primaryUserId);
        if (existing.isPresent()) {
            log.warn("Family profile already exists for user {}", primaryUserId);
            return existing.get();
        }

        FamilyProfile profile = new FamilyProfile();
        profile.setPrimaryUserId(primaryUserId);
        profile.setFamilyName(familyName);
        profile.setMembers(new ArrayList<>());
        profile.setCreatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());

        log.info("Creating family profile for user {}", primaryUserId);
        return familyProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public Optional<FamilyProfile> getFamilyProfile(String primaryUserId) {
        return familyProfileRepository.findByPrimaryUserId(primaryUserId);
    }

    public Optional<FamilyProfile> updateFamilySettings(String primaryUserId,
                                                         boolean sharedMedicalHistory,
                                                         boolean sharedPaymentMethod) {
        return familyProfileRepository.findByPrimaryUserId(primaryUserId)
                .map(profile -> {
                    profile.setSharedMedicalHistory(sharedMedicalHistory);
                    profile.setSharedPaymentMethod(sharedPaymentMethod);
                    profile.setUpdatedAt(Instant.now());
                    log.info("Updated family settings for user {}", primaryUserId);
                    return familyProfileRepository.save(profile);
                });
    }

    // ==================== Family Members ====================

    public FamilyProfile addFamilyMember(String primaryUserId, FamilyProfile.FamilyMember member) {
        FamilyProfile profile = familyProfileRepository.findByPrimaryUserId(primaryUserId)
                .orElseThrow(() -> new RuntimeException("Family profile not found"));

        member.setId(UUID.randomUUID().toString());
        member.setAddedAt(Instant.now());

        // Set default permissions
        if (member.isCanBookAppointments() == false &&
            member.isCanViewHistory() == false &&
            member.isCanMakePayments() == false) {
            member.setCanBookAppointments(true);
            member.setCanViewHistory(false);
            member.setCanMakePayments(false);
        }

        profile.getMembers().add(member);
        profile.setUpdatedAt(Instant.now());

        log.info("Added family member {} to profile for user {}", member.getName(), primaryUserId);
        return familyProfileRepository.save(profile);
    }

    public Optional<FamilyProfile> updateFamilyMember(String primaryUserId, String memberId,
                                                       FamilyProfile.FamilyMember updated) {
        return familyProfileRepository.findByPrimaryUserId(primaryUserId)
                .map(profile -> {
                    profile.getMembers().stream()
                            .filter(m -> m.getId().equals(memberId))
                            .findFirst()
                            .ifPresent(member -> {
                                member.setName(updated.getName());
                                member.setRelation(updated.getRelation());
                                member.setDateOfBirth(updated.getDateOfBirth());
                                member.setGender(updated.getGender());
                                member.setPhone(updated.getPhone());
                                member.setEmail(updated.getEmail());
                                member.setBloodGroup(updated.getBloodGroup());
                                member.setAllergies(updated.getAllergies());
                                member.setConditions(updated.getConditions());
                                member.setCanBookAppointments(updated.isCanBookAppointments());
                                member.setCanViewHistory(updated.isCanViewHistory());
                                member.setCanMakePayments(updated.isCanMakePayments());
                            });
                    profile.setUpdatedAt(Instant.now());
                    log.info("Updated family member {} for user {}", memberId, primaryUserId);
                    return familyProfileRepository.save(profile);
                });
    }

    public Optional<FamilyProfile> removeFamilyMember(String primaryUserId, String memberId) {
        return familyProfileRepository.findByPrimaryUserId(primaryUserId)
                .map(profile -> {
                    profile.getMembers().removeIf(m -> m.getId().equals(memberId));
                    profile.setUpdatedAt(Instant.now());
                    log.info("Removed family member {} from profile for user {}", memberId, primaryUserId);
                    return familyProfileRepository.save(profile);
                });
    }

    @Transactional(readOnly = true)
    public Optional<FamilyProfile.FamilyMember> getFamilyMember(String primaryUserId, String memberId) {
        return familyProfileRepository.findByPrimaryUserId(primaryUserId)
                .flatMap(profile -> profile.getMembers().stream()
                        .filter(m -> m.getId().equals(memberId))
                        .findFirst());
    }

    // ==================== Member Permissions ====================

    public Optional<FamilyProfile> updateMemberPermissions(String primaryUserId, String memberId,
                                                            boolean canBook, boolean canViewHistory,
                                                            boolean canPay) {
        return familyProfileRepository.findByPrimaryUserId(primaryUserId)
                .map(profile -> {
                    profile.getMembers().stream()
                            .filter(m -> m.getId().equals(memberId))
                            .findFirst()
                            .ifPresent(member -> {
                                member.setCanBookAppointments(canBook);
                                member.setCanViewHistory(canViewHistory);
                                member.setCanMakePayments(canPay);
                            });
                    profile.setUpdatedAt(Instant.now());
                    log.info("Updated permissions for family member {} of user {}", memberId, primaryUserId);
                    return familyProfileRepository.save(profile);
                });
    }

    public boolean deleteFamilyProfile(String primaryUserId) {
        return familyProfileRepository.findByPrimaryUserId(primaryUserId)
                .map(profile -> {
                    familyProfileRepository.delete(profile);
                    log.info("Deleted family profile for user {}", primaryUserId);
                    return true;
                })
                .orElse(false);
    }
}
