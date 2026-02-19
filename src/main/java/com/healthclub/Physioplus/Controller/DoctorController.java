package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Dto.PageResponse;
import com.healthclub.Physioplus.Dto.TherapistView;
import com.healthclub.Physioplus.Model.Doctor;
import com.healthclub.Physioplus.Repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * POST /api/doctors/register
     * Register a new doctor (start onboarding)
     */
    @PostMapping("/register")
    public ResponseEntity<Doctor> registerDoctor(@RequestBody Doctor doctor) {
        doctor.setOnboardingStatus(Doctor.OnboardingStatus.PENDING);
        doctor.setActive(false);
        doctor.setCreatedAt(Instant.now());
        doctor.setUpdatedAt(Instant.now());
        Doctor saved = doctorRepository.save(doctor);
        return ResponseEntity.ok(saved);
    }

    /**
     * GET /api/doctors
     * Get all active doctors as TherapistView (for patients to browse).
     * Returns a flat list so the frontend can consume it directly.
     */
    @GetMapping
    public ResponseEntity<List<TherapistView>> getActiveDoctors() {
        List<TherapistView> therapists = doctorRepository.findByActive(true).stream()
                .map(TherapistView::from)
                .toList();
        return ResponseEntity.ok(therapists);
    }

    /**
     * GET /api/doctors/{id}
     * Get doctor by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable String id) {
        return doctorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/doctors/user/{userId}
     * Get doctor by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Doctor> getDoctorByUserId(@PathVariable String userId) {
        return doctorRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/doctors/{id}
     * Update doctor profile
     */
    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable String id, @RequestBody Doctor updatedDoctor) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(id);
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Doctor doctor = doctorOpt.get();

        // Update fields
        if (updatedDoctor.getName() != null) doctor.setName(updatedDoctor.getName());
        if (updatedDoctor.getSpecialization() != null) doctor.setSpecialization(updatedDoctor.getSpecialization());
        if (updatedDoctor.getQualification() != null) doctor.setQualification(updatedDoctor.getQualification());
        if (updatedDoctor.getExperienceYears() != null) doctor.setExperienceYears(updatedDoctor.getExperienceYears());
        if (updatedDoctor.getBio() != null) doctor.setBio(updatedDoctor.getBio());
        if (updatedDoctor.getProfileImageUrl() != null) doctor.setProfileImageUrl(updatedDoctor.getProfileImageUrl());
        if (updatedDoctor.getConsultationFee() != null) doctor.setConsultationFee(updatedDoctor.getConsultationFee());
        if (updatedDoctor.getSlotDurationMinutes() != null) doctor.setSlotDurationMinutes(updatedDoctor.getSlotDurationMinutes());
        if (updatedDoctor.getAvailableDays() != null) doctor.setAvailableDays(updatedDoctor.getAvailableDays());
        if (updatedDoctor.getClinicAddress() != null) doctor.setClinicAddress(updatedDoctor.getClinicAddress());
        if (updatedDoctor.getPhone() != null) doctor.setPhone(updatedDoctor.getPhone());

        doctor.setUpdatedAt(Instant.now());
        Doctor saved = doctorRepository.save(doctor);
        return ResponseEntity.ok(saved);
    }

    /**
     * PUT /api/doctors/{id}/documents
     * Upload documents for verification
     */
    @PutMapping("/{id}/documents")
    public ResponseEntity<Doctor> uploadDocuments(@PathVariable String id, @RequestBody Doctor documentUpdate) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(id);
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Doctor doctor = doctorOpt.get();

        // Update document URLs
        if (documentUpdate.getIdProofUrl() != null) doctor.setIdProofUrl(documentUpdate.getIdProofUrl());
        if (documentUpdate.getMedicalLicenseUrl() != null) doctor.setMedicalLicenseUrl(documentUpdate.getMedicalLicenseUrl());
        if (documentUpdate.getDegreeCertificateUrl() != null) doctor.setDegreeCertificateUrl(documentUpdate.getDegreeCertificateUrl());
        if (documentUpdate.getAdditionalDocuments() != null) doctor.setAdditionalDocuments(documentUpdate.getAdditionalDocuments());

        // Update status if all required documents are uploaded
        if (doctor.getIdProofUrl() != null && doctor.getMedicalLicenseUrl() != null && doctor.getDegreeCertificateUrl() != null) {
            doctor.setOnboardingStatus(Doctor.OnboardingStatus.DOCUMENTS_UPLOADED);
        }

        doctor.setUpdatedAt(Instant.now());
        Doctor saved = doctorRepository.save(doctor);
        return ResponseEntity.ok(saved);
    }

    /**
     * GET /api/doctors/{id}/status
     * Get onboarding status
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<?> getOnboardingStatus(@PathVariable String id) {
        return doctorRepository.findById(id)
                .map(doctor -> ResponseEntity.ok(java.util.Map.of(
                        "id", doctor.getId(),
                        "status", doctor.getOnboardingStatus(),
                        "active", doctor.isActive(),
                        "rejectionReason", doctor.getRejectionReason() != null ? doctor.getRejectionReason() : ""
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
