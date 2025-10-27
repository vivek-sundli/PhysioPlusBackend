package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for the Patient entity.
 * This will be managed by PatientMongoConfig.
 */
@Repository
public interface PatientRepository extends MongoRepository<Patient, String> {

    // Find a patient by their custom patientId
    Optional<Patient> findByPatientId(String patientId);

    // Find a patient by their email
    Optional<Patient> findByEmail(String email);
}
