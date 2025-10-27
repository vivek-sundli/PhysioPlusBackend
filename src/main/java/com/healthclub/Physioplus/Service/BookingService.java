package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Dto.BookingStatus;
import com.healthclub.Physioplus.Model.Bookings;
import com.healthclub.Physioplus.Repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * This is the Service Layer, which contains the business logic.
 * It coordinates between the Controller and the Repository.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    // @Autowired constructor injection is the recommended way to inject dependencies
    @Autowired
    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Creates a new booking.
     * @param booking The booking object from the request.
     * @return The saved booking with its new ID.
     */
    public Bookings createBooking(Bookings booking) {
        // You could add validation logic here, e.g., check if the slot is available
        // For now, just set status and save.
        booking.setStatus(BookingStatus.REQUESTED);
        return bookingRepository.save(booking);
    }

    /**
     * Finds a booking by its unique ID.
     * @param id The MongoDB ObjectId string.
     * @return An Optional containing the booking if found.
     */
    public Optional<Bookings> getBookingById(String id) {
        return bookingRepository.findById(id);
    }

    /**
     * Finds all bookings for a specific doctor.
     * @param doctorId The ID of the doctor.
     * @return A list of bookings.
     */
    public List<Bookings> getBookingsForDoctor(String doctorId) {
        return bookingRepository.findByDoctorId(doctorId);
    }

    /**
     * Finds all bookings for a specific patient.
     * @param patientId The ID of the patient.
     * @return A list of bookings.
     */
    public List<Bookings> getBookingsForPatient(String patientId) {
        return bookingRepository.findByPatientId(patientId);
    }

    /**
     * Updates a booking's status to CONFIRMED.
     * @param id The ID of the booking to confirm.
     * @return The updated booking, or null if not found.
     */
    public Bookings confirmBooking(String id) {
        Optional<Bookings> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Bookings booking = bookingOpt.get();
            booking.setStatus(BookingStatus.CONFIRMED);
            return bookingRepository.save(booking);
        }
        return null; // Or throw an exception
    }

    /**
     * Updates a booking's status to CANCELLED.
     * @param id The ID of the booking to cancel.
     * @return The updated booking, or null if not found.
     */
    public Bookings cancelBooking(String id) {
        Optional<Bookings> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Bookings booking = bookingOpt.get();
            booking.setStatus(BookingStatus.CANCELLED);
            return bookingRepository.save(booking);
        }
        return null; // Or throw an exception
    }
}