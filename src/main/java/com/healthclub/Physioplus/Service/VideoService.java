package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Dto.CreateRoomRequest;
import com.healthclub.Physioplus.Dto.RoomResponse;
import com.healthclub.Physioplus.Dto.VideoTokenResponse;
import com.healthclub.Physioplus.Model.VideoSession;
import com.healthclub.Physioplus.Repository.VideoSessionRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VideoService {

    @Value("${hms.access-key}")
    private String accessKey;

    @Value("${hms.secret}")
    private String appSecret;

    @Value("${hms.template-id}")
    private String templateId;

    private final VideoSessionRepository videoSessionRepository;

    @Autowired
    public VideoService(VideoSessionRepository videoSessionRepository) {
        this.videoSessionRepository = videoSessionRepository;
    }

    public RoomResponse createRoom(CreateRoomRequest request) {
        // Check if room already exists for this booking
        Optional<VideoSession> existing = videoSessionRepository.findByBookingId(request.getBookingId());
        if (existing.isPresent()) {
            return RoomResponse.success("Room already exists", existing.get());
        }

        // Create new video session
        VideoSession session = new VideoSession();
        session.setBookingId(request.getBookingId());
        session.setDoctorId(request.getDoctorId());
        session.setPatientId(request.getPatientId());
        session.setScheduledTime(request.getScheduledTime() != null ?
                request.getScheduledTime() : LocalDateTime.now());
        session.setRoomId(generateRoomId());
        session.setRoomName(request.getRoomName() != null ?
                request.getRoomName() : "Consultation-" + request.getBookingId());
        session.setTemplateId(templateId);
        session.setStatus(VideoSession.SessionStatus.SCHEDULED);
        session.setCreatedAt(Instant.now());
        session.setUpdatedAt(Instant.now());

        VideoSession saved = videoSessionRepository.save(session);
        return RoomResponse.success("Room created successfully", saved);
    }

    public VideoTokenResponse generateToken(String roomId, String participantId, boolean isHost) {
        Optional<VideoSession> sessionOpt = videoSessionRepository.findByRoomId(roomId);
        if (sessionOpt.isEmpty()) {
            return VideoTokenResponse.error("Room not found");
        }

        VideoSession session = sessionOpt.get();
        if (!session.canJoin()) {
            return VideoTokenResponse.error("Session is not available for joining");
        }

        String role = isHost ? "host" : "guest";
        String token = generateHmsAuthToken(roomId, participantId, role);

        // Update session status if needed
        if (session.getStatus() == VideoSession.SessionStatus.SCHEDULED) {
            session.setStatus(VideoSession.SessionStatus.ACTIVE);
            session.setStartedAt(LocalDateTime.now());
            session.setUpdatedAt(Instant.now());
            videoSessionRepository.save(session);
        }

        return VideoTokenResponse.success(token, roomId, role);
    }

    public VideoTokenResponse getTokenForDoctor(String roomId, String doctorId) {
        Optional<VideoSession> sessionOpt = videoSessionRepository.findByRoomId(roomId);
        if (sessionOpt.isEmpty()) {
            return VideoTokenResponse.error("Room not found");
        }

        VideoSession session = sessionOpt.get();
        if (!session.getDoctorId().equals(doctorId)) {
            return VideoTokenResponse.error("Unauthorized: Doctor ID mismatch");
        }

        return generateToken(roomId, doctorId, true);  // Doctor is host
    }

    public VideoTokenResponse getTokenForPatient(String roomId, String patientId) {
        Optional<VideoSession> sessionOpt = videoSessionRepository.findByRoomId(roomId);
        if (sessionOpt.isEmpty()) {
            return VideoTokenResponse.error("Room not found");
        }

        VideoSession session = sessionOpt.get();
        if (!session.getPatientId().equals(patientId)) {
            return VideoTokenResponse.error("Unauthorized: Patient ID mismatch");
        }

        return generateToken(roomId, patientId, false);  // Patient is guest
    }

    public RoomResponse endSession(String roomId) {
        Optional<VideoSession> sessionOpt = videoSessionRepository.findByRoomId(roomId);
        if (sessionOpt.isEmpty()) {
            return RoomResponse.error("Room not found");
        }

        VideoSession session = sessionOpt.get();
        session.setStatus(VideoSession.SessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        session.setUpdatedAt(Instant.now());

        VideoSession saved = videoSessionRepository.save(session);
        return RoomResponse.success("Session ended successfully", saved);
    }

    public Optional<VideoSession> getSessionByBookingId(String bookingId) {
        return videoSessionRepository.findByBookingId(bookingId);
    }

    public Optional<VideoSession> getSessionByRoomId(String roomId) {
        return videoSessionRepository.findByRoomId(roomId);
    }

    public List<VideoSession> getSessionsForDoctor(String doctorId) {
        return videoSessionRepository.findByDoctorId(doctorId);
    }

    public List<VideoSession> getActiveSessionsForDoctor(String doctorId) {
        return videoSessionRepository.findByDoctorIdAndStatus(doctorId, VideoSession.SessionStatus.ACTIVE);
    }

    private String generateRoomId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String generateHmsAuthToken(String roomId, String userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 86400000);  // 24 hours validity

        Map<String, Object> claims = new HashMap<>();
        claims.put("access_key", accessKey);
        claims.put("type", "app");
        claims.put("version", 2);
        claims.put("room_id", roomId);
        claims.put("user_id", userId);
        claims.put("role", role);
        claims.put("jti", UUID.randomUUID().toString());

        SecretKey key = getSigningKey();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .notBefore(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = appSecret.getBytes(StandardCharsets.UTF_8);
        // Ensure key is at least 256 bits (32 bytes) for HS256
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            keyBytes = paddedKey;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
