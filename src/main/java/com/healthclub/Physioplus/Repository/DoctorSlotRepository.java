package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.DoctorSlot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorSlotRepository extends MongoRepository<DoctorSlot, String> {

    List<DoctorSlot> findByDoctorIdAndDate(String doctorId, LocalDate date);

    List<DoctorSlot> findByDoctorIdAndDateBetween(String doctorId, LocalDate startDate, LocalDate endDate);

    List<DoctorSlot> findByDoctorIdAndStatus(String doctorId, DoctorSlot.SlotStatus status);

    List<DoctorSlot> findByDoctorIdAndDateAndStatus(String doctorId, LocalDate date, DoctorSlot.SlotStatus status);

    Optional<DoctorSlot> findByBookingId(String bookingId);

    List<DoctorSlot> findByDoctorIdAndDateGreaterThanEqualAndStatus(
            String doctorId, LocalDate date, DoctorSlot.SlotStatus status);
}
