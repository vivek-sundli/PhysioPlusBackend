package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Model.Patient;
import com.healthclub.Physioplus.Repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service class for handling business logic for Patients.
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * Creates a new patient record.
     */
    public Patient createPatient(Patient patient) {
        // Set audit fields
        patient.setCreatedAt(Instant.now());
        patient.setUpdatedAt(Instant.now());
        // You might add logic here to generate a unique patientId if not provided
        return patientRepository.save(patient);
    }

    /**
     * Retrieves all patients.
     */
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    /**
     * Retrieves a patient by their main database ID.
     */
    public Optional<Patient> getPatientById(String id) {
        return patientRepository.findById(id);
    }

    /**
     * Retrieves a patient by their custom patientId.
     */
    public Optional<Patient> getPatientByPatientId(String patientId) {
        return patientRepository.findByPatientId(patientId);
    }

    /**
     * Updates an existing patient record.
     */
    public Optional<Patient> updatePatient(String id, Patient updatedPatient) {
        return patientRepository.findById(id)
                .map(existingPatient -> {
                    // Update all mutable fields
                    existingPatient.setFirstName(updatedPatient.getFirstName());
                    existingPatient.setLastName(updatedPatient.getLastName());
                    existingPatient.setEmail(updatedPatient.getEmail());
                    existingPatient.setContactNumber(updatedPatient.getContactNumber());
                    existingPatient.setDateOfBirth(updatedPatient.getDateOfBirth());
                    existingPatient.setAddress(updatedPatient.getAddress());
                    existingPatient.setMedicalHistory(updatedPatient.getMedicalHistory());
                    existingPatient.setCurrentMedications(updatedPatient.getCurrentMedications());
                    existingPatient.setUpdatedAt(Instant.now()); // Update timestamp
                    return patientRepository.save(existingPatient);
                });
    }

    /**
     * Deletes a patient by their ID.
     */
    public boolean deletePatient(String id) {
        if (patientRepository.existsById(id)) {
            patientRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
