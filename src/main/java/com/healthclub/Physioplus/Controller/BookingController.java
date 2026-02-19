package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Dto.PageResponse;
import com.healthclub.Physioplus.Model.Bookings;
import com.healthclub.Physioplus.Service.BookingService;
import jakarta.validation.Valid;
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
    @PostMapping("/create")
    public ResponseEntity<Bookings> createBooking(@Valid @RequestBody Bookings booking) {
        Bookings newBooking = bookingService.createBooking(booking);
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
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
     * Gets all bookings for a specific doctor with optional pagination.
     * @param doctorId The doctor's ID from the URL path.
     * @param page Page number (0-indexed), defaults to 0.
     * @param size Page size, defaults to 20.
     * @return A paginated list of bookings.
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<PageResponse<Bookings>> getBookingsByDoctor(
            @PathVariable("doctorId") String doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Bookings> bookings = bookingService.getBookingsForDoctor(doctorId, page, size);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * GET /api/bookings/patient/{patientId}
     * Gets all bookings for a specific patient with optional pagination.
     * @param patientId The patient's ID from the URL path.
     * @param page Page number (0-indexed), defaults to 0.
     * @param size Page size, defaults to 20.
     * @return A paginated list of bookings.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<PageResponse<Bookings>> getBookingsByPatient(
            @PathVariable("patientId") String patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Bookings> bookings = bookingService.getBookingsForPatient(patientId, page, size);
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