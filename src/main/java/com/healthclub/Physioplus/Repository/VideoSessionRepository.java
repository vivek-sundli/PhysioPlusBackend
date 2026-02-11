package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.VideoSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoSessionRepository extends MongoRepository<VideoSession, String> {

    Optional<VideoSession> findByRoomId(String roomId);

    Optional<VideoSession> findByBookingId(String bookingId);

    List<VideoSession> findByDoctorId(String doctorId);

    List<VideoSession> findByPatientId(String patientId);

    List<VideoSession> findByStatus(VideoSession.SessionStatus status);

    List<VideoSession> findByDoctorIdAndStatus(String doctorId, VideoSession.SessionStatus status);
}
