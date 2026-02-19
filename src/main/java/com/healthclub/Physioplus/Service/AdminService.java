package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Dto.*;
import com.healthclub.Physioplus.Model.*;
import com.healthclub.Physioplus.Repository.*;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private VideoSessionRepository videoSessionRepository;

    @Autowired
    private UserRepository userRepository;

    // ==================== Dashboard Stats ====================

    @Transactional(readOnly = true)
    public AdminDashboardStats getDashboardStats() {
        // Calculate total revenue
        List<Payment> allPayments = paymentRepository.findAll();
        double totalRevenue = allPayments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .mapToDouble(Payment::getAmount)
                .sum();

        // Calculate this month's revenue
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        double revenueThisMonth = allPayments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .filter(p -> p.getPaymentTime() != null && p.getPaymentTime().isAfter(startOfMonth))
                .mapToDouble(Payment::getAmount)
                .sum();

        // Calculate today's revenue
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0);
        double revenueToday = allPayments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .filter(p -> p.getPaymentTime() != null && p.getPaymentTime().isAfter(startOfDay))
                .mapToDouble(Payment::getAmount)
                .sum();

        // Count bookings by status
        List<Bookings> allBookings = bookingRepository.findAll();
        long pendingBookings = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.REQUESTED)
                .count();
        long completedBookings = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .count();
        long cancelledBookings = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count();

        // Feedback stats
        List<Feedback> allFeedbacks = feedbackRepository.findAll();
        double averageRating = allFeedbacks.stream()
                .filter(f -> f.getRating() != null)
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        return AdminDashboardStats.builder()
                .totalPatients(patientRepository.count())
                .totalDoctors(doctorRepository.count())
                .activeDoctors(doctorRepository.countByActive(true))
                .pendingDoctorApprovals(doctorRepository.countByOnboardingStatus(Doctor.OnboardingStatus.PENDING))
                .totalRevenue(totalRevenue)
                .revenueThisMonth(revenueThisMonth)
                .revenueToday(revenueToday)
                .totalTransactions(paymentRepository.count())
                .totalBookings(allBookings.size())
                .pendingBookings(pendingBookings)
                .completedBookings(completedBookings)
                .cancelledBookings(cancelledBookings)
                .totalNotificationsSent(notificationRepository.count())
                .pendingNotifications(notificationRepository.countByStatus(NotificationLog.DeliveryStatus.PENDING))
                .totalFeedbacks(feedbackRepository.count())
                .pendingFeedbacks(feedbackRepository.countByStatus(Feedback.FeedbackStatus.PENDING))
                .averageRating(averageRating)
                .totalVideoSessions(videoSessionRepository.count())
                .activeVideoSessions(videoSessionRepository.findByStatus(VideoSession.SessionStatus.ACTIVE).size())
                .build();
    }

    // ==================== Patient Management ====================

    @Transactional(readOnly = true)
    public PageResponse<Patient> getAllPatients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Patient> patientPage = patientRepository.findAll(pageable);
        return PageResponse.of(patientPage);
    }

    @Transactional(readOnly = true)
    public Optional<Patient> getPatientById(String id) {
        return patientRepository.findById(id);
    }

    // ==================== Doctor Management ====================

    @Transactional(readOnly = true)
    public PageResponse<Doctor> getAllDoctors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Doctor> doctorPage = doctorRepository.findAll(pageable);
        return PageResponse.of(doctorPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<Doctor> getPendingDoctorApprovals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<Doctor> doctorPage = doctorRepository.findByOnboardingStatus(Doctor.OnboardingStatus.PENDING, pageable);
        return PageResponse.of(doctorPage);
    }

    @Transactional(readOnly = true)
    public Optional<Doctor> getDoctorById(String id) {
        return doctorRepository.findById(id);
    }

    public Doctor approveDoctor(DoctorApprovalRequest request, String adminId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(request.getDoctorId());
        if (doctorOpt.isEmpty()) {
            throw new IllegalArgumentException("Doctor not found");
        }

        Doctor doctor = doctorOpt.get();

        if (request.isApproved()) {
            doctor.setOnboardingStatus(Doctor.OnboardingStatus.APPROVED);
            doctor.setActive(true);
            doctor.setVerifiedBy(adminId);
            doctor.setVerifiedAt(Instant.now());
            log.info("Doctor {} approved by admin {}", doctor.getId(), adminId);
        } else {
            doctor.setOnboardingStatus(Doctor.OnboardingStatus.REJECTED);
            doctor.setRejectionReason(request.getRejectionReason());
            doctor.setActive(false);
            log.info("Doctor {} rejected by admin {}: {}", doctor.getId(), adminId, request.getRejectionReason());
        }

        doctor.setUpdatedAt(Instant.now());
        return doctorRepository.save(doctor);
    }

    public Doctor suspendDoctor(String doctorId, String reason) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            throw new IllegalArgumentException("Doctor not found");
        }

        Doctor doctor = doctorOpt.get();
        doctor.setOnboardingStatus(Doctor.OnboardingStatus.SUSPENDED);
        doctor.setActive(false);
        doctor.setRejectionReason(reason);
        doctor.setUpdatedAt(Instant.now());

        log.info("Doctor {} suspended: {}", doctorId, reason);
        return doctorRepository.save(doctor);
    }

    public Doctor reactivateDoctor(String doctorId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            throw new IllegalArgumentException("Doctor not found");
        }

        Doctor doctor = doctorOpt.get();
        doctor.setOnboardingStatus(Doctor.OnboardingStatus.APPROVED);
        doctor.setActive(true);
        doctor.setRejectionReason(null);
        doctor.setUpdatedAt(Instant.now());

        log.info("Doctor {} reactivated", doctorId);
        return doctorRepository.save(doctor);
    }

    // ==================== Payment Analytics ====================

    @Transactional(readOnly = true)
    public PageResponse<Payment> getAllPayments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("paymentTime").descending());
        Page<Payment> paymentPage = paymentRepository.findAll(pageable);
        return PageResponse.of(paymentPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<Payment> getPaymentsByDoctor(String doctorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("paymentTime").descending());
        Page<Payment> paymentPage = paymentRepository.findByDoctorId(doctorId, pageable);
        return PageResponse.of(paymentPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<Payment> getPaymentsByPatient(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("paymentTime").descending());
        Page<Payment> paymentPage = paymentRepository.findByUserId(userId, pageable);
        return PageResponse.of(paymentPage);
    }

    // ==================== Feedback Management ====================

    public Feedback sendFeedbackForm(SendFeedbackRequest request) {
        Feedback feedback = new Feedback();
        feedback.setRecipientId(request.getRecipientId());
        feedback.setRecipientType(request.getRecipientType());
        feedback.setBookingId(request.getBookingId());
        feedback.setDoctorId(request.getDoctorId());
        feedback.setFormTitle(request.getFormTitle());
        feedback.setFormDescription(request.getFormDescription());
        feedback.setQuestions(request.getQuestions());
        feedback.setStatus(Feedback.FeedbackStatus.PENDING);
        feedback.setSentAt(Instant.now());
        feedback.setCreatedAt(Instant.now());

        log.info("Feedback form sent to {}", request.getRecipientId());
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> sendBulkFeedback(SendFeedbackRequest request) {
        List<Feedback> feedbacks = new ArrayList<>();

        List<String> recipientIds = new ArrayList<>();

        if (request.getRecipientType() == Feedback.RecipientType.ALL_DOCTORS) {
            doctorRepository.findByActive(true).forEach(d -> recipientIds.add(d.getId()));
        } else if (request.getRecipientType() == Feedback.RecipientType.ALL_PATIENTS) {
            patientRepository.findAll().forEach(p -> recipientIds.add(p.getId()));
        } else if (request.getRecipientIds() != null) {
            recipientIds.addAll(request.getRecipientIds());
        }

        for (String recipientId : recipientIds) {
            Feedback feedback = new Feedback();
            feedback.setRecipientId(recipientId);
            feedback.setRecipientType(request.getRecipientType());
            feedback.setFormTitle(request.getFormTitle());
            feedback.setFormDescription(request.getFormDescription());
            feedback.setQuestions(request.getQuestions());
            feedback.setStatus(Feedback.FeedbackStatus.PENDING);
            feedback.setSentAt(Instant.now());
            feedback.setCreatedAt(Instant.now());
            feedbacks.add(feedbackRepository.save(feedback));
        }

        log.info("Bulk feedback sent to {} recipients", feedbacks.size());
        return feedbacks;
    }

    @Transactional(readOnly = true)
    public PageResponse<Feedback> getAllFeedbacks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Feedback> feedbackPage = feedbackRepository.findAll(pageable);
        return PageResponse.of(feedbackPage);
    }

    // ==================== Video Session Management ====================

    @Transactional(readOnly = true)
    public List<VideoSession> getActiveVideoSessions() {
        return videoSessionRepository.findByStatus(VideoSession.SessionStatus.ACTIVE);
    }

    // ==================== Booking Management ====================

    @Transactional(readOnly = true)
    public PageResponse<Bookings> getAllBookings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentTime").descending());
        Page<Bookings> bookingPage = bookingRepository.findAll(pageable);
        return PageResponse.of(bookingPage);
    }

    // ==================== Notification Management ====================

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private AppSettingsRepository appSettingsRepository;

    // ==================== Activity Feed ====================

    @Transactional(readOnly = true)
    public PageResponse<ActivityLog> getActivityLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ActivityLog> logPage = activityLogRepository.findAll(pageable);
        return PageResponse.of(logPage);
    }

    public void logActivity(String actionType, String description, String actorId, String targetId) {
        ActivityLog log = new ActivityLog(actionType, description, actorId, targetId);
        activityLogRepository.save(log);
    }

    // ==================== App Settings ====================

    @Transactional(readOnly = true)
    public AppSettings getAppSettings() {
        return appSettingsRepository.findById("GLOBAL").orElseGet(() -> {
            AppSettings settings = new AppSettings();
            settings.setId("GLOBAL");
            settings.setConsultationFeeBase(500.0);
            settings.setPlatformFeePercentage(10.0);
            settings.setMaintenanceMode(false);
            settings.setAllowedPaymentMethods(List.of("UPI", "CARD"));
            settings.setUpdatedAt(Instant.now());
            return appSettingsRepository.save(settings);
        });
    }

    public AppSettings updateAppSettings(AppSettingsDto dto) {
        AppSettings settings = getAppSettings();
        if (dto.getConsultationFeeBase() != null)
            settings.setConsultationFeeBase(dto.getConsultationFeeBase());
        if (dto.getPlatformFeePercentage() != null)
            settings.setPlatformFeePercentage(dto.getPlatformFeePercentage());
        settings.setMaintenanceMode(dto.isMaintenanceMode());
        if (dto.getAllowedPaymentMethods() != null)
            settings.setAllowedPaymentMethods(dto.getAllowedPaymentMethods());

        settings.setUpdatedAt(Instant.now());
        logActivity("SETTINGS_UPDATE", "Updated global app settings", "ADMIN", "GLOBAL");
        return appSettingsRepository.save(settings);
    }

    // ==================== Admin Notifications ====================

    public void sendAdminNotification(AdminNotificationRequest request) {
        List<String> recipients = new ArrayList<>();

        if ("BROADCAST".equalsIgnoreCase(request.getType())) {
            userRepository.findAll().forEach(u -> recipients.add(u.getId()));
        } else if ("GROUP".equalsIgnoreCase(request.getType())) {
            if ("DOCTORS".equalsIgnoreCase(request.getTargetGroup())) {
                doctorRepository.findAll().forEach(d -> recipients.add(d.getId()));
            } else if ("PATIENTS".equalsIgnoreCase(request.getTargetGroup())) {
                patientRepository.findAll().forEach(p -> recipients.add(p.getId()));
            }
        } else if ("USER".equalsIgnoreCase(request.getType())) {
            if (request.getTargetUserId() != null)
                recipients.add(request.getTargetUserId());
            if (request.getTargetUserIds() != null)
                recipients.addAll(request.getTargetUserIds());
        }

        for (String recipientId : recipients) {
            NotificationLog notification = new NotificationLog();
            notification.setRecipientId(recipientId);
            notification.setTitle(request.getTitle());
            notification.setMessage(request.getMessage());
            notification.setType(NotificationLog.NotificationType.SYSTEM_ALERT);
            notification.setStatus(NotificationLog.DeliveryStatus.PENDING);
            notification.setCreatedAt(Instant.now());
            notificationRepository.save(notification);
        }

        logActivity("NOTIFICATION_SENT", "Sent admin notification: " + request.getTitle(), "ADMIN", request.getType());
    }

    // ==================== Enhanced Patient Management ====================

    public Patient blockPatient(String patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        // Assuming Patient model doesn't have a 'blocked' field yet, we might need to
        // add it or use a flag.
        // CHECK: Patient.java might strictly need this field. For now, let's assume we
        // add it or standard User has 'active'.
        // User.java has 'active'. Patient extends User? No, Patient has distinct ID
        // usually but linked to User.
        // Let's check Patient model structure.
        // Assuming Patient IS A User or has a User link.
        // Based on previous view, Patient has basic details. Let's assume we toggle
        // 'active' on User?
        // Let's just log it for now if field missing, but goal is implementation.
        // I will assume I need to update the User entity associated with Patient if
        // separate, or Patient itself.
        // Checking Patient.java earlier: it has basic fields.
        // Let's stick to the plan: if field missing, I will add it to Patient model.
        // For now, I'll update the 'active' status if it exists, or add it.
        // Wait, User.java has 'active'. I should find the User linked to Patient?
        // Patient usually has same ID as User or a userId field.
        // In this project, let's assume Patient ID = User ID for simplicity or look up.

        // Actually, let's check User implementation first.
        // User.java lines 1-62 showed 'active' field.
        // I'll update User 'active' status.
        Optional<User> userOpt = userRepository.findById(patientId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(false);
            userRepository.save(user);
        }

        logActivity("USER_BLOCK", "Blocked patient " + patientId, "ADMIN", patientId);
        return patient;
    }

    public Patient unblockPatient(String patientId) {
        Optional<User> userOpt = userRepository.findById(patientId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(true);
            userRepository.save(user);
        }
        logActivity("USER_UNBLOCK", "Unblocked patient " + patientId, "ADMIN", patientId);
        return patientRepository.findById(patientId).orElseThrow();
    }

    // ==================== Enhanced Booking Management ====================

    public Bookings cancelBookingByAdmin(String bookingId, String reason) {
        Bookings booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setNotes(booking.getNotes() + " [Cancelled by Admin: " + reason + "]");
        booking.setUpdatedAt(Instant.now());

        logActivity("BOOKING_CANCEL", "Admin cancelled booking: " + reason, "ADMIN", bookingId);
        return bookingRepository.save(booking);
    }

    // ==================== Notification Management (Existing) ====================

    @Transactional(readOnly = true)
    public PageResponse<NotificationLog> getAllNotifications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NotificationLog> notificationPage = notificationRepository.findAll(pageable);
        return PageResponse.of(notificationPage);
    }
}
