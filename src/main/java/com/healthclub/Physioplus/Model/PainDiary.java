package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Document(collection = "PainDiaries")
@CompoundIndex(name = "patient_date_idx", def = "{'patientId': 1, 'date': -1}")
public class PainDiary {

    @Id
    private String id;

    private String patientId;
    private LocalDate date;
    private LocalTime time;

    // Pain Details
    private Integer painLevel;          // 1-10
    private String painType;            // SHARP, DULL, THROBBING, BURNING, ACHING
    private List<String> affectedAreas; // Body parts
    private Integer duration;           // Minutes
    private String trigger;             // What caused it

    // Activities
    private String activityBefore;
    private String activityDuring;
    private List<String> exercisesDone;
    private Integer exerciseCompliance; // Percentage

    // Relief Methods
    private List<String> reliefMethods; // ICE, HEAT, REST, MEDICATION, EXERCISE
    private String medicationTaken;
    private Integer reliefLevel;        // 1-10

    // Mood & Sleep
    private String mood;                // HAPPY, NEUTRAL, SAD, ANXIOUS, FRUSTRATED
    private Integer sleepQuality;       // 1-10
    private Integer sleepHours;

    // Notes
    private String notes;
    private List<String> photoUrls;

    private Instant createdAt;

    public PainDiary() {
        this.createdAt = Instant.now();
    }
}
