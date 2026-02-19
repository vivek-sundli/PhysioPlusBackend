package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Model.DoctorAvailability;
import com.healthclub.Physioplus.Model.DoctorSlot;
import com.healthclub.Physioplus.Repository.DoctorAvailabilityRepository;
import com.healthclub.Physioplus.Repository.DoctorSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SlotService {

    private static final Logger log = LoggerFactory.getLogger(SlotService.class);

    private final DoctorSlotRepository slotRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    public SlotService(DoctorSlotRepository slotRepository,
                       DoctorAvailabilityRepository availabilityRepository) {
        this.slotRepository = slotRepository;
        this.availabilityRepository = availabilityRepository;
    }

    // ==================== Availability Management ====================

    public DoctorAvailability setAvailability(DoctorAvailability availability) {
        availability.setUpdatedAt(Instant.now());
        log.info("Setting availability for doctor: {}", availability.getDoctorId());
        return availabilityRepository.save(availability);
    }

    @Transactional(readOnly = true)
    public Optional<DoctorAvailability> getAvailability(String doctorId) {
        return availabilityRepository.findByDoctorId(doctorId);
    }

    // ==================== Slot Generation ====================

    public List<DoctorSlot> generateSlotsForDate(String doctorId, LocalDate date) {
        Optional<DoctorAvailability> availabilityOpt = availabilityRepository.findByDoctorId(doctorId);
        if (availabilityOpt.isEmpty()) {
            log.warn("No availability configured for doctor: {}", doctorId);
            return List.of();
        }

        DoctorAvailability availability = availabilityOpt.get();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // Get time ranges for the day
        List<DoctorAvailability.TimeRange> timeRanges = availability.getWeeklySchedule().get(dayOfWeek);
        if (timeRanges == null || timeRanges.isEmpty()) {
            log.debug("Doctor {} not available on {}", doctorId, dayOfWeek);
            return List.of();
        }

        List<DoctorSlot> slots = new ArrayList<>();
        int slotDuration = availability.getSlotDurationMinutes();
        int bufferTime = availability.getBufferMinutes();

        for (DoctorAvailability.TimeRange timeRange : timeRanges) {
            LocalTime current = timeRange.getStartTime();
            LocalTime end = timeRange.getEndTime();

            while (current.plusMinutes(slotDuration).compareTo(end) <= 0) {
                DoctorSlot slot = new DoctorSlot();
                slot.setDoctorId(doctorId);
                slot.setDate(date);
                slot.setStartTime(current);
                slot.setEndTime(current.plusMinutes(slotDuration));
                slot.setDurationMinutes(slotDuration);
                slot.setStatus(DoctorSlot.SlotStatus.AVAILABLE);
                slot.setCreatedAt(Instant.now());

                slots.add(slotRepository.save(slot));
                current = current.plusMinutes(slotDuration + bufferTime);
            }
        }

        log.info("Generated {} slots for doctor {} on {}", slots.size(), doctorId, date);
        return slots;
    }

    public List<DoctorSlot> generateSlotsForDateRange(String doctorId, LocalDate startDate, LocalDate endDate) {
        List<DoctorSlot> allSlots = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            allSlots.addAll(generateSlotsForDate(doctorId, current));
            current = current.plusDays(1);
        }
        return allSlots;
    }

    // ==================== Slot Operations ====================

    @Transactional(readOnly = true)
    public List<DoctorSlot> getAvailableSlots(String doctorId, LocalDate date) {
        return slotRepository.findByDoctorIdAndDateAndStatus(doctorId, date, DoctorSlot.SlotStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public List<DoctorSlot> getSlotsByDateRange(String doctorId, LocalDate startDate, LocalDate endDate) {
        return slotRepository.findByDoctorIdAndDateBetween(doctorId, startDate, endDate);
    }

    public Optional<DoctorSlot> bookSlot(String slotId, String bookingId, String patientId) {
        return slotRepository.findById(slotId)
                .filter(slot -> slot.getStatus() == DoctorSlot.SlotStatus.AVAILABLE)
                .map(slot -> {
                    slot.setStatus(DoctorSlot.SlotStatus.BOOKED);
                    slot.setBookingId(bookingId);
                    slot.setPatientId(patientId);
                    slot.setUpdatedAt(Instant.now());
                    log.info("Slot {} booked for patient {}", slotId, patientId);
                    return slotRepository.save(slot);
                });
    }

    public Optional<DoctorSlot> cancelSlot(String slotId) {
        return slotRepository.findById(slotId)
                .filter(slot -> slot.getStatus() == DoctorSlot.SlotStatus.BOOKED)
                .map(slot -> {
                    slot.setStatus(DoctorSlot.SlotStatus.CANCELLED);
                    slot.setUpdatedAt(Instant.now());
                    log.info("Slot {} cancelled", slotId);
                    return slotRepository.save(slot);
                });
    }

    public Optional<DoctorSlot> blockSlot(String slotId, String reason) {
        return slotRepository.findById(slotId)
                .filter(slot -> slot.getStatus() == DoctorSlot.SlotStatus.AVAILABLE)
                .map(slot -> {
                    slot.setStatus(DoctorSlot.SlotStatus.BLOCKED);
                    slot.setUpdatedAt(Instant.now());
                    log.info("Slot {} blocked: {}", slotId, reason);
                    return slotRepository.save(slot);
                });
    }

    public Optional<DoctorSlot> completeSlot(String slotId) {
        return slotRepository.findById(slotId)
                .filter(slot -> slot.getStatus() == DoctorSlot.SlotStatus.BOOKED)
                .map(slot -> {
                    slot.setStatus(DoctorSlot.SlotStatus.COMPLETED);
                    slot.setUpdatedAt(Instant.now());
                    log.info("Slot {} marked completed", slotId);
                    return slotRepository.save(slot);
                });
    }

    @Transactional(readOnly = true)
    public Optional<DoctorSlot> getSlotByBookingId(String bookingId) {
        return slotRepository.findByBookingId(bookingId);
    }
}
