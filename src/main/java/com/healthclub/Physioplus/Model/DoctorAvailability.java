package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Document(collection = "DoctorAvailability")
public class DoctorAvailability {

    @Id
    private String id;

    @Indexed(unique = true)
    private String doctorId;

    // Weekly schedule: Map<DayOfWeek, List<TimeSlot>>
    private Map<DayOfWeek, List<TimeRange>> weeklySchedule;

    // Slot configuration
    private Integer slotDurationMinutes;  // Default 30
    private Integer bufferMinutes;        // Break between slots
    private Integer maxBookingsPerDay;

    // Leave/Holiday management
    private List<DateRange> blockedDates;
    private List<String> holidays;  // Holiday dates as strings

    // Instant consultation
    private boolean instantConsultEnabled;
    private Double instantConsultFee;

    // Home visit
    private boolean homeVisitEnabled;
    private Double homeVisitFee;
    private Integer homeVisitRadiusKm;

    private Instant createdAt;
    private Instant updatedAt;

    public DoctorAvailability() {
        this.slotDurationMinutes = 30;
        this.bufferMinutes = 5;
        this.maxBookingsPerDay = 20;
        this.instantConsultEnabled = false;
        this.homeVisitEnabled = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @Getter
    @Setter
    public static class TimeRange {
        private LocalTime startTime;
        private LocalTime endTime;

        public TimeRange() {}

        public TimeRange(LocalTime startTime, LocalTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    @Getter
    @Setter
    public static class DateRange {
        private String startDate;
        private String endDate;
        private String reason;

        public DateRange() {}
    }
}
