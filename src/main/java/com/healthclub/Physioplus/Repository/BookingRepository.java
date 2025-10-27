package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.Bookings;
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

    // Spring Data MongoDB will automatically implement this method
    // "Find all Bookings where the doctorId field matches the given doctorId"
    List<Bookings> findByDoctorId(String doctorId);

    // "Find all Bookings where the patientId field matches the given patientId"
    List<Bookings> findByPatientId(String patientId);

    // Example of a more complex query
    // "Find all Bookings for a specific doctor at a specific time"
    List<Bookings> findByDoctorIdAndAppointmentTime(String doctorId, java.time.LocalDateTime appointmentTime);
}