package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    @Query("{'participantIds': ?0}")
    Page<Conversation> findByParticipantId(String participantId, Pageable pageable);

    Optional<Conversation> findByDoctorIdAndPatientId(String doctorId, String patientId);

    Optional<Conversation> findByBookingId(String bookingId);

    List<Conversation> findByParticipant1IdOrParticipant2Id(String participant1Id, String participant2Id);

    @Query("{'participantIds': {$all: [?0, ?1]}}")
    Optional<Conversation> findByBothParticipants(String participantId1, String participantId2);
}
