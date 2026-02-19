package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.HealthProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthProfileRepository extends MongoRepository<HealthProfile, String> {

    Optional<HealthProfile> findByPatientId(String patientId);
}
