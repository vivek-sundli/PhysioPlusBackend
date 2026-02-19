package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.PainDiary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PainDiaryRepository extends MongoRepository<PainDiary, String> {

    Page<PainDiary> findByPatientIdOrderByCreatedAtDesc(String patientId, Pageable pageable);

    List<PainDiary> findByPatientIdAndCreatedAtBetween(String patientId, Instant start, Instant end);

    List<PainDiary> findByPatientIdOrderByCreatedAtDesc(String patientId);
}
