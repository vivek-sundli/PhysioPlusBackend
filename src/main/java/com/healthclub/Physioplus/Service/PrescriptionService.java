package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Dto.PrescriptionRequest;
import com.healthclub.Physioplus.Model.Prescription;
import com.healthclub.Physioplus.Repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    @Autowired
    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public Prescription createPrescription(PrescriptionRequest request) {
        // Check if prescription already exists for this booking
        if (prescriptionRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new IllegalArgumentException("Prescription already exists for this booking");
        }

        Prescription prescription = new Prescription();
        prescription.setBookingId(request.getBookingId());
        prescription.setDoctorId(request.getDoctorId());
        prescription.setPatientId(request.getPatientId());
        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setNotes(request.getNotes());
        
        if (request.getMedicines() != null) {
            List<Prescription.Medicine> medicines = request.getMedicines().stream()
                    .map(dto -> {
                        Prescription.Medicine medicine = new Prescription.Medicine();
                        medicine.setName(dto.getName());
                        medicine.setDosage(dto.getDosage());
                        medicine.setFrequency(dto.getFrequency());
                        medicine.setDuration(dto.getDuration());
                        medicine.setInstructions(dto.getInstructions());
                        return medicine;
                    })
                    .collect(Collectors.toList());
            prescription.setMedicines(medicines);
        } else {
            prescription.setMedicines(new ArrayList<>());
        }

        prescription.setLabTestsRecommended(request.getLabTestsRecommended());
        prescription.setCreatedAt(Instant.now());
        prescription.setUpdatedAt(Instant.now());

        return prescriptionRepository.save(prescription);
    }

    public Optional<Prescription> getPrescriptionById(String id) {
        return prescriptionRepository.findById(id);
    }

    public Optional<Prescription> getPrescriptionByBookingId(String bookingId) {
        return prescriptionRepository.findByBookingId(bookingId);
    }

    public List<Prescription> getPrescriptionsByPatientId(String patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    public List<Prescription> getPrescriptionsByDoctorId(String doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId);
    }
}
