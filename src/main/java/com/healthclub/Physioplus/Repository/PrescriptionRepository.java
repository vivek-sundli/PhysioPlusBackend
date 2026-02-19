package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {
    
    Optional<Prescription> findByBookingId(String bookingId);
    
    List<Prescription> findByPatientId(String patientId);
    
    List<Prescription> findByDoctorId(String doctorId);
}
