package com.healthclub.Physioplus.config;

import com.healthclub.Physioplus.Model.Doctor;
import com.healthclub.Physioplus.Repository.DoctorRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Seeds 4 doctors matching the frontend INITIAL_THERAPISTS (t1-t4) if the collection is empty.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private final DoctorRepository doctorRepository;

    public DataSeeder(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (doctorRepository.count() > 0) {
            return;
        }

        Doctor d1 = buildDoctor(
                "Dr. Ananya Rao",
                "PT, MPT (Ortho)",
                "https://images.unsplash.com/photo-1607746882042-944635dfe10e?q=80&w=600&auto=format&fit=crop",
                4.9, 214, 799,
                List.of("Back Pain", "Posture", "Sports Rehab"),
                List.of("Online", "Clinic"),
                "Hitech City, Hyderabad",
                List.of("09:00", "10:30", "14:00", "18:30"),
                "ananya.rao@physiohive.com"
        );

        Doctor d2 = buildDoctor(
                "Dr. Kabir Singh",
                "PT, COMT",
                "https://images.unsplash.com/photo-1544723795-3fb6469f5b39?q=80&w=600&auto=format&fit=crop",
                4.7, 142, 699,
                List.of("Neck Pain", "Ergonomics", "Manual Therapy"),
                List.of("Online"),
                "Remote (India)",
                List.of("08:00", "11:00", "16:00"),
                "kabir.singh@physiohive.com"
        );

        Doctor d3 = buildDoctor(
                "Dr. Meera Iyer",
                "PT, Women's Health",
                "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=600&auto=format&fit=crop",
                4.8, 176, 899,
                List.of("Women's Health", "Pelvic Floor", "Prenatal"),
                List.of("Clinic"),
                "Kondapur, Hyderabad",
                List.of("10:00", "12:30", "17:30"),
                "meera.iyer@physiohive.com"
        );

        Doctor d4 = buildDoctor(
                "Dr. Aarav Patel",
                "Sports Physiotherapist",
                "https://images.unsplash.com/photo-1556157382-97eda2d62296?q=80&w=600&auto=format&fit=crop",
                4.6, 98, 649,
                List.of("Knee", "Ankle", "Return to Sport"),
                List.of("Online", "Home Visit"),
                "Madhapur, Hyderabad",
                List.of("07:30", "13:00", "19:00"),
                "aarav.patel@physiohive.com"
        );

        doctorRepository.saveAll(List.of(d1, d2, d3, d4));
        System.out.println("[DataSeeder] Seeded 4 doctors into Doctors collection.");
    }

    private Doctor buildDoctor(String name, String title, String avatar,
                               double rating, int reviews, double fee,
                               List<String> specializations, List<String> formats,
                               String location, List<String> slots, String email) {
        Doctor d = new Doctor();
        d.setName(name);
        d.setTitle(title);
        d.setAvatar(avatar);
        d.setRating(rating);
        d.setTotalRatings(reviews);
        d.setConsultationFee(fee);
        d.setSpecializations(specializations);
        d.setFormats(formats);
        d.setLocation(location);
        d.setAvailableSlots(slots);
        d.setOnboardingStatus(Doctor.OnboardingStatus.APPROVED);
        d.setActive(true);
        d.setEmail(email);
        d.setCreatedAt(Instant.now());
        d.setUpdatedAt(Instant.now());
        return d;
    }
}
