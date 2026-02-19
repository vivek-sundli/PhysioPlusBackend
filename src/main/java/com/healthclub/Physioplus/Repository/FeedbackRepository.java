package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String> {

    List<Feedback> findByRecipientId(String recipientId);

    List<Feedback> findByDoctorId(String doctorId);

    List<Feedback> findByBookingId(String bookingId);

    List<Feedback> findByStatus(Feedback.FeedbackStatus status);

    Page<Feedback> findByStatus(Feedback.FeedbackStatus status, Pageable pageable);

    long countByStatus(Feedback.FeedbackStatus status);

    List<Feedback> findByRecipientType(Feedback.RecipientType type);
}
