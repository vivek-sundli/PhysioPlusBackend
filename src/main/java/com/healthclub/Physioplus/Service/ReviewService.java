package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Dto.PageResponse;
import com.healthclub.Physioplus.Model.Review;
import com.healthclub.Physioplus.Repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // ==================== Create & Update ====================

    public Review createReview(Review review) {
        // Check if user already reviewed this doctor
        Optional<Review> existing = reviewRepository.findByDoctorIdAndPatientId(
                review.getDoctorId(), review.getPatientId());
        if (existing.isPresent()) {
            throw new RuntimeException("You have already reviewed this doctor");
        }

        review.setStatus(Review.ReviewStatus.PENDING);
        review.setHelpfulCount(0);
        review.setHelpfulByUserIds(new ArrayList<>());
        review.setCreatedAt(Instant.now());
        review.setUpdatedAt(Instant.now());

        log.info("Creating review for doctor {} by patient {}", review.getDoctorId(), review.getPatientId());
        return reviewRepository.save(review);
    }

    public Optional<Review> updateReview(String reviewId, String patientId, Review updated) {
        return reviewRepository.findById(reviewId)
                .filter(review -> review.getPatientId().equals(patientId))
                .filter(review -> review.getStatus() == Review.ReviewStatus.PENDING ||
                                  review.getStatus() == Review.ReviewStatus.APPROVED)
                .map(review -> {
                    review.setOverallRating(updated.getOverallRating());
                    review.setPunctualityRating(updated.getPunctualityRating());
                    review.setCommunicationRating(updated.getCommunicationRating());
                    review.setTreatmentRating(updated.getTreatmentRating());
                    review.setClinicRating(updated.getClinicRating());
                    review.setTitle(updated.getTitle());
                    review.setContent(updated.getContent());
                    review.setTags(updated.getTags());
                    review.setStatus(Review.ReviewStatus.PENDING); // Re-review after edit
                    review.setUpdatedAt(Instant.now());
                    log.info("Updated review {}", reviewId);
                    return reviewRepository.save(review);
                });
    }

    // ==================== Read ====================

    @Transactional(readOnly = true)
    public Optional<Review> getReviewById(String reviewId) {
        return reviewRepository.findById(reviewId);
    }

    @Transactional(readOnly = true)
    public Optional<Review> getReviewByBooking(String bookingId) {
        return reviewRepository.findByBookingId(bookingId);
    }

    @Transactional(readOnly = true)
    public PageResponse<Review> getReviewsForDoctor(String doctorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByDoctorIdAndStatusOrderByCreatedAtDesc(
                doctorId, Review.ReviewStatus.APPROVED, pageable);
        return PageResponse.of(reviews);
    }

    @Transactional(readOnly = true)
    public PageResponse<Review> getReviewsByPatient(String patientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable);
        return PageResponse.of(reviews);
    }

    @Transactional(readOnly = true)
    public List<Review> getPendingReviews() {
        return reviewRepository.findByStatus(Review.ReviewStatus.PENDING);
    }

    // ==================== Doctor Response ====================

    public Optional<Review> addDoctorResponse(String reviewId, String doctorId, String response) {
        return reviewRepository.findById(reviewId)
                .filter(review -> review.getDoctorId().equals(doctorId))
                .map(review -> {
                    review.setDoctorResponse(response);
                    review.setDoctorResponseAt(Instant.now());
                    review.setUpdatedAt(Instant.now());
                    log.info("Doctor {} responded to review {}", doctorId, reviewId);
                    return reviewRepository.save(review);
                });
    }

    // ==================== Moderation ====================

    public Optional<Review> approveReview(String reviewId, String adminId) {
        return reviewRepository.findById(reviewId)
                .map(review -> {
                    review.setStatus(Review.ReviewStatus.APPROVED);
                    review.setModeratedAt(Instant.now());
                    review.setUpdatedAt(Instant.now());
                    log.info("Review {} approved by {}", reviewId, adminId);
                    return reviewRepository.save(review);
                });
    }

    public Optional<Review> rejectReview(String reviewId, String adminId, String reason) {
        return reviewRepository.findById(reviewId)
                .map(review -> {
                    review.setStatus(Review.ReviewStatus.REJECTED);
                    review.setModerationNotes(reason);
                    review.setModeratedAt(Instant.now());
                    review.setUpdatedAt(Instant.now());
                    log.info("Review {} rejected by {}: {}", reviewId, adminId, reason);
                    return reviewRepository.save(review);
                });
    }

    public Optional<Review> flagReview(String reviewId, String reason) {
        return reviewRepository.findById(reviewId)
                .map(review -> {
                    review.setStatus(Review.ReviewStatus.FLAGGED);
                    review.setModerationNotes(reason);
                    review.setUpdatedAt(Instant.now());
                    log.info("Review {} flagged: {}", reviewId, reason);
                    return reviewRepository.save(review);
                });
    }

    // ==================== Helpfulness ====================

    public Optional<Review> markAsHelpful(String reviewId, String userId) {
        return reviewRepository.findById(reviewId)
                .filter(review -> review.getStatus() == Review.ReviewStatus.APPROVED)
                .map(review -> {
                    if (review.getHelpfulByUserIds() == null) {
                        review.setHelpfulByUserIds(new ArrayList<>());
                    }
                    if (!review.getHelpfulByUserIds().contains(userId)) {
                        review.getHelpfulByUserIds().add(userId);
                        review.setHelpfulCount(review.getHelpfulCount() + 1);
                        review.setUpdatedAt(Instant.now());
                        log.info("Review {} marked helpful by {}", reviewId, userId);
                        return reviewRepository.save(review);
                    }
                    return review;
                });
    }

    // ==================== Statistics ====================

    @Transactional(readOnly = true)
    public Double getAverageRating(String doctorId) {
        Double avg = reviewRepository.getAverageRatingByDoctorId(doctorId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    @Transactional(readOnly = true)
    public long getReviewCount(String doctorId) {
        return reviewRepository.countByDoctorIdAndStatus(doctorId, Review.ReviewStatus.APPROVED);
    }

    public boolean deleteReview(String reviewId, String patientId) {
        return reviewRepository.findById(reviewId)
                .filter(review -> review.getPatientId().equals(patientId))
                .map(review -> {
                    reviewRepository.delete(review);
                    log.info("Review {} deleted by patient {}", reviewId, patientId);
                    return true;
                })
                .orElse(false);
    }
}
