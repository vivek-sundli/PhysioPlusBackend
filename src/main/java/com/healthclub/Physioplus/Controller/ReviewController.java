package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Dto.PageResponse;
import com.healthclub.Physioplus.Model.Review;
import com.healthclub.Physioplus.Service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Doctor review and rating APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ==================== Create & Update ====================

    @PostMapping
    @Operation(summary = "Create a new review")
    public ResponseEntity<Review> createReview(@Valid @RequestBody Review review) {
        return ResponseEntity.ok(reviewService.createReview(review));
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "Update a review (patient only)")
    public ResponseEntity<Review> updateReview(
            @PathVariable String reviewId,
            @RequestParam String patientId,
            @Valid @RequestBody Review review) {
        return reviewService.updateReview(reviewId, patientId, review)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Read ====================

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID")
    public ResponseEntity<Review> getReview(@PathVariable String reviewId) {
        return reviewService.getReviewById(reviewId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get review by booking ID")
    public ResponseEntity<Review> getReviewByBooking(@PathVariable String bookingId) {
        return reviewService.getReviewByBooking(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get approved reviews for a doctor with pagination")
    public ResponseEntity<PageResponse<Review>> getReviewsForDoctor(
            @PathVariable String doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getReviewsForDoctor(doctorId, page, size));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get reviews by a patient with pagination")
    public ResponseEntity<PageResponse<Review>> getReviewsByPatient(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getReviewsByPatient(patientId, page, size));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending reviews (admin)")
    public ResponseEntity<List<Review>> getPendingReviews() {
        return ResponseEntity.ok(reviewService.getPendingReviews());
    }

    // ==================== Doctor Response ====================

    @PostMapping("/{reviewId}/response")
    @Operation(summary = "Add doctor response to review")
    public ResponseEntity<Review> addDoctorResponse(
            @PathVariable String reviewId,
            @RequestParam String doctorId,
            @RequestBody Map<String, String> request) {
        return reviewService.addDoctorResponse(reviewId, doctorId, request.get("response"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Moderation ====================

    @PostMapping("/{reviewId}/approve")
    @Operation(summary = "Approve a review (admin)")
    public ResponseEntity<Review> approveReview(
            @PathVariable String reviewId,
            @RequestParam String adminId) {
        return reviewService.approveReview(reviewId, adminId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{reviewId}/reject")
    @Operation(summary = "Reject a review (admin)")
    public ResponseEntity<Review> rejectReview(
            @PathVariable String reviewId,
            @RequestParam String adminId,
            @RequestBody Map<String, String> request) {
        return reviewService.rejectReview(reviewId, adminId, request.get("reason"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{reviewId}/flag")
    @Operation(summary = "Flag a review for moderation")
    public ResponseEntity<Review> flagReview(
            @PathVariable String reviewId,
            @RequestBody Map<String, String> request) {
        return reviewService.flagReview(reviewId, request.get("reason"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Helpfulness ====================

    @PostMapping("/{reviewId}/helpful")
    @Operation(summary = "Mark review as helpful")
    public ResponseEntity<Review> markAsHelpful(
            @PathVariable String reviewId,
            @RequestParam String userId) {
        return reviewService.markAsHelpful(reviewId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Statistics ====================

    @GetMapping("/doctor/{doctorId}/stats")
    @Operation(summary = "Get rating statistics for a doctor")
    public ResponseEntity<Map<String, Object>> getDoctorRatingStats(@PathVariable String doctorId) {
        return ResponseEntity.ok(Map.of(
                "doctorId", doctorId,
                "averageRating", reviewService.getAverageRating(doctorId),
                "totalReviews", reviewService.getReviewCount(doctorId)
        ));
    }

    // ==================== Delete ====================

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete a review (patient only)")
    public ResponseEntity<Void> deleteReview(
            @PathVariable String reviewId,
            @RequestParam String patientId) {
        if (reviewService.deleteReview(reviewId, patientId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
