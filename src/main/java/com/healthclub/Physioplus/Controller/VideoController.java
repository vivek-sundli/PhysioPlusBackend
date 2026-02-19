package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Dto.CreateRoomRequest;
import com.healthclub.Physioplus.Dto.RoomResponse;
import com.healthclub.Physioplus.Dto.VideoTokenResponse;
import com.healthclub.Physioplus.Model.VideoSession;
import com.healthclub.Physioplus.Service.VideoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final VideoService videoService;

    @Autowired
    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * POST /api/video/create-room
     * Create a new video room for a booking
     *
     * Request body:
     * {
     *   "bookingId": "booking123",
     *   "doctorId": "doctor123",
     *   "patientId": "patient123",
     *   "scheduledTime": "2024-01-15T10:00:00",
     *   "roomName": "Consultation Room"
     * }
     */
    @PostMapping("/create-room")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        RoomResponse response = videoService.createRoom(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * GET /api/video/token/{roomId}
     * Get auth token for a participant to join the room
     *
     * Query params:
     * - participantId: ID of the participant (doctor or patient)
     * - isHost: true for doctor (host), false for patient (guest)
     */
    @GetMapping("/token/{roomId}")
    public ResponseEntity<VideoTokenResponse> getToken(
            @PathVariable String roomId,
            @RequestParam String participantId,
            @RequestParam(defaultValue = "false") boolean isHost) {

        VideoTokenResponse response = videoService.generateToken(roomId, participantId, isHost);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * GET /api/video/token/doctor/{roomId}
     * Get auth token for the doctor (host) to join the room
     */
    @GetMapping("/token/doctor/{roomId}")
    public ResponseEntity<VideoTokenResponse> getDoctorToken(
            @PathVariable String roomId,
            @RequestParam String doctorId) {

        VideoTokenResponse response = videoService.getTokenForDoctor(roomId, doctorId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * GET /api/video/token/patient/{roomId}
     * Get auth token for the patient (guest) to join the room
     */
    @GetMapping("/token/patient/{roomId}")
    public ResponseEntity<VideoTokenResponse> getPatientToken(
            @PathVariable String roomId,
            @RequestParam String patientId) {

        VideoTokenResponse response = videoService.getTokenForPatient(roomId, patientId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * GET /api/video/session/{roomId}
     * Get session details by room ID
     */
    @GetMapping("/session/{roomId}")
    public ResponseEntity<VideoSession> getSession(@PathVariable String roomId) {
        return videoService.getSessionByRoomId(roomId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/video/session/booking/{bookingId}
     * Get session details by booking ID
     */
    @GetMapping("/session/booking/{bookingId}")
    public ResponseEntity<VideoSession> getSessionByBooking(@PathVariable String bookingId) {
        return videoService.getSessionByBookingId(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/video/sessions/doctor/{doctorId}
     * Get all video sessions for a doctor
     */
    @GetMapping("/sessions/doctor/{doctorId}")
    public ResponseEntity<List<VideoSession>> getSessionsForDoctor(@PathVariable String doctorId) {
        List<VideoSession> sessions = videoService.getSessionsForDoctor(doctorId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * GET /api/video/sessions/doctor/{doctorId}/active
     * Get active video sessions for a doctor
     */
    @GetMapping("/sessions/doctor/{doctorId}/active")
    public ResponseEntity<List<VideoSession>> getActiveSessionsForDoctor(@PathVariable String doctorId) {
        List<VideoSession> sessions = videoService.getActiveSessionsForDoctor(doctorId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * POST /api/video/end/{roomId}
     * End a video session
     */
    @PostMapping("/end/{roomId}")
    public ResponseEntity<RoomResponse> endSession(@PathVariable String roomId) {
        RoomResponse response = videoService.endSession(roomId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
