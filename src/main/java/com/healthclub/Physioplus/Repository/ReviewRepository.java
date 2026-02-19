package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    Page<Review> findByDoctorIdAndStatusOrderByCreatedAtDesc(
            String doctorId, Review.ReviewStatus status, Pageable pageable);

    Page<Review> findByPatientIdOrderByCreatedAtDesc(String patientId, Pageable pageable);

    Optional<Review> findByBookingId(String bookingId);

    Optional<Review> findByDoctorIdAndPatientId(String doctorId, String patientId);

    List<Review> findByStatus(Review.ReviewStatus status);

    long countByDoctorIdAndStatus(String doctorId, Review.ReviewStatus status);

    @Aggregation(pipeline = {
            "{ '$match': { 'doctorId': ?0, 'status': 'APPROVED' } }",
            "{ '$group': { '_id': null, 'avgRating': { '$avg': '$overallRating' } } }"
    })
    Double getAverageRatingByDoctorId(String doctorId);
}
