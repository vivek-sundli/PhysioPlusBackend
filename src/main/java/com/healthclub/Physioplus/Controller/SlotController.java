package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Model.DoctorAvailability;
import com.healthclub.Physioplus.Model.DoctorSlot;
import com.healthclub.Physioplus.Service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/slots")
@Tag(name = "Slots", description = "Doctor availability and slot management APIs")
public class SlotController {

    private final SlotService slotService;

    @Autowired
    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    // ==================== Availability ====================

    @PostMapping("/availability")
    @Operation(summary = "Set doctor availability schedule")
    public ResponseEntity<DoctorAvailability> setAvailability(
            @Valid @RequestBody DoctorAvailability availability) {
        return ResponseEntity.ok(slotService.setAvailability(availability));
    }

    @GetMapping("/availability/{doctorId}")
    @Operation(summary = "Get doctor availability schedule")
    public ResponseEntity<DoctorAvailability> getAvailability(@PathVariable String doctorId) {
        return slotService.getAvailability(doctorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Slot Generation ====================

    @PostMapping("/generate/{doctorId}")
    @Operation(summary = "Generate slots for a specific date")
    public ResponseEntity<List<DoctorSlot>> generateSlots(
            @PathVariable String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotService.generateSlotsForDate(doctorId, date));
    }

    @PostMapping("/generate/{doctorId}/range")
    @Operation(summary = "Generate slots for a date range")
    public ResponseEntity<List<DoctorSlot>> generateSlotsForRange(
            @PathVariable String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(slotService.generateSlotsForDateRange(doctorId, startDate, endDate));
    }

    // ==================== Get Slots ====================

    @GetMapping("/{doctorId}/available")
    @Operation(summary = "Get available slots for a doctor on a specific date")
    public ResponseEntity<List<DoctorSlot>> getAvailableSlots(
            @PathVariable String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotService.getAvailableSlots(doctorId, date));
    }

    @GetMapping("/{doctorId}/range")
    @Operation(summary = "Get all slots for a doctor in a date range")
    public ResponseEntity<List<DoctorSlot>> getSlotsInRange(
            @PathVariable String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(slotService.getSlotsByDateRange(doctorId, startDate, endDate));
    }

    // ==================== Slot Operations ====================

    @PostMapping("/{slotId}/book")
    @Operation(summary = "Book a slot")
    public ResponseEntity<DoctorSlot> bookSlot(
            @PathVariable String slotId,
            @RequestBody Map<String, String> request) {
        return slotService.bookSlot(slotId, request.get("bookingId"), request.get("patientId"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{slotId}/cancel")
    @Operation(summary = "Cancel a booked slot")
    public ResponseEntity<DoctorSlot> cancelSlot(@PathVariable String slotId) {
        return slotService.cancelSlot(slotId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{slotId}/block")
    @Operation(summary = "Block a slot (make unavailable)")
    public ResponseEntity<DoctorSlot> blockSlot(
            @PathVariable String slotId,
            @RequestBody Map<String, String> request) {
        return slotService.blockSlot(slotId, request.get("reason"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{slotId}/complete")
    @Operation(summary = "Mark slot as completed")
    public ResponseEntity<DoctorSlot> completeSlot(@PathVariable String slotId) {
        return slotService.completeSlot(slotId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get slot by booking ID")
    public ResponseEntity<DoctorSlot> getSlotByBooking(@PathVariable String bookingId) {
        return slotService.getSlotByBookingId(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
