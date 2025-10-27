package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Model.Bookings;
import com.healthclub.Physioplus.Service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * This is the Controller you mentioned.
 * It handles all incoming HTTP requests for the /api/bookings path.
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * POST /api/bookings
     * Creates a new booking.
     * @param booking The booking data from the request body.
     * @return The created booking.
     */
    @PostMapping
    public ResponseEntity<Bookings> createBooking(@RequestBody Bookings booking) {
        try {
            Bookings newBooking = bookingService.createBooking(booking);
            return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/bookings/{id}
     * Gets a single booking by its ID.
     * @param id The ID from the URL path.
     * @return The booking, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Bookings> getBookingById(@PathVariable("id") String id) {
        Optional<Bookings> booking = bookingService.getBookingById(id);

        // Use .map to transform the Optional<Booking> to ResponseEntity<Booking>
        return booking.map(b -> new ResponseEntity<>(b, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET /api/bookings/doctor/{doctorId}
     * Gets all bookings for a specific doctor.
     * @param doctorId The doctor's ID from the URL path.
     * @return A list of bookings.
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Bookings>> getBookingsByDoctor(@PathVariable("doctorId") String doctorId) {
        List<Bookings> bookings = bookingService.getBookingsForDoctor(doctorId);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * GET /api/bookings/patient/{patientId}
     * Gets all bookings for a specific patient.
     * @param patientId The patient's ID from the URL path.
     * @return A list of bookings.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Bookings>> getBookingsByPatient(@PathVariable("patientId") String patientId) {
        List<Bookings> bookings = bookingService.getBookingsForPatient(patientId);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * PUT /api/bookings/{id}/confirm
     * Confirms a booking.
     * @param id The ID of the booking to confirm.
     * @return The updated booking.
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Bookings> confirmBooking(@PathVariable("id") String id) {
        Bookings updatedBooking = bookingService.confirmBooking(id);
        if (updatedBooking != null) {
            return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * PUT /api/bookings/{id}/cancel
     * Cancels a booking.
     * @param id The ID of the booking to cancel.
     * @return The updated booking.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Bookings> cancelBooking(@PathVariable("id") String id) {
        Bookings updatedBooking = bookingService.cancelBooking(id);
        if (updatedBooking != null) {
            return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}