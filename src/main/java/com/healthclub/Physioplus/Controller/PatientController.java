package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Model.Patient;
import com.healthclub.Physioplus.Service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for handling Patient CRUD operations.
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * POST /api/patients : Creates a new patient record.
     */
    @PostMapping
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
        Patient newPatient = patientService.createPatient(patient);
        return ResponseEntity.ok(newPatient);
    }

    /**
     * GET /api/patients : Gets all patient records.
     */
    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    /**
     * GET /api/patients/{id} : Gets a patient by their unique database ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable String id) {
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/patients/by-patient-id/{patientId} : Gets a patient by their custom, human-readable patientId.
     */
    @GetMapping("/by-patient-id/{patientId}")
    public ResponseEntity<Patient> getPatientByPatientId(@PathVariable String patientId) {
        return patientService.getPatientByPatientId(patientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/patients/{id} : Updates an existing patient record.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable String id, @RequestBody Patient patient) {
        return patientService.updatePatient(id, patient)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/patients/{id} : Deletes a patient record.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable String id) {
        if (patientService.deletePatient(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
