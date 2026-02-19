package com.healthclub.Physioplus.Dto;

import com.healthclub.Physioplus.Model.Doctor;

import java.util.List;

/**
 * DTO that maps Doctor → frontend Therapist interface.
 * Frontend expects: id, name, title, avatar, rating, reviews, price, specialties, formats, location, slots
 */
public record TherapistView(
        String id,
        String name,
        String title,
        String avatar,
        double rating,
        int reviews,
        double price,
        List<String> specialties,
        List<String> formats,
        String location,
        List<String> slots
) {
    public static TherapistView from(Doctor doctor) {
        // Resolve avatar: prefer avatar field, fall back to profileImageUrl
        String resolvedAvatar = doctor.getAvatar() != null ? doctor.getAvatar() : doctor.getProfileImageUrl();

        // Resolve specialties: prefer specializations list, fall back to single specialization
        List<String> resolvedSpecialties = doctor.getSpecializations() != null && !doctor.getSpecializations().isEmpty()
                ? doctor.getSpecializations()
                : (doctor.getSpecialization() != null ? List.of(doctor.getSpecialization()) : List.of());

        // Resolve location: prefer location field, fall back to clinicAddress
        String resolvedLocation = doctor.getLocation() != null ? doctor.getLocation() : doctor.getClinicAddress();

        // Resolve slots: prefer availableSlots, fall back to availableDays
        List<String> resolvedSlots = doctor.getAvailableSlots() != null && !doctor.getAvailableSlots().isEmpty()
                ? doctor.getAvailableSlots()
                : (doctor.getAvailableDays() != null ? doctor.getAvailableDays() : List.of());

        return new TherapistView(
                doctor.getId(),
                doctor.getName(),
                doctor.getTitle() != null ? doctor.getTitle() : doctor.getQualification(),
                resolvedAvatar,
                doctor.getRating() != null ? doctor.getRating() : 0.0,
                doctor.getTotalRatings() != null ? doctor.getTotalRatings() : 0,
                doctor.getConsultationFee() != null ? doctor.getConsultationFee() : 0.0,
                resolvedSpecialties,
                doctor.getFormats() != null ? doctor.getFormats() : List.of(),
                resolvedLocation != null ? resolvedLocation : "",
                resolvedSlots
        );
    }
}
