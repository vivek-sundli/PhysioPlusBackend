package com.healthclub.Physioplus.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AdminDashboardStats {

    // User Stats
    private long totalPatients;
    private long totalDoctors;
    private long activeDoctors;
    private long pendingDoctorApprovals;

    // Financial Stats
    private double totalRevenue;
    private double revenueThisMonth;
    private double revenueToday;
    private long totalTransactions;

    // Booking Stats
    private long totalBookings;
    private long pendingBookings;
    private long completedBookings;
    private long cancelledBookings;

    // Notification Stats
    private long totalNotificationsSent;
    private long pendingNotifications;

    // Feedback Stats
    private long totalFeedbacks;
    private long pendingFeedbacks;
    private double averageRating;

    // Video Stats
    private long totalVideoSessions;
    private long activeVideoSessions;
}
