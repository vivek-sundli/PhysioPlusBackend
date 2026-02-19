package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Dto.PageResponse;
import com.healthclub.Physioplus.Model.HealthProfile;
import com.healthclub.Physioplus.Model.PainDiary;
import com.healthclub.Physioplus.Repository.HealthProfileRepository;
import com.healthclub.Physioplus.Repository.PainDiaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HealthProfileService {

    private static final Logger log = LoggerFactory.getLogger(HealthProfileService.class);

    private final HealthProfileRepository healthProfileRepository;
    private final PainDiaryRepository painDiaryRepository;

    @Autowired
    public HealthProfileService(HealthProfileRepository healthProfileRepository,
                                PainDiaryRepository painDiaryRepository) {
        this.healthProfileRepository = healthProfileRepository;
        this.painDiaryRepository = painDiaryRepository;
    }

    // ==================== Health Profile ====================

    public HealthProfile createProfile(HealthProfile profile) {
        profile.setCreatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());
        log.info("Creating health profile for patient: {}", profile.getPatientId());
        return healthProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public Optional<HealthProfile> getProfileByPatientId(String patientId) {
        return healthProfileRepository.findByPatientId(patientId);
    }

    public Optional<HealthProfile> updateProfile(String patientId, HealthProfile updatedProfile) {
        return healthProfileRepository.findByPatientId(patientId)
                .map(existing -> {
                    // Update basic info
                    existing.setBloodGroup(updatedProfile.getBloodGroup());
                    existing.setHeight(updatedProfile.getHeight());
                    existing.setWeight(updatedProfile.getWeight());
                    existing.setGender(updatedProfile.getGender());
                    existing.setDateOfBirth(updatedProfile.getDateOfBirth());

                    // Update medical info
                    existing.setAllergies(updatedProfile.getAllergies());
                    existing.setChronicConditions(updatedProfile.getChronicConditions());
                    existing.setCurrentMedications(updatedProfile.getCurrentMedications());
                    existing.setPastSurgeries(updatedProfile.getPastSurgeries());
                    existing.setFamilyHistory(updatedProfile.getFamilyHistory());

                    // Update physiotherapy info
                    existing.setPrimaryComplaint(updatedProfile.getPrimaryComplaint());
                    existing.setInjuryType(updatedProfile.getInjuryType());
                    existing.setAffectedArea(updatedProfile.getAffectedArea());

                    // Update lifestyle
                    existing.setOccupation(updatedProfile.getOccupation());
                    existing.setActivityLevel(updatedProfile.getActivityLevel());
                    existing.setSmoker(updatedProfile.isSmoker());
                    existing.setAlcohol(updatedProfile.isAlcohol());
                    existing.setSleepHours(updatedProfile.getSleepHours());

                    // Update emergency contact
                    existing.setEmergencyContactName(updatedProfile.getEmergencyContactName());
                    existing.setEmergencyContactPhone(updatedProfile.getEmergencyContactPhone());
                    existing.setEmergencyContactRelation(updatedProfile.getEmergencyContactRelation());

                    // Update insurance
                    existing.setInsuranceProvider(updatedProfile.getInsuranceProvider());
                    existing.setInsurancePolicyNumber(updatedProfile.getInsurancePolicyNumber());
                    existing.setInsuranceExpiryDate(updatedProfile.getInsuranceExpiryDate());

                    existing.setUpdatedAt(Instant.now());
                    log.info("Updated health profile for patient: {}", patientId);
                    return healthProfileRepository.save(existing);
                });
    }

    public HealthProfile addMedication(String patientId, HealthProfile.Medication medication) {
        HealthProfile profile = healthProfileRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Health profile not found"));

        profile.getCurrentMedications().add(medication);
        profile.setUpdatedAt(Instant.now());

        return healthProfileRepository.save(profile);
    }

    public HealthProfile addAllergy(String patientId, String allergy) {
        HealthProfile profile = healthProfileRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Health profile not found"));

        profile.getAllergies().add(allergy);
        profile.setUpdatedAt(Instant.now());

        return healthProfileRepository.save(profile);
    }

    // ==================== Pain Diary ====================

    public PainDiary addPainEntry(PainDiary entry) {
        entry.setCreatedAt(Instant.now());
        log.info("Adding pain diary entry for patient: {}", entry.getPatientId());
        return painDiaryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public PageResponse<PainDiary> getPainDiaryEntries(String patientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PainDiary> entries = painDiaryRepository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable);
        return PageResponse.of(entries);
    }

    @Transactional(readOnly = true)
    public List<PainDiary> getPainDiaryEntriesByDateRange(String patientId, Instant start, Instant end) {
        return painDiaryRepository.findByPatientIdAndCreatedAtBetween(patientId, start, end);
    }

    @Transactional(readOnly = true)
    public List<PainDiary> getRecentPainEntries(String patientId, int days) {
        Instant start = Instant.now().minus(days, ChronoUnit.DAYS);
        return painDiaryRepository.findByPatientIdAndCreatedAtBetween(patientId, start, Instant.now());
    }

    public Optional<PainDiary> updatePainEntry(String entryId, PainDiary updated) {
        return painDiaryRepository.findById(entryId)
                .map(existing -> {
                    existing.setPainLevel(updated.getPainLevel());
                    existing.setAffectedAreas(updated.getAffectedAreas());
                    existing.setPainType(updated.getPainType());
                    existing.setTrigger(updated.getTrigger());
                    existing.setReliefMethods(updated.getReliefMethods());
                    existing.setMedicationTaken(updated.getMedicationTaken());
                    existing.setExercisesDone(updated.getExercisesDone());
                    existing.setMood(updated.getMood());
                    existing.setSleepQuality(updated.getSleepQuality());
                    existing.setNotes(updated.getNotes());
                    return painDiaryRepository.save(existing);
                });
    }

    public boolean deletePainEntry(String entryId) {
        if (painDiaryRepository.existsById(entryId)) {
            painDiaryRepository.deleteById(entryId);
            return true;
        }
        return false;
    }

    // ==================== Analytics ====================

    @Transactional(readOnly = true)
    public Double getAveragePainLevel(String patientId, int days) {
        List<PainDiary> entries = getRecentPainEntries(patientId, days);
        return entries.stream()
                .mapToInt(PainDiary::getPainLevel)
                .average()
                .orElse(0.0);
    }
}
