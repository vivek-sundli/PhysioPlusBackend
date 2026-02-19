package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.Bookings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This is the Data Access Layer.
 * By extending MongoRepository, Spring Data automatically creates implementations
 * for standard CRUD operations (save, findById, findAll, deleteById, etc.).
 *
 * We can also add custom query methods just by defining their signature.
 */
@Repository
public interface BookingRepository extends MongoRepository<Bookings, String> {

    // Non-paginated versions
    List<Bookings> findByDoctorId(String doctorId);
    List<Bookings> findByPatientId(String patientId);

    // Paginated versions
    Page<Bookings> findByDoctorId(String doctorId, Pageable pageable);
    Page<Bookings> findByPatientId(String patientId, Pageable pageable);

    // Find bookings for a specific doctor at a specific time
    List<Bookings> findByDoctorIdAndAppointmentTime(String doctorId, java.time.LocalDateTime appointmentTime);
}