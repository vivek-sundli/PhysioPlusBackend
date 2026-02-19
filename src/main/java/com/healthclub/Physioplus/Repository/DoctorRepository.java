package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends MongoRepository<Doctor, String> {

    Optional<Doctor> findByUserId(String userId);

    Optional<Doctor> findByEmail(String email);

    List<Doctor> findByOnboardingStatus(Doctor.OnboardingStatus status);

    Page<Doctor> findByOnboardingStatus(Doctor.OnboardingStatus status, Pageable pageable);

    List<Doctor> findByActive(boolean active);

    Page<Doctor> findByActive(boolean active, Pageable pageable);

    List<Doctor> findBySpecialization(String specialization);

    long countByOnboardingStatus(Doctor.OnboardingStatus status);

    long countByActive(boolean active);
}
