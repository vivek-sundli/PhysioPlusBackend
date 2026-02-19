package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Dto.PrescriptionRequest;
import com.healthclub.Physioplus.Model.Prescription;
import com.healthclub.Physioplus.Service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /**
     * POST /api/prescriptions
     * Create a new prescription
     */
    @PostMapping
    public ResponseEntity<Prescription> createPrescription(@Valid @RequestBody PrescriptionRequest request) {
        Prescription prescription = prescriptionService.createPrescription(request);
        return ResponseEntity.ok(prescription);
    }

    /**
     * GET /api/prescriptions/{id}
     * Get prescription by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Prescription> getPrescriptionById(@PathVariable String id) {
        return prescriptionService.getPrescriptionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/prescriptions/booking/{bookingId}
     * Get prescription by Booking ID
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Prescription> getPrescriptionByBooking(@PathVariable String bookingId) {
        return prescriptionService.getPrescriptionByBookingId(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/prescriptions/patient/{patientId}
     * Get all prescriptions for a patient
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Prescription>> getPrescriptionsByPatient(@PathVariable String patientId) {
        List<Prescription> prescriptions = prescriptionService.getPrescriptionsByPatientId(patientId);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * GET /api/prescriptions/doctor/{doctorId}
     * Get all prescriptions created by a doctor
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Prescription>> getPrescriptionsByDoctor(@PathVariable String doctorId) {
        List<Prescription> prescriptions = prescriptionService.getPrescriptionsByDoctorId(doctorId);
        return ResponseEntity.ok(prescriptions);
    }
}
