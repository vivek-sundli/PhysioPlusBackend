package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Dto.*;
import com.healthclub.Physioplus.Model.*;
import com.healthclub.Physioplus.Service.AdminService;
import com.healthclub.Physioplus.Service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private VideoService videoService;

    // ==================== Dashboard ====================

    /**
     * GET /api/admin/dashboard
     * Get dashboard statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardStats> getDashboardStats() {
        AdminDashboardStats stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    // ==================== Patient Management ====================

    /**
     * GET /api/admin/patients
     * Get all patients with pagination
     */
    @GetMapping("/patients")
    public ResponseEntity<PageResponse<Patient>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Patient> patients = adminService.getAllPatients(page, size);
        return ResponseEntity.ok(patients);
    }

    /**
     * GET /api/admin/patients/{id}
     * Get patient details
     */
    @GetMapping("/patients/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable String id) {
        return adminService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Doctor Management ====================

    /**
     * GET /api/admin/doctors
     * Get all doctors with pagination
     */
    @GetMapping("/doctors")
    public ResponseEntity<PageResponse<Doctor>> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Doctor> doctors = adminService.getAllDoctors(page, size);
        return ResponseEntity.ok(doctors);
    }

    /**
     * GET /api/admin/doctors/pending
     * Get doctors pending approval
     */
    @GetMapping("/doctors/pending")
    public ResponseEntity<PageResponse<Doctor>> getPendingDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Doctor> doctors = adminService.getPendingDoctorApprovals(page, size);
        return ResponseEntity.ok(doctors);
    }

    /**
     * GET /api/admin/doctors/{id}
     * Get doctor details
     */
    @GetMapping("/doctors/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable String id) {
        return adminService.getDoctorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/admin/doctors/approve
     * Approve or reject doctor application
     *
     * Request body:
     * {
     * "doctorId": "doc123",
     * "approved": true,
     * "rejectionReason": "Optional reason if rejected"
     * }
     */
    @PostMapping("/doctors/approve")
    public ResponseEntity<Doctor> approveDoctor(@RequestBody DoctorApprovalRequest request) {
        // In production, get adminId from JWT token
        String adminId = "admin";
        Doctor doctor = adminService.approveDoctor(request, adminId);
        return ResponseEntity.ok(doctor);
    }

    /**
     * POST /api/admin/doctors/{id}/suspend
     * Suspend a doctor
     */
    @PostMapping("/doctors/{id}/suspend")
    public ResponseEntity<Doctor> suspendDoctor(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        Doctor doctor = adminService.suspendDoctor(id, reason);
        return ResponseEntity.ok(doctor);
    }

    /**
     * POST /api/admin/doctors/{id}/reactivate
     * Reactivate a suspended doctor
     */
    @PostMapping("/doctors/{id}/reactivate")
    public ResponseEntity<Doctor> reactivateDoctor(@PathVariable String id) {
        Doctor doctor = adminService.reactivateDoctor(id);
        return ResponseEntity.ok(doctor);
    }

    // ==================== Payment Management ====================

    /**
     * GET /api/admin/payments
     * Get all payments with pagination
     */
    @GetMapping("/payments")
    public ResponseEntity<PageResponse<Payment>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Payment> payments = adminService.getAllPayments(page, size);
        return ResponseEntity.ok(payments);
    }

    /**
     * GET /api/admin/payments/doctor/{doctorId}
     * Get payments received by a doctor
     */
    @GetMapping("/payments/doctor/{doctorId}")
    public ResponseEntity<PageResponse<Payment>> getPaymentsByDoctor(
            @PathVariable String doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Payment> payments = adminService.getPaymentsByDoctor(doctorId, page, size);
        return ResponseEntity.ok(payments);
    }

    /**
     * GET /api/admin/payments/patient/{userId}
     * Get payments made by a patient
     */
    @GetMapping("/payments/patient/{userId}")
    public ResponseEntity<PageResponse<Payment>> getPaymentsByPatient(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Payment> payments = adminService.getPaymentsByPatient(userId, page, size);
        return ResponseEntity.ok(payments);
    }

    // ==================== Booking Management ====================

    /**
     * GET /api/admin/bookings
     * Get all bookings with pagination
     */
    @GetMapping("/bookings")
    public ResponseEntity<PageResponse<Bookings>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Bookings> bookings = adminService.getAllBookings(page, size);
        return ResponseEntity.ok(bookings);
    }

    // ==================== Video Session Management ====================

    /**
     * GET /api/admin/video-sessions/active
     * Get all active video sessions
     */
    @GetMapping("/video-sessions/active")
    public ResponseEntity<List<VideoSession>> getActiveVideoSessions() {
        List<VideoSession> sessions = adminService.getActiveVideoSessions();
        return ResponseEntity.ok(sessions);
    }

    /**
     * POST /api/admin/video/setup-call
     * Setup a video call between doctor and patient
     */
    @PostMapping("/video/setup-call")
    public ResponseEntity<RoomResponse> setupCall(@RequestBody CreateRoomRequest request) {
        RoomResponse response = videoService.createRoom(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    // ==================== Feedback Management ====================

    /**
     * GET /api/admin/feedbacks
     * Get all feedbacks with pagination
     */
    @GetMapping("/feedbacks")
    public ResponseEntity<PageResponse<Feedback>> getAllFeedbacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Feedback> feedbacks = adminService.getAllFeedbacks(page, size);
        return ResponseEntity.ok(feedbacks);
    }

    /**
     * POST /api/admin/feedbacks/send
     * Send feedback form to a user
     *
     * Request body:
     * {
     * "recipientId": "user123",
     * "recipientType": "PATIENT",
     * "formTitle": "Consultation Feedback",
     * "formDescription": "Please rate your experience",
     * "questions": {
     * "q1": "How was your experience?",
     * "q2": "Would you recommend us?"
     * }
     * }
     */
    @PostMapping("/feedbacks/send")
    public ResponseEntity<Feedback> sendFeedback(@RequestBody SendFeedbackRequest request) {
        Feedback feedback = adminService.sendFeedbackForm(request);
        return ResponseEntity.ok(feedback);
    }

    /**
     * POST /api/admin/feedbacks/send-bulk
     * Send feedback forms to multiple users or all doctors/patients
     *
     * Request body:
     * {
     * "recipientType": "ALL_DOCTORS",
     * "formTitle": "Monthly Survey",
     * "formDescription": "Help us improve",
     * "questions": { ... }
     * }
     */
    @PostMapping("/feedbacks/send-bulk")
    public ResponseEntity<List<Feedback>> sendBulkFeedback(@RequestBody SendFeedbackRequest request) {
        List<Feedback> feedbacks = adminService.sendBulkFeedback(request);
        return ResponseEntity.ok(feedbacks);
    }

    // ==================== Notification Management ====================

    /**
     * GET /api/admin/notifications
     * Get all notifications with pagination
     */
    @GetMapping("/notifications")
    public ResponseEntity<PageResponse<NotificationLog>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<NotificationLog> notifications = adminService.getAllNotifications(page, size);
        return ResponseEntity.ok(notifications);
    }
    // ==================== Activity Feed ====================

    /**
     * GET /api/admin/activity-feed
     * Get system activity logs
     */
    @GetMapping("/activity-feed")
    public ResponseEntity<PageResponse<ActivityLog>> getActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ActivityLog> logs = adminService.getActivityLogs(page, size);
        return ResponseEntity.ok(logs);
    }

    // ==================== App Settings ====================

    /**
     * GET /api/admin/settings
     * Get global app settings
     */
    @GetMapping("/settings")
    public ResponseEntity<AppSettings> getAppSettings() {
        AppSettings settings = adminService.getAppSettings();
        return ResponseEntity.ok(settings);
    }

    /**
     * PUT /api/admin/settings
     * Update global app settings
     */
    @PutMapping("/settings")
    public ResponseEntity<AppSettings> updateAppSettings(@RequestBody AppSettingsDto settingsDto) {
        AppSettings settings = adminService.updateAppSettings(settingsDto);
        return ResponseEntity.ok(settings);
    }

    // ==================== Admin Notifications ====================

    /**
     * POST /api/admin/notifications/send-alert
     * Send custom system alert/notification
     */
    @PostMapping("/notifications/send-alert")
    public ResponseEntity<Void> sendAdminNotification(@RequestBody AdminNotificationRequest request) {
        adminService.sendAdminNotification(request);
        return ResponseEntity.ok().build();
    }

    // ==================== Enhanced Patient Management ====================

    /**
     * POST /api/admin/patients/{id}/block
     * Block a patient
     */
    @PostMapping("/patients/{id}/block")
    public ResponseEntity<Patient> blockPatient(@PathVariable String id) {
        Patient patient = adminService.blockPatient(id);
        return ResponseEntity.ok(patient);
    }

    /**
     * POST /api/admin/patients/{id}/unblock
     * Unblock a patient
     */
    @PostMapping("/patients/{id}/unblock")
    public ResponseEntity<Patient> unblockPatient(@PathVariable String id) {
        Patient patient = adminService.unblockPatient(id);
        return ResponseEntity.ok(patient);
    }

    // ==================== Enhanced Booking Management ====================

    /**
     * POST /api/admin/bookings/{id}/cancel
     * Admin override to cancel a booking
     */
    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<Bookings> cancelBooking(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        Bookings booking = adminService.cancelBookingByAdmin(id, reason);
        return ResponseEntity.ok(booking);
    }
}
